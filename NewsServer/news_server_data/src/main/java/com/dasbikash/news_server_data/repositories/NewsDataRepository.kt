/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.news_server_data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageArticleFetchStatus
import com.dasbikash.news_server_data.models.room_entity.SavedArticle
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.NewsDataRepositoryRoomImpl
import com.dasbikash.news_server_data.utills.ExceptionUtils

abstract class NewsDataRepository{

    private val newsDataService: NewsDataService = DataServiceImplProvider.getNewsDataServiceImpl()

    abstract fun getLatestArticleByPageFromLocalDb(page: Page): Article?
    abstract fun findArticleById(articleId:String):Article?

    abstract protected fun setPageAsNotSynced(page:Page)
    abstract protected fun setPageAsSynced(page:Page)
    abstract protected fun setPageAsEndReached(page:Page)
    abstract protected fun insertArticles(articles:List<Article>)
    abstract protected fun getLastArticle(page: Page): Article?

    abstract fun saveArticleToLocalDisk(article: Article,context: Context): SavedArticle
    abstract fun checkIfAlreadySaved(article: Article):Boolean
    abstract fun getAllSavedArticle(): LiveData<List<SavedArticle>>
    abstract fun deleteSavedArticle(savedArticle: SavedArticle)
    abstract fun findSavedArticleById(savedArticleId:String):SavedArticle?

    private fun insertArticle(article: Article){
        insertArticles(listOf(article))
    }

    fun init(context: Context){
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val db = NewsServerDatabase.getDatabase(context)
        db.pageDao.markAllNotSynced()
        db.articleDao.nukeTable()
    }

    fun getLatestArticleByPage(page: Page):Article{
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val article = newsDataService.getLatestArticlesByPage(page, 1).first()
        if (findArticleById(article.id) == null){
            setPageAsNotSynced(page)
            insertArticle(article)
        }else{
            setPageAsSynced(page)
        }
        return article
    }

    fun downloadMoreArticlesByPage(page: Page):List<Article>{
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val article = getLastArticle(page)
        article?.let {
            try {
                val articles = newsDataService.getArticlesAfterLastArticle(page,it)
                if (page.articleFetchStatus == PageArticleFetchStatus.NOT_SYNCED){
                    for (articleData in articles){
                        if (findArticleById(articleData.id) !=null){
                            setPageAsSynced(page)
                            break
                        }
                    }
                }
                insertArticles(articles)
                return articles
            }catch (ex:DataNotFoundException){
                setPageAsEndReached(page)
                throw ex
            }
        }
        throw DataNotFoundException()
    }

    abstract fun getArticleLiveDataForPage(page: Page): LiveData<List<Article>>

    companion object {

        private val MIN_ARTICLE_REFRESH_INTERVAL = 5 * 60 * 1000L

        @Volatile
        private lateinit var INSTANCE: NewsDataRepository

        internal fun getImpl(context: Context,dbImplementation: DbImplementation): NewsDataRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(NewsDataRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        when(dbImplementation) {
                            DbImplementation.ROOM -> INSTANCE = NewsDataRepositoryRoomImpl(context)
                        }
                    }
                }
            }
            return INSTANCE
        }
    }


}
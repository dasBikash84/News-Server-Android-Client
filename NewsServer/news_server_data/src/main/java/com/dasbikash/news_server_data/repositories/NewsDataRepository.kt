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
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.NewsCategory
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.SavedArticle
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.NewsDataRepositoryRoomImpl
import com.dasbikash.news_server_data.utills.ExceptionUtils

abstract class NewsDataRepository {

    private val newsDataService: NewsDataService = DataServiceImplProvider.getNewsDataServiceImpl()

    abstract fun getLatestArticleByPageFromLocalDb(page: Page): Article?
    abstract fun findArticleByIdFromLocalDb(articleId: String): Article?

    abstract protected fun setPageAsEndReached(page: Page)
    abstract protected fun insertArticles(articles: List<Article>)
    abstract protected fun getOldestArticle(page: Page): Article?

    abstract fun saveArticleToLocalDisk(article: Article, context: Context): SavedArticle
    abstract fun checkIfAlreadySaved(article: Article): Boolean
    abstract fun getAllSavedArticle(): LiveData<List<SavedArticle>>
    abstract fun deleteSavedArticle(savedArticle: SavedArticle)
    abstract fun findSavedArticleById(savedArticleId: String): SavedArticle?

    private fun insertArticle(article: Article) {
        insertArticles(listOf(article))
    }

    fun init(context: Context) {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val db = NewsServerDatabase.getDatabase(context)
        db.pageDao.markAllHasMoreArticle()
        db.articleDao.nukeTable()
    }

    fun getLatestArticleByPage(page: Page): Article {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val article = newsDataService.getLatestArticlesByPage(page, 1).first()
        insertArticle(article)
        return article
    }

    fun downloadPreviousArticlesByPage(page: Page): List<Article> {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val article = getOldestArticle(page)
        article?.let {
            try {
                val articles = newsDataService.getArticlesBeforeLastArticle(page, it)
                insertArticles(articles)
                return articles
            } catch (ex: DataNotFoundException) {
                setPageAsEndReached(page)
                throw ex
            }
        }
        throw DataNotFoundException()
    }

    fun downloadNewArticlesByPage(page: Page, latestArticle: Article): List<Article> {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val articles = mutableListOf<Article>()
        try {
            articles.addAll(newsDataService.getArticlesAfterLastArticle(page, latestArticle))
        }catch (ex: DataServerException) {
            latestArticle.resetCreated()
            articles.add(latestArticle)
        }
        insertArticles(articles)
        return articles
    }

    fun findArticleByIdFromRemoteDb(articleId: String,pageId:String): Article?{
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        return newsDataService.findArticleById(articleId,pageId)
    }

    fun getLatestArticlesByNewsCategory(newsCategory: NewsCategory, context: Context
                                        ,loadChunkSize:Int= DEFAULT_ARTICLE_LOAD_CHUNK_SIZE_FOR_NEWS_CATEGORY)
            : List<Article>{
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val rawArticles = newsDataService.getRawLatestArticlesByNewsCategory(newsCategory,loadChunkSize)
        return processRawArticlesFoundForNewsCategory(rawArticles, context)
    }

    fun getArticlesByNewsCategoryBeforeLastArticle(newsCategory: NewsCategory, lastArticle: Article, context: Context
                                                   ,loadChunkSize:Int= DEFAULT_ARTICLE_LOAD_CHUNK_SIZE_FOR_NEWS_CATEGORY)
            : List<Article>{
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val rawArticles = newsDataService
                                        .getRawArticlesByNewsCategoryBeforeLastArticle(newsCategory,lastArticle,loadChunkSize)
        return processRawArticlesFoundForNewsCategory(rawArticles, context)
    }

    private fun processRawArticlesFoundForNewsCategory(rawArticles: List<Article>,context: Context): List<Article> {
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context)
        return rawArticles.asSequence().filter {appSettingsRepository.findPageById(it.pageId!!) !=null}
                .map {
                    it.newspaperId = appSettingsRepository.getNewspaperByPage(appSettingsRepository.findPageById(it.pageId!!)!!).id
                    it
                }.toList()
    }


    abstract fun getArticleLiveDataForPage(page: Page): LiveData<List<Article>>
    abstract fun getArticleCountForPage(page: Page): Int

    companion object {
        private const val DEFAULT_ARTICLE_LOAD_CHUNK_SIZE_FOR_NEWS_CATEGORY = 10
        @Volatile
        private lateinit var INSTANCE: NewsDataRepository

        internal fun getImpl(context: Context, dbImplementation: DbImplementation): NewsDataRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(NewsDataRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        when (dbImplementation) {
                            DbImplementation.ROOM -> INSTANCE = NewsDataRepositoryRoomImpl(context)
                        }
                    }
                }
            }
            return INSTANCE
        }

        internal fun getFreshImpl(context: Context, dbImplementation: DbImplementation): NewsDataRepository {
            synchronized(NewsDataRepository::class.java) {
                when (dbImplementation) {
                    DbImplementation.ROOM -> INSTANCE = NewsDataRepositoryRoomImpl(context)
                }
            }
            return INSTANCE
        }
    }


}
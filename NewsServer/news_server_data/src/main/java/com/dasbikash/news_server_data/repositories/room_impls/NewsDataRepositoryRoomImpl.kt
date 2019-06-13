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

package com.dasbikash.news_server_data.repositories.room_impls

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.ImageUtils
import java.io.File

internal class NewsDataRepositoryRoomImpl internal constructor(context: Context) : NewsDataRepository() {

    private val newsServerDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    override fun getLatestArticleByPageFromLocalDb(page: Page): Article? {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return newsServerDatabase.articleDao.getLatestArticleByPageId(page.id)
    }

    override fun findArticleById(articleId:String):Article?{
        return newsServerDatabase.articleDao.findById(articleId)
    }

    override fun setPageAsNotSynced(page: Page) {
        page.articleFetchStatus = PageArticleFetchStatus.NOT_SYNCED
        newsServerDatabase.pageDao.save(page)
    }

    override fun setPageAsSynced(page: Page) {
        page.articleFetchStatus = PageArticleFetchStatus.SYNCED_WITH_SERVER
        newsServerDatabase.pageDao.save(page)
    }

    override fun setPageAsEndReached(page: Page) {
        page.articleFetchStatus = PageArticleFetchStatus.END_REACHED
        newsServerDatabase.pageDao.save(page)
    }

    override fun insertArticles(articles: List<Article>) {
        newsServerDatabase.articleDao.addArticles(articles)
    }

    override fun getArticleLiveDataForPage(page: Page): LiveData<List<Article>> {
        return newsServerDatabase.articleDao.getArticleLiveDataForPage(page.id)
    }

    override fun getArticleCountForPage(page: Page): Int {
        return newsServerDatabase.articleDao.getArticleCountForPage(page.id)
    }

    override fun saveArticleToLocalDisk(article: Article, context: Context): SavedArticle {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        val page = newsServerDatabase.pageDao.findById(article.pageId!!)
        val newspaper = newsServerDatabase.newsPaperDao.findById(article.newspaperId!!)

        article.previewImageLink?.let {
            article.previewImageLink = ImageUtils.urlToFile(it,article.id+"_preview",context)
        }

        val savedImageList = mutableListOf<ArticleImage>()

        var i=0
        article.imageLinkList?.asSequence()?.forEach {
            val articleImage = it
            i++
            ImageUtils.urlToFile(it.link!!,article.id+"_"+i,context)?.let {
                savedImageList.add(ArticleImage(it,articleImage.caption))
            }
        }
        val savedArticle = SavedArticle.getInstance(article,page,newspaper,savedImageList)
        newsServerDatabase.savedArticleDao.addArticles(savedArticle)
        return savedArticle
    }

    override fun getAllSavedArticle(): LiveData<List<SavedArticle>> {
        return newsServerDatabase.savedArticleDao.findAll()
    }

    override fun deleteSavedArticle(savedArticle: SavedArticle) {
        savedArticle.imageLinkList?.let {
            it.filter { !it.link.isNullOrEmpty() }.forEach {
                File(it.link!!).delete()
            }
        }
        savedArticle.previewImageLink?.let {
            File(it).delete()
        }
        newsServerDatabase.savedArticleDao.deleteOne(savedArticle)
    }

    override fun checkIfAlreadySaved(article: Article): Boolean {
        newsServerDatabase.savedArticleDao.findById(article.id)?.let {
            return true
        }
        return false
    }

    override fun findSavedArticleById(savedArticleId: String):SavedArticle? {
        return newsServerDatabase.savedArticleDao.findById(savedArticleId)
    }

    override fun getLastArticle(page: Page): Article? {
        when(page.articleFetchStatus){
            PageArticleFetchStatus.SYNCED_WITH_SERVER ->
                    { return newsServerDatabase.articleDao.getLastArticleForSyncedPage(page.id) }
            PageArticleFetchStatus.NOT_SYNCED->
                    { return newsServerDatabase.articleDao.getLastArticleForDesyncedPage(page.id) }
            else -> return null
        }
    }
}
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
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.data_sources.data_services.news_data_services.spring_mvc.NewsDataServiceUtils
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.display_models.entity.Article
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.utills.ExceptionUtils

class NewsDataRepository private constructor(context: Context) {

    private val newsDataService: NewsDataService = DataServiceImplProvider.getNewsDataServiceImpl()
    private val newsServerDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    private val TAG = "DataService"

    /*fun getLatestArticleByTopLevelPageId(topLevelPageId:String): Article{
        return newsDataService.getLatestArticleByTopLevelPageId(topLevelPageId)
    }
    //Latest articles from any page
    fun getLatestArticlesByPageId(pageId:String,
                                  articleRequestSize:Int? = null):List<Article>{
        articleRequestSize?.let {
            return newsDataService.getLatestArticlesByPageId(pageId,articleRequestSize = articleRequestSize)
        }
        return newsDataService.getLatestArticlesByPageId(pageId)
    }
    //Articles after last article ID
    fun getArticlesAfterLastId(pageId:String,lastArticleId:String,
                               articleRequestSize:Int? = null):List<Article>{
        articleRequestSize?.let {
            return newsDataService.getArticlesAfterLastId(pageId,lastArticleId,articleRequestSize = articleRequestSize)
        }
        return newsDataService.getArticlesAfterLastId(pageId,lastArticleId)
    }

    fun getTopPagesByNewsPaper(newspaper: Newspaper):List<Page>{
        return newsServerDatabase.pageDao.getTopPagesByNewsPaperId(newspaper.id)
    }*/


    fun getLatestArticleByPage(page: Page): Article? {
        ExceptionUtils.thowExceptionIfOnMainThred()

        val article: Article? = newsServerDatabase.articleDao.getLatestArticleByPageId(page.id)

        if (article != null) {
            if (System.currentTimeMillis() - article.getCreated() < MIN_ARTICLE_REFRESH_INTERVAL) {
                return article
            }
        }

        var latestArticle: Article? = null
        newsDataService.getLatestArticlesByPageId(page.id, 1).apply {
            if (this.size > 0) {
                latestArticle = this.first()
            }
        }
        latestArticle?.let {
            NewsDataServiceUtils.processFetchedArticleData(it, page)
            newsServerDatabase.articleDao.addArticles(it)
            return it
        }
        return null
    }

    companion object {

        private val MIN_ARTICLE_REFRESH_INTERVAL = 5 * 60 * 1000L

        @Volatile
        private lateinit var INSTANCE: NewsDataRepository

        fun getInstance(context: Context): NewsDataRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(NewsDataRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        INSTANCE = NewsDataRepository(context)
                    }
                }
            }
            return INSTANCE
        }
    }


}
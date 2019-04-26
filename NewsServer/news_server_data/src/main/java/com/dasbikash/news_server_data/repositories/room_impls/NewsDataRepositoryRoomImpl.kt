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
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.utills.ExceptionUtils

class NewsDataRepositoryRoomImpl internal constructor(context: Context) : NewsDataRepository {

    private val newsDataService: NewsDataService = DataServiceImplProvider.getNewsDataServiceImpl()
    private val newsServerDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    private val TAG = "NewsDataRepository"

    override fun getLatestArticleByPageFromLocalDb(page: Page): Article? {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return newsServerDatabase.articleDao.getLatestArticleByPageId(page.id)
    }

    override fun getLatestArticleByPage(page: Page): Article? {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        newsDataService.getLatestArticlesByPage(page, 1).apply {
            if (this.size > 0) {
                val article = this.first()
                newsServerDatabase.articleDao.addArticles(article)
                return article
            }
        }

        return null
    }

    override fun getArticlesByPage(page: Page):List<Article>{
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return newsServerDatabase.articleDao.findAllByPageId(page.id)
    }

    override fun findArticleById(articleId:String):Article?{
        return newsServerDatabase.articleDao.findById(articleId)
    }

    override fun downloadArticlesByPageAfterLastArticle(page: Page, lastArticle:Article?):List<Article>{
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        val articleList:List<Article>

        if (lastArticle != null){
            articleList = newsDataService.getArticlesAfterLastArticle(page,lastArticle)
        }else{
            articleList = newsDataService.getLatestArticlesByPage(page)
        }
        newsServerDatabase.articleDao.addArticles(articleList)
        return articleList
    }
}
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

package com.dasbikash.news_server_data.data_sources.data_services.news_data_services

import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.RealTimeDbArticleDataUtils
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.NewsCategory
import com.dasbikash.news_server_data.models.room_entity.Page

internal object RealTimeDbNewsDataService : NewsDataService() {
    override fun getRawLatestArticlesByPage(page: Page, articleRequestSize: Int): List<Article> {
        return RealTimeDbArticleDataUtils.getLatestArticlesByPage(page,articleRequestSize)
    }

    override fun getRawArticlesBeforeLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {
        return RealTimeDbArticleDataUtils.getArticlesBeforeLastArticle(page, lastArticle, articleRequestSize)
    }

    override fun getRawArticlesAfterLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int) =
            RealTimeDbArticleDataUtils.getArticlesAfterLastArticle(page,lastArticle,articleRequestSize)

    override fun findArticleById(articleId: String, pageId: String): Article? {
        return RealTimeDbArticleDataUtils.findArticleById(articleId,pageId)
    }

    override fun getRawLatestArticlesByNewsCategory(newsCategory: NewsCategory, articleRequestSize: Int)=
            RealTimeDbArticleDataUtils.getLatestArticlesByNewsCategory(newsCategory, articleRequestSize)

    override fun getRawArticlesByNewsCategoryBeforeLastArticle(newsCategory: NewsCategory, lastArticle: Article, articleRequestSize: Int)=
            RealTimeDbArticleDataUtils.getArticlesByNewsCategoryBeforeLastArticle(newsCategory, lastArticle, articleRequestSize)
}
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
import com.dasbikash.news_server_data.data_sources.data_services.web_services.spring_mvc.SpringMVCNewsDataUtils
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page

internal object SpringMVCNewsDataService : NewsDataService() {

    override fun getRawLatestArticlesByPage(page: Page, articleRequestSize: Int): List<Article> {
        return SpringMVCNewsDataUtils.getRawLatestArticlesByPage(page, articleRequestSize)
    }

    override fun getRawArticlesBeforeLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {
        return SpringMVCNewsDataUtils.getArticlesBeforeLastArticle(page, lastArticle, articleRequestSize)
    }

    override fun getRawArticlesAfterLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
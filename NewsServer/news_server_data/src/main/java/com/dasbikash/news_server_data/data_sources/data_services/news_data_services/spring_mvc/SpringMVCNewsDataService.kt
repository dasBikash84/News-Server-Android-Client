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

package com.dasbikash.news_server_data.data_sources.data_services.news_data_services.spring_mvc

import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.display_models.entity.Article

internal object SpringMVCNewsDataService: NewsDataService {

    private val springMVCWebService
            = SpringMVCWebService.RETROFIT.create(SpringMVCWebService::class.java)


    override fun getLatestArticleByTopLevelPageId(topLevelPageId: String): Article {
        return springMVCWebService
                .getLatestArticleByTopLevelPageId(topLevelPageId)
                .execute()
                .body()!!
    }

    override fun getLatestArticlesByPageId(pageId: String, articleRequestSize: Int): List<Article> {
        return springMVCWebService
                .getLatestArticlesByPageId(pageId,articleRequestSize)
                .execute()
                .body()!!
    }

    override fun getArticlesAfterLastId(pageId:String,lastArticleId: String, articleRequestSize: Int): List<Article> {
        return springMVCWebService
                .getArticlesAfterLastId(pageId,lastArticleId,articleRequestSize)
                .execute()
                .body()!!
    }
}
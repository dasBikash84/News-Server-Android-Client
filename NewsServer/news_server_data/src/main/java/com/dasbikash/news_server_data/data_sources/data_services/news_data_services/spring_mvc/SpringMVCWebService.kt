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

import com.dasbikash.news_server_data.display_models.entity.Article

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface SpringMVCWebService {

    @GET(TOP_LEVEL_PAGE_ARTICLE_URL)
    fun getLatestArticleByTopLevelPageId(@Path("topLevelPageId") topLevelPageId: String): Call<Article>

    @GET(LATEST_ARTICLE_BY_PAGE_ID_URL)
    fun getLatestArticlesByPageId(@Path("pageId") pageId: String,
                                  @Query("article_count") resultSize: Int?): Call<List<Article>>

    @GET(ARTICLES_AFTER_LAST_ID_URL)
    fun getArticlesAfterLastId(@Path("pageId") pageId: String,
                               @Path("lastArticleId") lastArticleId: String,
                               @Query("article_count") resultSize: Int?): Call<List<Article>>

    companion object {

        const val BASE_URL = "http://192.168.0.103:8099/"
        const val TOP_LEVEL_PAGE_ARTICLE_URL = "articles/top-level-page-id/{topLevelPageId}/latest-article"
        const val LATEST_ARTICLE_BY_PAGE_ID_URL = "articles/page-id/{pageId}"
        const val ARTICLES_AFTER_LAST_ID_URL = "articles/page-id/{pageId}/last-article-id/{lastArticleId}"

        val RETROFIT = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }
}

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

import android.util.Log
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.display_models.entity.Article
import com.dasbikash.news_server_data.utills.ExceptionUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

internal object SpringMVCNewsDataService: NewsDataService {

    private val TAG = "DataService"

    private val springMVCWebService
            = SpringMVCWebService.RETROFIT.create(SpringMVCWebService::class.java)


    override fun getLatestArticleByTopLevelPageId(topLevelPageId: String): Article {
//        ExceptionUtils.thowExceptionIfOnMainThred()
        return springMVCWebService
                .getLatestArticleByTopLevelPageId(topLevelPageId)
                .execute()
                .body()!!
    }

    override fun getLatestArticlesByPageId(pageId: String, articleRequestSize: Int): List<Article> {
//        ExceptionUtils.thowExceptionIfOnMainThred()

        val lock = Object()
        val articles = mutableListOf<Article>()

        springMVCWebService.getLatestArticlesByPageId(pageId,articleRequestSize).enqueue(object : Callback<List<Article>?>{
            override fun onFailure(call: Call<List<Article>?>, t: Throwable) {
                synchronized(lock){lock.notify()}
            }
            override fun onResponse(call: Call<List<Article>?>, response: Response<List<Article>?>) {
                response.body()?.let { articles.addAll(it) }
                synchronized(lock){lock.notify()}
            }

        })

        synchronized(lock){lock.wait()}
        return articles
    }

    override fun getArticlesAfterLastId(pageId:String,lastArticleId: String, articleRequestSize: Int): List<Article> {
//        ExceptionUtils.thowExceptionIfOnMainThred()
        return springMVCWebService
                .getArticlesAfterLastId(pageId,lastArticleId,articleRequestSize)
                .execute()
                .body()!!
    }
}
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

package com.dasbikash.news_server_data.data_sources.data_services.web_services.spring_mvc

import android.util.Log
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.exceptions.DataServerNotAvailableExcepption
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.utills.LoggerUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal object SpringMVCNewsDataUtils {

    private val springMVCWebService = SpringMVCWebService.RETROFIT.create(SpringMVCWebService::class.java)

    fun getRawLatestArticlesByPage(page: Page, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        LoggerUtils.debugLog("getRawLatestArticlesByPage for: ${page.id}",this::class.java)

        springMVCWebService
                .getLatestArticlesByPageId(page.id, articleRequestSize)
                .enqueue(object : Callback<SpringMVCWebService.Articles?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Articles?>, throwable: Throwable) {
                        LoggerUtils.debugLog("getRawLatestArticlesByPage for: ${page.id} onFailure",this::class.java)
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Articles?>, response: Response<SpringMVCWebService.Articles?>) {
                        LoggerUtils.debugLog("getRawLatestArticlesByPage for: ${page.id} onResponse",this::class.java)
                        if (response.isSuccessful) {

                            LoggerUtils.debugLog("getRawLatestArticlesByPage for: ${page.id} onResponse isSuccessful",this::class.java)
                            response.body()?.let { articles.addAll(it.articles) }
                        } else {

                            LoggerUtils.debugLog("getRawLatestArticlesByPage for: ${page.id} onResponse not Successful",this::class.java)
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        LoggerUtils.debugLog("getRawLatestArticlesByPage for: ${page.id} before wait",this::class.java)
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        LoggerUtils.debugLog("getRawLatestArticlesByPage for: ${page.id} before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog("getRawLatestArticlesByPage for: ${page.id} before throw DataNotFoundException()",this::class.java)
        if (articles.size == 0 ){throw DataNotFoundException()
        }

        LoggerUtils.debugLog("getRawLatestArticlesByPage for: ${page.id} before return",this::class.java)
        return articles
    }

    fun getRawArticlesAfterLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        springMVCWebService
                .getArticlesAfterLastId(page.id, lastArticle.id, articleRequestSize)
                .enqueue(object : Callback<SpringMVCWebService.Articles?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Articles?>, throwable: Throwable) {
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Articles?>, response: Response<SpringMVCWebService.Articles?>) {
                        if (response.isSuccessful) {
                            response.body()?.let { articles.addAll(it.articles) }
                        } else {
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })

        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }
        dataServerException?.let { throw it }
        return articles
    }
}
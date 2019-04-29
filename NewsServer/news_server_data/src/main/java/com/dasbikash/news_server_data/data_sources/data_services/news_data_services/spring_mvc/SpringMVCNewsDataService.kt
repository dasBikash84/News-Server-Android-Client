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
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.exceptions.DataServerNotAvailableExcepption
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal object SpringMVCNewsDataService : NewsDataService {

    private val TAG = "DataService"

    private val springMVCWebService = SpringMVCWebService.RETROFIT.create(SpringMVCWebService::class.java)

    override fun getRawLatestArticlesByPage(page: Page, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id}")

        springMVCWebService
                .getLatestArticlesByPageId(page.id, articleRequestSize)
                .enqueue(object : Callback<List<Article>?> {
                    override fun onFailure(call: Call<List<Article>?>, throwable: Throwable) {
                        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} onFailure")
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<List<Article>?>, response: Response<List<Article>?>) {
                        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} onResponse")
                        if (response.isSuccessful) {

                            Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} onResponse isSuccessful")
                            response.body()?.let { articles.addAll(it) }
                        } else {

                            Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} onResponse not Successful")
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before wait")
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before throw DataNotFoundException()")
        if (articles.size == 0 ){throw DataNotFoundException()}

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before return")
        return articles
    }

    override fun getRawArticlesAfterLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        springMVCWebService
                .getArticlesAfterLastId(page.id, lastArticle.id, articleRequestSize)
                .enqueue(object : Callback<List<Article>?> {
                    override fun onFailure(call: Call<List<Article>?>, throwable: Throwable) {
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<List<Article>?>, response: Response<List<Article>?>) {
                        if (response.isSuccessful) {
                            response.body()?.let { articles.addAll(it) }
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
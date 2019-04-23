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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal object SpringMVCNewsDataService : NewsDataService {

    private val TAG = "DataService"

    private val springMVCWebService = SpringMVCWebService.RETROFIT.create(SpringMVCWebService::class.java)


    override fun getLatestArticleByTopLevelPageId(topLevelPageId: String): Article {

        val lock = Object()
        var article:Article?=null
        var dataServerException: DataServerException? = null

        springMVCWebService
                .getLatestArticleByTopLevelPageId(topLevelPageId)
                .enqueue(object : Callback<Article?> {
                    override fun onFailure(call: Call<Article?>, throwable: Throwable) {
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }

                    }
                    override fun onResponse(call: Call<Article?>, response: Response<Article?>) {
                        if (response.isSuccessful) {
                            article = response.body()
                        } else {
                            dataServerException = DataNotFoundException()
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })

        synchronized(lock) { lock.wait() }
        dataServerException?.let { throw it }
        return article!!
    }

    override fun getLatestArticlesByPageId(pageId: String, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        Log.d(TAG,"getLatestArticlesByPageId for: ${pageId}")

        springMVCWebService
                .getLatestArticlesByPageId(pageId, articleRequestSize)
                .enqueue(object : Callback<List<Article>?> {
                    override fun onFailure(call: Call<List<Article>?>, throwable: Throwable) {
                        Log.d(TAG,"getLatestArticlesByPageId for: ${pageId} onFailure")
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<List<Article>?>, response: Response<List<Article>?>) {
                        Log.d(TAG,"getLatestArticlesByPageId for: ${pageId} onResponse")
                        if (response.isSuccessful) {

                            Log.d(TAG,"getLatestArticlesByPageId for: ${pageId} onResponse isSuccessful")
                            response.body()?.let { articles.addAll(it) }
                        } else {

                            Log.d(TAG,"getLatestArticlesByPageId for: ${pageId} onResponse not Successful")
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        Log.d(TAG,"getLatestArticlesByPageId for: ${pageId} before wait")
        synchronized(lock) { lock.wait(5000) }

        Log.d(TAG,"getLatestArticlesByPageId for: ${pageId} before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG,"getLatestArticlesByPageId for: ${pageId} before throw DataNotFoundException()")
        if (articles.size == 0 ){throw DataNotFoundException()}

        Log.d(TAG,"getLatestArticlesByPageId for: ${pageId} before return")
        return articles
    }

    override fun getArticlesAfterLastId(pageId: String, lastArticleId: String, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        springMVCWebService
                .getArticlesAfterLastId(pageId, lastArticleId, articleRequestSize)
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

        synchronized(lock) { lock.wait() }
        dataServerException?.let { throw it }
        return articles
    }
}
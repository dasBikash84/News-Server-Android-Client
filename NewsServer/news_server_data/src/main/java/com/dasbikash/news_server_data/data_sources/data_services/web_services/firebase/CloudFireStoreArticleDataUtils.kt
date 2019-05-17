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

package com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase

import android.util.Log
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page
import com.google.firebase.firestore.Query

internal object CloudFireStoreArticleDataUtils {

    private val TAG = "DataService"

    internal fun getLatestArticlesByPage(page: Page, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getArticleCollectionRef()
                .whereEqualTo("pageId",page.id)
                .orderBy("publicationTime", Query.Direction.DESCENDING)
                .limit(articleRequestSize.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    Log.d(TAG,"addOnSuccessListener")
                    for (document in documents) {
                        if (document.exists()){
                            articles.add(document.toObject(Article::class.java))
                        }
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG,"addOnFailureListener. Eror msg: ${exception.message}")
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before wait")
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before throw DataNotFoundException()")
        if (articles.size == 0 ){
            throw DataNotFoundException()
        }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before return")
        return articles
    }

    internal fun getArticlesAfterLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {

        Log.d(TAG,"getRawLatestArticlesByPage"+CloudFireStoreAppSettingsUtils.getServerAppSettingsUpdateTime())

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getArticleCollectionRef()
                .whereEqualTo("pageId",page.id)
                .whereLessThan("publicationTime",lastArticle.publicationTime!!)
                .orderBy("publicationTime", Query.Direction.DESCENDING)
                .limit(articleRequestSize.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    Log.d(TAG,"addOnSuccessListener")
                    for (document in documents) {
                        if (document.exists()){
                            articles.add(document.toObject(Article::class.java))
                        }
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG,"addOnFailureListener. Eror msg: ${exception.message}")
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before wait")
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before throw DataNotFoundException()")
        if (articles.size == 0 ){
            throw DataNotFoundException()
        }

        Log.d(TAG,"getRawLatestArticlesByPage for: ${page.id} before return")
        return articles

    }
}
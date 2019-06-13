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

import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.google.firebase.firestore.Query

internal object CloudFireStoreArticleDataUtils {

    private const val DB_PUBLICATION_TIME_FIELD_NAME = "publicationTime"
    private const val DB_PAGE_ID_FIELD_NAME = "pageId"

    internal fun getLatestArticlesByPage(page: Page, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getArticleCollectionRef()
                .whereEqualTo(DB_PAGE_ID_FIELD_NAME,page.id)
                .orderBy(DB_PUBLICATION_TIME_FIELD_NAME, Query.Direction.DESCENDING)
                .limit(articleRequestSize.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    LoggerUtils.debugLog("getLatestArticlesByPage addOnSuccessListener",this::class.java)
                    for (document in documents) {
                        if (document.exists()){
                            articles.add(document.toObject(Article::class.java))
                        }
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    LoggerUtils.debugLog("getLatestArticlesByPage addOnFailureListener. Eror msg: ${exception.message}",this::class.java)
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        LoggerUtils.debugLog("getLatestArticlesByPage for: ${page.id} before wait",this::class.java)
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        LoggerUtils.debugLog("getLatestArticlesByPage for: ${page.id} before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog("getLatestArticlesByPage for: ${page.id} before throw DataNotFoundException()",this::class.java)
        if (articles.size == 0 ){
            throw DataNotFoundException()
        }

        LoggerUtils.debugLog("getLatestArticlesByPage for: ${page.id} before return",this::class.java)
        return articles
    }

    internal fun getArticlesBeforeLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getArticleCollectionRef()
                .whereEqualTo(DB_PAGE_ID_FIELD_NAME,page.id)
                .whereLessThan(DB_PUBLICATION_TIME_FIELD_NAME,lastArticle.publicationTime!!)
                .orderBy(DB_PUBLICATION_TIME_FIELD_NAME, Query.Direction.DESCENDING)
                .limit(articleRequestSize.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    LoggerUtils.debugLog("getArticlesBeforeLastArticle addOnSuccessListener",this::class.java)
                    for (document in documents) {
                        if (document.exists()){
                            articles.add(document.toObject(Article::class.java))
                        }
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    LoggerUtils.debugLog("getArticlesBeforeLastArticle addOnFailureListener. Eror msg: ${exception.message}",this::class.java)
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        LoggerUtils.debugLog("getArticlesBeforeLastArticle for: ${page.id} before wait",this::class.java)
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        LoggerUtils.debugLog("getArticlesBeforeLastArticle for: ${page.id} before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog("getArticlesBeforeLastArticle for: ${page.id} before throw DataNotFoundException()",this::class.java)
        if (articles.size == 0 ){
            throw DataNotFoundException()
        }

        LoggerUtils.debugLog("getArticlesBeforeLastArticle for: ${page.id} before return",this::class.java)
        return articles

    }

    internal fun getArticlesAfterLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        var query = CloudFireStoreConUtils.getArticleCollectionRef()
                        .whereEqualTo(DB_PAGE_ID_FIELD_NAME,page.id)
                        .whereGreaterThan(DB_PUBLICATION_TIME_FIELD_NAME,lastArticle.publicationTime!!)
                        .orderBy(DB_PUBLICATION_TIME_FIELD_NAME, Query.Direction.ASCENDING)

        if (articleRequestSize != NewsDataService.DEFAULT_ARTICLE_REQUEST_SIZE){
            query = query.limit(articleRequestSize.toLong())
        }

        query
            .get()
            .addOnSuccessListener { documents ->
                LoggerUtils.debugLog("getArticlesAfterLastArticle addOnSuccessListener",this::class.java)
                for (document in documents) {
                    if (document.exists()){
                        articles.add(document.toObject(Article::class.java))
                    }
                }
                synchronized(lock) { lock.notify() }
            }
            .addOnFailureListener { exception ->
                LoggerUtils.debugLog("getArticlesAfterLastArticle addOnFailureListener. Eror msg: ${exception.message}",this::class.java)
                dataServerException = DataNotFoundException(exception)
                synchronized(lock) { lock.notify() }
            }

        LoggerUtils.debugLog("getArticlesAfterLastArticle for: ${page.id} before wait",this::class.java)
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        LoggerUtils.debugLog("getArticlesAfterLastArticle for: ${page.id} before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog("getArticlesAfterLastArticle for: ${page.id} before throw DataNotFoundException()",this::class.java)
        if (articles.size == 0 ){
            throw DataNotFoundException()
        }

        LoggerUtils.debugLog("getArticlesAfterLastArticle for: ${page.id} before return",this::class.java)
        return articles
    }
}
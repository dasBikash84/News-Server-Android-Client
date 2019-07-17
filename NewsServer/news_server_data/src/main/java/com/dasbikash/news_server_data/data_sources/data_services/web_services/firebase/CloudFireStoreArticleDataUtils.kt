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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

internal object CloudFireStoreArticleDataUtils {

    private const val DB_PUBLICATION_TIME_FIELD_NAME = "publicationTime"
    private const val DB_PAGE_ID_FIELD_NAME = "pageId"

    internal fun getLatestArticlesByPage(page: Page, articleRequestSize: Int): List<Article> {

        val query = CloudFireStoreConUtils.getArticleCollectionRef()
                .whereEqualTo(DB_PAGE_ID_FIELD_NAME,page.id)
                .orderBy(DB_PUBLICATION_TIME_FIELD_NAME, Query.Direction.DESCENDING)
                .limit(articleRequestSize.toLong())

        return getArticlesForQuery(query, page)
    }

    internal fun getArticlesBeforeLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {

        val query = CloudFireStoreConUtils.getArticleCollectionRef()
                .whereEqualTo(DB_PAGE_ID_FIELD_NAME,page.id)
                .whereLessThan(DB_PUBLICATION_TIME_FIELD_NAME,lastArticle.publicationTime!!)
                .orderBy(DB_PUBLICATION_TIME_FIELD_NAME, Query.Direction.DESCENDING)
                .limit(articleRequestSize.toLong())

        return getArticlesForQuery(query, page)
    }

    internal fun getArticlesAfterLastArticle(page: Page, lastArticle: Article, articleRequestSize: Int): List<Article> {

        var query = CloudFireStoreConUtils.getArticleCollectionRef()
                        .whereEqualTo(DB_PAGE_ID_FIELD_NAME,page.id)
                        .whereGreaterThan(DB_PUBLICATION_TIME_FIELD_NAME,lastArticle.publicationTime!!)
                        .orderBy(DB_PUBLICATION_TIME_FIELD_NAME, Query.Direction.ASCENDING)

        if (articleRequestSize != NewsDataService.DEFAULT_ARTICLE_REQUEST_SIZE){
            query = query.limit(articleRequestSize.toLong())
        }

        return getArticlesForQuery(query, page)
    }

    private fun getArticlesForQuery(query: Query, page: Page): List<Article> {

        val lock = Object()
        val articles = mutableListOf<Article>()
        var dataServerException: DataServerException? = null

        query.get()
            .addOnSuccessListener { documents ->
                LoggerUtils.debugLog("getArticlesForQuery addOnSuccessListener",this::class.java)
                for (document in documents) {
                    if (document.exists()){
                        articles.add(document.toObject(Article::class.java))
                    }
                }
                synchronized(lock) { lock.notify() }
            }
            .addOnFailureListener { exception ->
                LoggerUtils.debugLog("getArticlesForQuery addOnFailureListener. Eror msg: ${exception.message}",this::class.java)
                dataServerException = DataNotFoundException(exception)
                synchronized(lock) { lock.notify() }
            }

        LoggerUtils.debugLog("getArticlesForQuery for: ${page.id} before wait",this::class.java)
        try {
            synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        LoggerUtils.debugLog("getArticlesForQuery for: ${page.id} before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog("getArticlesForQuery for: ${page.id} before throw DataNotFoundException()",this::class.java)
        if (articles.size == 0 ){
            throw DataNotFoundException()
        }

        LoggerUtils.debugLog("getArticlesForQuery for: ${page.id} before return",this::class.java)
        return articles
    }

    fun findArticleById(articleId: String): Article? {

        val lock = Object()
        var article:Article?= null

        val query = CloudFireStoreConUtils.getArticleCollectionRef().document(articleId)

        query.get()
                .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot>{
                    override fun onComplete(task: Task<DocumentSnapshot>) {
                        LoggerUtils.debugLog("findArticleById for: ${articleId} onComplete",this::class.java)
                        if (task.isSuccessful){
                            article = task.result?.toObject(Article::class.java)
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })

        LoggerUtils.debugLog("findArticleById for: ${articleId} before wait",this::class.java)
//        try {
            synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }
//        }catch (ex:InterruptedException){}

        LoggerUtils.debugLog("findArticleById for: ${articleId} before return",this::class.java)
        return article
    }
}
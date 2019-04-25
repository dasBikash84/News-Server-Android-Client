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

package com.dasbikash.news_server_data.repositories

import android.content.Context
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.NewsDataRepositoryRoomImpl

interface NewsDataRepository{

    fun getLatestArticleByPageFromLocalDb(page: Page): Article?

    fun getLatestArticleByPage(page: Page): Article?

    fun getArticlesByPage(page: Page):List<Article>

    fun findArticleById(articleId:String):Article?

    fun downloadArticlesByPage(page: Page,lastArticleId:String?=null):List<Article>

    companion object {

        private val MIN_ARTICLE_REFRESH_INTERVAL = 5 * 60 * 1000L

        @Volatile
        private lateinit var INSTANCE: NewsDataRepository

        internal fun getImpl(context: Context,dbImplementation: DbImplementation): NewsDataRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(NewsDataRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        when(dbImplementation) {
                            DbImplementation.ROOM -> INSTANCE = NewsDataRepositoryRoomImpl(context)
                        }
                    }
                }
            }
            return INSTANCE
        }
    }


}
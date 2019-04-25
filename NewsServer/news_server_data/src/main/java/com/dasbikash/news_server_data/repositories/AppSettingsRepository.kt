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
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.AppSettingsRepositoryRoomImpl

interface AppSettingsRepository{

    fun loadAppSettings(context: Context)
    fun isAppSettingsUpdated(context: Context): Boolean
    fun isAppSettingsDataLoaded(): Boolean

    fun getNewsPapers():LiveData<List<Newspaper>>

    fun getTopPagesForNewspaper(newspaper: Newspaper): List<Page>
    fun getChildPagesForTopLevelPage(topLevelPage: Page):List<Page>
    fun findMatchingPages(it: String): List<Page>

    fun getLanguageByPage(page: Page): Language
    fun getNewspaperByPage(page: Page): Newspaper
    fun findPageById(pageId:String): Page?

    companion object{
        @Volatile
        private lateinit var  INSTANCE:AppSettingsRepository

        internal fun getImpl(context: Context,dbImplementation: DbImplementation):AppSettingsRepository{
            if (!::INSTANCE.isInitialized) {
                synchronized(AppSettingsRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        when(dbImplementation){
                            DbImplementation.ROOM -> INSTANCE = AppSettingsRepositoryRoomImpl(context)
                        }
                    }
                }
            }
            return INSTANCE
        }
    }

}
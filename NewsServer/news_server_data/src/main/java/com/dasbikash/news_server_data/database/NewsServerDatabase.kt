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

package com.dasbikash.news_server_data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dasbikash.news_server_data.database.daos.*
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.database.room_converters.ArticleImageConverter
import com.dasbikash.news_server_data.database.room_converters.DateConverter
import com.dasbikash.news_server_data.database.room_converters.IntListConverter
import com.dasbikash.news_server_data.database.room_converters.StringListConverter

@Database(entities = [Country::class, Language::class, Newspaper::class, Page::class, PageGroup::class, Article::class, UserPreferenceData::class, ArticleVisitHistory::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, IntListConverter::class, StringListConverter::class, ArticleImageConverter::class)
internal abstract class NewsServerDatabase : RoomDatabase() {

    abstract val countryDao: CountryDao
    abstract val languageDao: LanguageDao
    abstract val newsPaperDao: NewsPaperDao
    abstract val pageDao: PageDao
    abstract val pageGroupDao: PageGroupDao
    abstract val articleDao: ArticleDao


    abstract val userPreferenceDataDao: UserPreferenceDataDao

    companion object {

        private val DATABASE_NAME = "news_server_database"

        @Volatile
        private lateinit var INSTANCE: NewsServerDatabase

        fun getDatabase(context: Context): NewsServerDatabase {
            if (!::INSTANCE.isInitialized) {
                synchronized(NewsServerDatabase::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        INSTANCE = Room.databaseBuilder(
                                context.applicationContext,
                                NewsServerDatabase::class.java, DATABASE_NAME)
                                .build()
                    }
                }
            }
            return INSTANCE
        }
    }

    fun nukeAppSettings(){
        articleDao.nukeTable()
        pageGroupDao.nukeTable()
        pageDao.nukeTable()
        newsPaperDao.nukeTable()
        countryDao.nukeTable()
        languageDao.nukeTable()
    }

}

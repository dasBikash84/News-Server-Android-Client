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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.RealtimeDBUserSettingsUtils
import com.dasbikash.news_server_data.database.daos.*
import com.dasbikash.news_server_data.database.room_converters.ArticleImageConverter
import com.dasbikash.news_server_data.database.room_converters.DateConverter
import com.dasbikash.news_server_data.database.room_converters.IntListConverter
import com.dasbikash.news_server_data.database.room_converters.StringListConverter
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.UserSettingsRepository

@Database(entities = [Country::class, Language::class, Newspaper::class, Page::class, Article::class,
                        ArticleVisitHistory::class,SavedArticle::class,ArticleSearchKeyWord::class,NewsCategory::class,
                        FavouritePageEntry::class],
        version = 3, exportSchema = false)
@TypeConverters(DateConverter::class, IntListConverter::class, StringListConverter::class, ArticleImageConverter::class)
internal abstract class NewsServerDatabase internal constructor(): RoomDatabase() {

    abstract val countryDao: CountryDao
    abstract val languageDao: LanguageDao
    abstract val newsPaperDao: NewsPaperDao
    abstract val pageDao: PageDao
    abstract val articleDao: ArticleDao
    abstract val savedArticleDao: SavedArticleDao
    abstract val articleSearchKeyWordDao: ArticleSearchKeyWordDao
    abstract val newsCategoryDao: NewsCategoryDao
    abstract val favouritePageEntryDao: FavouritePageEntryDao

    companion object {

        private val DATABASE_NAME = "news_server_database"

        @Volatile
        private lateinit var INSTANCE: NewsServerDatabase

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `ArticleSearchKeyWord` (`id` TEXT NOT NULL, `created` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                database.execSQL("CREATE TABLE `NewsCategory` (`id` TEXT NOT NULL, `name` TEXT DEFAULT NULL, PRIMARY KEY(`id`))")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE `UserPreferenceData`")
                database.execSQL("DROP TABLE `PageGroup`")
                database.execSQL("CREATE TABLE `FavouritePageEntry` (`pageId` TEXT NOT NULL, `subscribed` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`pageId`))")
                RealtimeDBUserSettingsUtils.signOutUser()
            }
        }

        internal fun getDatabase(context: Context): NewsServerDatabase {
            if (!::INSTANCE.isInitialized) {
                synchronized(NewsServerDatabase::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                                    NewsServerDatabase::class.java, DATABASE_NAME)
                                                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
                                                    .build()
                    }
                }
            }
            return INSTANCE
        }
    }

    fun nukeAppSettings(){
        articleDao.nukeTable()
        pageDao.nukeTable()
        newsPaperDao.nukeTable()
        countryDao.nukeTable()
        languageDao.nukeTable()
        newsCategoryDao.nukeTable()
    }

}

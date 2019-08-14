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
import com.dasbikash.news_server_data.data_sources.AppSettingsDataService
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.models.DefaultAppSettings
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.AppSettingsRepositoryRoomImpl
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.LoggerUtils

abstract class AppSettingsRepository {

    private val mAppSettingsDataService: AppSettingsDataService = DataServiceImplProvider.getAppSettingsDataServiceImpl()

    abstract protected fun nukeAppSettings()
    abstract protected fun addLanguages(languages: List<Language>)
    abstract protected fun addCountries(countries: List<Country>)
    abstract protected fun addNewsPapers(newspapers: List<Newspaper>)
    abstract protected fun addPages(pages: List<Page>)
    abstract protected fun addNewsCategories(newsCategories: List<NewsCategory>)

    abstract protected fun getCountryCount(): Int
    abstract protected fun getLanguageCount(): Int
    abstract protected fun getPageCount(): Int

    abstract fun getNewsPapers(): List<Newspaper>
    abstract fun getNewsPapersLiveData(): LiveData<List<Newspaper>>
    abstract fun getTopPagesForNewspaper(newspaper: Newspaper): List<Page>
    abstract fun getPagesForNewspaper(newspaper: Newspaper): List<Page>
    abstract fun getChildPagesForTopLevelPage(topLevelPage: Page): List<Page>
    abstract fun findMatchingPages(it: String): List<Page>
    abstract fun getNewsCategories(): List<NewsCategory>

    abstract fun getLanguageByPage(page: Page): Language
    abstract fun getLanguageByNewspaper(newspaper: Newspaper): Language
    abstract fun getNewspaperByPage(page: Page): Newspaper
    abstract fun findPageById(pageId: String): Page?


    private fun isAppSettingsDataLoaded(): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return getLanguageCount() > 0 && getCountryCount() > 0 &&
                getNewsPapers().isNotEmpty() && getPageCount() > 0 && getNewsCategories().isNotEmpty()
    }

    private fun isAppSettingsUpdated(context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        val localAppSettingsUpdateTime = mAppSettingsDataService.getLocalAppSettingsUpdateTime(context)
        val appSettingsUpdateTime = mAppSettingsDataService.getServerAppSettingsUpdateTime(context)

        LoggerUtils.debugLog("appSettingsUpdateTime: " + appSettingsUpdateTime, this::class.java)

        return appSettingsUpdateTime > localAppSettingsUpdateTime
    }

    fun getRawAppsettings(context: Context): DefaultAppSettings{
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        return mAppSettingsDataService.getRawAppsettings(context)
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun loadAppSettings(context: Context) {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()

        val appSettings = mAppSettingsDataService.getAppSettings(context)

        nukeAppSettings()

        addLanguages(appSettings.languages!!.values.toList())
        addCountries(appSettings.countries!!.values.toList())
        addNewsPapers(appSettings.newspapers!!.values.toList())
        addPages(appSettings.pages!!.values.toList())
        addNewsCategories(appSettings.news_categories!!.values.toList())

        val latestSettingUpdateTime = appSettings.update_time!!.values.sorted().last()
        LoggerUtils.debugLog("latestSettingUpdateTime: $latestSettingUpdateTime",this::class.java)
        mAppSettingsDataService.saveLocalAppSettingsUpdateTime(context, latestSettingUpdateTime)
    }


    fun initAppSettings(context: Context) {

        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        if (!isAppSettingsDataLoaded() || isAppSettingsUpdated(context)) {
            loadAppSettings(context)
        }
    }

    abstract fun findNewsCategoryById(newsCategoryId: String): NewsCategory?


    companion object {
        @Volatile
        private lateinit var INSTANCE: AppSettingsRepository

        internal fun getImpl(context: Context, dbImplementation: DbImplementation): AppSettingsRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(AppSettingsRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        when (dbImplementation) {
                            DbImplementation.ROOM -> INSTANCE = AppSettingsRepositoryRoomImpl(context)
                        }
                    }
                }
            }
            return INSTANCE
        }

        internal fun getFreshImpl(context: Context, dbImplementation: DbImplementation): AppSettingsRepository {
            synchronized(AppSettingsRepository::class.java) {
                when (dbImplementation) {
                    DbImplementation.ROOM -> INSTANCE = AppSettingsRepositoryRoomImpl(context)
                }
            }
            return INSTANCE
        }
    }

}
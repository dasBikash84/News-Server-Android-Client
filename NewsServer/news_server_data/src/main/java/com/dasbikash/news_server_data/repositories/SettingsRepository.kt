/*
 * Copyright 2019 www.dasbikash.org. All rights reserved.
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
import android.util.Log
import com.dasbikash.news_server.utils.SharedPreferenceUtils
import com.dasbikash.news_server_data.data_sources.AppSettingsDataService
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.UserSettingsDataService
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.display_models.entity.*
import com.dasbikash.news_server_data.exceptions.OnMainThreadException

class SettingsRepository(context: Context) {

    private val TAG = "SettingsRepository"

    private val mAppSettingsDataService: AppSettingsDataService
    private val mUserSettingsDataService: UserSettingsDataService
    private val mDatabase: NewsServerDatabase
    private val mContext:Context

    init {
        mAppSettingsDataService =
                DataServiceImplProvider.getAppSettingsDataServiceImpl()
        mUserSettingsDataService =
                DataServiceImplProvider.getUserSettingsDataServiceImpl()
        mContext = context
        mDatabase = NewsServerDatabase.getDatabase(context)
    }

    fun isAppSettingsUpdated(): Boolean {
        val localAppSettingsUpdateTime = getLocalAppSettingsUpdateTime()
        val serverAppSettingsUpdateTime = getServerAppSettingsUpdateTime()
        return serverAppSettingsUpdateTime > localAppSettingsUpdateTime
    }

    private fun getLocalAppSettingsUpdateTime(): Long {
        val appSettingsUpdateTimestamp = SharedPreferenceUtils.getAppSettingsUpdateTimestamp(mContext)
        Log.d(TAG, "getLocalAppSettingsUpdateTime: $appSettingsUpdateTimestamp")
        return appSettingsUpdateTimestamp
    }

    private fun getServerAppSettingsUpdateTime(): Long {
        val serverAppSettingsUpdateTime = mAppSettingsDataService.getAppSettingsUpdateTime(mContext)
        Log.d(TAG, "getServerAppSettingsUpdateTime: $serverAppSettingsUpdateTime")
        SharedPreferenceUtils.saveGlobalSettingsUpdateTimestamp(mContext, serverAppSettingsUpdateTime)
        return serverAppSettingsUpdateTime
    }

    fun isSettingsDataLoaded(): Boolean {
        return getLanguageCount() > 0 && getCountryCount() > 0 &&
                getNewsPaperCount() > 0 && getPageCount() > 0 &&
                getPageGroupCount() > 0
    }

    private fun getCountryCount(): Int {
        val count = mDatabase.countryDao.count
        Log.d(TAG, "getCountryCount: $count")
        return count
    }

    private fun getLanguageCount(): Int {
        val count = mDatabase.languageDao.count
        Log.d(TAG, "getLanguageCount: $count")
        return count
    }

    private fun getNewsPaperCount(): Int {
        val count = mDatabase.newsPaperDao.count
        Log.d(TAG, "getNewsPaperCount: $count")
        return count
    }

    private fun getPageCount(): Int {
        val count = mDatabase.pageDao.count
        Log.d(TAG, "getPageCount: $count")
        return count
    }

    private fun getPageGroupCount(): Int {
        val count = mDatabase.pageGroupDao.count
        Log.d(TAG, "getPageGroupCount: $count")
        return count
    }

    @Throws(OnMainThreadException::class)/*, NoInternertConnectionException*/
    fun loadAppSettings() {
        Log.d(TAG, "loadAppSettings: ")
        val appSettings = mAppSettingsDataService.getAppSettings(mContext)

        val languages = ArrayList(appSettings.languages.values)
        mDatabase.languageDao.addLanguages(languages)
        Log.d(TAG, "loadAppSettings: languages$languages")

        val countries = ArrayList(appSettings.countries.values)
        mDatabase.countryDao.addCountries(countries)
        Log.d(TAG, "loadAppSettings: countries$countries")

        val newspapers = ArrayList(appSettings.newspapers.values)
        mDatabase.newsPaperDao.addNewsPapers(newspapers)
        Log.d(TAG, "loadAppSettings: newspapers$newspapers")

        val pages = ArrayList(appSettings.pages.values)
        mDatabase.pageDao.addPages(pages)
        Log.d(TAG, "loadAppSettings: pages$pages")

        val newsCategories = ArrayList(appSettings.page_groups.values)
        mDatabase.pageGroupDao.addPageGroups(newsCategories)
        Log.d(TAG, "loadAppSettings: newsCategories$newsCategories")

        val settingUpdateTimes = ArrayList(appSettings.update_time.values)
        SharedPreferenceUtils.saveGlobalSettingsUpdateTimestamp(mContext, settingUpdateTimes[settingUpdateTimes.size - 1])
    }



}
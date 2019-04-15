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

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.data_sources.AppSettingsDataService
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.UserSettingsDataService
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.display_models.entity.Newspaper
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.exceptions.OnMainThreadException

class SettingsRepository private constructor(context: Context) {

//    private val TAG = "SettingsRepository"

    private val mAppSettingsDataService: AppSettingsDataService
    private val mUserSettingsDataService: UserSettingsDataService
    private val mDatabase: NewsServerDatabase
    private val mContext: Context

    init {
        mAppSettingsDataService =
                DataServiceImplProvider.getAppSettingsDataServiceImpl()
        mUserSettingsDataService =
                DataServiceImplProvider.getUserSettingsDataServiceImpl()
        mContext = context
        mDatabase = NewsServerDatabase.getDatabase(context)
    }

    private fun getCountryCount(): Int = mDatabase.countryDao.count
    private fun getLanguageCount(): Int = mDatabase.languageDao.count
    private fun getNewsPaperCount(): Int = mDatabase.newsPaperDao.count
    private fun getPageCount(): Int = mDatabase.pageDao.count
    private fun getPageGroupCount(): Int = mDatabase.pageGroupDao.count

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Throws(OnMainThreadException::class, NoInternertConnectionException::class)
    fun loadAppSettings() {
        val appSettings = mAppSettingsDataService.getAppSettings(mContext)

        mDatabase.nukeAppSettings()

        mDatabase.languageDao.addLanguages(ArrayList(appSettings.languages?.values))
        mDatabase.countryDao.addCountries(ArrayList(appSettings.countries?.values))
        mDatabase.newsPaperDao.addNewsPapers(ArrayList(appSettings.newspapers?.values))
        mDatabase.pageDao.addPages(ArrayList(appSettings.pages?.values))
        mDatabase.pageGroupDao.addPageGroups(ArrayList(appSettings.page_groups?.values))

        val settingUpdateTimes = ArrayList(appSettings.update_time?.values)
        mAppSettingsDataService.saveLocalAppSettingsUpdateTime(mContext, settingUpdateTimes[settingUpdateTimes.size - 1])
    }

    fun isAppSettingsUpdated(): Boolean {
        val localAppSettingsUpdateTime = mAppSettingsDataService.getLocalAppSettingsUpdateTime(mContext)
        val serverAppSettingsUpdateTime = mAppSettingsDataService.getServerAppSettingsUpdateTime(mContext)
        return serverAppSettingsUpdateTime > localAppSettingsUpdateTime
    }

    fun isSettingsDataLoaded(): Boolean {
        return getLanguageCount() > 0 && getCountryCount() > 0 &&
                getNewsPaperCount() > 0 && getPageCount() > 0 &&
                getPageGroupCount() > 0
    }

    fun getNewsPapers():LiveData<List<Newspaper>>{
        return mDatabase.newsPaperDao.findAll()
    }

    fun getTopPagesForNewspaper(newspaper: Newspaper): List<Page> {
        return mDatabase.pageDao.getTopPagesByNewsPaperId(newspaper.id)
    }

    fun getChildPagesForTopLevelPage(topLevelPage: Page):List<Page>{
        return mDatabase.pageDao.getChildPagesByTopLevelPageId(topLevelPage.id)
    }

    companion object{
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var  INSTANCE:SettingsRepository

        fun getInstance(context: Context):SettingsRepository{
            if (!::INSTANCE.isInitialized) {
                synchronized(SettingsRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        INSTANCE = SettingsRepository(context)
                    }
                }
            }
            return INSTANCE
        }
    }

}
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

package com.dasbikash.news_server_data.repositories.room_impls

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.data_sources.AppSettingsDataService
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.exceptions.OnMainThreadException
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.utills.ExceptionUtils

class AppSettingsRepositoryRoomImpl internal constructor(context: Context): AppSettingsRepository {

    private val mAppSettingsDataService: AppSettingsDataService = DataServiceImplProvider.getAppSettingsDataServiceImpl()
    private val mDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    private fun getCountryCount(): Int = mDatabase.countryDao.count
    private fun getLanguageCount(): Int = mDatabase.languageDao.count
    private fun getNewsPaperCount(): Int = mDatabase.newsPaperDao.count
    private fun getPageCount(): Int = mDatabase.pageDao.count
    private fun getPageGroupCount(): Int = mDatabase.pageGroupDao.count

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Throws(OnMainThreadException::class, NoInternertConnectionException::class)
    override fun loadAppSettings(context: Context) {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val appSettings = mAppSettingsDataService.getAppSettings(context)

        mDatabase.nukeAppSettings()

        mDatabase.languageDao.addLanguages(ArrayList(appSettings.languages?.values))
        mDatabase.countryDao.addCountries(ArrayList(appSettings.countries?.values))
        mDatabase.newsPaperDao.addNewsPapers(ArrayList(appSettings.newspapers?.values))
        mDatabase.pageDao.addPages(ArrayList(appSettings.pages?.values))
        mDatabase.pageGroupDao.addPageGroups(ArrayList(appSettings.page_groups?.values))

        val settingUpdateTimes = ArrayList(appSettings.update_time?.values)
        mAppSettingsDataService.saveLocalAppSettingsUpdateTime(context, settingUpdateTimes[settingUpdateTimes.size - 1])
    }

    override fun isAppSettingsUpdated(context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val localAppSettingsUpdateTime = mAppSettingsDataService.getLocalAppSettingsUpdateTime(context)
        val appSettingsUpdateTime = mAppSettingsDataService.getServerAppSettingsUpdateTime(context)
        return appSettingsUpdateTime > localAppSettingsUpdateTime
    }

    override fun isAppSettingsDataLoaded(): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return getLanguageCount() > 0 && getCountryCount() > 0 &&
                getNewsPaperCount() > 0 && getPageCount() > 0 &&
                getPageGroupCount() > 0
    }

    override fun getNewsPapers():LiveData<List<Newspaper>>{
        return mDatabase.newsPaperDao.findAll()
    }

    override fun getTopPagesForNewspaper(newspaper: Newspaper): List<Page> {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageDao.getTopPagesByNewsPaperId(newspaper.id)
    }

    override fun getChildPagesForTopLevelPage(topLevelPage: Page):List<Page>{
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageDao.getChildPagesByTopLevelPageId(topLevelPage.id)
    }

    override fun getLanguageByPage(page: Page): Language {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        val newspaper = mDatabase.newsPaperDao.findById(page.newsPaperId!!)
        return mDatabase.languageDao.findByLanguageId(newspaper.languageId!!)
    }

    override fun getNewspaperByPage(page: Page): Newspaper {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.newsPaperDao.findById(page.newsPaperId ?: "")
    }

    override fun findMatchingPages(it: String): List<Page> {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageDao.findByNameContent("%"+it+"%")
    }

    override fun findPageById(pageId:String): Page? {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageDao.findById(pageId)
    }

}
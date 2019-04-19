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
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.display_models.entity.*
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.exceptions.OnMainThreadException
import java.util.*
import kotlin.collections.ArrayList

class AppSettingsRepository private constructor(context: Context) {

//    private val TAG = "AppSettingsRepository"

    private val mAppSettingsDataService: AppSettingsDataService = DataServiceImplProvider.getAppSettingsDataServiceImpl()
    private val mDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    private fun getCountryCount(): Int = mDatabase.countryDao.count
    private fun getLanguageCount(): Int = mDatabase.languageDao.count
    private fun getNewsPaperCount(): Int = mDatabase.newsPaperDao.count
    private fun getPageCount(): Int = mDatabase.pageDao.count
    private fun getPageGroupCount(): Int = mDatabase.pageGroupDao.count

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @Throws(OnMainThreadException::class, NoInternertConnectionException::class)
    fun loadAppSettings(context: Context) {
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

    fun isAppSettingsUpdated(context: Context): Boolean {
        val localAppSettingsUpdateTime = mAppSettingsDataService.getLocalAppSettingsUpdateTime(context)
        val serverAppSettingsUpdateTime = mAppSettingsDataService.getServerAppSettingsUpdateTime(context)
        return serverAppSettingsUpdateTime > localAppSettingsUpdateTime
    }

    fun isAppSettingsDataLoaded(): Boolean {
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

    fun getLanguageByPage(page: Page): Language {
        val newspaper = mDatabase.newsPaperDao.findById(page.newsPaperId!!)
        return mDatabase.languageDao.findByLanguageId(newspaper.languageId!!)
    }

    fun getTopPageforChildPage(it: Page): Page? {
        return mDatabase.pageDao.findById(it.parentPageId ?: "")
    }

    fun getNewspaperByPage(page: Page): Newspaper? {
        return mDatabase.newsPaperDao.findById(page.newsPaperId ?: "")
    }

    fun findMatchingPages(it: String): List<Page> {
        return mDatabase.pageDao.findByNameContent("%"+it+"%")
    }

    fun findArticleById(mFirstArticleId: String): Article? {
        return mDatabase.articleDao.findById(mFirstArticleId)
    }

    fun findPageById(pageId:String): Page? {
        return mDatabase.pageDao.findById(pageId)
    }

    companion object{
        @Volatile
        private lateinit var  INSTANCE:AppSettingsRepository

        internal fun getInstance(context: Context):AppSettingsRepository{
            if (!::INSTANCE.isInitialized) {
                synchronized(AppSettingsRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        INSTANCE = AppSettingsRepository(context)
                    }
                }
            }
            return INSTANCE
        }
    }

}
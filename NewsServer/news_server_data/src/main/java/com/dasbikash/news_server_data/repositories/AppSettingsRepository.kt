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
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.AppSettingsRepositoryRoomImpl
import com.dasbikash.news_server_data.utills.ExceptionUtils

abstract class AppSettingsRepository{

    private val mAppSettingsDataService: AppSettingsDataService = DataServiceImplProvider.getAppSettingsDataServiceImpl()

    abstract protected fun nukeAppSettings()
    abstract protected fun addLanguages(languages:List<Language>)
    abstract protected fun addCountries(countries:List<Country>)
    abstract protected fun addNewsPapers(newspapers:List<Newspaper>)
    abstract protected fun addPages(pages:List<Page>)
    abstract protected fun addPageGroups(pageGroups:List<PageGroup>)

    abstract protected fun getCountryCount(): Int
    abstract protected fun getLanguageCount(): Int
    abstract protected fun getNewsPaperCount(): Int
    abstract protected fun getPageCount(): Int
    abstract protected fun getPageGroupCount(): Int

    abstract fun getNewsPapers():LiveData<List<Newspaper>>
    abstract fun getTopPagesForNewspaper(newspaper: Newspaper): List<Page>
    abstract fun getChildPagesForTopLevelPage(topLevelPage: Page):List<Page>
    abstract fun findMatchingPages(it: String): List<Page>

    abstract fun getLanguageByPage(page: Page): Language
    abstract fun getNewspaperByPage(page: Page): Newspaper
    abstract fun findPageById(pageId:String): Page?


    private fun isAppSettingsDataLoaded(): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return getLanguageCount() > 0 && getCountryCount() > 0 &&
                getNewsPaperCount() > 0 && getPageCount() > 0
    }

    private fun isAppSettingsUpdated(context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        val localAppSettingsUpdateTime = mAppSettingsDataService.getLocalAppSettingsUpdateTime(context)
        val appSettingsUpdateTime = mAppSettingsDataService.getServerAppSettingsUpdateTime(context)

        return appSettingsUpdateTime > localAppSettingsUpdateTime
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun loadAppSettings(context: Context){
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        val appSettings = mAppSettingsDataService.getAppSettings(context)

        nukeAppSettings()

        addLanguages(ArrayList(appSettings.languages?.values))
        addCountries(ArrayList(appSettings.countries?.values))
        addNewsPapers(ArrayList(appSettings.newspapers?.values))
        addPages(ArrayList(appSettings.pages?.values))

        if (getPageGroupCount() == 0){
            addPageGroups(ArrayList(appSettings.page_groups?.values ?: emptyList()))
        }

        val settingUpdateTimes = ArrayList(appSettings.update_time?.values)
        mAppSettingsDataService.saveLocalAppSettingsUpdateTime(context, settingUpdateTimes.max()!!)
    }


    fun initAppSettings(context: Context){
        if (!isAppSettingsDataLoaded() || isAppSettingsUpdated(context)) {
            loadAppSettings(context)
        }
    }


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
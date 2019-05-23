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

package com.dasbikash.news_server_data.data_sources.data_services.app_settings_data_service_impls

import android.content.Context
import android.util.Log
import com.dasbikash.news_server_data.models.DefaultAppSettings
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils

internal object AppSettingsDataServiceUtils {

    fun processDefaultAppSettingsData(defaultAppSettings: DefaultAppSettings):
            DefaultAppSettings {

        defaultAppSettings.newspapers?.let {

            val filteredNewspaperMap = HashMap<String, Newspaper>()
            val filteredPageMap = HashMap<String, Page>()


            it.values
                    .filter { it.active }
                    .forEach {
                        filteredNewspaperMap.put(it.id, it)
                    }
            defaultAppSettings.newspapers = filteredNewspaperMap

            defaultAppSettings.pages?.let {

                val allPages = it.values

                val inactiveTopPageIds =
                        allPages
                                .asSequence()
                                .filter { it.parentPageId == Page.TOP_LEVEL_PAGE_PARENT_ID && !it.active }
                                .map { it.id }
                                .toCollection(mutableListOf<String>())

                LoggerUtils.debugLog("inactiveTopPageIds: ${inactiveTopPageIds.size}",this::class.java)
                allPages
                        .asSequence()
                        .filter {
                            it.active &&
                            !inactiveTopPageIds.contains(it.parentPageId) &&
                            filteredNewspaperMap.values.map { it.id }.contains(it.newspaperId)
                        }
                        .forEach { filteredPageMap.put(it.id, it) }

                filteredPageMap
                        .values
                        .filter {
                            it.parentPageId == Page.TOP_LEVEL_PAGE_PARENT_ID
                        }
                        .forEach {
                            val topPage = it
                            if (filteredPageMap.values.count {it.parentPageId == topPage.id} > 0){
                                topPage.hasChild = true
                            }
                        }

                defaultAppSettings.pages = filteredPageMap
                LoggerUtils.debugLog("filteredPageMap: ${filteredPageMap.size}",this::class.java)

            }
        }
        return defaultAppSettings
    }


    val APP_SETTINGS_UPDATE_TIME_STAMP_SP_KEY =
            "com.dasbikash.news_server_data.data_sources.data_services.app_settings_data_service_impls." +
                    "AppSettingsDataServiceUtils.APP_SETTINGS_UPDATE_TIME_STAMP_SP_KEY"

    fun getLocalAppSettingsUpdateTime(context: Context): Long{
        return SharedPreferenceUtils.
                    getData(context,SharedPreferenceUtils.DefaultValues.DEFAULT_LONG, APP_SETTINGS_UPDATE_TIME_STAMP_SP_KEY) as Long
    }
    fun saveLocalAppSettingsUpdateTime(context: Context, updateTime:Long){
        SharedPreferenceUtils.saveData(context,updateTime,APP_SETTINGS_UPDATE_TIME_STAMP_SP_KEY)
    }

}
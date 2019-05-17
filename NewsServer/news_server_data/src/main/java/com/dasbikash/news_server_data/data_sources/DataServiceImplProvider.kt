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

package com.dasbikash.news_server_data.data_sources

import com.dasbikash.news_server_data.data_sources.data_services.app_settings_data_service_impls.CloudFireStoreAppSettingsDataService
import com.dasbikash.news_server_data.data_sources.data_services.app_settings_data_service_impls.RealTimeDbAppSettingsDataService
import com.dasbikash.news_server_data.data_sources.data_services.app_settings_data_service_impls.SpringMVCAppSettingsDataService
import com.dasbikash.news_server_data.data_sources.data_services.news_data_services.CloudFireStoreNewsDataService
import com.dasbikash.news_server_data.data_sources.data_services.news_data_services.RealTimeDbNewsDataService
import com.dasbikash.news_server_data.data_sources.data_services.news_data_services.SpringMVCNewsDataService
import com.dasbikash.news_server_data.data_sources.data_services.user_settings_data_services.FirebaseUserSettingsDataService


internal object DataServiceImplProvider {

//    private val appSettingServiceOption = APP_SETTING_SERVICE_OPTIONS.SPRING_MVC_REST_SERVICE
    private lateinit var appSettingServiceOption : APP_SETTING_SERVICE_OPTIONS
//    private val newsDataServiceOption = NEWS_DATA_SERVICE_OPTIONS.SPRING_MVC_REST_SERVICE
    private lateinit var newsDataServiceOption : NEWS_DATA_SERVICE_OPTIONS

    private val userSettingServiceOption = USER_SETTING_SERVICE_OPTIONS.FIREBASE_REAL_TIME_DB

    private lateinit var appSettingsDataService: AppSettingsDataService
    private lateinit var userSettingsDataService: UserSettingsDataService
    private lateinit var newsDataService: NewsDataService

    fun getAppSettingsDataServiceImpl(): AppSettingsDataService {
        if (!::appSettingsDataService.isInitialized) {
            initAppSettingsDataService()
        }
        return appSettingsDataService
    }

    fun getUserSettingsDataServiceImpl(): UserSettingsDataService {
        if (!::userSettingsDataService.isInitialized) {
            initUserSettingsDataService()
        }
        return userSettingsDataService
    }

    fun getNewsDataServiceImpl():NewsDataService{
        if (!::newsDataService.isInitialized) {
            initNewsDataService()
        }
        return newsDataService
    }

    private fun initNewsDataService() {
        newsDataService =
                when (newsDataServiceOption) {

                    NEWS_DATA_SERVICE_OPTIONS.SPRING_MVC_REST_SERVICE
                            -> SpringMVCNewsDataService
                    NEWS_DATA_SERVICE_OPTIONS.CLOUD_FIRE_STORE
                            -> CloudFireStoreNewsDataService
                    NEWS_DATA_SERVICE_OPTIONS.FIREBASE_REAL_TIME_DB
                            -> RealTimeDbNewsDataService
                }
    }

    private fun initUserSettingsDataService() {
        userSettingsDataService =
                when (userSettingServiceOption) {

                    USER_SETTING_SERVICE_OPTIONS.FIREBASE_REAL_TIME_DB
                            -> FirebaseUserSettingsDataService
                }
    }

    private fun initAppSettingsDataService() {

        appSettingsDataService =
                when (appSettingServiceOption) {

                    APP_SETTING_SERVICE_OPTIONS.FIREBASE_REAL_TIME_DB
                    -> RealTimeDbAppSettingsDataService

                    APP_SETTING_SERVICE_OPTIONS.SPRING_MVC_REST_SERVICE
                    -> SpringMVCAppSettingsDataService

                    APP_SETTING_SERVICE_OPTIONS.CLOUD_FIRE_STORE
                    -> CloudFireStoreAppSettingsDataService
                }
    }

    fun initDataSourceImplementation():Boolean{
        var currentResult:Boolean = false
        if (SpringMVCAppSettingsDataService.ping()){
            appSettingServiceOption = APP_SETTING_SERVICE_OPTIONS.SPRING_MVC_REST_SERVICE
            newsDataServiceOption = NEWS_DATA_SERVICE_OPTIONS.SPRING_MVC_REST_SERVICE
            currentResult = true
        }else if(CloudFireStoreAppSettingsDataService.ping()){
            appSettingServiceOption = APP_SETTING_SERVICE_OPTIONS.CLOUD_FIRE_STORE
            newsDataServiceOption = NEWS_DATA_SERVICE_OPTIONS.CLOUD_FIRE_STORE
            currentResult = true
        }
        if(RealTimeDbAppSettingsDataService.ping()){
            if (!currentResult) {
                appSettingServiceOption = APP_SETTING_SERVICE_OPTIONS.FIREBASE_REAL_TIME_DB
                newsDataServiceOption = NEWS_DATA_SERVICE_OPTIONS.FIREBASE_REAL_TIME_DB
            }
            return true
        }else {
            return false
        }
    }

}

enum class APP_SETTING_SERVICE_OPTIONS {
    SPRING_MVC_REST_SERVICE,
    FIREBASE_REAL_TIME_DB,
    CLOUD_FIRE_STORE
}

enum class USER_SETTING_SERVICE_OPTIONS {
    FIREBASE_REAL_TIME_DB
}

enum class NEWS_DATA_SERVICE_OPTIONS {
    SPRING_MVC_REST_SERVICE,
    FIREBASE_REAL_TIME_DB,
    CLOUD_FIRE_STORE
}
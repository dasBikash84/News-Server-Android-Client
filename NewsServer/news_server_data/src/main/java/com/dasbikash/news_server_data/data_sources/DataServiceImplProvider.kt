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

package com.dasbikash.news_server_data.data_sources

import com.dasbikash.news_server.data_sources.data_services.app_settings_data_services.SpringMVCAppSettingsDataService
import com.dasbikash.news_server_data.data_sources.data_services.user_settings_data_services.FirebaseUserSettingsDataService
import com.dasbikash.news_server_data.data_sources.data_services.app_settings_data_services.FirebaseAppSettingsDataService


internal object DataServiceImplProvider {

    private val appSettingServiceOption = APP_SETTING_SERVICE_OPTIONS.FIREBASE_REAL_TIME_DB
    private val userSettingServiceOption = USER_SETTING_SERVICE_OPTIONS.FIREBASE_REAL_TIME_DB

    private lateinit var appSettingsDataService: AppSettingsDataService
    private lateinit var userSettingsDataService: UserSettingsDataService

    @JvmStatic
    fun getAppSettingsDataServiceImpl(): AppSettingsDataService {
        if (!DataServiceImplProvider::appSettingsDataService.isInitialized) {
            initAppSettingsDataService()
        }
        return appSettingsDataService
    }

    @JvmStatic
    fun getUserSettingsDataServiceImpl(): UserSettingsDataService {
        if (!DataServiceImplProvider::userSettingsDataService.isInitialized) {
            initUserSettingsDataService()
        }
        return userSettingsDataService
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
                    -> FirebaseAppSettingsDataService

                    APP_SETTING_SERVICE_OPTIONS.SPRING_MVC_REST_SERVICE
                    -> SpringMVCAppSettingsDataService
                }
    }

}

enum class APP_SETTING_SERVICE_OPTIONS {
    FIREBASE_REAL_TIME_DB,
    SPRING_MVC_REST_SERVICE
}

enum class USER_SETTING_SERVICE_OPTIONS {
    FIREBASE_REAL_TIME_DB
}
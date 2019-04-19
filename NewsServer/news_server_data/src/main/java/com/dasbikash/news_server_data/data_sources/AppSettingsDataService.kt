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

import android.content.Context
import com.dasbikash.news_server_data.data_sources.data_services.app_settings_data_service_impls.AppSettingsDataServiceUtils
import com.dasbikash.news_server_data.display_models.entity.DefaultAppSettings


internal interface AppSettingsDataService {

    fun getServerAppSettingsUpdateTime(context: Context): Long

    fun getRawAppSettings(context: Context): DefaultAppSettings

    fun getAppSettings(context: Context):
            DefaultAppSettings{
        return AppSettingsDataServiceUtils
                .processDefaultAppSettingsData(getRawAppSettings(context))
    }

    //Default implementations

    fun getLocalAppSettingsUpdateTime(context: Context): Long{
        return AppSettingsDataServiceUtils.getLocalAppSettingsUpdateTime(context)
    }
    fun saveLocalAppSettingsUpdateTime(context: Context, updateTime:Long){
        return AppSettingsDataServiceUtils.saveLocalAppSettingsUpdateTime(context,updateTime)
    }
}


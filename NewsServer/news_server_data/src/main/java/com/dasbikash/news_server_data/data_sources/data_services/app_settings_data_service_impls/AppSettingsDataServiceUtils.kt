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
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils

internal object AppSettingsDataServiceUtils {

    val TAG = "DbTest"

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
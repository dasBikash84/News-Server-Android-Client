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
import com.dasbikash.news_server_data.data_sources.AppSettingsDataService
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.RealtimeDBAppSettingsUtils
import com.dasbikash.news_server_data.models.DefaultAppSettings

internal object RealTimeDbAppSettingsDataService: AppSettingsDataService {

    override fun getServerAppSettingsUpdateTime(context: Context): Long {
        return RealtimeDBAppSettingsUtils.getServerAppSettingsUpdateTime()
    }

    override fun getRawAppsettings(context: Context): DefaultAppSettings {
        return RealtimeDBAppSettingsUtils.getServerAppSettingsData()
    }
}
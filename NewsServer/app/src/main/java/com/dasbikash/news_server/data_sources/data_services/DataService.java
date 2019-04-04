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

package com.dasbikash.news_server.data_sources.data_services;

import android.content.Context;

import com.dasbikash.news_server.data_sources.firebase.FirebaseRealtimeDBUtils;
import com.dasbikash.news_server.database.NewsServerDatabase;
import com.dasbikash.news_server.display_models.entity.DefaultAppSettings;

public class DataService {

    public static final String DB_ID_PREFIX = "DB_ID_";

    private NewsServerDatabase mDatabase;
    private Context mContext;

    public DataService(final Context context) {
        mDatabase = NewsServerDatabase.getDatabase(context);
        mContext = context;
    }

    public Long getServerAppSettingsUpdateTime() {
        return FirebaseRealtimeDBUtils.INSTANCE.getServerAppSettingsUpdateTime();
    }

    public DefaultAppSettings getServerAppSettings() {
        return FirebaseRealtimeDBUtils.INSTANCE.getServerAppSettingsData();
    }
}
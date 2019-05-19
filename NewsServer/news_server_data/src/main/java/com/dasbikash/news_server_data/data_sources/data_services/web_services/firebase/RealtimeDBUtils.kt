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

package com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


internal object RealtimeDBUtils {
    //App settings related nodes
    private const val APP_SETTINGS_NODE = "app_settings"
    //User settings related nodes
    private const val USER_SETTINGS_ROOT_NODE = "user_settings"
    private const val ARTICLE_DATA_ROOT_NODE = "article_data"

    val mFBDataBase: FirebaseDatabase

    init {
        mFBDataBase = FirebaseDatabase.getInstance()
    }

    val mRootReference = mFBDataBase.reference
    val mAppSettingsReference: DatabaseReference = mRootReference.child(APP_SETTINGS_NODE)
    val mUserSettingsRootReference: DatabaseReference = mRootReference.child(USER_SETTINGS_ROOT_NODE)
    val mArticleDataRootReference: DatabaseReference = mRootReference.child(ARTICLE_DATA_ROOT_NODE)
}


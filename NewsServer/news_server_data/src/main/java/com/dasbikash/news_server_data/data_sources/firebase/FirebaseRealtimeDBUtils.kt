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

package com.dasbikash.news_server_data.data_sources.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


internal object FirebaseRealtimeDBUtils {
    private val TAG = "SettingsRepository"

    //App settings related nodes
    private const val APP_SETTINGS_NODE = "app_settings"
    private const val COUNTRIES_NODE = "countries"
    private const val LANGUAGES_NODE = "languages"
    private const val NEWSPAPERS_NODE = "newspapers"
    private const val PAGES_NODE = "pages"
    private const val PAGE_GROUPS_NODE = "page_groups"
    private const val SETTINGS_UPDATE_TIME_NODE = "update_time"

    //User settings related nodes
    private const val USER_SETTINGS_ROOT_NODE = "user_settings"

    val mFBDataBase: FirebaseDatabase

    init {
        mFBDataBase = FirebaseDatabase.getInstance()
    }

    val mRootReference = mFBDataBase.reference

    val mAppSettingsReference: DatabaseReference = mRootReference.child(APP_SETTINGS_NODE)
    val mCountriesSettingsReference: DatabaseReference = mAppSettingsReference.child(COUNTRIES_NODE)
    val mLanguagesSettingsReference: DatabaseReference = mAppSettingsReference.child(LANGUAGES_NODE)
    val mNewspaperSettingsReference: DatabaseReference = mAppSettingsReference.child(NEWSPAPERS_NODE)
    val mPagesSettingsReference: DatabaseReference = mAppSettingsReference.child(PAGES_NODE)
    val mPageGroupsSettingsReference: DatabaseReference = mAppSettingsReference.child(PAGE_GROUPS_NODE)
    val mSettingsUpdateTimeReference: DatabaseReference = mAppSettingsReference.child(SETTINGS_UPDATE_TIME_NODE)

    val mUserSettingsRootReference: DatabaseReference = mRootReference.child(USER_SETTINGS_ROOT_NODE)
}


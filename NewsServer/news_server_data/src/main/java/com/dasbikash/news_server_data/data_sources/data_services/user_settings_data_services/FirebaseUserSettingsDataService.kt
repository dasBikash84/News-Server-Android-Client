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

package com.dasbikash.news_server_data.data_sources.data_services.user_settings_data_services

import android.content.Intent
import com.dasbikash.news_server_data.data_sources.UserSettingsDataService
import com.dasbikash.news_server_data.data_sources.firebase.FirebaseRealtimeDBUserSettingsUtils
import com.dasbikash.news_server_data.data_sources.firebase.FirebaseRealtimeDBUtils
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

internal object FirebaseUserSettingsDataService: UserSettingsDataService {
    override fun getCurrentUserName(): String? {
        return FirebaseRealtimeDBUserSettingsUtils.getCurrentUserName()
    }


    private val mSignInProviders = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build())

    //true if logged in
    override fun getLogInStatus() =
            FirebaseAuth.getInstance().currentUser != null

    override fun signOutUser(){
        FirebaseAuth.getInstance().signOut()
    }

    override fun getLastUserSettingsUpdateTime(): Long {
        return FirebaseRealtimeDBUserSettingsUtils.getLastUserSettingsUpdateTime()
    }

    override fun getUserPreferenceData(): UserPreferenceData {
        return FirebaseRealtimeDBUserSettingsUtils.getUserPreferenceData()
    }

    override fun uploadUserPreferenceData(userPreferenceData: UserPreferenceData) {
        return FirebaseRealtimeDBUserSettingsUtils.uploadUserPreferenceData(userPreferenceData)
    }

    override fun getLogInIntent(): Intent? {
        if (!getLogInStatus()){
            return AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(mSignInProviders)
                    .build()
        }
        return null
    }
}
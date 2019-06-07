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

import android.content.Context
import android.content.Intent
import com.dasbikash.news_server_data.data_sources.UserSettingsDataService
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.RealtimeDBAppSettingsUtils
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.RealtimeDBUserSettingsUtils
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

//internal enum class LogInStatus{NULL,}

internal object RealtimeDbUserSettingsDataService : UserSettingsDataService {

    override fun getCurrentUserName(): String? {
        return RealtimeDBUserSettingsUtils.getCurrentUserName()
    }

    private val mSignInProviders = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build())

    //true if logged in

    override fun getLogInStatus(): Boolean {
        LoggerUtils.debugLog("getLogInStatus()", this::class.java)

        val firebaseAuth = FirebaseAuth.getInstance()// != null
        if (firebaseAuth.currentUser == null) {
            RealtimeDBUserSettingsUtils.signInAnonymously()
            return false
        }
        if (firebaseAuth.currentUser!!.isAnonymous) {
            return false
        }
        return true
    }

    override fun signOutUser() {
        RealtimeDBUserSettingsUtils.signOutUser()
    }

    override fun getLastUserSettingsUpdateTime(): Long {
        return RealtimeDBUserSettingsUtils.getLastUserSettingsUpdateTime()
    }

    override fun getUserPreferenceData(): UserPreferenceData {
        return RealtimeDBUserSettingsUtils.getUserPreferenceData()
    }

    override fun uploadUserPreferenceData(userPreferenceData: UserPreferenceData) {
        return RealtimeDBUserSettingsUtils.uploadUserPreferenceData(userPreferenceData)
    }

    override fun getLogInIntent(): Intent? {
        RealtimeDBUserSettingsUtils.completeSignOut()
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(mSignInProviders)
                .build()
    }

    override fun getDefaultPageGroupSettings(): Map<String, PageGroup> {
        return RealtimeDBAppSettingsUtils.getServerAppSettingsData().page_groups?.toMap()
                ?: emptyMap()
    }

    override fun checkIfLoogedAsAdmin(): Boolean {
        return RealtimeDBUserSettingsUtils.checkIfLoogedAsAdmin()
    }

    override fun addPageToFavList(page: Page) {
        return RealtimeDBUserSettingsUtils.addPageToFavList(page)
    }

    override fun removePageFromFavList(page: Page) {
        return RealtimeDBUserSettingsUtils.removePageFromFavList(page)
    }

    override fun addPageGroup(pageGroup: PageGroup) {
        return RealtimeDBUserSettingsUtils.addPageGroup(pageGroup)
    }

    override fun deletePageGroup(pageGroup: PageGroup) {
        return RealtimeDBUserSettingsUtils.deletePageGroup(pageGroup)
    }

    override fun savePageGroup(oldPageGroup: PageGroup, pageGroup: PageGroup) {
        return RealtimeDBUserSettingsUtils.savePageGroup(oldPageGroup, pageGroup)
    }
    //    override fun getCurrentUser(): FirebaseUser? {
//        return FirebaseAuth.getInstance().currentUser
//    }
}
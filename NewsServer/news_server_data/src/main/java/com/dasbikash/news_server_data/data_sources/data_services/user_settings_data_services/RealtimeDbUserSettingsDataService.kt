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
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.RealtimeDBUserSettingsUtils
import com.dasbikash.news_server_data.models.room_entity.FavouritePageEntry
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

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

    override fun getUserPreferenceData(): List<FavouritePageEntry> {
        return RealtimeDBUserSettingsUtils.getUserPreferenceData()
    }

    override fun getLogInIntent(): Intent? {
        RealtimeDBUserSettingsUtils.completeSignOut()
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(mSignInProviders)
                .build()
    }

    override fun checkIfLoogedAsAdmin(): Boolean {
        return RealtimeDBUserSettingsUtils.checkIfLoogedAsAdmin()
    }

    override fun addPageToFavList(page: Page, doOnSuccess: (() -> Unit)?, doOnFailure: (() -> Unit)?) {
        return RealtimeDBUserSettingsUtils.addPageToFavList(page,doOnSuccess, doOnFailure)
    }

    override fun removePageFromFavList(page: Page, doOnSuccess: (() -> Unit)?, doOnFailure: (() -> Unit)?) {
        return RealtimeDBUserSettingsUtils.removePageFromFavList(page,doOnSuccess, doOnFailure)
    }

    override fun updateFavouritePageEntry(favouritePageEntry: FavouritePageEntry, doOnSuccess: (() -> Unit)?, doOnFailure: (() -> Unit)?) {
        return RealtimeDBUserSettingsUtils.updateFavouritePageEntry(favouritePageEntry, doOnSuccess, doOnFailure)
    }
}
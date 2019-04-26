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

package com.dasbikash.news_server_data.repositories

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.UserSettingsRepositoryRoomImpl
import com.firebase.ui.auth.IdpResponse

abstract class UserSettingsRepository{

    fun initUserSettings(context: Context){
        if (checkIfLoggedIn()) {
            updateUserSettingsIfModified(context)
        }
    }

    abstract fun processSignInRequestResult(data: Pair<Int, Intent?>, context: Context): Pair<SignInResult, Throwable?>

    protected abstract fun updateUserSettingsIfModified(context: Context)

    abstract fun getCurrentUserName():String?

    //Action methods
    abstract fun addPageToFavList(page: Page, context: Context): Boolean

    abstract fun removePageFromFavList(page: Page, context: Context): Boolean

    abstract fun addPageGroup(pageGroup: PageGroup, context: Context):Boolean

    abstract fun deletePageGroup(pageGroup: PageGroup, context: Context):Boolean

    abstract fun savePageGroup(oldId:String, pageGroup: PageGroup, context: Context):Boolean

    abstract fun checkIfOnFavList(mPage: Page): Boolean

    abstract fun findPageGroupByName(pageGroupName: String): PageGroup

    abstract fun checkIfLoggedIn(): Boolean

    abstract fun signOutUser()

    abstract fun getLogInIntent(): Intent?

    abstract fun getUserPreferenceLiveData(): LiveData<UserPreferenceData?>

    abstract fun getPageGroupListLive(): LiveData<List<PageGroup>>

    companion object {
        val TAG = "UserSettingsRepository"
        @Volatile
        private lateinit var INSTANCE: UserSettingsRepository

        internal fun getImpl(context: Context,dbImplementation: DbImplementation): UserSettingsRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(UserSettingsRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        when(dbImplementation){
                            DbImplementation.ROOM -> INSTANCE = UserSettingsRepositoryRoomImpl(context)
                        }
                    }
                }
            }
            return INSTANCE
        }
    }

    enum class SignInResult {
        USER_ABORT, SERVER_ERROR, SETTINGS_UPLOAD_ERROR, SUCCESS
    }

}

class UserLogInResponse(val iDpResponse: IdpResponse?) {}
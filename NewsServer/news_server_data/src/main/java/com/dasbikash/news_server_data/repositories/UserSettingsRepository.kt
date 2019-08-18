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

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.UserSettingsDataService
import com.dasbikash.news_server_data.exceptions.AuthServerException
import com.dasbikash.news_server_data.exceptions.SettingsServerException
import com.dasbikash.news_server_data.models.room_entity.FavouritePageEntry
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.UserSettingsRepositoryRoomImpl
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import com.firebase.ui.auth.IdpResponse
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

abstract class UserSettingsRepository {

    private val LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY =
            "com.dasbikash.news_server_data.repositories.UserSettingsRepository.LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY"

    private val mUserSettingsDataService: UserSettingsDataService = DataServiceImplProvider.getUserSettingsDataServiceImpl()

    abstract protected fun nukeUserPreferenceData()
    abstract protected fun addUserPreferenceDataToLocalDB(favouritePageEntries: List<FavouritePageEntry>)

    abstract protected fun addToFavouritePageEntry(page: Page):Boolean
    abstract protected fun removeFromFavouritePageEntry(page: Page):Boolean
    abstract protected fun updateFavouritePageEntry(favouritePageEntry: FavouritePageEntry):Boolean
    abstract fun getFavouritePageEntries(): List<FavouritePageEntry>
    abstract fun getFavouritePageEntryLiveData(): LiveData<List<FavouritePageEntry>>

    private fun doPostLogInProcessing(userLogInResponse: UserLogInResponse, context: Context) {

        val idpResponse = userLogInResponse.iDpResponse

        if (idpResponse == null || idpResponse.error != null) {
            throw AuthServerException()
        }
        return updateUserSettings(context)
    }

    private fun saveLastUserSettingsUpdateTime(updateTs: Long, context: Context) {
        ExceptionUtils.checkRequestValidityBeforeLocalDiskAccess()
        LoggerUtils.debugLog("saveLastUserSettingsUpdateTime: ${updateTs}", this::class.java)
        SharedPreferenceUtils.saveData(context, updateTs, LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY)
    }

    private fun getLastUserSettingsUpdateTime(context: Context): Long {
        ExceptionUtils.checkRequestValidityBeforeLocalDiskAccess()
        return SharedPreferenceUtils
                .getData(context, SharedPreferenceUtils.DefaultValues.DEFAULT_LONG, LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY) as Long
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun getServerUserPreferenceData(context: Context): List<FavouritePageEntry>{
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context)
        return mUserSettingsDataService.getUserPreferenceData().filter {appSettingsRepository.findPageById(it.pageId) !=null}.toList()
    }


    fun initUserSettings(context: Context) {
        if (checkIfLoggedIn()) {
            updateUserSettingsIfModified(context)
        }
    }

    fun processSignInRequestResult(data: Pair<Int, Intent?>, context: Context,doOnPostProcess:(()->Unit)?=null): Pair<SignInResult, Throwable?> {
        val response = IdpResponse.fromResultIntent(data.second)
        if (data.first == Activity.RESULT_OK) {
            try {
                doPostLogInProcessing(UserLogInResponse(response), context)
                doOnPostProcess?.let { it() }
                return Pair(SignInResult.SUCCESS, null)
            } catch (ex: Exception) {
                mUserSettingsDataService.getLogInStatus()
                LoggerUtils.printStackTrace(ex)
                when (ex) {
                    is SettingsServerException -> return Pair(SignInResult.SETTINGS_UPLOAD_ERROR, ex)
                    is AuthServerException -> return Pair(SignInResult.SETTINGS_UPLOAD_ERROR, ex)
                    else -> throw ex
                }
            }
        } else {
            mUserSettingsDataService.getLogInStatus()
            when {
                response == null -> return Pair(SignInResult.USER_ABORT, Throwable("Canceled by user."))
                else -> {
                    return Pair(SignInResult.SERVER_ERROR, Throwable(response.getError()))
                }
            }
        }
    }

    fun getCurrentUserName(): String? {
        return mUserSettingsDataService.getCurrentUserName()
    }

    fun getCurrentUserId(): String {
        return mUserSettingsDataService.getCurrentUserId()
    }


    fun addToFavouritePageEntryList(page: Page, context: Context,
                                    doOnSuccess: (() -> Unit)? = null, doOnFailure: (() -> Unit)? = null)
            : Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("addToFavouritePageEntryList: ${page.name}", this::class.java)

        val favouritePageEntry = findFavouritePageEntryById(page.id)
        if (favouritePageEntry != null){
            return true
        }

        mUserSettingsDataService.addPageToFavList(page,doOnSuccess ={executeBackGroundTask {
                                                        saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)
                                                        doOnSuccess?.let { it() }
                                                    }},
                                                    doOnFailure = {executeBackGroundTask {
                                                        removeFromFavouritePageEntry(page)
                                                        doOnFailure?.let { it() }
                                                    }}
                                                )
        addToFavouritePageEntry(page)
        return true
    }

    fun removeFromFavouritePageEntryList(page: Page, context: Context,
                                         doOnSuccess: (() -> Unit)? = null, doOnFailure: (() -> Unit)? = null)
            : Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("removeFromFavouritePageEntryList: ${page.name}", this::class.java)

        val favouritePageEntry = findFavouritePageEntryById(page.id)
        if (favouritePageEntry == null){
            return true
        }

        mUserSettingsDataService.removePageFromFavList(page,doOnSuccess ={executeBackGroundTask {
                                                                saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)
                                                                doOnSuccess?.let { it() }
                                                            }},
                                                            doOnFailure = {executeBackGroundTask {
                                                                addToFavouritePageEntry(page)
                                                                doOnFailure?.let { it() }
                                                            }}
                                                        )
        removeFromFavouritePageEntry(page)
        return true
    }

    abstract protected fun findFavouritePageEntryById(pageId: String): FavouritePageEntry?

    fun subscribeToFavouritePageEntry(favouritePageEntry: FavouritePageEntry, context: Context,
                                      doOnSuccess: (() -> Unit)? = null, doOnFailure: (() -> Unit)? = null)
            : Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("subscribeToFavouritePageEntry: ${favouritePageEntry.pageId}", this::class.java)

        if (favouritePageEntry.subscribed) {
            return true
        }
        favouritePageEntry.subscribed=true
        mUserSettingsDataService.updateFavouritePageEntry(favouritePageEntry,doOnSuccess ={executeBackGroundTask {
                                                                saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)
                                                                doOnSuccess?.let { it() }
                                                            }},
                                                            doOnFailure = {executeBackGroundTask {
                                                                favouritePageEntry.subscribed=false
                                                                updateFavouritePageEntry(favouritePageEntry)
                                                                doOnFailure?.let { it() }
                                                            }}
                                                        )
        updateFavouritePageEntry(favouritePageEntry)
        return true
    }

    fun unSubscribeFromFavouritePageEntry(favouritePageEntry: FavouritePageEntry, context: Context,
                                          doOnSuccess: (() -> Unit)? = null, doOnFailure: (() -> Unit)? = null)
            : Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("unSubscribeFromFavouritePageEntry: ${favouritePageEntry.pageId}", this::class.java)

        if (!favouritePageEntry.subscribed) {
            return true
        }
        favouritePageEntry.subscribed=false
        mUserSettingsDataService.updateFavouritePageEntry(favouritePageEntry,doOnSuccess ={executeBackGroundTask {
                                                                saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)
                                                                doOnSuccess?.let { it() }
                                                            }},
                                                            doOnFailure = {executeBackGroundTask {
                                                                favouritePageEntry.subscribed=true
                                                                updateFavouritePageEntry(favouritePageEntry)
                                                                doOnFailure?.let { it() }
                                                            }}
                                                        )
        updateFavouritePageEntry(favouritePageEntry)
        return true
    }

    private fun executeBackGroundTask(task:()->Unit){
        LoggerUtils.debugLog("executeBackGroundTask", this::class.java)
        Observable.just(task)
                .subscribeOn(Schedulers.io())
                .map {
                    task()
                }
                .subscribe()
    }

    private fun updateUserSettingsIfModified(context: Context) {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("updateUserSettingsIfModified", this::class.java)

        val serverLastUserSettingsUpdateTime = mUserSettingsDataService.getLastUserSettingsUpdateTime()
        if (serverLastUserSettingsUpdateTime > getLastUserSettingsUpdateTime(context)) {
            updateUserSettings(context)
        }
    }

    private fun updateUserSettings(context: Context) {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("updateUserSettings", this::class.java)

        nukeUserPreferenceData()
        addUserPreferenceDataToLocalDB(getServerUserPreferenceData(context))
        val serverLastUserSettingsUpdateTime = mUserSettingsDataService.getLastUserSettingsUpdateTime()
        saveLastUserSettingsUpdateTime(serverLastUserSettingsUpdateTime, context)
    }

    fun checkIfLoggedIn(): Boolean {
        return mUserSettingsDataService.getLogInStatus()
    }

    fun checkIfLoogedAsAdmin(): Boolean {
        return mUserSettingsDataService.checkIfLoogedAsAdmin()
    }

    fun signOutUser(context: Context,doBeforeSignOut:(()->Unit)?=null) {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        doBeforeSignOut?.let { it() }
        mUserSettingsDataService.signOutUser()
        resetUserSettings(context)
    }

    fun getLogInIntent(): Intent? {
        return mUserSettingsDataService.getLogInIntent()
    }

    fun resetUserSettings(context: Context) {
        resetUserSettings()
        saveLastUserSettingsUpdateTime(0L, context)
    }

    abstract protected fun resetUserSettings()

    companion object {
        @Volatile
        private lateinit var INSTANCE: UserSettingsRepository

        internal fun getImpl(context: Context, dbImplementation: DbImplementation): UserSettingsRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(UserSettingsRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        when (dbImplementation) {
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

    private class UserLogInResponse(val iDpResponse: IdpResponse?) {}
}


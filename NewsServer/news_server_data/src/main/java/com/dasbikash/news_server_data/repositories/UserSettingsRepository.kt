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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.UserSettingsDataService
import com.dasbikash.news_server_data.exceptions.AuthServerException
import com.dasbikash.news_server_data.exceptions.SettingsServerException
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.UserSettingsRepositoryRoomImpl
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import com.firebase.ui.auth.IdpResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

abstract class UserSettingsRepository {

    private val LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY =
            "com.dasbikash.news_server_data.repositories.UserSettingsRepository.LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY"

    private val mUserSettingsDataService: UserSettingsDataService = DataServiceImplProvider.getUserSettingsDataServiceImpl()

    abstract protected fun nukeUserPreferenceDataTable()
    abstract protected fun nukePageGroupTable()
    abstract protected fun addUserPreferenceDataToLocalDB(userPreferenceData: UserPreferenceData)

    abstract protected fun getUserPreferenceDataFromLocalDB(): UserPreferenceData
    abstract protected fun saveUserPreferenceDataToLocalDb(userPreferenceData: UserPreferenceData)
    abstract protected fun addPageGroupsToLocalDb(pageGroups: List<PageGroup>)
    abstract protected fun deletePageGroupFromLocalDb(pageGroup: PageGroup)
    abstract protected fun getLocalPreferenceData(): List<UserPreferenceData>
    abstract fun findPageGroupByName(pageGroupName: String): PageGroup?
    abstract fun getUserPreferenceLiveData(): LiveData<UserPreferenceData?>
    abstract fun getPageGroupListLive(): LiveData<List<PageGroup>>
    abstract fun getPageGroupListCount(): Int

    private fun doPostLogInProcessing(userLogInResponse: UserLogInResponse, context: Context) {

        val idpResponse = userLogInResponse.iDpResponse

        if (idpResponse == null || idpResponse.error != null) {
            throw AuthServerException()
        }

        if (idpResponse.isNewUser) {
            LoggerUtils.debugLog("New user", this::class.java)
            return uploadUserPreferenceData(context, getUserPreferenceDataFromLocalDB())
        } else {
            return updateUserSettingsIfModified(context)
        }
    }

    private fun uploadUserPreferenceData(context: Context, userPreferenceData: UserPreferenceData) {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("uploadUserPreferenceData", this::class.java)
        mUserSettingsDataService.uploadUserPreferenceData(userPreferenceData)
        saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)
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
    private fun getServerUserPreferenceData(): UserPreferenceData {

        val userPreferenceData = mUserSettingsDataService.getUserPreferenceData()
        //Remove if any null entry found
        userPreferenceData.favouritePageIds = userPreferenceData.favouritePageIds.filter { it != null }.toMutableList()

        return userPreferenceData
    }


    fun initUserSettings(context: Context) {
        if (checkIfLoggedIn()) {
            updateUserSettingsIfModified(context)
        } else {
            if (getPageGroupListCount() == 0) {
                resetUserSettings(context)
            }
        }
    }

    fun processSignInRequestResult(data: Pair<Int, Intent?>, context: Context): Pair<SignInResult, Throwable?> {
        val response = IdpResponse.fromResultIntent(data.second)
        if (data.first == Activity.RESULT_OK) {
            try {
                doPostLogInProcessing(UserLogInResponse(response), context)
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

//    fun getCurrentUser(): FirebaseUser? {
//        return mUserSettingsDataService.getCurrentUser()
//    }

    fun addPageToFavList(page: Page, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("addPageToFavList: ${page.name}", this::class.java)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        if (userPreferenceData.favouritePageIds.contains(page.id)) {
            return true
        }
        userPreferenceData.favouritePageIds.add(page.id)
        mUserSettingsDataService.addPageToFavList(page,doOnSuccess ={executeBackGroundTask {
                                                        saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)}},
                                                    doOnFailure = {executeBackGroundTask {
                                                        userPreferenceData.favouritePageIds.remove(page.id)
                                                        saveUserPreferenceDataToLocalDb(userPreferenceData)
                                                    }}
                                                )
        saveUserPreferenceDataToLocalDb(userPreferenceData)
        return true
    }

    fun removePageFromFavList(page: Page, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("removePageFromFavList: ${page.name}", this::class.java)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        if (!userPreferenceData.favouritePageIds.contains(page.id)) {
            return true
        }
        userPreferenceData.favouritePageIds.remove(page.id)
        mUserSettingsDataService.removePageFromFavList(page,doOnSuccess ={executeBackGroundTask {
                                                                saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)}},
                                                            doOnFailure = {executeBackGroundTask {
                                                                userPreferenceData.favouritePageIds.add(page.id)
                                                                saveUserPreferenceDataToLocalDb(userPreferenceData)
                                                            }}
                                                        )
        saveUserPreferenceDataToLocalDb(userPreferenceData)
        return true
    }

    fun addPageGroup(pageGroup: PageGroup, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("addPageGroup: ${pageGroup.name}", this::class.java)

        mUserSettingsDataService.addPageGroup(pageGroup,doOnSuccess ={executeBackGroundTask {
                                                                saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)}},
                                                        doOnFailure = {executeBackGroundTask {
                                                            deletePageGroupFromLocalDb(pageGroup)
                                                        }}
                                                )
        addPageGroupsToLocalDb(listOf<PageGroup>(pageGroup))
        return true
    }

    fun deletePageGroup(pageGroup: PageGroup, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("deletePageGroup: ${pageGroup.name}", this::class.java)

        mUserSettingsDataService.deletePageGroup(pageGroup,doOnSuccess ={executeBackGroundTask {
                                                                saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)}},
                                                            doOnFailure = {executeBackGroundTask {
                                                                addPageGroupsToLocalDb(listOf<PageGroup>(pageGroup))
                                                            }}
                                                )
        deletePageGroupFromLocalDb(pageGroup)
        return true
    }

    fun savePageGroup(oldPageGroup: PageGroup, pageGroup: PageGroup, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("savePageGroup: ${pageGroup.name}", this::class.java)

        mUserSettingsDataService.deletePageGroup(oldPageGroup,doOnSuccess =
                                                        {executeBackGroundTask {
                                                            mUserSettingsDataService.addPageGroup(pageGroup,doOnSuccess =
                                                                    {executeBackGroundTask {
                                                                        saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)}},
                                                                    doOnFailure = {executeBackGroundTask {
                                                                        addPageGroupsToLocalDb(listOf<PageGroup>(oldPageGroup))
                                                                        deletePageGroupFromLocalDb(pageGroup)
                                                                    }})
                                                        }},
                                                        doOnFailure = {executeBackGroundTask {
                                                            addPageGroupsToLocalDb(listOf<PageGroup>(oldPageGroup))
                                                            deletePageGroupFromLocalDb(pageGroup)
                                                        }}
                                                )
        deletePageGroupFromLocalDb(oldPageGroup)
        addPageGroupsToLocalDb(listOf<PageGroup>(pageGroup))
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
            val userPreferenceData = getServerUserPreferenceData()
            userPreferenceData.id = UUID.randomUUID().toString()
            nukeUserPreferenceDataTable()
            addUserPreferenceDataToLocalDB(userPreferenceData)
            nukePageGroupTable()
            addPageGroupsToLocalDb(userPreferenceData.pageGroups.values.toList())
            saveLastUserSettingsUpdateTime(serverLastUserSettingsUpdateTime, context)
        }
    }

    fun checkIfLoggedIn(): Boolean {
        return mUserSettingsDataService.getLogInStatus()
    }

    fun signOutUser(context: Context) {
        mUserSettingsDataService.signOutUser()
        resetUserSettings(context)
    }

    fun getLogInIntent(): Intent? {
        return mUserSettingsDataService.getLogInIntent()
    }

    fun resetUserSettings(context: Context) {
        saveLastUserSettingsUpdateTime(0L, context)
        resetUserSettings(getDefaultPageGroupSettings())
    }

    abstract protected fun resetUserSettings(defaultPageGroups: Map<String, PageGroup>)

    private fun getDefaultPageGroupSettings(): Map<String, PageGroup> {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        return mUserSettingsDataService.getDefaultPageGroupSettings()
    }

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


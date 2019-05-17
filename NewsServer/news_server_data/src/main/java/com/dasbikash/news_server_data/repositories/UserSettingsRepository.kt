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
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.dasbikash.news_server_data.repositories.repo_helpers.DbImplementation
import com.dasbikash.news_server_data.repositories.room_impls.UserSettingsRepositoryRoomImpl
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseUser
import java.util.*

abstract class UserSettingsRepository {

    private val LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY =
            "com.dasbikash.news_server_data.repositories.UserSettingsRepository.LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY"

    private val mUserSettingsDataService: UserSettingsDataService = DataServiceImplProvider.getUserSettingsDataServiceImpl()

    abstract protected fun nukeUserPreferenceDataTable()
    abstract protected fun nukePageGroupTable()
    abstract protected fun addUserPreferenceDataToLocalDB(userPreferenceData:UserPreferenceData)

    abstract protected fun getUserPreferenceDataFromLocalDB(): UserPreferenceData
    abstract protected fun saveUserPreferenceDataToLocalDb(userPreferenceData: UserPreferenceData)
    abstract protected fun addPageGroupsToLocalDb(pageGroups: List<PageGroup>)
    abstract protected fun deletePageGroupFromLocalDb(pageGroupName: String)
    abstract protected fun getLocalPreferenceData(): List<UserPreferenceData>
    abstract fun findPageGroupByName(pageGroupName: String): PageGroup
    abstract fun getUserPreferenceLiveData(): LiveData<UserPreferenceData?>
    abstract fun getPageGroupListLive(): LiveData<List<PageGroup>>

    private fun doPostLogInProcessing(userLogInResponse: UserLogInResponse, context: Context) {
        val idpResponse = userLogInResponse.iDpResponse
        if (idpResponse == null || idpResponse.error != null) {
            throw AuthServerException()
        }

        if (idpResponse.isNewUser) {
            LoggerUtils.debugLog( "New user",this::class.java)
            return uploadUserPreferenceData(context, getUserPreferenceDataFromLocalDB())
        } else {
            return updateUserSettingsIfModified(context)
        }
    }

    private fun uploadUserPreferenceData(context: Context, userPreferenceData: UserPreferenceData) {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog( "uploadUserPreferenceData",this::class.java)
        mUserSettingsDataService.uploadUserPreferenceData(userPreferenceData)
        saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)
    }

    private fun saveLastUserSettingsUpdateTime(updateTs: Long, context: Context) {
        ExceptionUtils.checkRequestValidityBeforeLocalDiskAccess()
        LoggerUtils.debugLog( "saveLastUserSettingsUpdateTime: ${updateTs}",this::class.java)
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
        userPreferenceData.favouritePageIds = userPreferenceData.favouritePageIds.filter { it != null }.toCollection(mutableListOf())

        return userPreferenceData
    }


    fun initUserSettings(context: Context) {
        if (checkIfLoggedIn()) {
            updateUserSettingsIfModified(context)
        }
    }

    fun processSignInRequestResult(data: Pair<Int, Intent?>, context: Context): Pair<SignInResult, Throwable?> {
        val response = IdpResponse.fromResultIntent(data.second)
        if (data.first == Activity.RESULT_OK) {
            try {
                doPostLogInProcessing(UserLogInResponse(response), context)
                return Pair(SignInResult.SUCCESS, null)
            } catch (ex: Exception) {
                ex.printStackTrace()
                when (ex) {
                    is SettingsServerException -> return Pair(SignInResult.SETTINGS_UPLOAD_ERROR, ex)
                    is AuthServerException -> return Pair(SignInResult.SETTINGS_UPLOAD_ERROR, ex)
                    else -> throw ex
                }
            }
        } else {
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
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        LoggerUtils.debugLog( "addPageToFavList: ${page.name}",this::class.java)
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        if (userPreferenceData.favouritePageIds.contains(page.id)){
            return true
        }
        userPreferenceData.favouritePageIds.add(page.id)
        uploadUserPreferenceData(context,userPreferenceData)

        saveUserPreferenceDataToLocalDb(userPreferenceData)

        return true
    }

    fun removePageFromFavList(page: Page, context: Context): Boolean{
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        LoggerUtils.debugLog( "removePageFromFavList: ${page.name}",this::class.java)
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        if (! userPreferenceData.favouritePageIds.contains(page.id)){
            return true
        }
        userPreferenceData.favouritePageIds.remove(page.id)
        uploadUserPreferenceData(context,userPreferenceData)
        saveUserPreferenceDataToLocalDb(userPreferenceData)

        return true
    }

    fun addPageGroup(pageGroup: PageGroup, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        LoggerUtils.debugLog( "addPageGroup: ${pageGroup.name}",this::class.java)
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        userPreferenceData.pageGroups.put(pageGroup.name,pageGroup)
        uploadUserPreferenceData(context,userPreferenceData)
        addPageGroupsToLocalDb(listOf<PageGroup>(pageGroup))
        return true
    }

    fun deletePageGroup(pageGroup: PageGroup, context: Context): Boolean{
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        LoggerUtils.debugLog( "deletePageGroup: ${pageGroup.name}",this::class.java)
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        if (!userPreferenceData.pageGroups.keys.contains(pageGroup.name)){
            return true
        }
        userPreferenceData.pageGroups.remove(pageGroup.name)
        uploadUserPreferenceData(context,userPreferenceData)
        deletePageGroupFromLocalDb(pageGroup.name)
        return true
    }

    fun savePageGroup(oldId: String, pageGroup: PageGroup, context: Context): Boolean{
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        LoggerUtils.debugLog( "savePageGroup: ${pageGroup.name}",this::class.java)
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        userPreferenceData.pageGroups.remove(oldId)
        pageGroup.pageEntityList.clear()
        userPreferenceData.pageGroups.put(pageGroup.name,pageGroup)
        uploadUserPreferenceData(context,userPreferenceData)
        deletePageGroupFromLocalDb(oldId)
        addPageGroupsToLocalDb(listOf(pageGroup))
        return true
    }

    fun checkIfOnFavList(mPage: Page): Boolean{
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        LoggerUtils.debugLog( "checkIfOnFavList: ${mPage.name}",this::class.java)
        val userPreferenceDataList = getLocalPreferenceData()
        if (userPreferenceDataList.size > 0) {
            return userPreferenceDataList.get(0).favouritePageIds.contains(mPage.id)
        }
        return false
    }

    private fun updateUserSettingsIfModified(context: Context) {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        LoggerUtils.debugLog("updateUserSettingsIfModified",this::class.java)

        if (mUserSettingsDataService.getLastUserSettingsUpdateTime() > getLastUserSettingsUpdateTime(context)) {
            val userPreferenceData = getServerUserPreferenceData()
            userPreferenceData.id = UUID.randomUUID().toString()
            nukeUserPreferenceDataTable()
            addUserPreferenceDataToLocalDB(userPreferenceData)
            nukePageGroupTable()
            addPageGroupsToLocalDb(userPreferenceData.pageGroups.values.toList())
        }
    }

    fun checkIfLoggedIn(): Boolean {
        return mUserSettingsDataService.getLogInStatus()
    }

    fun signOutUser(){
        return mUserSettingsDataService.signOutUser()
    }

    fun getLogInIntent(): Intent?{
        return mUserSettingsDataService.getLogInIntent()
    }

    companion object {
        val TAG = "UserSettingsRepository"
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

}

class UserLogInResponse(val iDpResponse: IdpResponse?) {}
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
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.UserSettingsDataService
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.display_models.entity.PageGroup
import com.dasbikash.news_server_data.display_models.entity.UserPreferenceData
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import com.firebase.ui.auth.IdpResponse
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class UserSettingsRepository private constructor(context: Context) {

    private val DISABLE_PROMPT_FOR_LOG_IN_FLAG_SP_KEY =
            "com.dasbikash.news_server_data.repositories.UserSettingsRepository.DISABLE_PROMPT_FOR_LOG_IN_FLAG_SP_KEY"

    private val LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY =
            "com.dasbikash.news_server_data.repositories.UserSettingsRepository.LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY"

    private val mUserSettingsDataService: UserSettingsDataService = DataServiceImplProvider.getUserSettingsDataServiceImpl()
    private val mDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    private fun saveLastUserSettingsUpdateTime(updateTs: Long, context: Context) {
        ExceptionUtils.checkRequestValidityBeforeLocalDiskAccess()
        Log.d("HomeActivity", "saveLastUserSettingsUpdateTime: ${updateTs}")
        SharedPreferenceUtils.saveData(context, updateTs, LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY)
    }

    private fun getLastUserSettingsUpdateTime(context: Context): Long {
        ExceptionUtils.checkRequestValidityBeforeLocalDiskAccess()
        return SharedPreferenceUtils
                .getData(context, SharedPreferenceUtils.DefaultValues.DEFAULT_LONG, LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY) as Long
    }

    private fun doPostLogInProcessing(userLogInResponse: UserLogInResponse, context: Context): Boolean {
        val idpResponse = userLogInResponse.iDpResponse
        if (idpResponse == null) return false
        idpResponse.error?.let { return false }
        if (idpResponse.isNewUser) {
            uploadUserSettingsToServer(context)
        } else {
//            download And Save User Settings From Server
            saveUserPreferenceData(downloadUserPreferenceData(), context)
        }
        return true
    }

    private fun saveUserPreferenceData(userPreferenceData: UserPreferenceData, context: Context) {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()

        val lastUserSettingsUpdateTime =
                getLastUserSettingsUpdateTime(userPreferenceData)

        saveLastUserSettingsUpdateTime(lastUserSettingsUpdateTime, context)

        mDatabase.userPreferenceDataDao.nukeTable()
        userPreferenceData.id = UUID.randomUUID().toString()
        mDatabase.userPreferenceDataDao.add(userPreferenceData)
        mDatabase.pageGroupDao.nukeTable()
        mDatabase.pageGroupDao.addPageGroups(userPreferenceData.pageGroups.values.toList())
    }

    private fun getLastUserSettingsUpdateTime(userPreferenceData: UserPreferenceData): Long {
        return userPreferenceData.updateLog.values.sortedBy { it.timeStamp }.last().timeStamp!!
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun downloadUserPreferenceData(): UserPreferenceData {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        val userPreferenceData = mUserSettingsDataService.getUserPreferenceData()

        //Remove if any null entry found
        userPreferenceData.favouritePageIds = userPreferenceData.favouritePageIds.filter { it != null }.toCollection(mutableListOf())
        userPreferenceData.inActivePageIds = userPreferenceData.inActivePageIds.filter { it != null }.toCollection(mutableListOf())
        userPreferenceData.inActiveNewsPaperIds = userPreferenceData.inActiveNewsPaperIds.filter { it != null }.toCollection(mutableListOf())

        return userPreferenceData
    }

    private fun uploadUserSettingsToServer(context: Context) {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        val userPreferenceData = mDatabase.userPreferenceDataDao.findUserPreferenceStaticData()
        mDatabase.pageGroupDao.findAllStatic()
                .asSequence()
                .forEach { userPreferenceData.pageGroups.put(it.name, it) }
        mUserSettingsDataService.uploadUserSettings(userPreferenceData)
        saveLastUserSettingsUpdateTime(getLastUserSettingsUpdateTime(downloadUserPreferenceData()), context)
    }

    private fun uploadUserSettingsToServerIfLoggedIn(context: Context) {
        if (checkIfLoggedIn()) {
            uploadUserSettingsToServer(context)
        }
    }

    fun shouldPromptForLogIn(context: Context): Boolean {
        return !(SharedPreferenceUtils
                .getData(context, SharedPreferenceUtils.DefaultValues.DEFAULT_BOOLEAN, DISABLE_PROMPT_FOR_LOG_IN_FLAG_SP_KEY) as Boolean)
    }

    fun disablePromptForLogIn(context: Context) {
        SharedPreferenceUtils
                .saveData(context, true, DISABLE_PROMPT_FOR_LOG_IN_FLAG_SP_KEY)
    }

    fun processSignInRequestResult(data: Pair<Int, Intent?>, context: Context): Pair<SignInResult, Throwable?> {

        val response = IdpResponse.fromResultIntent(data.second)

        if (data.first == Activity.RESULT_OK) {
            try {
                doPostLogInProcessing(UserLogInResponse(response), context)
                return Pair(SignInResult.SUCCESS, null)
            } catch (ex: Throwable) {
                ex.printStackTrace()
                return Pair(SignInResult.SETTINGS_UPLOAD_ERROR, ex)
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

    fun checkIfLoggedIn(): Boolean {
        return mUserSettingsDataService.getLogInStatus()
    }

    fun signOutUser() {
        return mUserSettingsDataService.signOutUser()
    }

    fun getLogInIntent(): Intent? {
        return mUserSettingsDataService.getLogInIntent()
    }

    fun updateUserSettingsIfModified(context: Context): Boolean {

        if (!checkIfLoggedIn()) return false
        val userPreferenceData = downloadUserPreferenceData()

        if (getLastUserSettingsUpdateTime(userPreferenceData) > getLastUserSettingsUpdateTime(context)) {
            saveUserPreferenceData(userPreferenceData, context)
        }
        return true
    }

    fun checkIfOnFavList(mPage: Page): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "checkIfOnFavList: ${mPage.name}")

        val userPreferenceDataList = mDatabase.userPreferenceDataDao.findAll()
        if (userPreferenceDataList.size > 0) {
            return userPreferenceDataList.get(0).favouritePageIds.contains(mPage.id)
        }
        return false
    }

    fun addPageToFavList(page: Page, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "addPageToFavList: ${page.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceDataList = mDatabase.userPreferenceDataDao.findAll()
        val userPreferenceData: UserPreferenceData
        if (userPreferenceDataList.size == 0) {
            userPreferenceData = UserPreferenceData(id = UUID.randomUUID().toString())
        } else {
            userPreferenceData = userPreferenceDataList.get(0)
        }
        if (!userPreferenceData.favouritePageIds.contains(page.id)) {
            userPreferenceData.favouritePageIds.add(page.id)
        } else {
            return true
        }
        if (userPreferenceDataList.size == 0) {
            mDatabase.userPreferenceDataDao.add(userPreferenceData)
        } else {
            mDatabase.userPreferenceDataDao.save(userPreferenceData)
        }
        uploadUserSettingsToServerIfLoggedIn(context)
        return true
    }

    fun removePageFromFavList(page: Page, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "removePageFromFavList: ${page.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceDataList = mDatabase.userPreferenceDataDao.findAll()
        val userPreferenceData: UserPreferenceData
        if (userPreferenceDataList.size == 0) {
            return false
        }
        userPreferenceData = userPreferenceDataList.get(0)
        if (userPreferenceData.favouritePageIds.contains(page.id)) {
            userPreferenceData.favouritePageIds.remove(page.id)
            mDatabase.userPreferenceDataDao.save(userPreferenceData)
            uploadUserSettingsToServerIfLoggedIn(context)
            return true
        } else {
            return false
        }
    }

    fun getUserPreferenceLiveData(): LiveData<UserPreferenceData> {
        return mDatabase.userPreferenceDataDao.findUserPreferenceData()
    }

    fun getPageGroupListLive(): LiveData<List<PageGroup>> {
        return mDatabase.pageGroupDao.findAllLive()
    }

    fun deletePageGroup(pageGroup: PageGroup, context: Context):Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "deletePageGroup: ${pageGroup.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        mDatabase.pageGroupDao.delete(pageGroup)
        uploadUserSettingsToServerIfLoggedIn(context)
        return true
    }

    fun findPageGroupByName(pageGroupName: String): PageGroup {
        return mDatabase.pageGroupDao.findById(pageGroupName)
    }

    fun add(pageGroup: PageGroup,context: Context):Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "add: ${pageGroup.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        mDatabase.pageGroupDao.add(pageGroup)
        uploadUserSettingsToServerIfLoggedIn(context)
        return true
    }

    fun save(pageGroup: PageGroup,context: Context):Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "save: ${pageGroup.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        mDatabase.pageGroupDao.save(pageGroup)
        uploadUserSettingsToServerIfLoggedIn(context)
        return true
    }

    companion object {
        val TAG = "UserSettingsRepository"
        @Volatile
        private lateinit var INSTANCE: UserSettingsRepository

        internal fun getInstance(context: Context): UserSettingsRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(UserSettingsRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        INSTANCE = UserSettingsRepository(context)
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
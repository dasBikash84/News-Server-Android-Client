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

package com.dasbikash.news_server_data.repositories.room_impls

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.UserSettingsDataService
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.exceptions.AuthServerException
import com.dasbikash.news_server_data.exceptions.SettingsServerException
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.dasbikash.news_server_data.repositories.UserLogInResponse
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import com.firebase.ui.auth.IdpResponse
import java.util.*

class UserSettingsRepositoryRoomImpl internal constructor(context: Context) :
        UserSettingsRepository() {

    private val LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY =
            "com.dasbikash.news_server_data.repositories.UserSettingsRepository.LAST_USER_SETTINGS_UPDATE_TIMESTAMP_SP_KEY"

    private val mUserSettingsDataService: UserSettingsDataService = DataServiceImplProvider.getUserSettingsDataServiceImpl()
    private val mDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)


    //Remote settings server com methods
    @Suppress("SENSELESS_COMPARISON")
    private fun getServerUserPreferenceData(): UserPreferenceData {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        val userPreferenceData = mUserSettingsDataService.getUserPreferenceData()
        //Remove if any null entry found
        userPreferenceData.favouritePageIds = userPreferenceData.favouritePageIds.filter { it != null }.toCollection(mutableListOf())

        return userPreferenceData
    }

    private fun uploadUserPreferenceData(context: Context,userPreferenceData:UserPreferenceData) {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        Log.d(TAG,"uploadUserPreferenceData")
        mUserSettingsDataService.uploadUserPreferenceData(userPreferenceData)
        saveLastUserSettingsUpdateTime(mUserSettingsDataService.getLastUserSettingsUpdateTime(), context)
    }

    override fun processSignInRequestResult(data: Pair<Int, Intent?>, context: Context): Pair<UserSettingsRepository.SignInResult, Throwable?> {
        val response = IdpResponse.fromResultIntent(data.second)
        if (data.first == Activity.RESULT_OK) {
            try {
                doPostLogInProcessing(UserLogInResponse(response), context)
                return Pair(UserSettingsRepository.SignInResult.SUCCESS, null)
            } catch (ex: Exception) {
                ex.printStackTrace()
                when(ex){
                    is SettingsServerException  -> return Pair(UserSettingsRepository.SignInResult.SETTINGS_UPLOAD_ERROR, ex)
                    is AuthServerException      -> return Pair(UserSettingsRepository.SignInResult.SETTINGS_UPLOAD_ERROR, ex)
                    else                        -> throw ex
                }
            }
        } else {
            when {
                response == null -> return Pair(UserSettingsRepository.SignInResult.USER_ABORT, Throwable("Canceled by user."))
                else -> {
                    return Pair(UserSettingsRepository.SignInResult.SERVER_ERROR, Throwable(response.getError()))
                }
            }
        }
    }

    private fun doPostLogInProcessing(userLogInResponse: UserLogInResponse, context: Context) {
        val idpResponse = userLogInResponse.iDpResponse
        if (idpResponse == null || idpResponse.error!= null) {throw AuthServerException()}

        if (idpResponse.isNewUser) {
            Log.d(TAG,"New user")
            return uploadUserPreferenceData(context,getUserPreferenceDataFromLocalDB())
        } else {
//            download And Save User Settings From Server
            return updateUserSettingsIfModified(context)
        }
    }

    private fun getUserPreferenceDataFromLocalDB(): UserPreferenceData {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        var userPreferenceData:UserPreferenceData? = mDatabase.userPreferenceDataDao.findUserPreferenceStaticData()
        if(userPreferenceData == null) {
            userPreferenceData = UserPreferenceData(id=UUID.randomUUID().toString())
            mDatabase.userPreferenceDataDao.add(userPreferenceData)
        }
        mDatabase.pageGroupDao.findAllStatic()
                .asSequence()
                .forEach { userPreferenceData.pageGroups.put(it.name, it) }
        Log.d(TAG,"getUserPreferenceDataFromLocalDB: ${userPreferenceData}")
        return userPreferenceData
    }

    override fun updateUserSettingsIfModified(context: Context) {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG,"updateUserSettingsIfModified")

        if (mUserSettingsDataService.getLastUserSettingsUpdateTime() > getLastUserSettingsUpdateTime(context)) {
            Log.d(TAG,"mUserSettingsDataService.getLastUserSettingsUpdateTime() > getLastUserSettingsUpdateTime(context)")
            val userPreferenceData = getServerUserPreferenceData()
            userPreferenceData.id = UUID.randomUUID().toString()
            mDatabase.userPreferenceDataDao.nukeTable()
            mDatabase.userPreferenceDataDao.add(userPreferenceData)
            mDatabase.pageGroupDao.nukeTable()
            mDatabase.pageGroupDao.addPageGroups(userPreferenceData.pageGroups.values.toList())
        }
    }

    override fun getCurrentUserName():String?{
        return mUserSettingsDataService.getCurrentUserName()
    }

    //Action methods
    override fun addPageToFavList(page: Page, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "addPageToFavList: ${page.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        if (userPreferenceData.favouritePageIds.contains(page.id)){
            return true
        }
        userPreferenceData.favouritePageIds.add(page.id)
        uploadUserPreferenceData(context,userPreferenceData)
        mDatabase.userPreferenceDataDao.save(userPreferenceData)

        return true
    }

    override fun removePageFromFavList(page: Page, context: Context): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "removePageFromFavList: ${page.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        if (! userPreferenceData.favouritePageIds.contains(page.id)){
            return true
        }
        userPreferenceData.favouritePageIds.remove(page.id)
        uploadUserPreferenceData(context,userPreferenceData)
        mDatabase.userPreferenceDataDao.save(userPreferenceData)

        return true
    }

    override fun addPageGroup(pageGroup: PageGroup, context: Context):Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "addPageGroup: ${pageGroup.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        userPreferenceData.pageGroups.put(pageGroup.name,pageGroup)
        uploadUserPreferenceData(context,userPreferenceData)
        mDatabase.pageGroupDao.add(pageGroup)
        return true
    }

    override fun deletePageGroup(pageGroup: PageGroup, context: Context):Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "deletePageGroup: ${pageGroup.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        if (!userPreferenceData.pageGroups.keys.contains(pageGroup.name)){
            return true
        }
        userPreferenceData.pageGroups.remove(pageGroup.name)
        uploadUserPreferenceData(context,userPreferenceData)
        mDatabase.pageGroupDao.delete(pageGroup)
        return true
    }

    override fun savePageGroup(oldId:String, pageGroup: PageGroup, context: Context):Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "savePageGroup: ${pageGroup.name}")
        //Before update fetch current settings from server
        updateUserSettingsIfModified(context)

        val userPreferenceData = getUserPreferenceDataFromLocalDB()
        userPreferenceData.pageGroups.remove(oldId)
        pageGroup.pageEntityList.clear()
        userPreferenceData.pageGroups.put(pageGroup.name,pageGroup)
        uploadUserPreferenceData(context,userPreferenceData)
        mDatabase.pageGroupDao.delete(oldId)
        mDatabase.pageGroupDao.add(pageGroup)
        return true
    }



    override fun checkIfOnFavList(mPage: Page): Boolean {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        Log.d(TAG, "checkIfOnFavList: ${mPage.name}")

        val userPreferenceDataList = mDatabase.userPreferenceDataDao.findAll()
        if (userPreferenceDataList.size > 0) {
            return userPreferenceDataList.get(0)!!.favouritePageIds.contains(mPage.id)
        }
        return false
    }

    override fun findPageGroupByName(pageGroupName: String): PageGroup {
        return mDatabase.pageGroupDao.findById(pageGroupName)
    }

    override fun checkIfLoggedIn(): Boolean {
        return mUserSettingsDataService.getLogInStatus()
    }

    override fun signOutUser() {
        return mUserSettingsDataService.signOutUser()
    }

    override fun getLogInIntent(): Intent? {
        return mUserSettingsDataService.getLogInIntent()
    }

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


    override fun getUserPreferenceLiveData(): LiveData<UserPreferenceData?> {
        return mDatabase.userPreferenceDataDao.findUserPreferenceData()
    }

    override fun getPageGroupListLive(): LiveData<List<PageGroup>> {
        return mDatabase.pageGroupDao.findAllLive()
    }
    companion object {
        val TAG = "UserSettingsRepository"
    }
}
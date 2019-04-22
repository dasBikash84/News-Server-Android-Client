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

import android.os.SystemClock
import android.util.Log
import com.dasbikash.news_server_data.data_sources.data_services.user_details_data_service.UserIpDataService
import com.dasbikash.news_server_data.models.DefaultAppSettings
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.dasbikash.news_server_data.models.UserSettingsUpdateDetails
import com.dasbikash.news_server_data.exceptions.AppSettingsNotFound
import com.dasbikash.news_server_data.exceptions.UserSettingsNotFoundException
import com.dasbikash.news_server_data.exceptions.UserSettingsUploadException
import com.dasbikash.news_server_data.models.NetworkResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


internal object FirebaseRealtimeDBUtils {

    private val MAX_SETTINGS_UPLOAD_RETRY = 3
    private val MAX_SETTINGS_UPLOAD_RETRY_SLEEP_PERIOD = 5000L
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
    private const val FAV_PAGE_ID_LIST_NODE = "favouritePageIds"
    private const val INACTIVE_NEWSPAPER_ID_LIST_NODE = "inActiveNewsPaperIds"
    private const val INACTIVE_PAGE_ID_LIST_NODE = "inActivePageIds"
    private const val PAGE_GROUP_LIST_NODE = "pageGroups"
    private const val UPDATE_LOG_NODE = "updateLog"

    val mFBDataBase : FirebaseDatabase

    init {
        mFBDataBase = FirebaseDatabase.getInstance()
        //mFBDataBase.setPersistenceEnabled(false)
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

    fun getServerAppSettingsUpdateTime(): NetworkResponse<Long> {

        var data = 0L
        val lock = Object()

        var appSettingsNotFound:AppSettingsNotFound? = null

        mSettingsUpdateTimeReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                appSettingsNotFound = AppSettingsNotFound(databaseError.message)
                synchronized(lock){lock.notify()}
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    try {
                        data = dataSnapshot.children.last().value as Long
                        synchronized(lock){lock.notify()}
                    } catch (e: Exception) {
                        appSettingsNotFound = AppSettingsNotFound(e)
                    }
                }
            }
        })

        synchronized(lock){lock.wait()}
        if (appSettingsNotFound !=null){
            return NetworkResponse.getFailureResponse(dummyPayload = data,exception = appSettingsNotFound as AppSettingsNotFound)
        }

        return NetworkResponse.getSuccessResponse(data)//data
    }

    fun getServerAppSettingsData(): NetworkResponse<DefaultAppSettings> {
        var data = DefaultAppSettings()
        val lock = Object()

        var appSettingsNotFound:AppSettingsNotFound? = null

        mAppSettingsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                appSettingsNotFound = AppSettingsNotFound(error.message)
                synchronized(lock){lock.notify()}
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        data = dataSnapshot.getValue(DefaultAppSettings::class.java)!!
                        synchronized(lock){lock.notify()}
                    } catch (ex: Exception) {
                        appSettingsNotFound = AppSettingsNotFound(ex)
                    }
                }
            }
        })

        synchronized(lock){lock.wait()}
//        if (appSettingsNotFound !=null) {throw appSettingsNotFound as AppSettingsNotFound}
        if (appSettingsNotFound !=null) {
            return NetworkResponse.getFailureResponse(data,appSettingsNotFound as AppSettingsNotFound)
        }

        return NetworkResponse.getSuccessResponse(data)
    }

    fun getUserPreferenceData(): UserPreferenceData {
        var data: UserPreferenceData? = null
        val lock = Object()

        var userSettingsException:UserSettingsNotFoundException?=null

        getUserSettingsRef(FirebaseAuth.getInstance().currentUser!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                userSettingsException =  UserSettingsNotFoundException(error.message)
                synchronized(lock){lock.notify()}
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        data = dataSnapshot.getValue(UserPreferenceData::class.java)!!
                        synchronized(lock){lock.notify()}
                    } catch (ex: Exception) {
                        userSettingsException =  UserSettingsNotFoundException(ex)
                    }
                }
            }
        })
        synchronized(lock){lock.wait()}
        if (userSettingsException !=null) {throw userSettingsException as UserSettingsNotFoundException}

        return data as UserPreferenceData
    }

    private fun getUserSettingsRef(user: FirebaseUser):DatabaseReference{
        return mUserSettingsRootReference.child(user.uid)
    }

    private fun getFavPageIdListRef(user: FirebaseUser):DatabaseReference{
        return getUserSettingsRef(user).child(FAV_PAGE_ID_LIST_NODE)
    }

    private fun getInactiveNewspaperIdListRef(user: FirebaseUser):DatabaseReference{
        return getUserSettingsRef(user).child(INACTIVE_NEWSPAPER_ID_LIST_NODE)
    }

    private fun getInactivepageIdListRef(user: FirebaseUser):DatabaseReference{
        return getUserSettingsRef(user).child(INACTIVE_PAGE_ID_LIST_NODE)
    }

    private fun getPageGroupListRef(user: FirebaseUser):DatabaseReference{
        return getUserSettingsRef(user).child(PAGE_GROUP_LIST_NODE)
    }

    private fun getUpdateLogRef(user: FirebaseUser):DatabaseReference{
        return getUserSettingsRef(user).child(UPDATE_LOG_NODE)
    }

    fun uploadUserSettings(userPreferenceData: UserPreferenceData,user:FirebaseUser): Boolean {

        if (user.isAnonymous) return false

        val lock = Object()

        var remainingTries = MAX_SETTINGS_UPLOAD_RETRY

        do {

            try {
                var userSettingsUploadException:UserSettingsUploadException? = null
                getFavPageIdListRef(user)
                        .setValue(userPreferenceData.favouritePageIds)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                synchronized(lock) { lock.notify() }
                            } else {
                                userSettingsUploadException = UserSettingsUploadException()
                                synchronized(lock) { lock.notify() }
                            }
                        }
                synchronized(lock) { lock.wait() }
                userSettingsUploadException?.let { throw it }

                getInactiveNewspaperIdListRef(user)
                        .setValue(userPreferenceData.inActiveNewsPaperIds)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                synchronized(lock) { lock.notify() }
                            } else {
                                userSettingsUploadException = UserSettingsUploadException()
                                synchronized(lock) { lock.notify() }
                            }
                        }
                synchronized(lock) { lock.wait() }
                userSettingsUploadException?.let { throw it }

                getInactivepageIdListRef(user)
                        .setValue(userPreferenceData.inActivePageIds)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                synchronized(lock) { lock.notify() }
                            } else {
                                userSettingsUploadException = UserSettingsUploadException()
                                synchronized(lock) { lock.notify() }
                            }
                        }
                synchronized(lock) { lock.wait() }
                userSettingsUploadException?.let { throw it }

                getPageGroupListRef(user)
                        .setValue(userPreferenceData.pageGroups)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                synchronized(lock) { lock.notify() }
                            } else {
                                userSettingsUploadException = UserSettingsUploadException()
                                synchronized(lock) { lock.notify() }
                            }
                        }
                synchronized(lock) { lock.wait() }
                userSettingsUploadException?.let { throw it }

                var ipAddress: String
                try {
                    ipAddress = UserIpDataService.getIpAddress()
                } catch (ex: Throwable) {
                    ipAddress = UserSettingsUpdateDetails.NULL_IP
                }

                getUpdateLogRef(user)
                        .push()
                        .setValue(UserSettingsUpdateDetails(userIp = ipAddress))
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                synchronized(lock) { lock.notify() }
                            } else {
                                userSettingsUploadException = UserSettingsUploadException()
                                synchronized(lock) { lock.notify() }
                            }
                        }
                synchronized(lock) { lock.wait() }
                userSettingsUploadException?.let { throw it }
                break
            }catch (ex:UserSettingsUploadException){
                Log.d(TAG,"${remainingTries}")
                remainingTries--
                if (remainingTries == 0){
                    return false
                }
                SystemClock.sleep(MAX_SETTINGS_UPLOAD_RETRY_SLEEP_PERIOD)
                continue
            }
        }while (remainingTries>0)

        return true
    }

}

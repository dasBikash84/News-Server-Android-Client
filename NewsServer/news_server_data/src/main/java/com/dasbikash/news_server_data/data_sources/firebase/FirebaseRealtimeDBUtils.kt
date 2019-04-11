/*
 * Copyright 2019 www.dasbikash.org. All rights reserved.
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

import android.util.Log
import com.dasbikash.news_server_data.display_models.entity.DefaultAppSettings
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.exceptions.OnMainThreadException
import com.dasbikash.news_server_data.exceptions.RemoteDbException
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.google.firebase.database.*
import java.util.concurrent.atomic.AtomicBoolean


internal object FirebaseRealtimeDBUtils {

    private val TAG = "FirebaseRealtimeDBUtils"

    val APP_SETTINGS_NODE = "app_settings"
    val COUNTRIES_NODE = "countries"
    val LANGUAGES_NODE = "languages"
    val NEWSPAPERS_NODE = "newspapers"
    val PAGES_NODE = "pages"
    val PAGE_GROUPS_NODE = "page_groups"
    val SETTINGS_UPDATE_TIME_NODE = "update_time"


    val mRootReference = FirebaseDatabase.getInstance().reference

    val mAppSettingsReference: DatabaseReference = mRootReference.child(APP_SETTINGS_NODE)
    val mCountriesSettingsReference: DatabaseReference = mAppSettingsReference.child(COUNTRIES_NODE)
    val mLanguagesSettingsReference: DatabaseReference = mAppSettingsReference.child(LANGUAGES_NODE)
    val mNewspaperSettingsReference: DatabaseReference = mAppSettingsReference.child(NEWSPAPERS_NODE)
    val mPagesSettingsReference: DatabaseReference = mAppSettingsReference.child(PAGES_NODE)
    val mPageGroupsSettingsReference: DatabaseReference = mAppSettingsReference.child(PAGE_GROUPS_NODE)
    val mSettingsUpdateTimeReference: DatabaseReference = mAppSettingsReference.child(SETTINGS_UPDATE_TIME_NODE)

     @Throws(OnMainThreadException::class,NoInternertConnectionException::class)
    private fun checkRequestValidity() {
        ExceptionUtils.thowExceptionIfOnMainThred()
        ExceptionUtils.thowExceptionIfNoInternetConnection()
    }
    @JvmStatic
    fun getServerAppSettingsUpdateTime() : Long {

        val REMOTE_DB_ERROR_FLAG = -1L

        checkRequestValidity()

        var data = 0L
        val waitFlag = AtomicBoolean(true)


        mSettingsUpdateTimeReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                data = REMOTE_DB_ERROR_FLAG
                waitFlag.set(false)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()){
                    try {
                        data = dataSnapshot.children.last().value as Long
                    }catch (e:Exception){
                        data = REMOTE_DB_ERROR_FLAG
                    }
                }
                waitFlag.set(false)
            }
        })

        while (waitFlag.get()) {}
        if (data == REMOTE_DB_ERROR_FLAG){
            throw RemoteDbException();
        }
        return data
    }


    @JvmStatic
    //@Throws(NoInternertConnectionException::class,OnMainThreadException::class)
    fun getServerAppSettingsData(): DefaultAppSettings {

        checkRequestValidity()

        var data:DefaultAppSettings? = null
        val waitFlag = AtomicBoolean(true)

        Log.d(TAG,"getServerAppSettingsData:")

        mAppSettingsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                data = null
                Log.d(TAG,"onCancelled Error:"+error.details)
                waitFlag.set(false)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        Log.d(TAG,"Data:"+dataSnapshot.value)
                        data = dataSnapshot.getValue(DefaultAppSettings::class.java)!!
                    }catch (ex:Exception){
                        Log.d(TAG,"onDataChange Error:"+ex.message)
                        ex.printStackTrace()
                    }
                    Log.d(TAG,"onDataChange:")
                    waitFlag.set(false)
                }
            }
        })

        while (waitFlag.get()) {}
        if (data == null){
            throw RemoteDbException();
        }
        return data!!

    }

}

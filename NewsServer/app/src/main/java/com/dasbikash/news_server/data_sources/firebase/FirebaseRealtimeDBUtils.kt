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

package com.dasbikash.news_server.data_sources.firebase

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import com.dasbikash.news_server.display_models.entity.*
import com.dasbikash.news_server.exceptions.DataNotFoundException
import com.dasbikash.news_server.exceptions.NoInternertConnectionException
import com.dasbikash.news_server.exceptions.OnMainThreadException
import com.dasbikash.news_server.exceptions.RemoteDbException
import com.dasbikash.news_server.utils.NetConnectivityUtility
import com.google.firebase.database.*
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong


object FirebaseRealtimeDBUtils {

    private val TAG = "FirebaseRealtimeDBUtils"

    private val APP_SETTINGS_NODE = "app_settings"
    /*private val COUNTRIES_NODE = "countries"
    private val LANGUAGES_NODE = "languages"
    private val NEWSPAPERS_NODE = "newspapers"
    private val PAGES_NODE = "pages"
    private val PAGE_GROUPS_NODE = "page_groups"*/
    private val SETTINGS_UPDATE_TIME_NODE = "update_time"


    private val mFirebaseDatabase = FirebaseDatabase.getInstance()
    val mRootReference = FirebaseDatabase.getInstance().reference

    private val mAppSettingsReference: DatabaseReference = mRootReference.child(APP_SETTINGS_NODE)
    /*private val mCountriesSettingsReference: DatabaseReference = mAppSettingsReference.child(COUNTRIES_NODE)
    private val mLanguagesSettingsReference: DatabaseReference = mAppSettingsReference.child(LANGUAGES_NODE)
    private val mNewspaperSettingsReference: DatabaseReference = mAppSettingsReference.child(NEWSPAPERS_NODE)
    private val mPagesSettingsReference: DatabaseReference = mAppSettingsReference.child(PAGES_NODE)
    private val mPageGroupsSettingsReference: DatabaseReference = mAppSettingsReference.child(PAGE_GROUPS_NODE)*/
    private val mSettingsUpdateTimeReference: DatabaseReference = mAppSettingsReference.child(SETTINGS_UPDATE_TIME_NODE)

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

    private fun checkRequestValidity() {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            throw OnMainThreadException();
        }
        if (!NetConnectivityUtility.isConnected) {
            throw NoInternertConnectionException();
        }
    }

    fun getServerAppSettingsData(): DefaultAppSettings {

        checkRequestValidity()

        var data:DefaultAppSettings? = null
        val waitFlag = AtomicBoolean(true)

        mAppSettingsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                data = null
                waitFlag.set(false)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        data = dataSnapshot.getValue(DefaultAppSettings::class.java)!!
                    }catch (ex:Exception){
                        ex.printStackTrace()
                    }
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

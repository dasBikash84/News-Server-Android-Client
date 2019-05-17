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

package com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase

import com.dasbikash.news_server_data.exceptions.AppSettingsNotFound
import com.dasbikash.news_server_data.exceptions.SettingsServerException
import com.dasbikash.news_server_data.models.DefaultAppSettings
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


internal object RealtimeDBAppSettingsUtils{

    private const val SETTINGS_UPDATE_TIME_NODE = "update_time"
    val mSettingsUpdateTimeReference: DatabaseReference = RealtimeDBUtils.mAppSettingsReference.child(SETTINGS_UPDATE_TIME_NODE)
    const val WAITING_MS_FOR_NET_RESPONSE = 30000L

    fun getServerAppSettingsUpdateTime(): Long{

        var data = 0L
        val lock = Object()

        var appSettingsNotFound: SettingsServerException? = null


        mSettingsUpdateTimeReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                appSettingsNotFound = SettingsServerException(databaseError.message)
                synchronized(lock) { lock.notify() }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    try {
                        data = dataSnapshot.children.last().value as Long
                        synchronized(lock) { lock.notify() }
                    } catch (e: Exception) {
                        appSettingsNotFound = AppSettingsNotFound(e)
                    }
                }
            }
        })

        synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        if (appSettingsNotFound != null) {
            throw appSettingsNotFound as SettingsServerException
        }
        if (data == 0L){
            throw AppSettingsNotFound()
        }

        return data
    }

    fun getServerAppSettingsData(): DefaultAppSettings {
        var data : DefaultAppSettings?=null
        val lock = Object()

        var appSettingsNotFound: SettingsServerException? = null

        RealtimeDBUtils.mAppSettingsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                appSettingsNotFound = SettingsServerException(error.message)
                synchronized(lock) { lock.notify() }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        data = dataSnapshot.getValue(DefaultAppSettings::class.java)!!
                        synchronized(lock) { lock.notify() }
                    } catch (ex: Exception) {
                        appSettingsNotFound = AppSettingsNotFound(ex)
                    }
                }
            }
        })

        synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        if (appSettingsNotFound != null) {
            throw appSettingsNotFound as SettingsServerException
        }
        if (data==null){
            throw AppSettingsNotFound()
        }

        return data!!
    }

    fun ping(): Boolean {
        try {
            getServerAppSettingsUpdateTime()
            return true
        }catch (ex:Exception){
            return false
        }
    }
}
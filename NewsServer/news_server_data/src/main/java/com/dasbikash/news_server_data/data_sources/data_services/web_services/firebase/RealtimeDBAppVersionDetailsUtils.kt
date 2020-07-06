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

import com.dasbikash.news_server_data.data_sources.data_services.AppVersionDetails
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

internal object RealtimeDBAppVersionDetailsUtils {

    private const val MAX_WAITING_MS_FOR_NET_RESPONSE = 30000L
    private const val QUERY_FIELD_NAME = "versionCode"

    fun getLatestVersionDetails(): AppVersionDetails? {

        var appVersionDetails:AppVersionDetails? = null
        val lock = Object()

        RealtimeDBUtils.mAppVersionHistoryNode
                .orderByChild(QUERY_FIELD_NAME)
                .limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.first()?.let {
                            appVersionDetails = it.getValue(AppVersionDetails::class.java)
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })
        try {
            synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:Throwable){}

        return appVersionDetails
    }
}
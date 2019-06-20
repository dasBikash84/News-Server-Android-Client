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

import androidx.annotation.Keep
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random


internal object RealTimeDbPageRequestServerUtils {

    const val REQUEST_SERVING_CHUNK_SIZE_LIMIT = 25
    const val FETCH_TIMES = 2

    fun getPageDownLoadRequests(): Map<String, PageDownLoadRequest> {

        val lock = Object()
        val pageDownLoadRequestMap = mutableMapOf<String, PageDownLoadRequest>()

        val query:Query

        if (Random(System.currentTimeMillis()).nextBoolean()) {
            LoggerUtils.debugLog("limitToFirst", this::class.java)
            query = RealtimeDBUtils.mPageDownLoadRequestReference.limitToFirst(REQUEST_SERVING_CHUNK_SIZE_LIMIT * FETCH_TIMES)
        } else {
            LoggerUtils.debugLog("limitToLast", this::class.java)
            query = RealtimeDBUtils.mPageDownLoadRequestReference.limitToLast(REQUEST_SERVING_CHUNK_SIZE_LIMIT * FETCH_TIMES)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    LoggerUtils.debugLog("onCancelled. Error msg: ${error.message}", this::class.java)
                    synchronized(lock) { lock.notify() }
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        LoggerUtils.debugLog("onDataChange", this::class.java)
                        dataSnapshot.children.asSequence()
                                .forEach {
                                    pageDownLoadRequestMap.put(it.key!!, it.getValue(PageDownLoadRequest::class.java)!!)
                                }
                    }
                    synchronized(lock) { lock.notify() }
                }
            })

        LoggerUtils.debugLog("getPageDownLoadRequests before wait", this::class.java)
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        LoggerUtils.debugLog("getPageDownLoadRequests before return", this::class.java)
        return pageDownLoadRequestMap.toMap()
    }

    fun checkIfPageDownLoadRequestExists(pageDownLoadRequestId: String): Boolean {

        val lock = Object()
        var data: PageDownLoadRequest? = null

        RealtimeDBUtils.mPageDownLoadRequestReference
                .child(pageDownLoadRequestId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        LoggerUtils.debugLog("checkIfPageDownLoadRequestExists onCancelled. Error msg: ${error.message}", this::class.java)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            LoggerUtils.debugLog("checkIfPageDownLoadRequestExists onDataChange", this::class.java)
                            data = dataSnapshot.getValue(PageDownLoadRequest::class.java)!!
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })

        LoggerUtils.debugLog("checkIfPageDownLoadRequestExists before wait", this::class.java)
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        LoggerUtils.debugLog("checkIfPageDownLoadRequestExists before true return", this::class.java)
        data?.let { return true }

        LoggerUtils.debugLog("checkIfPageDownLoadRequestExists before false return", this::class.java)
        return false
    }

}

@Keep
internal data class PageDownLoadRequest(
        var newsPaperId: String? = null,
        var link: String? = null,
        var requestId: String? = null
)
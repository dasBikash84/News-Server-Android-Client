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
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random


internal object RealTimeDbPageRequestServerUtils {

    const val WAITING_MS_FOR_NET_RESPONSE = 30000L

    fun getPageDownLoadRequests(fetchChunkSize:Int): Map<String, PageDownLoadRequest> {

        val lock = Object()
        val pageDownLoadRequestMap = mutableMapOf<String, PageDownLoadRequest>()

        val query:Query

        if (Random(System.currentTimeMillis()).nextBoolean()) {
            LoggerUtils.debugLog("limitToFirst", this::class.java)
            query = RealtimeDBUtils.mPageDownLoadRequestReference.limitToFirst(fetchChunkSize)
        } else {
            LoggerUtils.debugLog("limitToLast", this::class.java)
            query = RealtimeDBUtils.mPageDownLoadRequestReference.limitToLast(fetchChunkSize)
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
        synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }

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
        synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }

        LoggerUtils.debugLog("checkIfPageDownLoadRequestExists before true return", this::class.java)
        data?.let { return true }

        LoggerUtils.debugLog("checkIfPageDownLoadRequestExists before false return", this::class.java)
        return false
    }

    fun getPageDownLoadRequestSettings():PageDownLoadRequestSettings{

        val lock = Object()
        var data: PageDownLoadRequestSettings = PageDownLoadRequestSettings()

        RealtimeDBUtils.mPageDownLoadRequestSettingsReference
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        LoggerUtils.debugLog("getPageDownLoadRequestSettings onCancelled. Error msg: ${error.message}", this::class.java)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            LoggerUtils.debugLog("getPageDownLoadRequestSettings onDataChange", this::class.java)
                            data = dataSnapshot.getValue(PageDownLoadRequestSettings::class.java)!!
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })

        LoggerUtils.debugLog("getPageDownLoadRequestSettings before wait", this::class.java)
        synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }

        LoggerUtils.debugLog("getPageDownLoadRequestSettings before return", this::class.java)
        return data
    }

}

@Keep
internal data class PageDownLoadRequest(
        var newsPaperId: String? = null,
        var link: String? = null,
        var requestId: String? = null
)

@Keep
internal data class PageDownLoadRequestSettings(
        var requestServingChunkSize:Int = DEFAULT_REQUEST_SERVING_CHUNK_SIZE,
        var fetchMultiplier:Double = DEFAULT_FETCH_MULTIPLIER,
        var dailyMaxServingCountForNp: Map<String, Int> = mapOf()
){
    companion object{
        const val DEFAULT_REQUEST_SERVING_CHUNK_SIZE = 10
        const val DEFAULT_FETCH_MULTIPLIER = 1.5
        const val DEFAULT_DAILY_MAX_SERVING_COUNT_FOR_NP = 100
    }
    fun getDailyMaxServingCountForNp(npId:String):Int{
        if (dailyMaxServingCountForNp.keys.contains(npId)){
            return dailyMaxServingCountForNp.get(npId)!!
        }
        return DEFAULT_DAILY_MAX_SERVING_COUNT_FOR_NP
    }
    fun getFetchChunkSize():Int =
            (requestServingChunkSize*fetchMultiplier).toInt()
}
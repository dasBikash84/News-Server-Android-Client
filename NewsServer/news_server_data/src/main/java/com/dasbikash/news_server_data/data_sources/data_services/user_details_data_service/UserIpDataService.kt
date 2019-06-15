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

package com.dasbikash.news_server_data.data_sources.data_services.user_details_data_service

import com.dasbikash.news_server_data.models.IpAddress
import com.dasbikash.news_server_data.utills.LoggerUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal object UserIpDataService {

    private const val MAX_WAITING_MS_FOR_NET_RESPONSE = 30000L
    const val NULL_IP = "255.255.255.255"

    private val userIpWebService = UserIpWebService.RETROFIT.create(UserIpWebService::class.java)

    private var ipAddress: String? = null

    fun getIpAddress(): String {
        LoggerUtils.debugLog("getIpAddress", this::class.java)

        val lock = Object()
        val call = userIpWebService.getIpAddress()

        call.enqueue(object : Callback<IpAddress> {
            override fun onFailure(call: Call<IpAddress>, t: Throwable) {
                LoggerUtils.debugLog("onFailure", this::class.java)
                synchronized(lock) { lock.notify() }
            }

            override fun onResponse(call: Call<IpAddress>, response: Response<IpAddress>) {
                LoggerUtils.debugLog("onResponse", this::class.java)
                if (response.isSuccessful) {
                    LoggerUtils.debugLog("isSuccessful", this::class.java)
                    ipAddress = response.body()!!.ip!!
                }

                synchronized(lock) { lock.notify() }
            }
        })

        synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }

        ipAddress?.let {
            LoggerUtils.debugLog("return ipAddress!!", this::class.java)
            return ipAddress!!
        }
        LoggerUtils.debugLog("return NULL_IP", this::class.java)

        return NULL_IP
    }
}
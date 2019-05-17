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

import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal object UserIpDataService {

    private const val MAX_WAITING_MS_FOR_NET_RESPONSE = 30000L

    private val TAG = "UserIpDataService"

    private val userIpWebService
            = UserIpWebService.RETROFIT.create(UserIpWebService::class.java)

    private var ipAddress:String?=null

    fun getIpAddress(): String {

        ipAddress?.let { return ipAddress!! }

        val lock = Object()
        val call = userIpWebService.getIpAddress()

        var result:String? = null

        call.enqueue(object : Callback<IpAddress>{
            override fun onFailure(call: Call<IpAddress>, t: Throwable) {
                synchronized(lock){lock.notify()}
            }
            override fun onResponse(call: Call<IpAddress>, response: Response<IpAddress>) {
                if (response.isSuccessful){
                    ipAddress = response.body()!!.ip!!
                    result = ipAddress
                }

                synchronized(lock){lock.notify()}
            }
        })

        synchronized(lock){lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE)}

        if (result.isNullOrEmpty()){
            throw DataNotFoundException()
        }
        return result!!
    }
}

class IpAddress{
    var ip:String?=null
}
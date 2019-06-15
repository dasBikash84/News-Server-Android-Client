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

package com.dasbikash.news_server_data.page_request_server

import android.content.Context
import android.os.Build
import androidx.work.*
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import java.util.concurrent.TimeUnit

object PageRequestServerInitiator {

    const private val WORK_INITIATOR_KEY = "com.dasbikash.news_server_data.page_request_server.WORK_INITIATOR_KEY"
    const private val INIT_FLAG = "com.dasbikash.news_server_data.page_request_server.INIT_FLAG"

    fun initWork(context: Context){
        LoggerUtils.debugLog("initWork",this::class.java)
        if (!checkIfWorkInitiated(context)){
            doInit(context)
            saveInitFlag(context)
        }
    }

    private fun saveInitFlag(context: Context) {
        LoggerUtils.debugLog("saveInitFlag",this::class.java)
        SharedPreferenceUtils.saveData(context, INIT_FLAG, WORK_INITIATOR_KEY)
    }

    private fun doInit(context: Context) {
        LoggerUtils.debugLog("doInit",this::class.java)

        var constraintBuilder = Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .setRequiredNetworkType(NetworkType.CONNECTED)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            constraintBuilder = constraintBuilder.setRequiresDeviceIdle(true)
//        }

        val constraints = constraintBuilder.build()

        val workRequest =
                PeriodicWorkRequestBuilder<PageRequestServerWorker>(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                        .setConstraints(constraints)
                        .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun checkIfWorkInitiated(context: Context):Boolean{
        LoggerUtils.debugLog("checkIfWorkInitiated",this::class.java)
        return SharedPreferenceUtils
                .getData(context,SharedPreferenceUtils.DefaultValues.DEFAULT_STRING,WORK_INITIATOR_KEY) == INIT_FLAG
    }
}
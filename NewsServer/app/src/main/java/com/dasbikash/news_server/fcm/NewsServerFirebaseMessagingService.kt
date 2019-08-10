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

package com.dasbikash.news_server.fcm

import android.util.Log
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class NewsServerFirebaseMessagingService: FirebaseMessagingService() {

    private val TAG = "NS>>FCM"

    override fun onNewToken(token: String?) {
        debugLog("Refreshed token: $token")
    }

    fun generateNewToken(){

        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { LoggerUtils.printStackTrace(it)}
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result?.token

                    Log.d(TAG, token)
                })

    }
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        debugLog("From: ${remoteMessage?.from}")
    }

}
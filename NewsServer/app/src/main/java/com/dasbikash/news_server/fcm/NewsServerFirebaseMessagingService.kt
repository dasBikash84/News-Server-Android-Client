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

import android.content.Context
import android.util.Log
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class NewsServerFirebaseMessagingService: FirebaseMessagingService() {

    override fun onNewToken(token: String?) {
        debugLog("Refreshed token: $token")
    }

    /*fun generateNewToken(){

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

    }*/
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        debugLog("From: ${remoteMessage?.from}")
    }

    companion object{
        private const val NOTIFICATION_TOPIC_NAME = "article_broadcast"
        private const val SP_KEY = "com.dasbikash.news_server.fcm.NewsServerFirebaseMessagingService.BRODCAST_TOPIC_FCM_KEY"
        fun init(context: Context){
            ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
            val curValue = getSpFlag(context)
            if (!curValue){
                val lock = Object()
                FirebaseMessaging.getInstance().subscribeToTopic(NOTIFICATION_TOPIC_NAME)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                setSpFlag(context)
                                debugLog("subscribeToTopic $NOTIFICATION_TOPIC_NAME")
                            }
                            synchronized(lock){lock.notify()}
                        }
                try {
                    synchronized(lock){lock.wait(30000L)}
                }catch (ex:InterruptedException){}
            }else{
                debugLog("Already subscribeToTopic $NOTIFICATION_TOPIC_NAME")
            }
        }
        private fun setSpFlag(context: Context){
            SharedPreferenceUtils.saveData(context,true, SP_KEY)
        }
        private fun getSpFlag(context: Context):Boolean{
            return SharedPreferenceUtils.getData(context,SharedPreferenceUtils.DefaultValues.DEFAULT_BOOLEAN, SP_KEY) as Boolean
        }
    }

}
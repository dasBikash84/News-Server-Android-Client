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
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class NewsServerFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
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
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        debugLog("From: ${remoteMessage.from}")
    }

    companion object {
        private const val BROADCAST_NOTIFICATION_TOPIC_NAME = "article_broadcast"

        fun init(context: Context) {
            ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
            subscribeToTopic(BROADCAST_NOTIFICATION_TOPIC_NAME)
            subscribeToUserTopics(context)
        }

        fun subscribeToTopic(topicName: String): Task<Void> {
            debugLog("subscribeToTopic: $topicName")
            return FirebaseMessaging.getInstance().subscribeToTopic(topicName)
        }

        fun unSubscribeFromTopic(topicName: String): Task<Void> {
            debugLog("unSubscribeFromTopic: $topicName")
            return FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
        }

        fun subscribeToUserTopics(context: Context) {
            ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
            RepositoryFactory.getUserSettingsRepository(context).getFavouritePageEntries()
                    .filter { it.subscribed }.asSequence().forEach {
                        subscribeToTopic(it.pageId)
                    }
        }

        fun unSubscribeFromUserTopics(context: Context) {
            ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
            RepositoryFactory.getUserSettingsRepository(context).getFavouritePageEntries()
                    .filter { it.subscribed }.asSequence().forEach {
                        unSubscribeFromTopic(it.pageId)
                    }
        }
    }

}
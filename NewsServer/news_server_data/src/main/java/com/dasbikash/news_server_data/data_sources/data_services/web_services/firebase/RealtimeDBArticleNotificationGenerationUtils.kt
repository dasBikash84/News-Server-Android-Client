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

import com.dasbikash.news_server_data.data_sources.data_services.article_notification_gen_service.FcmNotificationGenerationRequest
import com.dasbikash.news_server_data.models.room_entity.Article

internal object RealtimeDBArticleNotificationGenerationUtils {

    private const val MAX_WAITING_MS_FOR_WRITE_COMPLETE = 30000L

    fun addNotificationGenerationRequestForArticle(article: Article,authToken:String):Boolean{
        val fcmNotificationGenerationRequest =
                FcmNotificationGenerationRequest(articleId = article.id,authToken = authToken)

        val task = RealtimeDBUtils.mFcmNotificationGenReqRef.push().setValue(fcmNotificationGenerationRequest)
        val startTime = System.currentTimeMillis()
        do {
            Thread.sleep(100)
        }while (!task.isComplete && (System.currentTimeMillis() - startTime)< MAX_WAITING_MS_FOR_WRITE_COMPLETE)
        return task.isSuccessful
    }
}
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

package com.dasbikash.news_server_data.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.database.ServerValue
@Keep
enum class ParserMode {
    OFF,RUNNING, GET_SYNCED,PARSE_THROUGH_CLIENT
}
@Keep
enum class OffOnStatus {
    ON,OFF
}
@Keep
enum class ArticleUploadTarget {
    REAL_TIME_DB,FIRE_STORE_DB,MONGO_REST_SERVICE
}
@Keep
enum class TwoStateStatus {
    ON,OFF
}

@Keep
class NewsPaperParserModeChangeRequest private constructor(
        val authToken: String,
        val targetNewspaperId: String,
        val parserMode: ParserMode
){
    companion object{
        fun getInstanceForRunningMode(
                authToken: String,targetNewspaperId: String) =
                NewsPaperParserModeChangeRequest(authToken,targetNewspaperId,ParserMode.RUNNING)

        fun getInstanceForGetSyncedMode(
                authToken: String,targetNewspaperId: String) =
                NewsPaperParserModeChangeRequest(authToken,targetNewspaperId,ParserMode.GET_SYNCED)

        fun getInstanceForParseThroughClientMode(
                authToken: String,targetNewspaperId: String) =
                NewsPaperParserModeChangeRequest(authToken,targetNewspaperId,ParserMode.PARSE_THROUGH_CLIENT)

        fun getInstanceForOFFMode(
                authToken: String,targetNewspaperId: String) =
                NewsPaperParserModeChangeRequest(authToken,targetNewspaperId,ParserMode.OFF)
    }
}
@Keep
class NewsPaperStatusChangeRequest private constructor(
        val authToken: String,
        val targetNewspaperId: String,
        val targetStatus: OffOnStatus
){
    companion object{
        fun getInstanceForOnMode(authToken: String,targetNewspaperId: String) =
                NewsPaperStatusChangeRequest(authToken,targetNewspaperId,OffOnStatus.ON)

        fun getInstanceForOffMode(authToken: String,targetNewspaperId: String) =
                NewsPaperStatusChangeRequest(authToken,targetNewspaperId,OffOnStatus.OFF)
    }
}
@Keep
class ArticleUploaderStatusChangeRequest private constructor(
    val authToken:String,
    val articleUploadTarget:ArticleUploadTarget,
    val status:TwoStateStatus
){
    companion object{
        fun getInstanceForOnMode(authToken: String,articleUploadTarget:ArticleUploadTarget) =
                ArticleUploaderStatusChangeRequest(authToken,articleUploadTarget,TwoStateStatus.ON)

        fun getInstanceForOffMode(authToken: String,articleUploadTarget:ArticleUploadTarget) =
                ArticleUploaderStatusChangeRequest(authToken,articleUploadTarget,TwoStateStatus.OFF)
    }
}

@Keep
class TokenGenerationRequest(val timeStampMs:Long = System.currentTimeMillis())
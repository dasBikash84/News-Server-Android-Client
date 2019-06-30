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

import com.dasbikash.news_server_data.models.ArticleUploaderStatusChangeRequest
import com.dasbikash.news_server_data.models.NewsPaperParserModeChangeRequest
import com.dasbikash.news_server_data.models.NewsPaperStatusChangeRequest
import com.dasbikash.news_server_data.models.TokenGenerationRequest
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

internal object RealtimeDBAdminTaskUtils {

    private const val MAX_WAITING_MS_FOR_NET_RESPONSE = 30000L

    enum class AdminTaskNode(val nodeName:String){
        TOKEN_GENERATION_REQUEST_NODE("token_generation_request"),
        NP_STATUS_CHANGE_REQUEST_NODE ("np_status_change_request"),
        NP_PARSER_MODE_CHANGE_REQUEST_NODE ("np_parser_mode_change_request"),
        ARTICLE_UPLOADER_STATUS_CHANGE_REQUEST_NODE ("article_uploader_status_change_request")
    }

    private fun addAdminTaskRequest(payload:Any,adminTaskNode: AdminTaskNode):Boolean{
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        var returnValue = false
        val lock = Object()

        RealtimeDBUtils.mAdminTaskDataNode.child(adminTaskNode.nodeName).push()
                .setValue(payload)
                .addOnCompleteListener(object : OnCompleteListener<Void>{
                    override fun onComplete(task: Task<Void>) {
                        returnValue = task.isSuccessful
                        synchronized(lock){lock.notify()}
                    }
                })
        try {
            synchronized(lock){lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE)}
        }catch (ex:InterruptedException){}

        return returnValue
    }

    fun addTokenGenerationRequest() =
            addAdminTaskRequest(TokenGenerationRequest(), AdminTaskNode.TOKEN_GENERATION_REQUEST_NODE)

    fun addNewsPaperStatusChangeRequest(newsPaperStatusChangeRequest: NewsPaperStatusChangeRequest) =
            addAdminTaskRequest(newsPaperStatusChangeRequest,AdminTaskNode.NP_STATUS_CHANGE_REQUEST_NODE)

    fun addNewsPaperParserModeChangeRequest(newsPaperParserModeChangeRequest: NewsPaperParserModeChangeRequest) =
            addAdminTaskRequest(newsPaperParserModeChangeRequest,AdminTaskNode.NP_PARSER_MODE_CHANGE_REQUEST_NODE)

    fun addArticleUploaderStatusChangeRequest(articleUploaderStatusChangeRequest: ArticleUploaderStatusChangeRequest) =
            addAdminTaskRequest(articleUploaderStatusChangeRequest,AdminTaskNode.ARTICLE_UPLOADER_STATUS_CHANGE_REQUEST_NODE)
}
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

import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.*
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.CloudFireStorePageRequestServerUtils
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.PageDownLoadRequest
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.PageDownLoadRequestResponse
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.PageDownLoadRequestSettings
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.RealTimeDbPageRequestServerUtils
import com.dasbikash.news_server_data.utills.LoggerUtils

internal object PageRequestServerUtils {

    private lateinit var mPageDownLoadRequestSettings: PageDownLoadRequestSettings

    private fun getPageDownLoadRequestSettings():PageDownLoadRequestSettings{
        if (!::mPageDownLoadRequestSettings.isInitialized){
            mPageDownLoadRequestSettings = RealTimeDbPageRequestServerUtils.getPageDownLoadRequestSettings()
            LoggerUtils.debugLog(mPageDownLoadRequestSettings.toString(),this::class.java)
        }
        return mPageDownLoadRequestSettings
    }

    private fun getDailyMaxServingCountForNp(npId:String):Int =
            getPageDownLoadRequestSettings().getDailyMaxServingCountForNp(npId)

    private fun getTodaysServingCountForNp(npId:String):Int {
        TODO()
    }

    fun getPageDownLoadRequests(): Map<String, PageDownLoadRequest> =
            RealTimeDbPageRequestServerUtils.getPageDownLoadRequests(getPageDownLoadRequestSettings().getFetchChunkSize())

    fun getRequestSservingChunkSizeLimit() =
            getPageDownLoadRequestSettings().requestServingChunkSize

    fun checkIfPageDownLoadRequestExists(pageDownLoadRequestId: String): Boolean =
            RealTimeDbPageRequestServerUtils.checkIfPageDownLoadRequestExists(pageDownLoadRequestId)

    fun writeArticleData(pageDownLoadRequestResponseData: Pair<String, PageDownLoadRequestResponse>) =
            CloudFireStorePageRequestServerUtils.writeArticleData(pageDownLoadRequestResponseData)

    fun logPageDownLoadRequestWorkerTask(servedDocumentIdList: List<String>)=
            CloudFireStorePageRequestServerUtils.logPageDownLoadRequestWorkerTask(servedDocumentIdList)

    fun incrementTodaysServingCountForNp(npId:String){
        TODO()
    }

    fun shouldServeRequestForNp(npId:String):Boolean{
//        return getTodaysServingCountForNp(npId) <= getDailyMaxServingCountForNp(npId)
        return true
    }

}
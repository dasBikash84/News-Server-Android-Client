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
import android.os.SystemClock
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.CloudFireStorePageRequestServerUtils
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.PageDownLoadRequestResponse
import com.dasbikash.news_server_data.utills.HttpUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility

internal class PageRequestServerWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    companion object{
        val NP_IDS_RESTRICTED_ON_WIFI = listOf("NP_ID_2")
        val MIN_DELAY_MS_BETWEEN_REQUESTS = 5000L
    }

    override fun doWork(): Result {
        LoggerUtils.debugLog("doWork entry", this::class.java)

        var nextRequestTs = System.currentTimeMillis()
        val pageDownLoadRequestMap = CloudFireStorePageRequestServerUtils.getPageDownLoadRequests()
        val servedDocumentIdList = mutableListOf<String>()
        LoggerUtils.debugLog("Page DownLoad Request chunk size: ${pageDownLoadRequestMap.size}", this::class.java)

        pageDownLoadRequestMap.keys.asSequence().forEach {
            val pageDownLoadRequest = pageDownLoadRequestMap.get(it)!!

            if (NetConnectivityUtility.isConnected &&
                    (NetConnectivityUtility.isOnMobileDataNetwork ||
                    !NP_IDS_RESTRICTED_ON_WIFI.contains(pageDownLoadRequest.newsPaperId))) {

                LoggerUtils.debugLog(pageDownLoadRequest.toString(), this::class.java)

                if (pageDownLoadRequest.link != null) {
                    if (nextRequestTs> System.currentTimeMillis()){
                        SystemClock.sleep(nextRequestTs-System.currentTimeMillis())
                    }
                    nextRequestTs = System.currentTimeMillis() + MIN_DELAY_MS_BETWEEN_REQUESTS
                    val pageContent = HttpUtils.getWebPageContent(pageDownLoadRequest.link!!)
                    if (pageContent != null) {
                        CloudFireStorePageRequestServerUtils
                                .writeArticleData(Pair(it,PageDownLoadRequestResponse(
                                                            link = pageDownLoadRequest.link!!, pageContent = pageContent)))
                        LoggerUtils.debugLog("servedDocumentId: ${it}", this::class.java)
                        servedDocumentIdList.add(it)
                    }
                }
            }else{
                LoggerUtils.debugLog("Skipped restricted NP(${NP_IDS_RESTRICTED_ON_WIFI}) on Wify.", this::class.java)
            }
        }
        CloudFireStorePageRequestServerUtils.logPageDownLoadRequestWorkerTask(servedDocumentIdList)
        LoggerUtils.debugLog("doWork exiting", this::class.java)
        return Result.success()
    }
}
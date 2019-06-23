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
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.PageDownLoadRequestResponse
import com.dasbikash.news_server_data.utills.HttpUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility

internal class PageRequestServerWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    private val mContext: Context

    init {
        mContext = appContext
    }

    companion object {
        val NP_IDS_RESTRICTED_ON_WIFI = listOf("NP_ID_2")
        val MIN_DELAY_MS_BETWEEN_REQUESTS = 5000L
    }

    override fun doWork(): Result {

        LoggerUtils.debugLog("doWork entry", this::class.java)
        LoggerUtils.fileLog("doWork entry", this::class.java, mContext)

        var nextRequestTs = System.currentTimeMillis()

        val pageDownLoadRequestMap = PageRequestServerUtils.getPageDownLoadRequests()

        val servedDocumentIdList = mutableListOf<String>()
        LoggerUtils.debugLog("Featched page downLoad request chunk size: ${pageDownLoadRequestMap.size}", this::class.java)
        LoggerUtils.fileLog("Featched page downLoad request chunk size: ${pageDownLoadRequestMap.size}", this::class.java, mContext)

        LoggerUtils.fileLog("NetworkStatus: ${NetConnectivityUtility.refreshNetworkStatus(mContext).name}", this::class.java, mContext)

        pageDownLoadRequestMap.keys.shuffled().take(PageRequestServerUtils.getRequestSservingChunkSizeLimit()).asSequence()
                .forEach {
                    val pageDownLoadRequest = pageDownLoadRequestMap.get(it)!!

                    NetConnectivityUtility.refreshNetworkStatus(mContext)

                    if (NetConnectivityUtility.isConnected && PageRequestServerUtils.shouldServeRequestForNp(pageDownLoadRequest.newsPaperId!!) &&
                            (NetConnectivityUtility.isOnMobileDataNetwork ||
                                    !NP_IDS_RESTRICTED_ON_WIFI.contains(pageDownLoadRequest.newsPaperId!!))) {

                        LoggerUtils.debugLog(pageDownLoadRequest.toString(), this::class.java)
//                        LoggerUtils.fileLog(pageDownLoadRequest.toString(), this::class.java, mContext)

                        if (pageDownLoadRequest.link != null && PageRequestServerUtils.checkIfPageDownLoadRequestExists(it)) {
                            if (nextRequestTs > System.currentTimeMillis()) {
                                SystemClock.sleep(nextRequestTs - System.currentTimeMillis())
                            }
                            nextRequestTs = System.currentTimeMillis() + MIN_DELAY_MS_BETWEEN_REQUESTS
                            val pageContent = HttpUtils.getWebPageContent(pageDownLoadRequest.link!!)
                            if (pageContent != null && PageRequestServerUtils.checkIfPageDownLoadRequestExists(it)) {
                                if (PageRequestServerUtils.writeArticleData(
                                                Pair(pageDownLoadRequest.requestId!!, PageDownLoadRequestResponse(
                                                        link = pageDownLoadRequest.link!!, pageContent = pageContent)))) {
                                    LoggerUtils.debugLog("servedDocumentId: ${it}", this::class.java)
//                                    LoggerUtils.fileLog("servedDocumentId: ${it}", this::class.java, mContext)
                                    servedDocumentIdList.add(it)
//                                    PageRequestServerUtils.incrementTodaysServingCountForNp(pageDownLoadRequest.newsPaperId!!)
                                }
                            }
                        }
                    } else {
                        if (NetConnectivityUtility.isConnected) {
                            LoggerUtils.debugLog("NetConnectivityUtility.isConnected", this::class.java)
                            LoggerUtils.fileLog("NetConnectivityUtility.isConnected", this::class.java, mContext)
                        }
                        if (NetConnectivityUtility.isOnMobileDataNetwork) {
                            LoggerUtils.debugLog("NetConnectivityUtility.isOnMobileDataNetwork", this::class.java)
                            LoggerUtils.fileLog("NetConnectivityUtility.isOnMobileDataNetwork", this::class.java, mContext)
                        }
                        if (!NP_IDS_RESTRICTED_ON_WIFI.contains(pageDownLoadRequest.newsPaperId!!)) {
                            LoggerUtils.debugLog("!NP_IDS_RESTRICTED_ON_WIFI.contains(pageDownLoadRequest.newsPaperId", this::class.java)
                            LoggerUtils.fileLog("!NP_IDS_RESTRICTED_ON_WIFI.contains(pageDownLoadRequest.newsPaperId", this::class.java, mContext)
                        }
                        LoggerUtils.debugLog("Skipped restricted NP(${NP_IDS_RESTRICTED_ON_WIFI}) on Wify.", this::class.java)
                        LoggerUtils.fileLog("Skipped restricted NP(${NP_IDS_RESTRICTED_ON_WIFI}) on Wify.", this::class.java, mContext)
                    }
                }
        PageRequestServerUtils.logPageDownLoadRequestWorkerTask(servedDocumentIdList)
        LoggerUtils.fileLog("servedDocumentIdList.size: ${servedDocumentIdList.size}", this::class.java)
        LoggerUtils.debugLog("doWork exiting", this::class.java)
        LoggerUtils.fileLog("doWork exiting", this::class.java, mContext)
        return Result.success()
    }
}
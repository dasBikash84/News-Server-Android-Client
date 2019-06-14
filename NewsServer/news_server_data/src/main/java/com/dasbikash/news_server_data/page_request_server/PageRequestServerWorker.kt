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
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.CloudFireStorePageRequestServerUtils
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.PageDownLoadRequestResponse
import com.dasbikash.news_server_data.utills.HttpUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility

internal class PageRequestServerWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to **synchronously** do your work and return the
     * [androidx.work.ListenableWorker.Result] from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
     * you need to do your work asynchronously on a thread of your own choice, see
     * [ListenableWorker].
     *
     *
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * [androidx.work.ListenableWorker.Result].  After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The [androidx.work.ListenableWorker.Result] of the computation; note that
     * dependent work will not execute if you use
     * [androidx.work.ListenableWorker.Result.failure] or
     * [androidx.work.ListenableWorker.Result.failure]
     */
    override fun doWork(): Result {
        LoggerUtils.debugLog("doWork entry", this::class.java)
//        if (!NetConnectivityUtility.isOnMobileDataNetwork){
//            LoggerUtils.debugLog("doWork exiting because on wify",this::class.java)
//            return Result.success()
//        }
        val pageDownLoadRequestMap = CloudFireStorePageRequestServerUtils.getPageDownLoadRequests()

        val pageDownLoadRequestResponseMap = mutableMapOf<String, PageDownLoadRequestResponse>()
        pageDownLoadRequestMap.keys.asSequence().forEach {
            val pageDownLoadRequest = pageDownLoadRequestMap.get(it)!!
            LoggerUtils.debugLog(pageDownLoadRequest.toString(), this::class.java)
            if (pageDownLoadRequest.link != null) {
                val pageContent = HttpUtils.getWebPageContent(pageDownLoadRequest.link!!)
                if (pageContent!=null) {
                    pageDownLoadRequestResponseMap.put(it, PageDownLoadRequestResponse(
                            link = pageDownLoadRequest.link!!, pageContent = pageContent
                    ))
                }
            }
        }
        if (pageDownLoadRequestResponseMap.isNotEmpty()) {
            CloudFireStorePageRequestServerUtils.writeArticleData(pageDownLoadRequestResponseMap)
        }
        CloudFireStorePageRequestServerUtils.logPageDownLoadRequestWorkerTask(pageDownLoadRequestResponseMap)
        LoggerUtils.debugLog("doWork exiting", this::class.java)
        return Result.success()
    }
}
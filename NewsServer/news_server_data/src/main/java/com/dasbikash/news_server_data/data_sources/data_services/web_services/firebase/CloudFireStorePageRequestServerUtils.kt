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

import android.os.SystemClock
import androidx.annotation.Keep
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.google.firebase.Timestamp
import java.util.*

internal object CloudFireStorePageRequestServerUtils {

    fun writeArticleData(pageDownLoadRequestResponseData: Pair<String,PageDownLoadRequestResponse>):Boolean {

        LoggerUtils.debugLog("writeArticleData", this::class.java)

        val writeTask = CloudFireStoreConUtils.getPageDownloadRequestResponseRef()
                                        .document(pageDownLoadRequestResponseData.first)
                                        .set(pageDownLoadRequestResponseData.second)
        do {
            SystemClock.sleep(100L)
        } while (!writeTask.isComplete)
        return writeTask.isSuccessful
    }

    fun logPageDownLoadRequestWorkerTask(servedDocumentIdList: List<String>){

        LoggerUtils.debugLog("logPageDownLoadRequestWorkerTask", this::class.java)
        val writeTask= CloudFireStoreConUtils.getPageDownloadRequestWorkerLogRef()
                                        .document().set(PageDownLoadRequestWorkerLog.getInstance(servedDocumentIdList))
        do {
            LoggerUtils.debugLog("SystemClock.sleep(100)", this::class.java)
            SystemClock.sleep(100)
        } while (!writeTask.isComplete)
    }

}

@Keep
internal data class PageDownLoadRequestResponse(
        var link: String? = null,
        var pageContent: String? = null,
        var created: Timestamp = Timestamp(Date())
) {
    override fun toString(): String {
        return "PageDownLoadRequestResponse(link=$link, pageContent=${pageContent?.length
                ?: 0}, created=${created.toDate()})"
    }
}

@Keep
internal data class PageDownLoadRequestWorkerLog(
        val created: Timestamp = Timestamp(Date()),
        val servedDocumentList:String?=null
){
    companion object{
        fun getInstance(servedDocumentIdList: List<String>):
            PageDownLoadRequestWorkerLog{
            if (servedDocumentIdList.isNotEmpty()){
                return PageDownLoadRequestWorkerLog(servedDocumentList = servedDocumentIdList.toString())
            }
            return PageDownLoadRequestWorkerLog()
        }
    }
}
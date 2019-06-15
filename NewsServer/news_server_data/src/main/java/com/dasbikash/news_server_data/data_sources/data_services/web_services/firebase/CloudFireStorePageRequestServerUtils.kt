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
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.data_sources.data_services.user_details_data_service.UserIpDataService
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import java.util.*


internal object CloudFireStorePageRequestServerUtils {

    private const val MAX_BATCH_SIZE_FOR_WRITE = 400

    fun getPageDownLoadRequests(): Map<String, PageDownLoadRequest> {

        val lock = Object()
        val pageDownLoadRequestMap = mutableMapOf<String, PageDownLoadRequest>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getPageDownloadRequestRef()
                .get()
                .addOnSuccessListener { documents ->
                    LoggerUtils.debugLog("getPageDownLoadRequests addOnSuccessListener", this::class.java)
                    for (document in documents) {
                        val pageDownLoadRequest = document.toObject(PageDownLoadRequest::class.java)
                        pageDownLoadRequestMap.put(document.id, pageDownLoadRequest)
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    LoggerUtils.debugLog("getPageDownLoadRequests addOnFailureListener", this::class.java)
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        LoggerUtils.debugLog("getPageDownLoadRequests before wait", this::class.java)
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

//        LoggerUtils.debugLog("getPageDownLoadRequests before throw it",this::class.java)
//        dataServerException?.let { throw it }

//        LoggerUtils.debugLog("getPageDownLoadRequests before throw DataNotFoundException()",this::class.java)
//        if (pageDownLoadRequestMap.isEmpty()){
//            throw DataNotFoundException()
//        }

        LoggerUtils.debugLog("getPageDownLoadRequests before return", this::class.java)
        return pageDownLoadRequestMap.toMap()
    }

    fun writeArticleData(pageDownLoadRequestResponseMap: Map<String, PageDownLoadRequestResponse>) {

        LoggerUtils.debugLog("writeArticleData", this::class.java)

        if (pageDownLoadRequestResponseMap.isEmpty() ||
                pageDownLoadRequestResponseMap.values.size > MAX_BATCH_SIZE_FOR_WRITE) {
            LoggerUtils.debugLog("IllegalArgument", this::class.java)
            return
//            throw IllegalArgumentException()
        }

        val batch = CloudFireStoreConUtils.getDbConnection().batch()
        val deleteTasks = mutableListOf<Task<Void>>()

        pageDownLoadRequestResponseMap.keys.asSequence().forEach {
            val pageDownLoadRequestResponse = pageDownLoadRequestResponseMap.get(it)!!
            pageDownLoadRequestResponse.created = Timestamp(Date())
            LoggerUtils.debugLog("PageDownLoadRequestResponse: ${pageDownLoadRequestResponse.toString()}", this::class.java)
            LoggerUtils.debugLog("Doc Id: ${it}", this::class.java)
            batch.set(CloudFireStoreConUtils.getPageDownloadRequestResponseRef().document(it), pageDownLoadRequestResponse)
            deleteTasks.add(CloudFireStoreConUtils.getPageDownloadRequestRef().document(it).delete())
        }
        val writeTask = batch.commit()
        do {
            SystemClock.sleep(100)
        } while (!writeTask.isComplete || deleteTasks.filter { !it.isComplete() }.count() > 0)
    }

    fun logPageDownLoadRequestWorkerTask(pageDownLoadRequestResponseMap: Map<String, PageDownLoadRequestResponse>){

        LoggerUtils.debugLog("logPageDownLoadRequestWorkerTask", this::class.java)
        val writeTask= CloudFireStoreConUtils.getPageDownloadRequestWorkerLogRef()
                                        .document().set(PageDownLoadRequestWorkerLog.getInstance(pageDownLoadRequestResponseMap))
        do {
            LoggerUtils.debugLog("SystemClock.sleep(100)", this::class.java)
            SystemClock.sleep(100)
        } while (!writeTask.isComplete)
    }

}

@Keep
internal data class PageDownLoadRequest(
        var newsPaperId: String? = null,
        var link: String? = null,
        var created: Timestamp? = null
)

@Keep
internal data class PageDownLoadRequestResponse(
        var link: String? = null,
        var pageContent: String? = null,
        var created: Timestamp? = null
) {
    override fun toString(): String {
        return "PageDownLoadRequestResponse(link=$link, pageContent=${pageContent?.length
                ?: 0}, created=${created?.toDate()})"
    }
}

@Keep
internal data class PageDownLoadRequestWorkerLog(
        val userIp: String = UserIpDataService.getIpAddress(),
//        val deviceDetails: String = "BRAND: ${Build.BRAND} Manufacture: ${Build.MANUFACTURER} " +
//                "MODEL: ${Build.MODEL} SDK_INT: ${Build.VERSION.SDK_INT}",
        val created: Timestamp = Timestamp(Date()),
        val servedDocumentList:String?=null
){
    companion object{
        fun getInstance(pageDownLoadRequestResponseMap: Map<String, PageDownLoadRequestResponse>):
            PageDownLoadRequestWorkerLog{
            if (pageDownLoadRequestResponseMap.isNotEmpty()){
                return PageDownLoadRequestWorkerLog(servedDocumentList = pageDownLoadRequestResponseMap.keys.toString())
            }
            return PageDownLoadRequestWorkerLog()
        }
    }
}
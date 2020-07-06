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

package com.dasbikash.news_server_data.utills

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object FileDownloaderUtils {

    fun downloadImageInExternalFilesDir(context: Context,link:String){
        downloadFileInExternalFilesDir(context,link,Environment.DIRECTORY_PICTURES,"ns-image.jpg")
    }

    private fun downloadFileInExternalFilesDir(context: Context,link:String,
                                       dirType:String, subPath:String){
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(link))
                                                .setDestinationInExternalFilesDir(context,dirType,subPath)
                                                .setDescription("Download by news-server")
                                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                .setAllowedOverMetered(true)
                                                .setAllowedOverRoaming(true)

        downloadManager.enqueue(request)
    }
}
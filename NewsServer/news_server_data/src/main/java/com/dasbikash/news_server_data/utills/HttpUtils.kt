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

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object HttpUtils {

    const val ERROR_CONTENT = "Error"

    fun getWebPageContent(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        var response: Response? = null
        try {
            response = client.newCall(request).execute()
            if (response != null) {
                val pageContent: String
                if (response.isSuccessful) {
                    pageContent = response.body()?.string() ?: ERROR_CONTENT
                } else {
                    pageContent = ERROR_CONTENT
                }
                response.close()
                return pageContent
            }
        } catch (ex: Exception) {
            LoggerUtils.printStackTrace(ex)
            response?.close()
        }
        return null
    }
}
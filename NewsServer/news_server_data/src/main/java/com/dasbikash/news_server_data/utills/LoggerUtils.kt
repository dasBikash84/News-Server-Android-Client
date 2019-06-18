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

import android.util.Log
import com.dasbikash.news_server_data.BuildConfig

object LoggerUtils {
    private const val TAG = "NS>>"
    private const val MAX_TAG_LENGTH = 23

    fun printStackTrace(ex:Throwable){
        if (BuildConfig.DEBUG){
            debugLog("Error StackTrace: \n" + ExceptionUtils.getStackTraceAsString(ex), this::class.java)
        }
    }

    fun <T> debugLog(message:String,type:Class<T>){
        if (BuildConfig.DEBUG) {
            var classNameEndIndex = type.simpleName.length
            if (classNameEndIndex > (MAX_TAG_LENGTH- TAG.length)){
                classNameEndIndex = MAX_TAG_LENGTH- TAG.length
            }
            Log.d(TAG + type.simpleName.substring(0,classNameEndIndex), message)
        }
    }
}
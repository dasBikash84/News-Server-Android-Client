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

object LoggerUtils {
    val opMode = OpMode.DEBUG
    val TAG = "News-Server | "

    fun printStackTrace(ex:Throwable){
        if (opMode==LoggerUtils.OpMode.DEBUG){
            ex.printStackTrace()
        }
    }

    fun <T> debugLog(message:String,type:Class<T>){
        Log.d(TAG+type.simpleName,message)
    }

    enum class OpMode{
        DEBUG,RELEASE
    }
}
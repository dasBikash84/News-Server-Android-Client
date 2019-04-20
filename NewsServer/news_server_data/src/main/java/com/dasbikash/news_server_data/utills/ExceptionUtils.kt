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

import android.os.Looper
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.exceptions.OnMainThreadException

object ExceptionUtils {

    fun thowExceptionIfOnMainThred(){
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            throw OnMainThreadException()
        }
    }
    fun thowExceptionIfNoInternetConnection(){
        if (!NetConnectivityUtility.isConnected) {
            throw NoInternertConnectionException();
        }
    }

    fun checkRequestValidityBeforeNetworkAccess(){
        thowExceptionIfNoInternetConnection()
        thowExceptionIfOnMainThred()
    }

    fun checkRequestValidityBeforeDatabaseAccess(){
        thowExceptionIfNoInternetConnection()
        thowExceptionIfOnMainThred()
    }

    fun checkRequestValidityBeforeLocalDiskAccess() {
        thowExceptionIfOnMainThred()
    }
}
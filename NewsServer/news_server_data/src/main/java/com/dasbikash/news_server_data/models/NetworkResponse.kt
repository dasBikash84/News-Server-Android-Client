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

package com.dasbikash.news_server_data.models

import com.dasbikash.news_server_data.exceptions.NewsServerException

internal class NetworkResponse<T> private
        constructor(val status: ResponseStatus,val payload: T,val exception:NewsServerException?=null){

    constructor(status: ResponseStatus,payload: T): this (status = status, payload = payload,exception = null)

    enum class ResponseStatus{
        SUCCESS,FAILURE
    }

    companion object{
        internal fun <T> getSuccessResponse(payload:T) = NetworkResponse<T>(ResponseStatus.SUCCESS,payload)
        internal fun <T>getFailureResponse(dummyPayload: T, exception:NewsServerException) =
                NetworkResponse(status = ResponseStatus.FAILURE,payload = dummyPayload,exception = exception)
    }

}
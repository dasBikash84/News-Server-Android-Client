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

import android.os.Build
import com.google.firebase.database.ServerValue

data class UserSettingsUpdateDetails(
        val timeStamp: Map<String,String> = ServerValue.TIMESTAMP,
        var userIp:String,
        var deviceDetails:String = "BRAND: ${Build.BRAND} Manufacture: ${Build.MANUFACTURER} " +
                                    "MODEL: ${Build.MODEL} SDK_INT: ${Build.VERSION.SDK_INT}"
){
    companion object{
        const val NULL_IP = "255.255.255.255"
    }
}

class UserSettingsUpdateTime(
        var timeStamp: Long = 0L
)
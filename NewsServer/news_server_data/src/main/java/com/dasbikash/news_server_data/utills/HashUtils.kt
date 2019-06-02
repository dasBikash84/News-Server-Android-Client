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

object HashUtils {

    val SUPPLIED_PRIME_NUMBER = 157L;

    fun hash(s: String): String {
        var h = 0L
        for (i in 0 until s.length) {
            h = SUPPLIED_PRIME_NUMBER * h + s[i].toLong()
        }
        return h.toString()
    }
}

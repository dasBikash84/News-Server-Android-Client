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

object TextFilterUtils {
    private val INVALID_NAME_CHECKER_REGEX = "(f[uc]{1,}k)|(s[uc]{1,}k)|(d[ic]{1,}k)|" +
                                                        "(c[un]{2,}t)|(p[us]{1,}sy)|(penis)|(vagina)"
    private val INVALID_NAME_CHECKER_REGEX_LIST = listOf<String>(
            "f[uc]{1,}k","s[uc]{1,}k","d[ic]{1,}k","c[un]{2,}t","p[us]{1,}sy",
            "penis","vagina"
    )


    fun checkIfValidTextInput(text: String): Boolean {
        return !text.matches(INVALID_NAME_CHECKER_REGEX.toRegex(setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL)))
    }
}
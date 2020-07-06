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

package com.dasbikash.news_server_data.database.room_converters

import androidx.room.TypeConverter
import com.dasbikash.news_server_data.utills.LoggerUtils
import java.util.*

internal object IntListConverter {

    private val DATA_BRIDGE = "@#@#@#"

    @TypeConverter
    @JvmStatic
    internal fun fromIntList(entry: List<Int>): String {
        val stringBuilder = StringBuilder("")
        for (i in entry.indices) {
            stringBuilder.append(entry[i])
            if (i != entry.size - 1) {
                stringBuilder.append(DATA_BRIDGE)
            }
        }
        return stringBuilder.toString()
    }

    @TypeConverter
    @JvmStatic
    internal fun toIntList(entryListString: String?): List<Int> {
        val entry = ArrayList<Int>()

        if (entryListString != null) {
            for (entryStr in entryListString.split(DATA_BRIDGE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                try {
                    entry.add(Integer.parseInt(entryStr))
                } catch (e: NumberFormatException) {
                    LoggerUtils.printStackTrace(e)
                }

            }
        }

        return entry
    }
}

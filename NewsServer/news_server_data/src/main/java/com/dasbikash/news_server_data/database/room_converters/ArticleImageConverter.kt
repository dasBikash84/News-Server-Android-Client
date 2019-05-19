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

import android.util.Log
import androidx.room.TypeConverter
import com.dasbikash.news_server_data.models.room_entity.ArticleImage
import com.dasbikash.news_server_data.utills.LoggerUtils
import java.util.*

internal object ArticleImageConverter {

    private val DATA_BRIDGE = "@#@#@#"
    private val PARAM_BRIDGE = "_____"

    @TypeConverter
    @JvmStatic
    internal fun fromArticleImage(entry: List<ArticleImage>?): String {

        val stringBuilder = StringBuilder("")

        entry?.asSequence()
                ?.map {
                    stringBuilder.append(it.link).append(PARAM_BRIDGE).append(it.caption).append(DATA_BRIDGE)
                    it
                }
                ?.filter { entry.indexOf(it) == entry.size-1 }
                ?.forEach { stringBuilder.append(DATA_BRIDGE) }
        LoggerUtils.debugLog("stringBuilder: ${stringBuilder.toString()}",this::class.java)
        LoggerUtils.debugLog("entry: ${entry.toString()}",this::class.java)
        return stringBuilder.toString()
    }

    @TypeConverter
    @JvmStatic
    internal fun toArticleImage(entryCatString: String): List<ArticleImage> {

        val entryList = ArrayList<ArticleImage>()

        for (entryStr in entryCatString.split(DATA_BRIDGE).iterator()) {

            val articleImage = ArticleImage()

            val dataFragments = entryStr.split(PARAM_BRIDGE)

            if (dataFragments.size == 1) {
                articleImage.link = dataFragments.get(0)
            } else if (dataFragments.size == 2) {
                articleImage.link = dataFragments.get(0)
                articleImage.caption = dataFragments.get(1)
            }
            entryList.add(articleImage)
        }
        LoggerUtils.debugLog("entryCatString: ${entryCatString.toString()}",this::class.java)
        LoggerUtils.debugLog("entryList: ${entryList}",this::class.java)

        return entryList
    }
}

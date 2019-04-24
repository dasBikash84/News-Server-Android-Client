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
import java.util.*

internal object ArticleImageConverter {

    private val DATA_BRIDGE = "@#@#@#"
    private val PARAM_BRIDGE = "_____"

    @TypeConverter
    @JvmStatic
    fun fromArticleImage(entry: List<ArticleImage>): String {

        val stringBuilder = StringBuilder("")

        entry.asSequence()
                .map {
                    stringBuilder.append(it.link).append(PARAM_BRIDGE).append(it.captin).append(DATA_BRIDGE)
                    it
                }
                .filter { entry.indexOf(it) == entry.size-1 }
                .forEach { stringBuilder.append(DATA_BRIDGE) }

        /*for (i in entry.indices) {
            stringBuilder.append(entry[i].link + PARAM_BRIDGE + entry[i].captin)
            if (i != entry.size - 1) {
                stringBuilder.append(DATA_BRIDGE)
            }
        }*/
        Log.d("ArticleImageConverter","stringBuilder: ${stringBuilder.toString()}")
        Log.d("ArticleImageConverter","entry: ${entry.toString()}")
        return stringBuilder.toString()
    }

    @TypeConverter
    @JvmStatic
    fun toArticleImage(entryCatString: String): List<ArticleImage> {

        val entryList = ArrayList<ArticleImage>()

        for (entryStr in entryCatString.split(DATA_BRIDGE).iterator()) {

            val articleImage = ArticleImage()

            val dataFragments = entryStr.split(PARAM_BRIDGE)

            if (dataFragments.size == 1) {
                articleImage.link = dataFragments.get(0)
            } else if (dataFragments.size == 2) {
                articleImage.link = dataFragments.get(0)
                articleImage.captin = dataFragments.get(1)
            }
            entryList.add(articleImage)
        }
        Log.d("ArticleImageConverter","entryCatString: ${entryCatString.toString()}")
        Log.d("ArticleImageConverter","entryList: ${entryList}")

        return entryList
    }
}
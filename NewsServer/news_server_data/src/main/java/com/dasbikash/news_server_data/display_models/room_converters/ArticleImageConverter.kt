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

package com.dasbikash.news_server_data.display_models.room_converters

import androidx.room.TypeConverter
import com.dasbikash.news_server_data.display_models.entity.ArticleImage
import java.util.*

internal object ArticleImageConverter {

    private val DATA_BRIDGE = "@#@#@#"
    private val PARAM_BRIDGE = "_____"

    @TypeConverter
    @JvmStatic
    fun fromArticleImage(entry: List<ArticleImage>): String {

        val stringBuilder = StringBuilder("")
        for (i in entry.indices) {
            stringBuilder.append(entry[i].imageLink + PARAM_BRIDGE + entry[i].imageCaptin)
            if (i != entry.size - 1) {
                stringBuilder.append(DATA_BRIDGE)
            }
        }
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
                articleImage.imageLink = dataFragments.get(0)
            } else if (dataFragments.size == 2) {
                articleImage.imageLink = dataFragments.get(0)
                articleImage.imageCaptin = dataFragments.get(1)
            }
            entryList.add(articleImage)
        }

        return entryList
    }
}

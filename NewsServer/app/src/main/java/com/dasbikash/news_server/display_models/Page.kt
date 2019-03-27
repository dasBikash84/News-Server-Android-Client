/*
 * Copyright 2019 www.dasbikash.org. All rights reserved.
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

package com.dasbikash.news_server.display_models

import java.io.Serializable

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        foreignKeys = [
            ForeignKey(entity = Newspaper::class, parentColumns = ["id"], childColumns = ["newsPaperId"])
        ],
        indices = [
            Index("newsPaperId"), Index("parentPageId")
        ]
)
data class Page(
        @PrimaryKey val id: Int, val newsPaperId: Int, val parentPageId: Int,
        val title: String, val active: Boolean, val favourite: Boolean)
    : Serializable {

    val isTopLevelPage: Boolean
        @Ignore
        get() = parentPageId == TOP_LEVEL_PAGE_PARENT_ID

    companion object {
        @JvmStatic
        public val TOP_LEVEL_PAGE_PARENT_ID = 0
    }
}

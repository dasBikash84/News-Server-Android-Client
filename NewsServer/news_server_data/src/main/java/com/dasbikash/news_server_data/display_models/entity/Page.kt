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

package com.dasbikash.news_server_data.display_models.entity

import androidx.room.*
import com.dasbikash.news_server_data.display_models.entity.Newspaper
import com.google.firebase.database.Exclude
import java.io.Serializable

@Entity(
        foreignKeys = [
            ForeignKey(entity = Newspaper::class, parentColumns = ["id"], childColumns = ["newsPaperId"])
        ],
        indices = [
            Index("newsPaperId"), Index("parentPageId")
        ]
)
data class Page(
        @PrimaryKey
        var id: String="",
        var newsPaperId: String?=null,
        var parentPageId: String?=null,
        var name: String?=null,
        @Ignore
        var active: Boolean = false,
        @Ignore
        var linkFormat:String? = null
): Serializable{
    @Exclude
    var hasChild:Boolean = false
    @Exclude
    var hasData:Boolean = false

    companion object {
        @JvmField
        val TOP_LEVEL_PAGE_PARENT_ID = "PAGE_ID_0"
    }
}
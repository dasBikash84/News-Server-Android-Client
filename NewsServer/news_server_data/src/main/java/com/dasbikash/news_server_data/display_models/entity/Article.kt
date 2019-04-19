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
import com.google.gson.annotations.Expose
import java.io.Serializable
import java.util.*

@Entity(
        foreignKeys = [
            ForeignKey(entity = Page::class, parentColumns = ["id"], childColumns = ["pageId"]),
            ForeignKey(entity = Newspaper::class, parentColumns = ["id"], childColumns = ["newsPaperId"])
        ],
        indices = [
            Index(value = ["pageId"], name = "article_page_id_index"),
            Index(value = ["newsPaperId"], name = "article_newsPaperId_index")
        ]
)
data class Article(
        @PrimaryKey
        var id: String="",
        var pageId: String?=null,
        @Expose(serialize = false, deserialize = false)
        var newsPaperId: String?=null, //Have to fill after fetch from server
        var title: String?=null,
        var articleText: String?=null,
        @Ignore
        var modificationTime: Long = 0L,
        @Ignore
        var publicationTime: Long = 0L,
        @Expose(serialize = false, deserialize = false)
        var publicationDate: Date ? = null, //have to fill after fetch from server
        var imageLinkList: List<ArticleImage>? = mutableListOf(),
        var previewImageLink: String? = null,
        @Expose(serialize = false, deserialize = false)
        private var created:Long = 0L
) : Serializable {

    fun getCreated():Long{
        return System.currentTimeMillis()
    }
    fun setCreated(time:Long){
        created = time
    }
}
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

package com.dasbikash.news_server_data.models.room_entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import com.google.gson.annotations.Expose
import java.io.Serializable
import java.util.*

@Entity(
        foreignKeys = [
            ForeignKey(entity = Page::class, parentColumns = ["id"], childColumns = ["pageId"]),
            ForeignKey(entity = Newspaper::class, parentColumns = ["id"], childColumns = ["newspaperId"])
        ],
        indices = [
            Index(value = ["pageId"], name = "article_page_id_index"),
            Index(value = ["newspaperId"], name = "article_newsPaperId_index")
        ]
)
data class Article(
        @PrimaryKey
        var id: String="",
        var pageId: String?=null,
        @Exclude
        @com.google.firebase.firestore.Exclude
        @Expose(serialize = false, deserialize = false)
        var newspaperId: String?=null, //Have to fill after fetch from server
        var title: String?=null,
        var articleText: String?=null,
        var publicationTimeRTDB: Long? = null,
        var publicationTime: Date? = null,
        var imageLinkList: List<ArticleImage>? = null,
        var previewImageLink: String? = null,
        @Exclude
        @com.google.firebase.firestore.Exclude
        @Expose(serialize = false, deserialize = false)
        var created:Long = System.currentTimeMillis()
) : Serializable {

    override fun toString(): String {
        return "Article(id='$id', pageId=$pageId, newspaperId=$newspaperId, title=$title, publicationTime=$publicationTime)"
    }


}
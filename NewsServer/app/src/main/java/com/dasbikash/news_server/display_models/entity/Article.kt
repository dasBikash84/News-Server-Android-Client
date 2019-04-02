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

package com.dasbikash.news_server.display_models.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
        foreignKeys = [
            ForeignKey(entity = Page::class, parentColumns = ["id"], childColumns = ["pageId"])
        ],
        indices = [
            Index(value = ["pageId"], name = "article_page_id_index")
        ]
)
class Article (): Serializable{

    @PrimaryKey
    var id: Int?=0
    var pageId: Int?=0
    var title: String?=null
    var lastModificationTS: Long?=0
    var imageLinkList: List<String>?=ArrayList()

    constructor(
            id: Int,pageId: Int,title: String,
            lastModificationTS: Long,
            imageLinkList:List<String>):this(){
        this.id=id
        this.pageId=pageId
        this.title = title
        this.lastModificationTS=lastModificationTS
        this.imageLinkList = imageLinkList
    }

    override fun toString(): String {
        return "Article(id=$id, pageId=$pageId, title=$title, lastModificationTS=$lastModificationTS, imageLinkList=$imageLinkList)"
    }


}

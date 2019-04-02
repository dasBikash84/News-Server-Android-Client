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

import java.io.Serializable

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dasbikash.news_server.display_models.mapped_embedded.IntDataList

@Entity
class PageGroup  (): Serializable{
    @PrimaryKey
    var id: Int = 0
    var title: String? = null
    var active: Boolean=true
    var pageList: List<Int>?=null

    constructor(id: Int,title: String,
                active: Boolean,pageList: List<Int>):this(){
        this.id = id
        this.title =title
        this.active = active
        this.pageList = pageList
    }

    override fun toString(): String {
        return "PageGroup(id=$id, title=$title, active=$active, pageList=$pageList)"
    }
}

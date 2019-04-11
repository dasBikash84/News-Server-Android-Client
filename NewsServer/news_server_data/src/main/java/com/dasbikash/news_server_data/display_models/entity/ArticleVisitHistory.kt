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

package com.dasbikash.news_server_data.display_models.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
        foreignKeys = [
            ForeignKey(entity = Page::class, parentColumns = ["id"], childColumns = ["pageId"]),
            ForeignKey(entity = Article::class, parentColumns = ["id"], childColumns = ["articleId"]
            )],
        indices = [
            Index(name = "article_visit_history_page_id_index", value = ["pageId"]),
            Index(name = "article_id_index", value = ["articleId"])
        ])
data class ArticleVisitHistory (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val articleId: Int,
    val pageId: Int,
    val visitTime: Date = Date()
)

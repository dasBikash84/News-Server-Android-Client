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
import com.google.gson.annotations.Expose
import java.io.Serializable
import java.util.*

@Entity(
//        foreignKeys = [
//            ForeignKey(entity = Article::class, parentColumns = ["id"], childColumns = ["id"])
//        ]
)
data class SavedArticle(
        @PrimaryKey
        val id: String,
        val pageName: String,
        var newspaperName: String,
        var title: String,
        var articleText: String,
        var publicationTime: Date,
        var imageLinkList: List<ArticleImage>?,
        var previewImageLink: String?
) : Serializable {
    companion object{
        fun getInstance(article: Article,page: Page,newspaper: Newspaper,articleImageList:List<ArticleImage>?):SavedArticle{
            return SavedArticle(article.id,page.name!!,newspaper.name!!,article.title!!,article.articleText!!,
                                article.publicationTime!!,articleImageList,article.previewImageLink)
        }
    }

    override fun toString(): String {
        return "SavedArticle(id='$id', pageName='$pageName', newspaperName='$newspaperName', title='$title')"
    }

}
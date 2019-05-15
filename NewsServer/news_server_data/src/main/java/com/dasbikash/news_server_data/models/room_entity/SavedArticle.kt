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

@Entity
data class SavedArticle(
        @PrimaryKey
        val id: String,
        val pageName: String,
        val pageId: String,
        var newspaperName: String,
        var title: String,
        var publicationTime: Date,
        var previewImageLink: String?
) : Serializable {
    var imageLinkList: List<ArticleImage>?=null
    var articleText: String?=null
    companion object{
        fun getInstance(article: Article,page: Page,newspaper: Newspaper,articleImageList:List<ArticleImage>?):SavedArticle{
            val savedArticle =  SavedArticle(article.id,page.name!!,page.id,newspaper.name!!,article.title!!,
                                article.publicationTime!!,article.previewImageLink)
            savedArticle.imageLinkList = articleImageList
            savedArticle.articleText = article.articleText
            return savedArticle
        }
    }

    override fun toString(): String {
        return "SavedArticle(id='$id', pageName='$pageName', newspaperName='$newspaperName', title='$title')"
    }

}
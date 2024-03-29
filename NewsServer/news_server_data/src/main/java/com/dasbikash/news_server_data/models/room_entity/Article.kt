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

import android.os.Parcel
import android.os.Parcelable
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
) : Serializable, Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            null,
            Date().setTs(parcel.readLong()),
            parcel.createTypedArrayList(ArticleImage),
            parcel.readString(),
            parcel.readLong()) {
    }

    fun resetCreated(){
        created = System.currentTimeMillis()
    }

    override fun toString(): String {
        return "Article(id='$id', pageId=$pageId, newspaperId=$newspaperId, title=$title, publicationTime=$publicationTime)"
    }

    fun checkIfSameArticle(other: Article):Boolean{
        return (id == other.id) || (id.substringBefore('_') == other.id.substringBefore('_'))
                || (title!!.trim() == other.title!!.trim())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(pageId)
        parcel.writeString(newspaperId)
        parcel.writeString(title)
        parcel.writeString(articleText)
        parcel.writeLong(publicationTime?.time ?: 0L)
        parcel.writeTypedList(imageLinkList)
        parcel.writeString(previewImageLink)
        parcel.writeLong(created)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Article> {

        fun removeDuplicates(articles:List<Article>):
                List<Article>{
            val output = mutableListOf<Article>()
            articles.asSequence()
                    .filter {
                        val article = it
                        output.count { it.checkIfSameArticle(article) } == 0
                    }.forEach { output.add(it) }
            return output
        }
        override fun createFromParcel(parcel: Parcel): Article {
            return Article(parcel)
        }

        override fun newArray(size: Int): Array<Article?> {
            return arrayOfNulls(size)
        }
    }
}

fun Date.setTs(time:Long):Date{
    this.time = time
    return this
}
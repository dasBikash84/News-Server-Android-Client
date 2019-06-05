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
import androidx.room.*
import com.google.firebase.database.Exclude
import com.google.gson.annotations.Expose

@Entity(
        foreignKeys = [
            ForeignKey(entity = Newspaper::class, parentColumns = ["id"], childColumns = ["newspaperId"])
        ],
        indices = [
            Index("newspaperId"), Index("parentPageId")
        ]
)
data class Page(
        @PrimaryKey
        var id: String="",
        var newspaperId: String?=null,
        var parentPageId: String?=null,
        var name: String?=null,
        @Expose(serialize = false,deserialize = false)
        @com.google.firebase.firestore.Exclude
        var articleFetchStatus: String = PageArticleFetchStatus.NOT_SYNCED
): Parcelable {
    var hasChild:Boolean = false
    var hasData:Boolean = false
    var topLevelPage:Boolean = false
    @Ignore
    var active:Boolean = true

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
            ) {
        topLevelPage = parcel.readByte() != 0.toByte()
        hasChild = parcel.readByte() != 0.toByte()
        hasData = parcel.readByte() != 0.toByte()
    }

    companion object {
        const val TOP_LEVEL_PAGE_PARENT_ID = "PAGE_ID_0"

        @JvmField
        @Ignore
        @Exclude
        @com.google.firebase.firestore.Exclude
        @Expose(serialize = false,deserialize = false)
        val CREATOR: Parcelable.Creator<Page> = object : Parcelable.Creator<Page> {
            override fun createFromParcel(source: Parcel): Page {
                return Page(source)
            }
            override fun newArray(size: Int): Array<Page> {
                return arrayOf<Page>()
            }
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(newspaperId)
        parcel.writeString(parentPageId)
        parcel.writeString(name)
        parcel.writeByte(if (topLevelPage) 1 else 0)
        parcel.writeByte(if (hasChild) 1 else 0)
        parcel.writeByte(if (hasData) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Page(id='$id', newspaperId=$newspaperId, name=$name)"
    }
}

object PageArticleFetchStatus{
    const val SYNCED_WITH_SERVER: String ="synced"
    const val NOT_SYNCED: String ="notSynced"
    const val END_REACHED: String ="atEnd"
}
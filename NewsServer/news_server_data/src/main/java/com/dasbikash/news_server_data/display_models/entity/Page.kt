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

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.firebase.database.Exclude
import com.google.gson.annotations.Expose
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
): Serializable, Parcelable {
    @Exclude
    var hasChild:Boolean = false
    @Exclude
    private var hasData:Boolean = false

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString()) {
        hasChild = parcel.readByte() != 0.toByte()
        hasData = parcel.readByte() != 0.toByte()
    }

    fun getHasData():Boolean{
        if (linkFormat != null){ //will be used during initial write by room
            return true
        }
        return hasData //Wiil be used on application runtime
    }

    fun setHasData(hasData:Boolean){
        this.hasData = hasData
    }

    companion object {
        const val TOP_LEVEL_PAGE_PARENT_ID = "PAGE_ID_0"

        @JvmField
        @Ignore
        @Exclude
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
        parcel.writeString(newsPaperId)
        parcel.writeString(parentPageId)
        parcel.writeString(name)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(linkFormat)
        parcel.writeByte(if (hasChild) 1 else 0)
        parcel.writeByte(if (hasData) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }
}
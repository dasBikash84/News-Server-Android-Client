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
import com.google.gson.annotations.Expose

class ArticleImage(
        @Expose(deserialize = true,serialize = true)
        var link: String?=null,
        @Expose(deserialize = true,serialize = true)
        var caption:String?=null
) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString()) {
        }

        override fun toString(): String {
                return "ArticleImage(link=$link, caption=$caption)"
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(link)
                parcel.writeString(caption)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<ArticleImage> {
                override fun createFromParcel(parcel: Parcel): ArticleImage {
                        return ArticleImage(parcel)
                }

                override fun newArray(size: Int): Array<ArticleImage?> {
                        return arrayOfNulls(size)
                }
        }
}
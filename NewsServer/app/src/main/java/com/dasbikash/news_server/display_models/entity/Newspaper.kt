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
            ForeignKey(entity = Country::class, parentColumns = ["name"], childColumns = ["countryName"]),
            ForeignKey(entity = Language::class, parentColumns = ["id"], childColumns = ["languageId"])],
        indices = [
            Index(value = ["countryName"], name = "country_name_index"),
            Index(value = ["languageId"], name = "language_id_index")])
data class Newspaper(
        @PrimaryKey
        var id: String="",
        var name: String?=null,
        var countryName: String?=null,
        var languageId: String?=null,
        var active: Boolean=true
) : Serializable
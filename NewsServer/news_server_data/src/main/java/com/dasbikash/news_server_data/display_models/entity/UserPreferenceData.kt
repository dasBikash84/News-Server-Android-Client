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

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude

@Entity
data class UserPreferenceData(
        @PrimaryKey
        @Exclude
        var id:String="",
        var favouritePageIds: MutableList<String> = mutableListOf(),
        var inActiveNewsPaperIds: MutableList<String> = mutableListOf(),
        var inActivePageIds: MutableList<String> = mutableListOf(),
        @Ignore
        var pageGroups:MutableMap<String,PageGroup> = mutableMapOf(),
        @Ignore
        var updateLog:MutableMap<String,userSettingsUpdateTime> = mutableMapOf()
)
class userSettingsUpdateTime(
        var timeStamp:Long? = null
){
        override fun toString(): String {
                return "userSettingsUpdateTime(timeStamp=$timeStamp)"
        }
}

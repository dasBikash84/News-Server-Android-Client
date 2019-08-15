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

package com.dasbikash.news_server_data.data_sources

import android.content.Intent
import com.dasbikash.news_server_data.models.room_entity.FavouritePageEntry
import com.dasbikash.news_server_data.models.room_entity.Page

internal interface UserSettingsDataService {
    fun getLogInStatus(): Boolean
    fun getLogInIntent(): Intent?
    fun signOutUser()
    fun getCurrentUserName(): String?
    fun checkIfLoogedAsAdmin(): Boolean
    fun getLastUserSettingsUpdateTime(): Long
    fun getUserPreferenceData(): List<FavouritePageEntry>
    fun addPageToFavList(page: Page,doOnSuccess:(()->Unit)?=null,doOnFailure:(()->Unit)?=null)
    fun removePageFromFavList(page: Page,doOnSuccess:(()->Unit)?=null,doOnFailure:(()->Unit)?=null)
}
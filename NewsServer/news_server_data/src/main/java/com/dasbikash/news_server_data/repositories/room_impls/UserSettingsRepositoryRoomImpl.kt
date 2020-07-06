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

package com.dasbikash.news_server_data.repositories.room_impls

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.models.room_entity.FavouritePageEntry
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.UserSettingsRepository

internal class UserSettingsRepositoryRoomImpl internal constructor(context: Context) :
        UserSettingsRepository() {

    private val mDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    override fun addToFavouritePageEntry(page: Page): Boolean {
        mDatabase.favouritePageEntryDao.add(FavouritePageEntry(pageId = page.id))
        return true
    }

    override fun removeFromFavouritePageEntry(page: Page): Boolean {
        mDatabase.favouritePageEntryDao.findByPageId(page.id)?.let {
            mDatabase.favouritePageEntryDao.delete(it)
            return true
        }
        return false
    }

    override fun updateFavouritePageEntry(favouritePageEntry: FavouritePageEntry): Boolean {
        mDatabase.favouritePageEntryDao.findByPageId(favouritePageEntry.pageId)?.let {
            mDatabase.favouritePageEntryDao.update(favouritePageEntry)
            return true
        }
        return false
    }

    override fun nukeUserPreferenceData() {
        mDatabase.favouritePageEntryDao.nukeTable()
    }

    override fun addUserPreferenceDataToLocalDB(favouritePageEntries: List<FavouritePageEntry>) {
        if (favouritePageEntries.isNotEmpty()){
            mDatabase.favouritePageEntryDao.addAll(favouritePageEntries)
        }
    }

    override fun getFavouritePageEntries(): List<FavouritePageEntry> {
        return mDatabase.favouritePageEntryDao.findAll()
    }

    override fun getFavouritePageEntryLiveData(): LiveData<List<FavouritePageEntry>> {
        return mDatabase.favouritePageEntryDao.findAllLiveData()
    }

    override fun resetUserSettings() {
        mDatabase.favouritePageEntryDao.nukeTable()
    }

    override fun findFavouritePageEntryById(pageId: String): FavouritePageEntry? {
        return mDatabase.favouritePageEntryDao.findByPageId(pageId)
    }
}
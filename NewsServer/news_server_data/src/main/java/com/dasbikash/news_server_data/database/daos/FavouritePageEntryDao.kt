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

package com.dasbikash.news_server_data.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dasbikash.news_server_data.models.room_entity.FavouritePageEntry

@Dao
internal interface FavouritePageEntryDao {

    @Insert
    fun addAll(favouritePageEntries: List<FavouritePageEntry>)

    @Insert
    fun add(favouritePageEntry: FavouritePageEntry)

    @Query("SELECT * from FavouritePageEntry")
    fun findAll(): List<FavouritePageEntry>

    @Query("SELECT * from FavouritePageEntry WHERE pageId=:pageId")
    fun findByPageId(pageId:String): FavouritePageEntry?

    @Query("SELECT * from FavouritePageEntry")
    fun findAllLiveData(): LiveData<List<FavouritePageEntry>>

    @Query("DELETE FROM FavouritePageEntry")
    fun nukeTable()

    @Delete
    fun delete(favouritePageEntry: FavouritePageEntry)

    @Update
    fun update(favouritePageEntry: FavouritePageEntry)
}


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
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dasbikash.news_server_data.display_models.entity.UserPreferenceData

@Dao
internal interface UserPreferenceDataDao {
    @Insert
    fun add(vararg userPreferenceData: UserPreferenceData)

    @Query("SELECT * from UserPreferenceData")
    fun findAll(): List<UserPreferenceData>

    @Query("SELECT * from UserPreferenceData LIMIT 1")
    fun findUserPreferenceStaticData(): UserPreferenceData

    @Query("SELECT * from UserPreferenceData LIMIT 1")
    fun findUserPreferenceData(): LiveData<UserPreferenceData>

    @Query("DELETE FROM UserPreferenceData")
    fun nukeTable()

    @Update
    fun save(userPreferenceData: UserPreferenceData)
}

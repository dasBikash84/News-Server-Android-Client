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
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.dasbikash.news_server_data.utills.LoggerUtils
import java.util.*

internal class UserSettingsRepositoryRoomImpl internal constructor(context: Context) :
        UserSettingsRepository() {

    private val mDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    override fun getUserPreferenceDataFromLocalDB(): UserPreferenceData {
        var userPreferenceData:UserPreferenceData? = mDatabase.userPreferenceDataDao.findUserPreferenceStaticData()
        if(userPreferenceData == null) {
            userPreferenceData = UserPreferenceData(id=UUID.randomUUID().toString())
            mDatabase.userPreferenceDataDao.add(userPreferenceData)
        }
        LoggerUtils.debugLog("getUserPreferenceDataFromLocalDB: ${userPreferenceData}",this::class.java)
        return userPreferenceData
    }

    override fun saveUserPreferenceDataToLocalDb(userPreferenceData: UserPreferenceData) {
        mDatabase.userPreferenceDataDao.save(userPreferenceData)
    }

    override fun getLocalPreferenceData(): List<UserPreferenceData> {
        return mDatabase.userPreferenceDataDao.findAll()
    }

    override fun nukeUserPreferenceDataTable() {
        mDatabase.userPreferenceDataDao.nukeTable()
    }

    override fun addUserPreferenceDataToLocalDB(userPreferenceData:UserPreferenceData) {
        mDatabase.userPreferenceDataDao.add(userPreferenceData)
    }

    override fun getUserPreferenceLiveData(): LiveData<UserPreferenceData?> {
        return mDatabase.userPreferenceDataDao.findUserPreferenceDataLiveData()
    }

    override fun resetUserSettings() {
        mDatabase.userPreferenceDataDao.nukeTable()
    }
}
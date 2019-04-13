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

package com.dasbikash.news_server.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.display_models.entity.Newspaper
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.display_models.entity.PageGroup
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.exceptions.OnMainThreadException
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.SettingsRepository

class HomeViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private val mSettingsRepository: SettingsRepository
    private val mNewsDataRepository:NewsDataRepository
    private val mMostVisitedPages: LiveData<List<Page>>? = null
    private val mFavouritePages: LiveData<List<Page>>? = null
    private val mActiveNewspapers: LiveData<List<Newspaper>>? = null
    private val mPageGroups: LiveData<List<PageGroup>>? = null

    val isSettingsDataLoaded: Boolean
        get() = mSettingsRepository.isSettingsDataLoaded()

    val isAppSettingsUpdated: Boolean
        get() = mSettingsRepository.isAppSettingsUpdated()

    init {
        mSettingsRepository = SettingsRepository(mApplication)
        mNewsDataRepository = NewsDataRepository(mApplication)
    }

    override fun onCleared() {
        super.onCleared()
    }

    @Throws(OnMainThreadException::class, NoInternertConnectionException::class)
    fun loadAppSettings() {
        mSettingsRepository.loadAppSettings()
    }

    fun getNewsPapers():List<Newspaper>{
        return mSettingsRepository.getNewsPapers()
    }


}

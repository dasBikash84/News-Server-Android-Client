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
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


class PageViewViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    val TAG = "PageViewViewModel"

    private val mAppSettingsRepository: AppSettingsRepository
    private val mUserSettingsRepository: UserSettingsRepository
    private val mNewsDataRepository:NewsDataRepository

    private val disposable:CompositeDisposable = CompositeDisposable()

    init {
        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(mApplication)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(mApplication)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(mApplication)
    }

    fun getArticleLiveDataForPage(page: Page):LiveData<List<Article>>{
        return mNewsDataRepository.getArticleLiveDataForPage(page)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}

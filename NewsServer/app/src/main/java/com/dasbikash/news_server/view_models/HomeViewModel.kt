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
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.RepositoryFactory
import com.dasbikash.news_server_data.display_models.entity.*
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.SettingsRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


class HomeViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    val TAG = "HomeViewModel"

    private val mSettingsRepository: SettingsRepository
    private val mNewsDataRepository:NewsDataRepository
    private val mMostVisitedPages: LiveData<List<Page>>? = null
    private val mFavouritePages: LiveData<List<Page>>? = null
    private val mActiveNewspapers: LiveData<List<Newspaper>>? = null
    private val mPageGroups: LiveData<List<PageGroup>>? = null

    private val disposable:CompositeDisposable = CompositeDisposable();

    private val MAX_PARALLEL_ARTICLE_REQUEST = 5
    private var currentArticleRequestCount = AtomicInteger(0)


    init {
        mSettingsRepository = RepositoryFactory.getSettingsRepository(mApplication)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(mApplication)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun getNewsPapers():LiveData<List<Newspaper>>{
        return mSettingsRepository.getNewsPapers()
    }

    fun getUserPreferenceData():LiveData<UserPreferenceData>{
        return mSettingsRepository.getUserPreferenceData()
    }

    fun getLatestArticleProvider(requestPayload:Pair<UUID,Page>):Observable<Pair<UUID,Article?>>{
        return Observable.just(requestPayload)
                .subscribeOn(Schedulers.io())
                .map {
                    do {
                        Log.d(TAG,"Waiting for page: ${it.second.name}")
                        SystemClock.sleep(Random(System.currentTimeMillis()).nextLong(100L))
                    }while (currentArticleRequestCount.get() >= MAX_PARALLEL_ARTICLE_REQUEST)
                    Log.d(TAG,"Going to increment count for page: ${it.second.name}")
                    currentArticleRequestCount.incrementAndGet()
                    Log.d(TAG,"Request send for page: ${it.second.name}")
                    val article = mNewsDataRepository.getLatestArticleByPage(it.second)
                    Log.d(TAG,"Response received for page: ${it.second.name}")
                    Log.d(TAG,"Going to decrement count for page: ${it.second.name}")
                    currentArticleRequestCount.decrementAndGet()
                    Pair(it.first,article)
                }
    }


}

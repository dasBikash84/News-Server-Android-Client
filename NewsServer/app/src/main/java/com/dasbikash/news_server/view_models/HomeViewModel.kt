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
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
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


class HomeViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val mAppSettingsRepository: AppSettingsRepository
    private val mUserSettingsRepository: UserSettingsRepository
    private val mNewsDataRepository: NewsDataRepository

    private val disposable: CompositeDisposable = CompositeDisposable();

    private val MAX_PARALLEL_ARTICLE_REQUEST = 10
    private var currentArticleRequestCount = AtomicInteger(0)

    private val MIN_ARTICLE_REFRESH_INTERVAL = 60 * 1000L


    init {
        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(mApplication)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(mApplication)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(mApplication)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun getNewsPapersLiveData(): LiveData<List<Newspaper>> {
        return mAppSettingsRepository.getNewsPapersLiveData()
    }

    fun getUserPreferenceLiveData(): LiveData<UserPreferenceData?> {
        return mUserSettingsRepository.getUserPreferenceLiveData()
    }

    fun getPageGroupsLiveData(): LiveData<List<PageGroup>> {
        return mUserSettingsRepository.getPageGroupListLive()
    }

    fun getSavedArticlesLiveData(): LiveData<List<SavedArticle>> {
        return mNewsDataRepository.getAllSavedArticle()
    }

    fun getLatestArticleProvider(requestPayload: Pair<UUID, Page>): Observable<Pair<UUID, Article?>> {
        var amDisposed = false
        return Observable.just(requestPayload)
                .subscribeOn(Schedulers.io())
                .map {
                    Triple(it.first, it.second, mNewsDataRepository.getLatestArticleByPageFromLocalDb(it.second))
                }
                .map {
                    val input = it

                    it.third?.let {
                        if (System.currentTimeMillis() - it.created < MIN_ARTICLE_REFRESH_INTERVAL) {
                            return@map Pair(input.first, it)
                        }
                    }

                    do {
                        try {
                            Thread.sleep(Random(System.currentTimeMillis()).nextLong(100L))
                        } catch (ex: InterruptedException) {
                            if (!amDisposed) {
                                throw ex
                            }
                        }
                    } while (currentArticleRequestCount.get() >= MAX_PARALLEL_ARTICLE_REQUEST)

                    currentArticleRequestCount.incrementAndGet()

                    var article: Article? = null
                    try {
                        if (it.third != null) {
                            val newArticles = mNewsDataRepository.downloadNewArticlesByPage(it.second, it.third!!)
                            article = newArticles.sortedBy { it.publicationTime!!.time }.last()
                        } else {
                            article = mNewsDataRepository.getLatestArticleByPage(it.second)
                        }
                    } catch (ex: Exception) {
                        if (!amDisposed) {
                            throw ex
                        }
                    }
                    currentArticleRequestCount.decrementAndGet()
                    article?.let { return@map Pair(input.first, it) }
                    Pair(it.first, input.third)
                }.onErrorReturn {
                    currentArticleRequestCount.decrementAndGet()
                    throw it
                }.doOnDispose {
                    amDisposed = true
                }
    }


}

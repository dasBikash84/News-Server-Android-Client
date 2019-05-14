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
import com.dasbikash.news_server.view_controllers.NewspaperPerviewFragment
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
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

    val TAG = "HomeViewModel"

    private val mAppSettingsRepository: AppSettingsRepository
    private val mUserSettingsRepository: UserSettingsRepository
    private val mNewsDataRepository:NewsDataRepository

    private val disposable:CompositeDisposable = CompositeDisposable();

    private val MAX_PARALLEL_ARTICLE_REQUEST = 10
    private var currentArticleRequestCount = AtomicInteger(0)

    private val MIN_ARTICLE_REFRESH_INTERVAL = 5 * 60 * 1000L


    init {
        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(mApplication)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(mApplication)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(mApplication)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun getNewsPapers():LiveData<List<Newspaper>>{
        return mAppSettingsRepository.getNewsPapers()
    }

    fun getUserPreferenceData():LiveData<UserPreferenceData?>{
        return mUserSettingsRepository.getUserPreferenceLiveData()
    }

    fun getPageGroups():LiveData<List<PageGroup>>{
        return mUserSettingsRepository.getPageGroupListLive()
    }

    fun getLatestArticleProvider(requestPayload:Pair<UUID,Page>):Observable<Pair<UUID,Article?>>{
        var amDisposed = false
        return Observable.just(requestPayload)
                .subscribeOn(Schedulers.io())
                .map {
                    Triple(it.first,it.second,mNewsDataRepository.getLatestArticleByPageFromLocalDb(it.second))
                }
                .map {
                    val input = it
                    it.third?.let {
                        Log.d(NewspaperPerviewFragment.TAG,"Already dl art found for page: ${input.second.name} Np: ${input.second.newspaperId}")
                        if (System.currentTimeMillis() - it.getCreated() < MIN_ARTICLE_REFRESH_INTERVAL) {
                            return@map Pair(input.first,it)
                        }
                    }
                    Log.d(NewspaperPerviewFragment.TAG,"need to search art for page: ${input.second.name} Np: ${input.second.newspaperId}")
                    do {
                        Log.d(TAG,"Waiting for page: ${it.second.name}")
                        try {
                            Thread.sleep(Random(System.currentTimeMillis()).nextLong(100L))
                        }catch (ex:InterruptedException){
                            ex.printStackTrace()
                        }
                    }while (currentArticleRequestCount.get() >= MAX_PARALLEL_ARTICLE_REQUEST)
//                    Log.d(TAG+"1","Going to increment count for page: ${it.second.name}")
                    currentArticleRequestCount.incrementAndGet()
//                    Log.d(TAG+"1","Request send for page: ${it.second.name}")
                    Log.d(NewspaperPerviewFragment.TAG,"Going to search art for page: ${input.second.name} Np: ${input.second.newspaperId}")
                    var article:Article? = null
                    try {
                        article = mNewsDataRepository.getLatestArticleByPage(it.second)
                    }catch (ex:InterruptedException){
                        Log.d(NewspaperPerviewFragment.TAG,"InterruptedException Error in art search for page: ${input.second.name} Np: ${input.second.newspaperId}")
                        ex.printStackTrace()
                    }catch (ex:DataNotFoundException){
                        ex.printStackTrace()
                        Log.d(NewspaperPerviewFragment.TAG,"DataNotFoundException Error in art search for page: ${input.second.name} Np: ${input.second.newspaperId}")
                        if (!amDisposed){
                            throw ex
                        }
                    }
                    Log.d(NewspaperPerviewFragment.TAG,"art found for page: ${input.second.name} Np: ${input.second.newspaperId}")
//                    Log.d(TAG+"1","Response received for page: ${it.second.name}")
//                    Log.d(TAG+"1","Going to decrement count for page: ${it.second.name}")
                    currentArticleRequestCount.decrementAndGet()
                    article?.let { return@map Pair(input.first,it) }
                    Pair(it.first,input.third)
                }.onErrorReturn {
                    Log.d(TAG+"1","decrementAndGet after error:${it::class.java.canonicalName}")
                    currentArticleRequestCount.decrementAndGet()
                    throw it
                }.doOnDispose({
                    amDisposed = true
                })
    }


}

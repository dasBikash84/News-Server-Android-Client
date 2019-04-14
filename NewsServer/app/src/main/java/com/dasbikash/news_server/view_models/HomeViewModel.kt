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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.RepositoryFactory
import com.dasbikash.news_server_data.display_models.entity.Newspaper
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.display_models.entity.PageGroup
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.SettingsRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class HomeViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private val mSettingsRepository: SettingsRepository
    private val mNewsDataRepository:NewsDataRepository
    private val mMostVisitedPages: LiveData<List<Page>>? = null
    private val mFavouritePages: LiveData<List<Page>>? = null
    private val mActiveNewspapers: LiveData<List<Newspaper>>? = null
    private val mPageGroups: LiveData<List<PageGroup>>? = null

    val mArticleCountForNewspaperMap = mutableMapOf<Newspaper,LiveData<Int>>()

    private val mNewspapers = mutableListOf<Newspaper>()

    private val disposable:CompositeDisposable = CompositeDisposable();


    init {
        mSettingsRepository = RepositoryFactory.getSettingsRepository(mApplication)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(mApplication)

        //Create map of ArticleCountByNewspaper Livedata
        disposable.add(
            Observable.just(true)
                .subscribeOn(Schedulers.io())
                .map {
                    do {
                        SystemClock.sleep(100)
                    }while (getNewsPapers().value == null || getNewsPapers().value?.size==0)
                    getNewsPapers().value
                }
                .map {
                    mNewspapers.addAll(it)
                    it.forEach {
                        mArticleCountForNewspaperMap.put(it,mNewsDataRepository.getArticleCountByNewsPaper(it))
                    }
                }
                .subscribe()
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun getNewsPapers():LiveData<List<Newspaper>>{
        return mSettingsRepository.getNewsPapers()
    }


}

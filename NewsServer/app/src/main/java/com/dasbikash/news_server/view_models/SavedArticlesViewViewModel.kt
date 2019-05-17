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
import com.dasbikash.news_server_data.models.room_entity.SavedArticle
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory


class SavedArticlesViewViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val mNewsDataRepository:NewsDataRepository

    init {
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(mApplication)
    }

    fun getSavedArticlesLiveData():LiveData<List<SavedArticle>>{
        return mNewsDataRepository.getAllSavedArticle()
    }

}

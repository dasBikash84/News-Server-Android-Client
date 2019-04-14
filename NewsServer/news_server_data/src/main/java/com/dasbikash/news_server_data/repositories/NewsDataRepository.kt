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

package com.dasbikash.news_server_data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.display_models.entity.Article
import com.dasbikash.news_server_data.display_models.entity.Newspaper
import com.dasbikash.news_server_data.display_models.entity.Page

class NewsDataRepository internal constructor(context: Context) {
    private val mContext:Context
    private val newsDataService:NewsDataService
    private val newsServerDatabase:NewsServerDatabase
    init {
        mContext = context
        newsDataService = DataServiceImplProvider.getNewsDataServiceImpl()
        newsServerDatabase = NewsServerDatabase.getDatabase(mContext)
    }

    fun getLatestArticleByTopLevelPageId(topLevelPageId:String): Article{
        return newsDataService.getLatestArticleByTopLevelPageId(topLevelPageId)
    }
    //Latest articles from any page
    fun getLatestArticlesByPageId(pageId:String,
                                  articleRequestSize:Int? = null):List<Article>{
        articleRequestSize?.let {
            return newsDataService.getLatestArticlesByPageId(pageId,articleRequestSize = articleRequestSize)
        }
        return newsDataService.getLatestArticlesByPageId(pageId)
    }
    //Articles after last article ID
    fun getArticlesAfterLastId(pageId:String,lastArticleId:String,
                               articleRequestSize:Int? = null):List<Article>{
        articleRequestSize?.let {
            return newsDataService.getArticlesAfterLastId(pageId,lastArticleId,articleRequestSize = articleRequestSize)
        }
        return newsDataService.getArticlesAfterLastId(pageId,lastArticleId)
    }

    fun getTopPagesByNewsPaper(newspaper: Newspaper):List<Page>{
        return newsServerDatabase.pageDao.getTopPagesByNewsPaperId(newspaper.id)
    }

    fun getArticleCountByNewsPaper(newsPaper: Newspaper): LiveData<Int>{
        return newsServerDatabase.articleDao.getArticleCountByNewsPaperId(newsPaper.id)
    }
}
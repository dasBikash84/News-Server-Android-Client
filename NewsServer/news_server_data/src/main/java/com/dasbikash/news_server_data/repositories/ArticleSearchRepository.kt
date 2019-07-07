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
import com.dasbikash.news_server_data.data_sources.data_services.article_search_service.RealTimeDbArticleSearchService
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.models.room_entity.ArticleSearchKeyWord
import com.dasbikash.news_server_data.utills.ExceptionUtils

object ArticleSearchRepository {
    private const val ONE_DAY_IN_MS = 24*60*60*1000L
    private const val ONE_WEEK_IN_MS = 7 * ONE_DAY_IN_MS

    fun getMatchingSerachKeyWords(userInput:String,context: Context):List<String>{
        val newsServerDatabase = NewsServerDatabase.getDatabase(context)
        val articleSearchKeyWord = newsServerDatabase.articleSearchKeyWordDao.getFirst()
        if (articleSearchKeyWord == null ||
                (System.currentTimeMillis() - articleSearchKeyWord.created)> ONE_WEEK_IN_MS){
            val searchKeyWords = getSerachKeyWordsFromRemoteDb()
            newsServerDatabase.articleSearchKeyWordDao.addArticleSearchKeyWords(searchKeyWords.map { ArticleSearchKeyWord(id=it) })
        }
        return newsServerDatabase.articleSearchKeyWordDao.getMatchingSerachKeyWords("%${userInput.trim()}%").map { it.id }
    }

    private fun getSerachKeyWordsFromRemoteDb(): List<String> {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        return RealTimeDbArticleSearchService.getSerachKeyWords()
    }

    fun getKeyWordSerachResult(keyWord: String): Map<String, String> {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        return RealTimeDbArticleSearchService.getKeyWordSerachResult(keyWord)
    }
}
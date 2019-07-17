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
import com.dasbikash.news_server_data.data_sources.data_services.news_data_services.NewsDataServiceUtils
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.ArticleSearchKeyWord
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import java.lang.IllegalStateException

object ArticleSearchRepository {
    private const val ONE_DAY_IN_MS = 24*60*60*1000L
    private const val ONE_WEEK_IN_MS = 7 * ONE_DAY_IN_MS
    const val MINIMUM_KEYWORD_LENGTH = 3

    fun updateSerachKeyWordsIfRequired(context: Context):Boolean{
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        val newsServerDatabase = NewsServerDatabase.getDatabase(context)
        val articleSearchKeyWord = newsServerDatabase.articleSearchKeyWordDao.getFirst()
        if (articleSearchKeyWord == null ||
                (System.currentTimeMillis() - articleSearchKeyWord.created)> ONE_DAY_IN_MS){
            val searchKeyWords = getSerachKeyWordsFromRemoteDb()
            if (searchKeyWords.isNotEmpty()) {
                newsServerDatabase.articleSearchKeyWordDao.nukeTable()
                newsServerDatabase.articleSearchKeyWordDao.addArticleSearchKeyWords(searchKeyWords.map { ArticleSearchKeyWord(id = it) })
                return true
            }
        }
        return false
    }

    private fun getSerachKeyWordsFromRemoteDb(): List<String> {
        return RealTimeDbArticleSearchService.getSerachKeyWords()
    }

    fun getMatchingSerachKeyWords(userInput:String,context: Context):List<String>{
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()

        val matchingSerachKeyWords = mutableSetOf<ArticleSearchKeyWord>()

        getMatchingOnlyFromBeginning(userInput, context).apply {matchingSerachKeyWords.addAll(this)}
        getMatchingAll(userInput, context).apply {matchingSerachKeyWords.addAll(this)}

        if (matchingSerachKeyWords.isNotEmpty()) {
            return matchingSerachKeyWords.map { it.id }.toList()
        }
        return emptyList()
    }

    private fun getMatchingOnlyFromBeginning(userInput:String,context: Context):List<ArticleSearchKeyWord>{
        val newsServerDatabase = NewsServerDatabase.getDatabase(context)
        return newsServerDatabase.articleSearchKeyWordDao
                .getMatchingSerachKeyWords("${userInput.trim()}%")
    }

    private fun getMatchingAll(userInput:String,context: Context):List<ArticleSearchKeyWord>{
        val newsServerDatabase = NewsServerDatabase.getDatabase(context)
        return newsServerDatabase.articleSearchKeyWordDao
                .getMatchingSerachKeyWords("%${userInput.trim()}%")
    }

    fun getArticleSearchResultForKeyWords(context: Context,keyWords: List<String>):Map<String,String>{
        val searchKeyWords = mutableSetOf<String>()

        keyWords.filter { it.length>=MINIMUM_KEYWORD_LENGTH }.asSequence().forEach {
            searchKeyWords.addAll(getMatchingOnlyFromBeginning(it,context).map { it.id })
        }

        keyWords.filter { it.length>=MINIMUM_KEYWORD_LENGTH }.asSequence().forEach {
            searchKeyWords.addAll(getMatchingAll(it,context).map { it.id })
        }
        searchKeyWords.addAll(keyWords)
        LoggerUtils.debugLog("searchKeyWords: ${searchKeyWords}",this::class.java)
        val keyWordSerachResultMap = mutableMapOf<String,String>()
        searchKeyWords.asSequence().forEach {
            val searchResult = getKeyWordSerachResultFromRemoteDb(it)
            searchResult.keys.asSequence().forEach {
                if (!keyWordSerachResultMap.contains(it)){
                    keyWordSerachResultMap.put(it,searchResult.get(it)!!)
                }
            }
        }
        return keyWordSerachResultMap.toMap()
    }

    private fun getKeyWordSerachResultFromRemoteDb(keyWord: String): Map<String, String> {
        return RealTimeDbArticleSearchService.getKeyWordSerachResult(keyWord)
    }

    fun findArticleByIdAndPageId(articleId: String, pageId: String,context: Context):Pair<Article?,Page?> {
        val newsDataRepository = RepositoryFactory.getNewsDataRepository(context)
        newsDataRepository.findArticleByIdFromLocalDb(articleId)?.let {
            return Pair(it,findPageById(pageId,context))
        }
        val article =  newsDataRepository.findArticleByIdFromRemoteDb(articleId, pageId)
        article?.let {
            findPageById(pageId, context)?.let {
                NewsDataServiceUtils.processFetchedArticleData(article,it)
            }
        }
        return Pair(article,findPageById(pageId,context))
    }

    private fun findPageById(pageId: String,context: Context): Page? {
        val newsServerDatabase = NewsServerDatabase.getDatabase(context)
        return newsServerDatabase.pageDao.findById(pageId)
    }
}
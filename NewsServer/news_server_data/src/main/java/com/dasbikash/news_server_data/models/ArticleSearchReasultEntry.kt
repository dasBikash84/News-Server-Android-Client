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

package com.dasbikash.news_server_data.models

import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page

data class ArticleSearchReasultEntry(
        val articleId:String,
        val pageId:String,
        val publicationTime:Long,
        private val matchingKeyWords:MutableSet<String> = mutableSetOf()
){
    var article:Article?=null
    var page:Page?=null

    fun addMatchingKeyWord(keyWord: String){
        if (keyWord.isNotBlank()){
            matchingKeyWords.add(keyWord)
        }
    }

    fun getMatchingKeyWords():List<String> = matchingKeyWords.toList()

    override fun toString(): String {
        return "ArticleSearchReasultEntry(articleId='$articleId', pageId='$pageId', matchingKeyWords=$matchingKeyWords, article=$article, page=$page)"
    }

    companion object{

        fun getInstance(articleId:String,pageId:String, publicationTime:Long,matchingKeyWords:Collection<String>):
                ArticleSearchReasultEntry?{
            if (articleId.isNotBlank() && pageId.isNotBlank() && matchingKeyWords.isNotEmpty() &&
                    publicationTime >0 &&
                    (matchingKeyWords.filter { it.isNotBlank() }.count() == matchingKeyWords.size)) {
                val articleSearchReasultEntry = ArticleSearchReasultEntry(articleId, pageId,publicationTime)
                matchingKeyWords.forEach { articleSearchReasultEntry.addMatchingKeyWord(it) }
                return articleSearchReasultEntry
            }
            return null
        }
    }

}
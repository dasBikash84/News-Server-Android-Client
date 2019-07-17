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

data class ArticleSearchReasultEntry(
        val articleId:String,
        val pageId:String,
        private val matchingKeyWords:MutableSet<String> = mutableSetOf()
){
    fun addMatchingKeyWord(keyWord: String){
        if (keyWord.isNotBlank()){
            matchingKeyWords.add(keyWord)
        }
    }

    fun getMatchingKeyWords():List<String> = matchingKeyWords.toList()

    companion object{

        fun getInstance(articleId:String,pageId:String, matchingKeyWords:Collection<String>):
                ArticleSearchReasultEntry?{
            if (articleId.isNotBlank() && pageId.isNotBlank() && matchingKeyWords.isNotEmpty() &&
                    (matchingKeyWords.filter { it.isNotBlank() }.count() == matchingKeyWords.size)) {
                val articleSearchReasultEntry = ArticleSearchReasultEntry(articleId, pageId)
                matchingKeyWords.forEach { articleSearchReasultEntry.addMatchingKeyWord(it) }
                return articleSearchReasultEntry
            }
            return null
        }
    }
}
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

package com.dasbikash.news_server_data.models.room_converters

import com.dasbikash.news_server_data.database.room_converters.ArticleImageConverter
import com.dasbikash.news_server_data.models.room_entity.ArticleImage
import org.junit.After
import org.junit.Before
import org.junit.Test

class ArticleImageConverterTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testConversion(){
        val dataList = mutableListOf<ArticleImage>()
        dataList.add(ArticleImage("l1",""))
        dataList.add(ArticleImage("l2",""))
        dataList.add(ArticleImage("l3","c3"))
        println(ArticleImageConverter.fromArticleImage(dataList))
        println(ArticleImageConverter.toArticleImage(ArticleImageConverter.fromArticleImage(dataList)))
    }
}
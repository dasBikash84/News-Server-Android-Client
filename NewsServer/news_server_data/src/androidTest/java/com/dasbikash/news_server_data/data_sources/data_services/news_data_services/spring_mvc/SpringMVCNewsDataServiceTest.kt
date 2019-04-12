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

package com.dasbikash.news_server_data.data_sources.data_services.news_data_services.spring_mvc

import android.util.Log
import androidx.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpringMVCNewsDataServiceTest {

    val TAG = "DataServiceTest"

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getLatestArticleByTopLevelPageId() {
        Log.d(TAG, SpringMVCNewsDataService.getLatestArticleByTopLevelPageId("PAGE_ID_815").toString())
    }

    @Test
    fun getLatestArticlesByPageId() {
    }

    @Test
    fun getArticlesAfterLastId() {
    }
}
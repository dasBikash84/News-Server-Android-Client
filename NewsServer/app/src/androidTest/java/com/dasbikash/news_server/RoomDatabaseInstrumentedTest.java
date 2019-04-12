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

package com.dasbikash.news_server;

import android.content.Context;
import android.util.Log;

import com.dasbikash.news_server_data.repositories.NewsDataRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RoomDatabaseInstrumentedTest {

    private final String TAG = "DataServiceTest";

    Context context;
    NewsDataRepository newsDataRepository;

    @Before
    public void createDb() {
        context = ApplicationProvider.getApplicationContext();
        newsDataRepository = new NewsDataRepository(context);
    }


    @Test
    public void getLatestArticleByTopLevelPageIdTest(){
        Log.d(TAG, newsDataRepository.getLatestArticleByTopLevelPageId("PAGE_ID_815").toString());
    }

    @Test
    public void  getLatestArticlesByPageId() {
        Log.d(TAG, newsDataRepository.getLatestArticlesByPageId("PAGE_ID_814",5).toString());
    }

    @Test
    public void  getArticlesAfterLastId() {
        Log.d(TAG, newsDataRepository.getArticlesAfterLastId("PAGE_ID_814","-6645600438004371204",1).toString());
    }

}

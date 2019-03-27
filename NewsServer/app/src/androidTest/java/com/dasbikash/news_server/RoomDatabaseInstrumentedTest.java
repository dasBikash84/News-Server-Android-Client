/*
 * Copyright 2019 www.dasbikash.org. All rights reserved.
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

import androidx.test.core.app.ApplicationProvider;

import com.dasbikash.news_server.database.NewsServerDatabase;
import com.dasbikash.news_server.database.daos.ArticleDao;
import com.dasbikash.news_server.database.daos.CountryDao;
import com.dasbikash.news_server.database.daos.LanguageDao;
import com.dasbikash.news_server.database.daos.NewsCategoryDao;
import com.dasbikash.news_server.database.daos.NewsPaperDao;
import com.dasbikash.news_server.database.daos.PageDao;
import com.dasbikash.news_server.display_models.Article;
import com.dasbikash.news_server.display_models.Country;
import com.dasbikash.news_server.display_models.Language;
import com.dasbikash.news_server.display_models.NewsCategory;
import com.dasbikash.news_server.display_models.NewsCategoryEntry;
import com.dasbikash.news_server.display_models.Newspaper;
import com.dasbikash.news_server.display_models.Page;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.room.Room;
import androidx.test.runner.AndroidJUnit4;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RoomDatabaseInstrumentedTest {

    private final String TAG = "DbTest";

    private NewsServerDatabase mDatabase;

    private CountryDao mCountryDao;
    private LanguageDao mLanguageDao;
    private NewsPaperDao mNewsPaperDao;
    private PageDao mPageDao;
    private NewsCategoryDao mNewsCategoryDao;
    private ArticleDao mArticleDao;

    private Country mCountry;
    private Language mLanguage;
    private Newspaper mNewspaper;
    private Page mPage;
    private Page page2;
    private NewsCategory mNewsCategory;
    private Article mArticle;


    private void dataBootStrap(){

        mCountry = new Country("Bangladesh","BD","Asia/Dhaka");
        mLanguage = new Language(1,"Bangla-Bangladesh");
        mNewspaper = new Newspaper(1,"প্রথম আলো",mCountry.getName(),mLanguage.getId(),true);
        mPage = new Page(1,mNewspaper.getId(),0,"সর্বশেষ",true,false);
        page2 = new Page(2,mNewspaper.getId(),1,"বাংলাদেশ",true,false);

        mNewsCategory = new NewsCategory(1,"Cat 1",true,
                new NewsCategoryEntry(new ArrayList<>(Arrays.asList(mPage.getId(),page2.getId()))));

        mCountryDao.addCountry(mCountry);
        mLanguageDao.addLanguage(mLanguage);
        mNewsPaperDao.addNewsPaper(mNewspaper);
        mPageDao.addPage(mPage);
        mNewsCategoryDao.addNewsCategory(mNewsCategory);

    }

    @Before
    public void createDb() {

        Context context = ApplicationProvider.getApplicationContext();

        mDatabase = Room.inMemoryDatabaseBuilder(context, NewsServerDatabase.class).build();

        mCountryDao = mDatabase.getCountryDao();
        mLanguageDao = mDatabase.getLanguageDao();
        mNewsPaperDao = mDatabase.getNewsPaperDao();
        mPageDao = mDatabase.getPageDao();
        mNewsCategoryDao = mDatabase.getNewsCategoryDao();
        mArticleDao = mDatabase.getArticleDao();

        dataBootStrap();
    }

    @After
    public void closeDb() throws IOException {
        mDatabase.close();
    }

    @Test
    public void testCountryClass(){
        List<Country> countries = mCountryDao.findAll();
        assertThat(mCountry, equalTo(countries.get(0)));
    }

    @Test
    public void testLanguageClass(){
        List<Language> languages = mLanguageDao.findAll();
        assertThat(mLanguage, equalTo(languages.get(0)));
    }

    @Test
    public void testNewsPaperClass(){
        assertThat(mNewspaper, equalTo(mNewsPaperDao.findAll().get(0)));
        assertThat(mNewspaper, equalTo(mNewsPaperDao.findById(mNewspaper.getId())));
        mNewspaper.setActive(false);
        //assertThat(mNewspaper, equalTo(mNewsPaperDao.findById(mNewspaper.getId())));
        mNewsPaperDao.updateNewsPapers(mNewspaper);
        mNewspaper.setActive(true);
        mNewsPaperDao.addNewsPaper(mNewspaper);
        assertThat(mNewspaper, equalTo(mNewsPaperDao.findById(mNewspaper.getId())));
    }

    @Test
    public void testPageClass(){
        assertThat(mPage, equalTo(mPageDao.findAllActivePagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageDao.findAllTopLevelPagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageDao.findAllActiveTopLevelPagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageDao.findAllByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageDao.findById(mNewspaper.getId())));
        mPage.setActive(false);
        mPageDao.save(mPage);
        //assertThat(mPage, equalTo(mPageDao.findAllActivePagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageDao.findAllTopLevelPagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageDao.findAllByNewsPaperId(mNewspaper.getId()).get(0)));
        //assertThat(mPage, equalTo(mPageDao.findById(mNewspaper.getId())));
        //assertThat(mPage, equalTo(mPageDao.findAllActiveTopLevelPagesByNewsPaperId(mNewspaper.getId()).get(0)));

        Page page2 = new Page(2,mNewspaper.getId(),1,"বাংলাদেশ",true,false);
        mPage.setActive(true);
        mPageDao.addPages(Arrays.asList(mPage,page2));
        assertThat(page2, equalTo(mPageDao.findActiveChildrenByParentPageId(mPage.getId()).get(0)));
        assertThat(page2, equalTo(mPageDao.findChildrenByParentPageId(mPage.getId()).get(0)));

    }

    @Test
    public void testNewsCategoryClass(){
        //Log.d(TAG, "testNewsCategoryClass: "+mNewsCategoryDao.findAll());
        assertThat(mPage.getId(), equalTo(mNewsCategoryDao.findAll().get(0).getNewsCategoryEntry().getEntries().get(0)));
        assertThat(page2.getId(), equalTo(mNewsCategoryDao.findAll().get(0).getNewsCategoryEntry().getEntries().get(1)));
        assertThat(mPage.getId(), equalTo(mNewsCategoryDao.findById(mNewsCategory.getId()).getNewsCategoryEntry().getEntries().get(0)));
        assertThat(page2.getId(), equalTo(mNewsCategoryDao.findById(mNewsCategory.getId()).getNewsCategoryEntry().getEntries().get(1)));

        Page page3 = new Page(3,mNewspaper.getId(),1,"আন্তর্জাতিক",true,false);
        Log.d(TAG, "testNewsCategoryClass: "+mNewsCategoryDao.findAll());
        mNewsCategory.getNewsCategoryEntry().getEntries().add(page3.getId());
        mNewsCategoryDao.save(mNewsCategory);
        assertThat(mPage.getId(), equalTo(mNewsCategoryDao.findAll().get(0).getNewsCategoryEntry().getEntries().get(0)));
        assertThat(page2.getId(), equalTo(mNewsCategoryDao.findAll().get(0).getNewsCategoryEntry().getEntries().get(1)));
        assertThat(page3.getId(), equalTo(mNewsCategoryDao.findAll().get(0).getNewsCategoryEntry().getEntries().get(2)));
        assertThat(mPage.getId(), equalTo(mNewsCategoryDao.findById(mNewsCategory.getId()).getNewsCategoryEntry().getEntries().get(0)));
        assertThat(page2.getId(), equalTo(mNewsCategoryDao.findById(mNewsCategory.getId()).getNewsCategoryEntry().getEntries().get(1)));
        assertThat(page3.getId(), equalTo(mNewsCategoryDao.findById(mNewsCategory.getId()).getNewsCategoryEntry().getEntries().get(2)));
    }



}

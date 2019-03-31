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

import com.dasbikash.news_server.database.NewsServerDatabase;
import com.dasbikash.news_server.database.daos.ArticleBackEndDao;
import com.dasbikash.news_server.database.daos.CountryBackEndDao;
import com.dasbikash.news_server.database.daos.LanguageFrontEndDao;
import com.dasbikash.news_server.database.daos.NewsPaperFrontEndDao;
import com.dasbikash.news_server.database.daos.PageFrontEndDao;
import com.dasbikash.news_server.database.daos.PageGroupDao;
import com.dasbikash.news_server.database.daos.UserPreferenceDataDao;
import com.dasbikash.news_server.display_models.entity.Article;
import com.dasbikash.news_server.display_models.entity.Country;
import com.dasbikash.news_server.display_models.entity.Language;
import com.dasbikash.news_server.display_models.entity.Newspaper;
import com.dasbikash.news_server.display_models.entity.Page;
import com.dasbikash.news_server.display_models.entity.PageGroup;
import com.dasbikash.news_server.display_models.entity.UserPreferenceData;
import com.dasbikash.news_server.display_models.mapped_embedded.IntDataList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
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

    private CountryBackEndDao mCountryBackEndDao;
    private LanguageFrontEndDao mLanguageFrontEndDao;
    private NewsPaperFrontEndDao mNewsPaperFrontEndDao;
    private PageFrontEndDao mPageFrontEndDao;
    private PageGroupDao mPageGroupDao;
    private ArticleBackEndDao mArticleBackEndDao;

    private Country mCountry;
    private Language mLanguage;
    private Newspaper mNewspaper;
    private Page mPage;
    private Page page2;
    private PageGroup mPageGroup;
    private Article mArticle;


    private void dataBootStrap(){

        mCountry = new Country("Bangladesh","BD","Asia/Dhaka");
        mLanguage = new Language(1,"Bangla-Bangladesh");
        mNewspaper = new Newspaper(1,"প্রথম আলো",mCountry.getName(),mLanguage.getId(),true);
        //mPage = new Page(1,mNewspaper.getId(),0,"সর্বশেষ",true,false,0);
        //page2 = new Page(2,mNewspaper.getId(),1,"বাংলাদেশ",true,false,0);

       /* mPageGroup = new PageGroup(1,"Cat 1",true,
                new IntDataList(new ArrayList<>(Arrays.asList(mPage.getId(),page2.getId()))));*/

        /*mCountryBackEndDao.addCountry(mCountry);
        mLanguageFrontEndDao.addLanguage(mLanguage);
        getNewsPaperFrontEndDao.addNewsPaper(mNewspaper);
        mPageFrontEndDao.addPage(mPage);
        mPageGroupDao.addPageGroup(mPageGroup);*/

    }

    @Before
    public void createDb() {

        Context context = ApplicationProvider.getApplicationContext();

        mDatabase = Room.inMemoryDatabaseBuilder(context, NewsServerDatabase.class).build();

        /*mCountryBackEndDao = mDatabase.getCountryDao();
        mLanguageFrontEndDao = mDatabase.getLanguageDao();
        getNewsPaperFrontEndDao = mDatabase.getNewsPaperFrontEndDao();
        mPageFrontEndDao = mDatabase.getPageDao();
        mPageGroupDao = mDatabase.getPageGroupDao();
        mArticleBackEndDao = mDatabase.getArticleDao();*/

        //dataBootStrap();
    }

    @After
    public void closeDb() throws IOException {
        mDatabase.close();
    }

    /*@Test
    public void testCountryClass(){
        List<Country> countries = mCountryBackEndDao.findAll();
        assertThat(mCountry, equalTo(countries.get(0)));
    }

    @Test
    public void testLanguageClass(){
        List<Language> languages = mLanguageFrontEndDao.findAll();
        assertThat(mLanguage, equalTo(languages.get(0)));
    }

    @Test
    public void testNewsPaperClass(){
        assertThat(mNewspaper, equalTo(getNewsPaperFrontEndDao.findAll().get(0)));
        assertThat(mNewspaper, equalTo(getNewsPaperFrontEndDao.findById(mNewspaper.getId())));
        mNewspaper.setActive(false);
        //assertThat(mNewspaper, equalTo(getNewsPaperFrontEndDao.findById(mNewspaper.getId())));
        getNewsPaperFrontEndDao.updateNewsPapers(mNewspaper);
        mNewspaper.setActive(true);
        getNewsPaperFrontEndDao.addNewsPaper(mNewspaper);
        assertThat(mNewspaper, equalTo(getNewsPaperFrontEndDao.findById(mNewspaper.getId())));
    }

    @Test
    public void testPageClass(){
        assertThat(mPage, equalTo(mPageFrontEndDao.findAllActivePagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageFrontEndDao.findAllTopLevelPagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageFrontEndDao.findAllActiveTopLevelPagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageFrontEndDao.findAllByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageFrontEndDao.findById(mNewspaper.getId())));
        mPage.setActive(false);
        mPageFrontEndDao.save(mPage);
        //assertThat(mPage, equalTo(mPageFrontEndDao.findAllActivePagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageFrontEndDao.findAllTopLevelPagesByNewsPaperId(mNewspaper.getId()).get(0)));
        assertThat(mPage, equalTo(mPageFrontEndDao.findAllByNewsPaperId(mNewspaper.getId()).get(0)));
        //assertThat(mPage, equalTo(mPageFrontEndDao.findById(mNewspaper.getId())));
        //assertThat(mPage, equalTo(mPageFrontEndDao.findAllActiveTopLevelPagesByNewsPaperId(mNewspaper.getId()).get(0)));

        Page page2 = new Page(2,mNewspaper.getId(),1,"বাংলাদেশ",true,false,0);
        mPage.setActive(true);
        mPageFrontEndDao.addPages(Arrays.asList(mPage,page2));
        assertThat(page2, equalTo(mPageFrontEndDao.findActiveChildrenByParentPageId(mPage.getId()).get(0)));
        assertThat(page2, equalTo(mPageFrontEndDao.findChildrenByParentPageId(mPage.getId()).get(0)));

    }

    @Test
    public void testNewsCategoryClass(){
        //Log.d(TAG, "testNewsCategoryClass: "+mPageGroupDao.findAll());
        //assertThat(mPage.getId(), equalTo(mPageGroupDao.findAll().getValue().get(0).getPageList().getEntries().get(0)));
        //assertThat(page2.getId(), equalTo(mPageGroupDao.findAll().getValue().get(0).getPageList().getEntries().get(1)));
        assertThat(mPage.getId(), equalTo(mPageGroupDao.findById(mPageGroup.getId()).getPageList().getEntries().get(0)));
        assertThat(page2.getId(), equalTo(mPageGroupDao.findById(mPageGroup.getId()).getPageList().getEntries().get(1)));

        Page page3 = new Page(3,mNewspaper.getId(),1,"আন্তর্জাতিক",true,false,0);
        Log.d(TAG, "testNewsCategoryClass: "+ mPageGroupDao.findAll());
        mPageGroup.getPageList().getEntries().add(page3.getId());
        mPageGroupDao.save(mPageGroup);
        //assertThat(mPage.getId(), equalTo(mPageGroupDao.findAll().getValue().get(0).getPageList().getEntries().get(0)));
        //assertThat(page2.getId(), equalTo(mPageGroupDao.findAll().getValue().get(0).getPageList().getEntries().get(1)));
        //assertThat(page3.getId(), equalTo(mPageGroupDao.findAll().getValue().get(0).getPageList().getEntries().get(2)));
        assertThat(mPage.getId(), equalTo(mPageGroupDao.findById(mPageGroup.getId()).getPageList().getEntries().get(0)));
        assertThat(page2.getId(), equalTo(mPageGroupDao.findById(mPageGroup.getId()).getPageList().getEntries().get(1)));
        assertThat(page3.getId(), equalTo(mPageGroupDao.findById(mPageGroup.getId()).getPageList().getEntries().get(2)));
    }*/

    @Test
    public void testUserPreferenceDataTable(){
        UserPreferenceDataDao dao = mDatabase.getUserPreferenceDataDao();

        UserPreferenceData data =
                new UserPreferenceData(
                        1,
                        Arrays.asList(1,2,3),
                        Arrays.asList(4,5,6),
                        Arrays.asList(7,8,9)
                );

        Log.d(TAG, "testUserPreferenceDataTable In data: "+data);

        dao.add(data);

        Log.d(TAG, "testUserPreferenceDataTable read data: "+dao.findAll().get(0));
    }

    @Test
    public void showTs(){
        Log.d(TAG, "showTs: "+System.currentTimeMillis());
    }


}

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
import android.os.SystemClock;
import android.util.Log;

import com.dasbikash.news_server.database.NewsServerDatabase;
import com.dasbikash.news_server.database.daos.ArticleDao;
import com.dasbikash.news_server.database.daos.CountryDao;
import com.dasbikash.news_server.database.daos.LanguageDao;
import com.dasbikash.news_server.database.daos.NewsPaperDao;
import com.dasbikash.news_server.database.daos.PageDao;
import com.dasbikash.news_server.database.daos.PageGroupDao;
import com.dasbikash.news_server.display_models.entity.Article;
import com.dasbikash.news_server.display_models.entity.Country;
import com.dasbikash.news_server.display_models.entity.Language;
import com.dasbikash.news_server.display_models.entity.Newspaper;
import com.dasbikash.news_server.display_models.entity.Page;
import com.dasbikash.news_server.display_models.entity.PageGroup;
import com.dasbikash.news_server.utils.SharedPreferenceUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;

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

    private CountryDao mCountryDao;
    private LanguageDao mLanguageDao;
    private NewsPaperDao mNewsPaperDao;
    private PageDao mPageDao;
    private PageGroupDao mPageGroupDao;
    private ArticleDao mArticleDao;

    private Country mCountry;
    private Language mLanguage;
    private Newspaper mNewspaper;
    private Page mPage;
    private Page page2;
    private PageGroup mPageGroup;
    private Article mArticle;
    Context context;

    @Before
    public void createDb() {


        context = ApplicationProvider.getApplicationContext();

       /* mDatabase = Room.inMemoryDatabaseBuilder(context, NewsServerDatabase.class).build();

        mCountryDao = mDatabase.getCountryDao();
        mLanguageDao = mDatabase.getLanguageDao();
        mNewsPaperDao = mDatabase.getNewsPaperDao();
        mPageDao = mDatabase.getPageDao();
        mArticleDao = mDatabase.getArticleDao();
        dataBootStrap();*/
    }


    private void dataBootStrap() {

        mCountry = new Country("Bangladesh", "BD", "Asia/Dhaka");
//        mLanguage = new Language(1,"Bangla-Bangladesh");
//        mNewspaper = new Newspaper(1,"প্রথম আলো",mCountry.getName(),mLanguage.getId(),true);
        //mPage = new Page(1,mNewspaper.getId(),0,"সর্বশেষ",true);
//        mArticle = new Article(1,mPage.getId(),"Article 1",45432435L,Arrays.asList("image1","Image 2","Image 3"));

        mLanguageDao.addLanguages(Arrays.asList(mLanguage));
        mCountryDao.addCountries(Arrays.asList(mCountry));
        mNewsPaperDao.addNewsPapers(Arrays.asList(mNewspaper));
        mPageDao.addPages(Arrays.asList(mPage));
        mArticleDao.addArticles(Arrays.asList(mArticle));
    }

    @After
    public void closeDb() throws IOException {
        //mDatabase.close();
    }

    @Test
    public void  testForString() {
        SharedPreferenceUtils.saveData(context, "Test String", "test_string");
        SystemClock.sleep(1000);
        Log.d(TAG, SharedPreferenceUtils.getData(context, "", "test_string").toString());
    }

    @Test
    public void  testForLong() {
        SharedPreferenceUtils.saveData(context, 1245L, "test_long");
        SystemClock.sleep(1000);
        Log.d(TAG, "" + (Long) SharedPreferenceUtils.getData(context, 0L, "test_long"));
    }

    @Test
    public void  testForInt() {
        SharedPreferenceUtils.saveData(context, 12413125, "test_int");
        SystemClock.sleep(1000);
        Log.d(TAG, "" + (Integer) SharedPreferenceUtils.getData(context, 0, "test_int"));
    }

    @Test
    public void  testForFloat() {
        SharedPreferenceUtils.saveData(context, 123.23F, "test_Float");
        SystemClock.sleep(1000);
        Log.d(TAG, "" + (Float) SharedPreferenceUtils.getData(context, 0F, "test_Float"));
    }

    @Test
    public void  testForBoolean() {
        SharedPreferenceUtils.saveData(context, true, "test_Boolean");
        SystemClock.sleep(1000);
        Log.d(TAG, "" + (Boolean) SharedPreferenceUtils.getData(context, false, "test_Boolean"));
    }

    /*@Test
    public void testCountryClass(){
        List<Country> countries = mCountryBackEndDao.findAll();
        assertThat(mCountry, equalTo(countries.get(0)));
    }

    @Test
    public void testLanguageClass(){
        List<Language> languages = mLanguageDao.findAll();
        assertThat(mLanguage, equalTo(languages.get(0)));
    }

    @Test
    public void testNewsPaperClass(){
        assertThat(mNewspaper, equalTo(getNewsPaperDao.findAll().get(0)));
        assertThat(mNewspaper, equalTo(getNewsPaperDao.findById(mNewspaper.getId())));
        mNewspaper.setActive(false);
        //assertThat(mNewspaper, equalTo(getNewsPaperDao.findById(mNewspaper.getId())));
        getNewsPaperDao.updateNewsPapers(mNewspaper);
        mNewspaper.setActive(true);
        getNewsPaperDao.addNewsPaper(mNewspaper);
        assertThat(mNewspaper, equalTo(getNewsPaperDao.findById(mNewspaper.getId())));
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

        Page page2 = new Page(2,mNewspaper.getId(),1,"বাংলাদেশ",true,false,0);
        mPage.setActive(true);
        mPageDao.addPages(Arrays.asList(mPage,page2));
        assertThat(page2, equalTo(mPageDao.findActiveChildrenByParentPageId(mPage.getId()).get(0)));
        assertThat(page2, equalTo(mPageDao.findChildrenByParentPageId(mPage.getId()).get(0)));

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

    /*@Test
    public void testUserPreferenceDataTable() {
        UserPreferenceDataDao dao = mDatabase.getUserPreferenceDataDao();

        UserPreferenceData data =
                new UserPreferenceData(
                        1,
                        Arrays.asList(1, 2, 3),
                        Arrays.asList(4, 5, 6),
                        Arrays.asList(7, 8, 9)
                );

        Log.d(TAG, "testUserPreferenceDataTable In data: " + data);

        dao.add(data);

        Log.d(TAG, "testUserPreferenceDataTable read data: " + dao.findAll().get(0));
    }

    @Test
    public void testArticleTable() {

        Log.d(TAG, "testArticleTable In data: " + mArticle.toString());

        Log.d(TAG, "testArticleTable: " + mArticleDao.findId(1).toString());
    }

    @Test
    public void showTs() {
        Log.d(TAG, "showTs: " + System.currentTimeMillis());
    }*/


}

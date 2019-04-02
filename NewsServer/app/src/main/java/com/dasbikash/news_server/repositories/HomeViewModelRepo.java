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

package com.dasbikash.news_server.repositories;

import android.content.Context;
import android.util.Log;

import com.dasbikash.news_server.data_sources.data_services.DataService;
import com.dasbikash.news_server.database.NewsServerDatabase;
import com.dasbikash.news_server.database.daos.CountryDao;
import com.dasbikash.news_server.database.daos.LanguageDao;
import com.dasbikash.news_server.database.daos.NewsPaperDao;
import com.dasbikash.news_server.database.daos.PageDao;
import com.dasbikash.news_server.database.daos.PageGroupDao;
import com.dasbikash.news_server.display_models.entity.DefaultAppSettings;
import com.dasbikash.news_server.display_models.entity.Newspaper;
import com.dasbikash.news_server.display_models.entity.Page;
import com.dasbikash.news_server.display_models.entity.PageGroup;
import com.dasbikash.news_server.utils.SharedPreferenceUtils;
import com.dasbikash.news_server.utils.ToDoUtils;

import java.util.List;

import androidx.lifecycle.LiveData;

public final class HomeViewModelRepo {

    private static final String TAG = "InitFragment";

    private NewsServerDatabase mDatabase;

    private DataService mDataService;
    private Context mContext;


    public HomeViewModelRepo(final Context context) {
        mDatabase = NewsServerDatabase.getDatabase(context);
        mDataService = new DataService(context);
        mContext = context;
    }

    //All active newspaper
    /*public LiveData<List<Newspaper>> getAllActiveNewsPapers() {
        return mDatabase.getNewsPaperDao().findAllActive();
    }

    //All corresponding top level pages.
    public List<Page> getTopLevelPagesForNewspaper(Newspaper newspaper) {
        return mPageDao.findAllActiveTopLevelPagesByNewsPaperId(newspaper.getId());
    }

    //All corresponding children pages
    public List<Page> getActiveChildrenPagesForPage(Page page) {
        return mPageDao.findActiveChildrenByParentPageId(page.getId());
    }*/

    /*//List of most visited pages in descending order
    public LiveData<List<Page>> getMostVisitedPageList(){
        return mDatabase.getPageDao().getMostVisitedPageList();
    }*/

    //find list of favourite pages in ascending title order
    /*public LiveData<List<Page>> getFavouritePageList() {
        return null;//mDatabase.getPageDao().getFavouritePageList();
    }

    //All saved page group data

    public LiveData<List<PageGroup>> getAllPageGroups() {
        ToDoUtils.workToDo();
        return null;//mDatabase.getPageGroupDao().findAll();
    }*/

    public boolean isAppSettingsUpdated() {
        long localAppSettingsUpdateTime = getLocalAppSettingsUpdateTime();
        long serverAppSettingsUpdateTime = getServerAppSettingsUpdateTime();
        return serverAppSettingsUpdateTime > localAppSettingsUpdateTime;
    }

    private long getLocalAppSettingsUpdateTime() {
        long appSettingsUpdateTimestamp = SharedPreferenceUtils.getAppSettingsUpdateTimestamp(mContext);
        Log.d(TAG, "getLocalAppSettingsUpdateTime: "+appSettingsUpdateTimestamp);
        return appSettingsUpdateTimestamp;
    }

    private long getServerAppSettingsUpdateTime() {
        Long serverAppSettingsUpdateTime = mDataService.getServerAppSettingsUpdateTime();
        Log.d(TAG, "getServerAppSettingsUpdateTime: "+serverAppSettingsUpdateTime);
        SharedPreferenceUtils.saveGlobalSettingsUpdateTimestamp(mContext,serverAppSettingsUpdateTime);
        return serverAppSettingsUpdateTime;
    }

    public boolean isSettingsDataLoaded() {
        return getLanguageCount()>0 && getCountryCount()>0 &&
                getNewsPaperCount()>0 && getPageCount()>0 &&
                getPageGroupCount()>0;
    }

    private int getCountryCount() {
        int count = mDatabase.getCountryDao().getCount();
        Log.d(TAG, "getCountryCount: "+count);
        return count;
    }

    private int getLanguageCount() {
        int count = mDatabase.getLanguageDao().getCount();
        Log.d(TAG, "getLanguageCount: "+count);
        return count;
    }

    private int getNewsPaperCount() {
        int count = mDatabase.getNewsPaperDao().getCount();
        Log.d(TAG, "getNewsPaperCount: "+count);
        return count;
    }

    private int getPageCount() {
        int count = mDatabase.getPageDao().getCount();
        Log.d(TAG, "getPageCount: "+count);
        return count;
    }

    private int getPageGroupCount() {
        int count = mDatabase.getPageGroupDao().getCount();
        Log.d(TAG, "getPageGroupCount: "+count);
        return count;
    }

    public void loadAppSettings() {
        Log.d(TAG, "loadAppSettings: ");
        DefaultAppSettings appSettings =
                mDataService.getServerAppSettings();
        mDatabase.getLanguageDao().addLanguages(appSettings.getLanguages());
        mDatabase.getCountryDao().addCountries(appSettings.getCountries());
        mDatabase.getNewsPaperDao().addNewsPapers(appSettings.getNewspapers());
        mDatabase.getPageDao().addPages(appSettings.getPages());
        mDatabase.getPageGroupDao().addPageGroups(appSettings.getPage_groups());
    }
}

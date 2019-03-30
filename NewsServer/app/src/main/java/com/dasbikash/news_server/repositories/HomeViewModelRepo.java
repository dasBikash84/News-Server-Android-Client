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

import com.dasbikash.news_server.data_sources.data_services.DataService;
import com.dasbikash.news_server.database.NewsServerDatabase;
import com.dasbikash.news_server.database.daos.CountryFrontEndDao;
import com.dasbikash.news_server.database.daos.LanguageFrontEndDao;
import com.dasbikash.news_server.database.daos.NewsPaperFrontEndDao;
import com.dasbikash.news_server.database.daos.PageFrontEndDao;
import com.dasbikash.news_server.display_models.entity.Newspaper;
import com.dasbikash.news_server.display_models.entity.Page;
import com.dasbikash.news_server.display_models.entity.PageGroup;
import com.dasbikash.news_server.utils.ToDoUtils;

import java.util.List;

import androidx.lifecycle.LiveData;

public final class HomeViewModelRepo {

    private NewsServerDatabase mDatabase;

    private CountryFrontEndDao mCountryFrontEndDao;
    private LanguageFrontEndDao mLanguageFrontEndDao;
    private NewsPaperFrontEndDao mNewsPaperFrontEndDao;
    private PageFrontEndDao mPageFrontEndDao;

    private DataService mDataService;


    public HomeViewModelRepo(final Context context) {
        mDatabase = NewsServerDatabase.getDatabase(context);
        mNewsPaperFrontEndDao = mDatabase.getNewsPaperFrontEndDao();
        mCountryFrontEndDao = mDatabase.getCountryFrontEndDao();
        mLanguageFrontEndDao = mDatabase.getLanguageFrontEndDao();
        mPageFrontEndDao = mDatabase.getPageFrontEndDao();

        mDataService = new DataService(context);
    }

    public int getCountryCount() {
        return mCountryFrontEndDao.getCount();
    }

    public int getLanguageCount() {
        return mLanguageFrontEndDao.getCount();
    }

    public int getNewsPaperCount() {
        return mNewsPaperFrontEndDao.getCount();
    }

    public int getPageCount() {
        return mPageFrontEndDao.getCount();
    }

    //All active newspaper
    public LiveData<List<Newspaper>> getAllActiveNewsPapers() {
        return mDatabase.getNewsPaperFrontEndDao().findAllActive();
    }

    //All corresponding top level pages.
    public List<Page> getTopLevelPagesForNewspaper(Newspaper newspaper) {
        return mPageFrontEndDao.findAllActiveTopLevelPagesByNewsPaperId(newspaper.getId());
    }

    //All corresponding children pages
    public List<Page> getActiveChildrenPagesForPage(Page page) {
        return mPageFrontEndDao.findActiveChildrenByParentPageId(page.getId());
    }

    /*//List of most visited pages in descending order
    public LiveData<List<Page>> getMostVisitedPageList(){
        return mDatabase.getPageDao().getMostVisitedPageList();
    }*/

    //find list of favourite pages in ascending title order
    public LiveData<List<Page>> getFavouritePageList() {
        return null;//mDatabase.getPageDao().getFavouritePageList();
    }

    //All saved page group data

    public LiveData<List<PageGroup>> getAllPageGroups() {
        ToDoUtils.workToDo();
        return null;//mDatabase.getPageGroupDao().findAll();
    }

    public boolean isGlobalSettingsUpdated() {
        return mDataService.isGlobalSettingsUpdated();
    }

    public boolean isSettingsDataLoaded() {
        return getLanguageCount()>0 && getCountryCount()>0 &&
                getNewsPaperCount()>0 && getPageCount()>0;
    }
}

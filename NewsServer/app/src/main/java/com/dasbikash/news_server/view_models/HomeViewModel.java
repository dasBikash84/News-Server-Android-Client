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

package com.dasbikash.news_server.view_models;

import android.app.Application;

import com.dasbikash.news_server.display_models.entity.Newspaper;
import com.dasbikash.news_server.display_models.entity.Page;
import com.dasbikash.news_server.display_models.entity.PageGroup;
import com.dasbikash.news_server.repositories.HomeViewmodelRepo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class HomeViewModel extends AndroidViewModel {

    private Application mApplication;
    private HomeViewmodelRepo mHomeViewmodelRepo;
    private LiveData<List<Page>> mMostVisitedPages;
    private LiveData<List<Page>> mFavouritePages;
    private LiveData<List<Newspaper>> mActiveNewspapers;
    private LiveData<List<PageGroup>> mPageGroups;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mApplication = application;
        mHomeViewmodelRepo = new HomeViewmodelRepo(mApplication);
        //mMostVisitedPages = mHomeViewmodelRepo.getMostVisitedPageList();
        mFavouritePages = mHomeViewmodelRepo.getFavouritePageList();
        mActiveNewspapers = mHomeViewmodelRepo.getAllActiveNewsPapers();
        mPageGroups = mHomeViewmodelRepo.getAllPageGroups();
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     * <p>
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public LiveData<List<Page>> getMostVisitedPages() {
        return mMostVisitedPages;
    }

    public LiveData<List<Newspaper>> getActiveNewspapers() {
        return mActiveNewspapers;
    }

    //Top level pages for newspaper
    public List<Page> getTopLevelPagesForNewspaper(Newspaper newspaper){
        if (newspaper!=null) {
            return mHomeViewmodelRepo.getTopLevelPagesForNewspaper(newspaper);
        }
        return null;
    }

    //Top level pages for newspaper
    public List<Page> getActiveChildrenPagesForPage(Page page){
        if (page!=null) {
            return mHomeViewmodelRepo.getActiveChildrenPagesForPage(page);
        }
        return null;
    }

    public LiveData<List<Page>> getFavouritePages() {
        return mFavouritePages;
    }

    public LiveData<List<PageGroup>> getPageGroups() {
        return mPageGroups;
    }
}

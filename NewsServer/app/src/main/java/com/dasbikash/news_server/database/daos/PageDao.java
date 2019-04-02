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

package com.dasbikash.news_server.database.daos;

import com.dasbikash.news_server.display_models.entity.Page;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface PageDao {


    @Query("SELECT COUNT(*) FROM Page")
    public int getCount();


    //find one by PageId

    @Query("SELECT * FROM Page WHERE id=:pageId AND active")
    public Page findById(int pageId);

    //find all by NewsPaper (including in-actives)

    @Query("SELECT * FROM Page WHERE newsPaperId=:newsPaperId")
    public List<Page> findAllByNewsPaperId(int newsPaperId);

    //find all by Newspaper (only actives)

    @Query("SELECT * FROM Page WHERE newsPaperId=:newsPaperId AND active")
    public List<Page> findAllActivePagesByNewsPaperId(int newsPaperId);

    //find all top level page by NewsPaperId(only actives)

    @Query("SELECT * FROM Page WHERE newsPaperId=:newsPaperId AND parentPageId="+ Page.TOP_LEVEL_PAGE_PARENT_ID +" AND active")
    public List<Page> findAllActiveTopLevelPagesByNewsPaperId(int newsPaperId);

    //find all top level page by NewsPaperId(including in-actives)

    @Query("SELECT * FROM Page WHERE newsPaperId=:newsPaperId AND parentPageId="+ Page.TOP_LEVEL_PAGE_PARENT_ID)
    public List<Page> findAllTopLevelPagesByNewsPaperId(int newsPaperId);

    //find all child pages for top level page(only actives)
    @Query("SELECT * FROM Page WHERE parentPageId=:parentPageId AND active")
    public List<Page> findActiveChildrenByParentPageId(int parentPageId);

    //find all child pages for top level page(including in-actives)
    @Query("SELECT * FROM Page WHERE parentPageId=:parentPageId")
    public List<Page> findChildrenByParentPageId(int parentPageId);

    //find list of favourite pages

    //@Query("SELECT * FROM Page WHERE favourite ORDER BY title ASC")
    //public LiveData<List<Page>> getFavouritePageList();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addPages(List<Page> pages);

}

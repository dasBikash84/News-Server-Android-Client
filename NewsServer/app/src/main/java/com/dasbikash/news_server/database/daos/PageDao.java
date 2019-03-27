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

import com.dasbikash.news_server.display_models.Page;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addPage(Page page);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addPages(Page... pages);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addPages(List<Page> pages);

    @Update
    public void updatePage(Page page);

    //find all by NewsPaper (including in-actives)

    @Query("SELECT * FROM Page WHERE mNewsPaperId=:newsPaperId")
    public List<Page> findAllByNewsPaperId(int newsPaperId);

    //find one by PageId

    @Query("SELECT * FROM Page WHERE mId=:pageId AND mActive=true")
    public Page findById(int pageId);

    //find all by Newspaper (only actives)

    @Query("SELECT * FROM Page WHERE mNewsPaperId=:newsPaperId AND mActive=true")
    public List<Page> findAllActiveByNewsPaperId(int newsPaperId);

    //find all top level page by NewsPaperId

    @Query("SELECT * FROM Page WHERE mNewsPaperId=:newsPaperId AND mParentPageId="+Page.TOP_LEVEL_PAGE_PARENT_ID+" AND mActive=true")
    public List<Page> findAllTopLevelPageByNewsPaperId(int newsPaperId);

    //find all child pages for top level page
    @Query("SELECT * FROM Page WHERE mParentPageId=:parentPageId AND mActive=true")
    public List<Page> findChildrenByParentPageId(int parentPageId);
}

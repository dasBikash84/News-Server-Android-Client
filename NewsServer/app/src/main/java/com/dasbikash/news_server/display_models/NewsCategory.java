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

package com.dasbikash.news_server.display_models;

import java.io.Serializable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class NewsCategory implements Serializable{
    @PrimaryKey
    private int mId;
    private String mTitle;
    private boolean mActive;
    private NewsCategoryEntry mNewsCategoryEntry;

    public NewsCategory() {
    }

    @Ignore
    public NewsCategory(int id, String title, boolean active, NewsCategoryEntry newsCategoryEntry) {
        mId = id;
        mTitle = title;
        mActive = active;
        mNewsCategoryEntry = newsCategoryEntry;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setId(int id) {
        mId = id;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setActive(boolean active) {
        mActive = active;
    }

    public NewsCategoryEntry getNewsCategoryEntry() {
        return mNewsCategoryEntry;
    }

    public void setNewsCategoryEntry(NewsCategoryEntry newsCategoryEntry) {
        mNewsCategoryEntry = newsCategoryEntry;
    }
}

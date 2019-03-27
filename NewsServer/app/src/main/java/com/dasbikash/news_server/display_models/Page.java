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
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Newspaper.class,
                    parentColumns = "mId",
                    childColumns = "mId")
},
indices = {
        @Index("mNewsPaperId"),
        @Index("mParentPageId")
})
public final class Page implements Serializable{

    public static final int TOP_LEVEL_PAGE_PARENT_ID = 0;

    @PrimaryKey
    private int mId;
    private int mNewsPaperId;
    private int mParentPageId;
    private String mTitle;
    private boolean mActive;
    private boolean mFavourite;

    public Page(int id, int newsPaperId, int parentPageId, String title, boolean active, boolean favourite) {
        mId = id;
        mNewsPaperId = newsPaperId;
        mParentPageId = parentPageId;
        mTitle = title;
        mActive = active;
        mFavourite = favourite;
    }

    public int getId() {
        return mId;
    }

    public int getNewsPaperId() {
        return mNewsPaperId;
    }

    public int getParentPageId() {
        return mParentPageId;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isActive() {
        return mActive;
    }

    public boolean isFavourite() {
        return mFavourite;
    }

    @Ignore
    public boolean isTopLevelPage(){
        return mParentPageId == TOP_LEVEL_PAGE_PARENT_ID;
    }
}

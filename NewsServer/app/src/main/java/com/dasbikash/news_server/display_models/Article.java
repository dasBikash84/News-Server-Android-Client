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
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Page.class,
                    parentColumns = "mId",
                    childColumns = "mPageId")
})
public final class Article implements Serializable{
    @PrimaryKey
    private int mId;
    private int mPageId;
    private String mTitle;
    private long mLastModificationTS;
    private boolean mSavedLocally;
    private ImageLinkList mImageLinkList;

    public Article() {
    }

    public Article(int id, int pageId, String title,
                   long lastModificationTS, boolean savedLocally,
                   ImageLinkList imageLinkList) {
        mId = id;
        mPageId = pageId;
        mTitle = title;
        mLastModificationTS = lastModificationTS;
        mSavedLocally = savedLocally;
        mImageLinkList = imageLinkList;
    }

    public int getId() {
        return mId;
    }

    public int getPageId() {
        return mPageId;
    }

    public String getTitle() {
        return mTitle;
    }

    public long getLastModificationTS() {
        return mLastModificationTS;
    }

    public boolean isSavedLocally() {
        return mSavedLocally;
    }

    public void setId(int id) {
        mId = id;
    }

    public void setPageId(int pageId) {
        mPageId = pageId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setLastModificationTS(long lastModificationTS) {
        mLastModificationTS = lastModificationTS;
    }

    public void setSavedLocally(boolean savedLocally) {
        mSavedLocally = savedLocally;
    }

    public ImageLinkList getImageLinkList() {
        return mImageLinkList;
    }

    public void setImageLinkList(ImageLinkList imageLinkList) {
        mImageLinkList = imageLinkList;
    }
}

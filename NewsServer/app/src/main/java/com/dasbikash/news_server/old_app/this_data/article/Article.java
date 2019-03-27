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

package com.dasbikash.news_server.old_app.this_data.article;

import java.io.Serializable;

public final class Article implements Serializable{

    private int mId;
    private int mFeatureId;
    private String mLink;
    private int mLinkHashCode;
    private String mTitle;
    //private String mSummary;
    private int mPreviewImageId;
    private long mPublicationTS;
    private long mLastModificationTS;
    private boolean mSavedLocally;

    public Article(int id, int featureId, String link, int linkHashCode, String title,
                   int previewImageId, long publicationTS,
                   long lastModificationTS, boolean savedLocally) {
        mId = id;
        mFeatureId = featureId;
        mLink = link;
        mLinkHashCode = linkHashCode;
        mTitle = title;
        mPreviewImageId = previewImageId;
        mPublicationTS = publicationTS;
        mLastModificationTS = lastModificationTS;
        mSavedLocally = savedLocally;
    }

    public int getId() {
        return mId;
    }

    public int getFeatureId() {
        return mFeatureId;
    }

    public String getLink() {
        return mLink;
    }

    public int getLinkHashCode() {
        return mLinkHashCode;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getPreviewImageId() {
        return mPreviewImageId;
    }

    public long getPublicationTS() {
        return mPublicationTS;
    }

    public long getLastModificationTS() {
        return mLastModificationTS;
    }

    public boolean isSavedLocally() {
        return mSavedLocally;
    }
}

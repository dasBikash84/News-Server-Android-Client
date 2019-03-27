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

package com.dasbikash.news_server.old_app.this_data.feature;

import java.io.Serializable;

public final class Feature implements Serializable{

    private int mId;
    private int mNewsPaperId;
    private int mParentFeatureId;
    private String mTitle;
    private boolean mWeekly;
    private int mWeeklyPublicationDay;
    private String mLinkFormat;
    private String mLinkVariablePartFormat;
    private String mFirstEditionDateString;
    private boolean mActive;
    private boolean mFavourite;

    public Feature(int id, int newsPaperId, int parentFeatureId, String title, boolean weekly,
                   int weeklyPublicationDay, String linkFormat, String linkVariablePartFormat,
                   String firstEditionDateString,boolean active,boolean favourite) {
        mId = id;
        mNewsPaperId = newsPaperId;
        mParentFeatureId = parentFeatureId;
        mTitle = title;
        mWeekly = weekly;
        mWeeklyPublicationDay = weeklyPublicationDay;
        mLinkFormat = linkFormat;
        mLinkVariablePartFormat = linkVariablePartFormat;
        mFirstEditionDateString = firstEditionDateString;
        mActive = active;
        mFavourite = favourite;
    }

    public int getId() {
        return mId;
    }

    public int getNewsPaperId() {
        return mNewsPaperId;
    }

    public int getParentFeatureId() {
        return mParentFeatureId;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isWeekly() {
        return mWeekly;
    }

    public int getWeeklyPublicationDay() {
        return mWeeklyPublicationDay;
    }

    public String getLinkFormat() {
        return mLinkFormat;
    }

    public String getLinkVariablePartFormat() {
        return mLinkVariablePartFormat;
    }

    public String getFirstEditionDateString() {
        return mFirstEditionDateString;
    }

    public boolean isActive() {
        return mActive;
    }

    public boolean isFavourite() {
        return mFavourite;
    }
}

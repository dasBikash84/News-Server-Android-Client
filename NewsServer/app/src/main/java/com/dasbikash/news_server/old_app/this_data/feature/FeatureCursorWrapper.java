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

import android.database.Cursor;
import android.database.CursorWrapper;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

import static com.dasbikash.news_server.old_app.database.NewsServerDBSchema.IS_WEEKLY_FLAG;
import static com.dasbikash.news_server.old_app.database.NewsServerDBSchema.ITEM_ACTIVE_FLAG;

class FeatureCursorWrapper extends CursorWrapper {

    private static final String TAG = "FeatureCursorWrapper";

    FeatureCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    Feature getInstance() {

        try {
            int id = getInt(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.Id.NAME));
            int newspaperId = getInt(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.NewsPaperId.NAME));
            int parentFeatureId = getInt(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.ParentFeatureId.NAME));
            String title = getString(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.Title.NAME));
            boolean weekly = getInt(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.IsWeekly.NAME))== IS_WEEKLY_FLAG ? true:false;
            int weeklyPublicationDay = getInt(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.WeeklyPublicationDay.NAME));
            String linkFormat = getString(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.LinkFormat.NAME));
            String linkVariablePartFormat = getString(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.LinkVariablePartFormat.NAME));
            String firstEditionDateString = getString(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.FirstEditionDateString.NAME));
            int isActiveFlag = getInt(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME));
            boolean active = isActiveFlag == ITEM_ACTIVE_FLAG;
            int isFavouriteFlag = getInt(getColumnIndex(NewsServerDBSchema.FeatureTable.Cols.IsFavourite.NAME));
            boolean favourite = isFavouriteFlag == ITEM_ACTIVE_FLAG;

            if (id<1||
                newspaperId<1 ||
                title.trim().length()<1)   return null;


            return new Feature(id,newspaperId,parentFeatureId,title,
                                weekly,weeklyPublicationDay,linkFormat,
                                linkVariablePartFormat,firstEditionDateString,active,favourite);

        } catch (Exception ex){
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

}

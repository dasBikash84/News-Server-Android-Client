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

package com.dasbikash.news_server.old_app.this_data.notification_info;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

import java.util.ArrayList;

import static com.dasbikash.news_server.old_app.database.NewsServerDBSchema.ITEM_ACTIVE_FLAG;

class FeatureNotificationInfoCursorWrapper extends CursorWrapper {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "FeatureNotificationInfoCursorWrapper";

    FeatureNotificationInfoCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    FeatureNotificationInfo getInstance() {

        try {
            int featureId = getInt(getColumnIndex(NewsServerDBSchema.NotificationInfoTable.Cols.FeatureId.NAME));
            int isActiveFlag = getInt(getColumnIndex(NewsServerDBSchema.NotificationInfoTable.Cols.IsActive.NAME));
            boolean active = (isActiveFlag == ITEM_ACTIVE_FLAG ? true:false);
            String inclusionFilter = getString(getColumnIndex(NewsServerDBSchema.NotificationInfoTable.Cols.InclusionFilter.NAME));
            String exclusionFilter = getString(getColumnIndex(NewsServerDBSchema.NotificationInfoTable.Cols.ExclusionFilter.NAME));


            ArrayList<String> inclusionFilterKeywordList = new ArrayList<>();
            ArrayList<String> exclusionFilterKeywordList = new ArrayList<>();

            for (String inclusionKeyword :
                    inclusionFilter.split
                            (NewsServerDBSchema.NOTIFICATION_FILTER_INFO_SEPERATOR)) {
                inclusionFilterKeywordList.add(inclusionKeyword);
            }

            for (String exclusionKeyword :
                    exclusionFilter.split
                            (NewsServerDBSchema.NOTIFICATION_FILTER_INFO_SEPERATOR)) {
                exclusionFilterKeywordList.add(exclusionKeyword);
            }



            if (featureId<1)   return null;


            return new FeatureNotificationInfo(featureId,active,inclusionFilterKeywordList,exclusionFilterKeywordList);

        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}

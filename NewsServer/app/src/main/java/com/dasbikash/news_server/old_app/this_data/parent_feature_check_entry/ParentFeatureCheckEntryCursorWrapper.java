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

package com.dasbikash.news_server.old_app.this_data.parent_feature_check_entry;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

class ParentFeatureCheckEntryCursorWrapper extends CursorWrapper {

    private static final String TAG = "StackTrace";

    ParentFeatureCheckEntryCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    ParentFeatureCheckEntry getInstance() {

        try {
            int id = getInt(getColumnIndex(NewsServerDBSchema.ParentFeatureCheckLog.Cols.Id.NAME));
            long entryTimeStamp = getLong(getColumnIndex(NewsServerDBSchema.ParentFeatureCheckLog.Cols.EntryTs.NAME));
            int parentFeatureId = getInt(getColumnIndex(NewsServerDBSchema.ParentFeatureCheckLog.Cols.ParentFeatureId.NAME));
            int checkedFeatureId = getInt(getColumnIndex(NewsServerDBSchema.ParentFeatureCheckLog.Cols.CheckedFeatureId.NAME));
            boolean checkStatus =
                    getInt(getColumnIndex(NewsServerDBSchema.ParentFeatureCheckLog.Cols.CheckStatus.NAME)) ==
                            NewsServerDBSchema.POSITIVE_STATUS;


            if (id<1)   return null;

            return new ParentFeatureCheckEntry(id, entryTimeStamp,parentFeatureId,checkedFeatureId,checkStatus) ;

        } catch (Exception ex){
            ex.printStackTrace();
            //Log.d(TAG, "ConfigCheckSummaryEntryCursorWrapper: Error: "+ex.getMessage());
            return null;
        }
    }
}

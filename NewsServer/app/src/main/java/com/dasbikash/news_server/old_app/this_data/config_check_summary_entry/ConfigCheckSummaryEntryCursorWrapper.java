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

package com.dasbikash.news_server.old_app.this_data.config_check_summary_entry;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

class ConfigCheckSummaryEntryCursorWrapper extends CursorWrapper {

    private static final String TAG = "StackTrace";

    ConfigCheckSummaryEntryCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    ConfigCheckSummaryEntry getInstance() {

        try {
            int id = getInt(getColumnIndex(NewsServerDBSchema.ConfigIntegrityCheckReportTable.Cols.Id.NAME));
            long entryTimeStamp = getLong(getColumnIndex(NewsServerDBSchema.ConfigIntegrityCheckReportTable.Cols.EntryTs.NAME));
            String reportText = getString(getColumnIndex(NewsServerDBSchema.ConfigIntegrityCheckReportTable.Cols.ReportDetails.NAME));

            if (id<1)   return null;

            return new ConfigCheckSummaryEntry(id, entryTimeStamp,reportText) ;

        } catch (Exception ex){
            ex.printStackTrace();
            //Log.d(TAG, "ConfigCheckSummaryEntryCursorWrapper: Error: "+ex.getMessage());
            return null;
        }
    }
}

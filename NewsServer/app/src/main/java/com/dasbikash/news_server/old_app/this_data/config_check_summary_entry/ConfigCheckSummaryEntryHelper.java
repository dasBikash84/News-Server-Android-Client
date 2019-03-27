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
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

public abstract class ConfigCheckSummaryEntryHelper {

    private static final String TAG = "StackTrace";

    public static ArrayList<ConfigCheckSummaryEntry> getConfigCheckSummaryEntries(){

        ArrayList<ConfigCheckSummaryEntry>
                configCheckSummaryEntries = new ArrayList<>();

        String sqlForConfigCheckSummaryEntries =
                "SELECT * FROM "+NewsServerDBSchema.ConfigIntegrityCheckReportTable.NAME+
                " ORDER BY "+NewsServerDBSchema.ConfigIntegrityCheckReportTable.Cols.Id.NAME+" DESC;";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForConfigCheckSummaryEntries,null)){
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    configCheckSummaryEntries.add(new ConfigCheckSummaryEntryCursorWrapper(cursor).getInstance());
                    cursor.moveToNext();
                } while (!cursor.isAfterLast());
            }
        } catch (Exception ex){
            //Log.d(TAG,"ConfigCheckSummaryEntryHelper: Error: "+ex.getMessage());
            ex.printStackTrace();
        }

        return configCheckSummaryEntries;
    }
}

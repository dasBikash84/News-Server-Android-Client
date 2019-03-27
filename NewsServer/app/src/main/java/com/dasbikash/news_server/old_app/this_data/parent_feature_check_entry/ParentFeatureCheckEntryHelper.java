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
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

public abstract class ParentFeatureCheckEntryHelper {

    private static final String TAG = "StackTrace";

    public static ArrayList<ParentFeatureCheckEntry> getParentFeatureCheckEntries(){

        ArrayList<ParentFeatureCheckEntry>
                parentFeatureCheckEntries = new ArrayList<>();

        String sqlForParentFeatureCheckEntries =
                "SELECT * FROM "+NewsServerDBSchema.ParentFeatureCheckLog.NAME+
                        " ORDER BY "+NewsServerDBSchema.ParentFeatureCheckLog.Cols.Id.NAME+" DESC;";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForParentFeatureCheckEntries,null)){
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    parentFeatureCheckEntries.add(new ParentFeatureCheckEntryCursorWrapper(cursor).getInstance());
                    cursor.moveToNext();
                } while (!cursor.isAfterLast());
            }
        } catch (Exception ex){
            //Log.d(TAG,"ParentFeatureCheckEntryHelper: Error: "+ex.getMessage());
            ex.printStackTrace();
        }

        return parentFeatureCheckEntries;
    }
}

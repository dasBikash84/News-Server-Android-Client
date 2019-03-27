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
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

public abstract class FeatureNotificationInfoHelper {

    private static final String TAG = "FeatureNotificationInfoHelper";

    private static ArrayList<FeatureNotificationInfo> getFeatureNotificationInfoBySql(String sqlForFeatureNotificationInfo){

        ArrayList<FeatureNotificationInfo> featureNotificationInfoList = new ArrayList<>();

        if (sqlForFeatureNotificationInfo == null) return featureNotificationInfoList;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForFeatureNotificationInfo,null)){
            if (cursor.getCount() == 0){
                return featureNotificationInfoList;
            }
            cursor.moveToFirst();
            do {
                featureNotificationInfoList.add(new FeatureNotificationInfoCursorWrapper(cursor).getInstance());
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
        } catch (Exception ex){
            //Log.d(TAG, "getInstance: Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        return featureNotificationInfoList;

    }

    public static ArrayList<FeatureNotificationInfo> getAllFeatureNotificationInfo(){

        String sqlForAllFeatureNotificationInfo =
                "SELECT * FROM "+ NewsServerDBSchema.NotificationInfoTable.NAME;

        return getFeatureNotificationInfoBySql(sqlForAllFeatureNotificationInfo);
    }

    public static ArrayList<FeatureNotificationInfo> getAllActiveFeatureNotificationInfo(){

        String sqlForAllActiveFeatureNotificationInfo =
                "SELECT * FROM "+ NewsServerDBSchema.NotificationInfoTable.NAME+
                " WHERE "+
                NewsServerDBSchema.NotificationInfoTable.Cols.IsActive.NAME+
                " = "+
                NewsServerDBSchema.ITEM_ACTIVE_FLAG;

        return getFeatureNotificationInfoBySql(sqlForAllActiveFeatureNotificationInfo);
    }

}

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

package com.dasbikash.news_server.old_app.this_data.feature_group_entry;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroup;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

public abstract class FeatureGroupEntryHelper {

    private static final String TAG = "FeatureGroupHelper";

    private static ArrayList<FeatureGroupEntry> getFeatureGroupEntrysBySql(String sqlForFeatureGroupEntries){

        ArrayList<FeatureGroupEntry> featureGroupEntries = new ArrayList<>();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForFeatureGroupEntries,null)){
            if (cursor.getCount() == 0){
                return featureGroupEntries;
            }
            cursor.moveToFirst();
            do {
                featureGroupEntries.add(new FeatureGroupEntryCursorWrapper(cursor).getInstance());
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
        } catch (Exception ex){
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
        }

        return featureGroupEntries;
    }

    public static ArrayList<FeatureGroupEntry> getEntriesForFeatureGroup(FeatureGroup featureGroup){

        String sqlForFeatureGroupEntries =
                "SELECT * FROM "+
                NewsServerDBSchema.FeatureGroupEntryTable.NAME+
                " WHERE "+
                NewsServerDBSchema.FeatureGroupEntryTable.Cols.GroupId.NAME+
                " = "+
                featureGroup.getId()+
                " ORDER BY "+NewsServerDBSchema.FeatureGroupEntryTable.Cols.Id.NAME;

        return getFeatureGroupEntrysBySql(sqlForFeatureGroupEntries);
    }

    public static Feature getFeatureFromFeatureGroupEntry(FeatureGroupEntry featureGroupEntry){
        if (featureGroupEntry == null) return null;
        return FeatureHelper.findActiveFeatureById(featureGroupEntry.getFeatureId());
    }

    public static boolean addFeatureToNewspaperHome(Feature feature, Newspaper newspaper) {
        if (feature == null || newspaper == null) return false;
        FeatureGroup newspaperHomeFeatureGroup =
                FeatureGroupHelper.getFeatureGroupForNewsPaperHomePage(newspaper);
        if (newspaperHomeFeatureGroup == null) return false;

        return addFeatureToFeatureGroup(feature,newspaperHomeFeatureGroup);
    }

    public static boolean addFeatureToAppHome(Feature feature) {

        if (feature == null) return false;

        //Log.d("NSUtility", "addFeatureToAppHome: feature.getTitle(): "+feature.getTitle());

        FeatureGroup appHomeFeatureGroup =
                FeatureGroupHelper.getFeatureGroupForHomePage();
        if (appHomeFeatureGroup == null) return false;

        return addFeatureToFeatureGroup(feature,appHomeFeatureGroup);
    }

    public static boolean addFeatureToFeatureGroup(Feature feature,FeatureGroup featureGroup){

        if (feature == null ||
                featureGroup == null){
            return false;
        }

        String sqlToAddFeatureOnFeatureGroup =
                "INSERT INTO "+ NewsServerDBSchema.FeatureGroupEntryTable.NAME+
                        " ("+NewsServerDBSchema.FeatureGroupEntryTable.Cols.GroupId.NAME+
                        ","+NewsServerDBSchema.FeatureGroupEntryTable.Cols.MemberFeatureId.NAME+
                        ") VALUES ("+
                        featureGroup.getId()+","+feature.getId()+");";

        //Log.d("NSUtility", "addFeatureToAppHome: sqlToAddOnNewspaperHome: "+sqlToAddOnNewspaperHome);

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlToAddFeatureOnFeatureGroup);
            return true;
            //Log.d("NSUtility", "addFeatureToAppHome: Added to app home");
        } catch (Exception ex){
            //Log.d("NSUtility", "addFeatureToNewspaperHome: Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean removeFeatureFromNewspaperHomePage(Feature feature, Newspaper newspaper) {

        if (feature == null || newspaper == null) return false;

        FeatureGroup newspaperHomeFeatureGroup =
                FeatureGroupHelper.getFeatureGroupForNewsPaperHomePage(newspaper);
        if (newspaperHomeFeatureGroup == null) return false;

        return removeFeatureFromFeatureGroup(feature,newspaperHomeFeatureGroup);
    }

    public static boolean removeFeatureFromFeatureGroup(Feature feature,FeatureGroup featureGroup){

        if (feature == null || featureGroup == null) return false;

        String sqlToRemoveFeatureFromFeatureGroup =
                "DELETE FROM "+ NewsServerDBSchema.FeatureGroupEntryTable.NAME+
                " WHERE "+NewsServerDBSchema.FeatureGroupEntryTable.Cols.GroupId.NAME+" = "+
                featureGroup.getId()+
                " AND "+NewsServerDBSchema.FeatureGroupEntryTable.Cols.MemberFeatureId.NAME+" = "+
                feature.getId();
        //Log.d("NSUtility", "removeFeatureFromNewspaperHomePage: sqlToRemoveFeatureFromFeatureGroup: "+
                //sqlToRemoveFeatureFromFeatureGroup);
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlToRemoveFeatureFromFeatureGroup);
            return true;
        } catch (Exception ex){
            //Log.d("NSUtility", "addFeatureToNewspaperHome: Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }



    public static boolean removeAllFeaturesFromFeatureGroup(FeatureGroup featureGroup){

        if (featureGroup == null){
            //Log.d("StackTrace", "Error: 1");
            return false;
        }

        String sqlToRemoveFeatureFromFeatureGroup =
                "DELETE FROM "+ NewsServerDBSchema.FeatureGroupEntryTable.NAME+
                " WHERE "+NewsServerDBSchema.FeatureGroupEntryTable.Cols.GroupId.NAME+" = "+
                featureGroup.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlToRemoveFeatureFromFeatureGroup);
            return true;
        } catch (Exception ex){
            //Log.d("StackTrace", "addFeatureToNewspaperHome: Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }


}

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

package com.dasbikash.news_server.old_app.this_data.feature_group;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.feature_group_entry.FeatureGroupEntry;
import com.dasbikash.news_server.old_app.this_data.feature_group_entry.FeatureGroupEntryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import java.util.ArrayList;

public abstract class FeatureGroupHelper {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "FeatureGroupHelper";

    public enum NEWS_CATEGORY_EDIT_REQUEST_RESULT {
        EMPTY_NAME,
        ALREADY_EXISTS,
        ERROR,
        SUCCESS,
        INVALID_NAME
    }

    private static ArrayList<FeatureGroup> getFeatureGroupsBySql(String sqlForFeatureGroups){

        ArrayList<FeatureGroup> featureGroups = new ArrayList<>();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForFeatureGroups,null)){
            if (cursor.getCount() == 0){
                return featureGroups;
            }
            cursor.moveToFirst();
            do {
                featureGroups.add(new FeatureGroupCursorWrapper(cursor).getInstance());
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
        } catch (Exception ex){
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
        }

        return featureGroups;
    }

    public static FeatureGroup getFeatureGroupForHomePage(){

        String sqlForHomeFeatureGroup =
                "SELECT * FROM "+
                NewsServerDBSchema.FeatureGroupTable.NAME+
                " WHERE "+
                NewsServerDBSchema.FeatureGroupTable.Cols.GroupCategoryidentifier.NAME+
                " = "+
                NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_HOMEPAGE;

        ArrayList<FeatureGroup> featureGroups = getFeatureGroupsBySql(sqlForHomeFeatureGroup);

        if (featureGroups.size() == 1){
            return featureGroups.get(0);
        }

        return null;
    }

    public static FeatureGroup getFeatureGroupForNewsPaperHomePage(Newspaper newspaper){

        if (newspaper == null) return null;

        String sqlForNewspaperHomeFeatureGroup =
                "SELECT * FROM "+
                NewsServerDBSchema.FeatureGroupTable.NAME+
                " WHERE "+
                NewsServerDBSchema.FeatureGroupTable.Cols.GroupCategoryidentifier.NAME+
                " = "+
                newspaper.getId();

        ArrayList<FeatureGroup> featureGroups = getFeatureGroupsBySql(sqlForNewspaperHomeFeatureGroup);

        if (featureGroups.size() == 1){
            return featureGroups.get(0);
        }

        return null;
    }

    public static FeatureGroup findFeatureGroupByTitle(String title){

        if (title == null || title.trim().length()==0) return null;

        String sqlForNewspaperHomeFeatureGroup =
                "SELECT * FROM "+
                NewsServerDBSchema.FeatureGroupTable.NAME+
                " WHERE "+
                NewsServerDBSchema.FeatureGroupTable.Cols.Title.NAME+
                " = '"+title+"'";

        ArrayList<FeatureGroup> featureGroups = getFeatureGroupsBySql(sqlForNewspaperHomeFeatureGroup);

        if (featureGroups.size() == 1){
            return featureGroups.get(0);
        }

        return null;
    }

    public static ArrayList<FeatureGroup> getCustomFeatureGroups(){

        String sqlForCustomFeatureGroups =
                "SELECT * FROM "+
                NewsServerDBSchema.FeatureGroupTable.NAME+
                " WHERE "+
                NewsServerDBSchema.FeatureGroupTable.Cols.GroupCategoryidentifier.NAME+
                " = "+
                NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP+
                " AND "+
                NewsServerDBSchema.FeatureGroupTable.Cols.IsActive.NAME+
                " = "+
                NewsServerDBSchema.ITEM_ACTIVE_FLAG+
                " ORDER BY "+NewsServerDBSchema.FeatureGroupTable.Cols.Title.NAME;

        return getFeatureGroupsBySql(sqlForCustomFeatureGroups);
    }

    public static ArrayList<FeatureGroup> getAllCustomFeatureGroups(){

        String sqlForCustomFeatureGroups =
                "SELECT * FROM "+
                NewsServerDBSchema.FeatureGroupTable.NAME+
                " WHERE "+
                NewsServerDBSchema.FeatureGroupTable.Cols.GroupCategoryidentifier.NAME+
                " = "+
                NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP;

        return getFeatureGroupsBySql(sqlForCustomFeatureGroups);
    }

    public static boolean checkIfOnHomeGroup(Feature feature){
        if (feature == null) return false;
        FeatureGroup homeFeatureGroup = getFeatureGroupForHomePage();
        if (homeFeatureGroup == null) return false;

        String sqlToCheckHomeGroup =
                "SELECT * FROM "+NewsServerDBSchema.FeatureGroupEntryTable.NAME+
                " WHERE "+NewsServerDBSchema.FeatureGroupEntryTable.Cols.GroupId.NAME+
                " = "+homeFeatureGroup.getId()+ " AND "+
                NewsServerDBSchema.FeatureGroupEntryTable.Cols.MemberFeatureId.NAME+
                " = "+feature.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlToCheckHomeGroup,null)){
            if (cursor.getCount() > 0)  return true;
        } catch (Exception e){
            //Log.d(TAG, "checkIfOnHomeGroup: Error: "+e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public static boolean checkIfOnNewspaperHomeGroup(Feature feature){
        if (feature == null) return false;
        Newspaper newspaper = NewspaperHelper.findNewspaperById(feature.getNewsPaperId());
        if (newspaper == null) return false;
        FeatureGroup newspaperHomeGroup = getFeatureGroupForNewsPaperHomePage(newspaper);
        if (newspaperHomeGroup == null) return false;

        String sqlToCheckHomeGroup =
                "SELECT * FROM "+NewsServerDBSchema.FeatureGroupEntryTable.NAME+
                        " WHERE "+NewsServerDBSchema.FeatureGroupEntryTable.Cols.GroupId.NAME+
                        " = "+newspaperHomeGroup.getId()+ " AND "+
                        NewsServerDBSchema.FeatureGroupEntryTable.Cols.MemberFeatureId.NAME+
                        " = "+feature.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlToCheckHomeGroup,null)){
            if (cursor.getCount() > 0)  return true;
        } catch (Exception e){
            //Log.d(TAG, "checkIfOnHomeGroup: Error: "+e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public static ArrayList<Integer> getFeatureIdsForFeatureGroup(FeatureGroup featureGroup){

        ArrayList<Integer> featureIdList = new ArrayList<>();

        if (featureGroup == null) return featureIdList;

        ArrayList<FeatureGroupEntry> featureGroupEntries =
                FeatureGroupEntryHelper.getEntriesForFeatureGroup(featureGroup);
        if (featureGroupEntries.size()==0) return featureIdList;

        for (FeatureGroupEntry featureGroupEntry :
                featureGroupEntries) {
            if (featureGroupEntry != null) {
                featureIdList.add(featureGroupEntry.getFeatureId());
            }
        }

        return featureIdList;
    }

    public static ArrayList<Integer> getFeatureIdsForNewspaperHomeFeatureGroup(Newspaper newspaper){

        ArrayList<Integer> featureIdList = new ArrayList<>();
        if (newspaper == null) return featureIdList;

        FeatureGroup featureGroup = getFeatureGroupForNewsPaperHomePage(newspaper);

        featureIdList = getFeatureIdsForFeatureGroup(featureGroup);

        return featureIdList;
    }

    private static boolean setFeatureGroupActivityStatus(FeatureGroup featureGroup, int status){
        if (featureGroup.getCategoryIdentifier() !=
                NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP){
            return false;
        }
        String sqlToChangeActivityStatus =
                "UPDATE "+NewsServerDBSchema.FeatureGroupTable.NAME+
                " SET "+NewsServerDBSchema.FeatureGroupTable.Cols.IsActive.NAME+" = "+
                status+ " WHERE "+
                NewsServerDBSchema.FeatureGroupTable.Cols.Id.NAME+" = "+
                featureGroup.getId();
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        try {
            dbCon.execSQL(sqlToChangeActivityStatus);
            return true;
        } catch (SQLException e) {
            //Log.d(TAG, "setFeatureGroupActivityStatus: Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean activateFeatureGroup(FeatureGroup featureGroup) {
        if (featureGroup == null) return false;
        if (featureGroup.isActive()) return true;
        return setFeatureGroupActivityStatus(featureGroup,NewsServerDBSchema.ITEM_ACTIVE_FLAG);
    }

    public static boolean deactivateFeatureGroup(FeatureGroup featureGroup) {
        if (featureGroup == null) return false;
        if (!featureGroup.isActive())   return true;
        return setFeatureGroupActivityStatus(featureGroup,NewsServerDBSchema.ITEM_INACTIVE_FLAG);
    }

    public static boolean deleteFeatureGroup(FeatureGroup featureGroup) {
        if (featureGroup == null || featureGroup.getCategoryIdentifier() !=
                NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP){
            return false;
        }

        if (!FeatureGroupEntryHelper.removeAllFeaturesFromFeatureGroup(featureGroup)){
            return false;
        }

        String sqlToChangeActivityStatus =
                "DELETE FROM "+NewsServerDBSchema.FeatureGroupTable.NAME+
                        " WHERE "+
                        NewsServerDBSchema.FeatureGroupTable.Cols.Id.NAME+" = "+
                        featureGroup.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlToChangeActivityStatus);
            return true;
        } catch (SQLException e) {
            //Log.d("StackTrace", "setFeatureGroupActivityStatus: Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }

    }

    private static boolean findGroupByName(String featureGroupName) {

        String sqlToFindGroup =
                "SELECT * FROM "+NewsServerDBSchema.FeatureGroupTable.NAME+
                " WHERE "+NewsServerDBSchema.FeatureGroupTable.Cols.Title.NAME+
                " = '"+featureGroupName.trim()+"'";
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        //Log.d(TAG, "findGroupByName: ");

        try(Cursor cursor = dbCon.rawQuery(sqlToFindGroup,null)){
            if (cursor.getCount() > 0){
                return true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public static NEWS_CATEGORY_EDIT_REQUEST_RESULT createCustomNewsCategory(String featureGroupName){

        if (featureGroupName ==null ||
                featureGroupName.trim().length() == 0){
            return NEWS_CATEGORY_EDIT_REQUEST_RESULT.EMPTY_NAME;
        }

        if (!DisplayUtility.checkIfValidTextInput(featureGroupName)){
            return NEWS_CATEGORY_EDIT_REQUEST_RESULT.INVALID_NAME;
        }

        if (findGroupByName(featureGroupName)){
            return NEWS_CATEGORY_EDIT_REQUEST_RESULT.ALREADY_EXISTS;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(NewsServerDBSchema.FeatureGroupTable.Cols.Title.NAME, featureGroupName);

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        int insertedId = -1;

        try{
            insertedId =  (int)dbCon.insert(NewsServerDBSchema.FeatureGroupTable.NAME, null, contentValues);
        } catch (Exception ex){
            //Log.d(TAG, "createCustomNewsCategory: Error: "+ex.getMessage());
            ex.printStackTrace();
        }

        if (insertedId != -1){
            return NEWS_CATEGORY_EDIT_REQUEST_RESULT.SUCCESS;
        } else {
            return NEWS_CATEGORY_EDIT_REQUEST_RESULT.ERROR;
        }
    }

    public static NEWS_CATEGORY_EDIT_REQUEST_RESULT renameCustomNewsCategory(FeatureGroup featureGroup,String newName) {

        String sqlToCheckIfHaveEntryWithSameName =
                "SELECT * FROM "+NewsServerDBSchema.FeatureGroupTable.NAME+
                " WHERE "+NewsServerDBSchema.FeatureGroupTable.Cols.Title.NAME+
                " = '"+newName.trim()+"'";
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try(Cursor cursor = dbCon.rawQuery(sqlToCheckIfHaveEntryWithSameName,null)){
            if (cursor.getCount()>0){
                return NEWS_CATEGORY_EDIT_REQUEST_RESULT.ALREADY_EXISTS;
            }
        }catch (Exception ex){
            //Log.d(TAG, "renameCustomNewsCategory: Error: "+ex.getMessage());
            ex.printStackTrace();
            return NEWS_CATEGORY_EDIT_REQUEST_RESULT.ERROR;
        }

        String sqlToUpdateName =
                "UPDATE "+NewsServerDBSchema.FeatureGroupTable.NAME+
                " SET "+NewsServerDBSchema.FeatureGroupTable.Cols.Title.NAME+"= '"+
                newName.trim()+"' WHERE "+NewsServerDBSchema.FeatureGroupTable.Cols.Id.NAME+
                " = "+featureGroup.getId();

        try{
            dbCon.execSQL(sqlToUpdateName);
            return NEWS_CATEGORY_EDIT_REQUEST_RESULT.SUCCESS;
        }catch (Exception ex){
            //Log.d(TAG, "renameCustomNewsCategory: Error: "+ex.getMessage());
            ex.printStackTrace();
            return NEWS_CATEGORY_EDIT_REQUEST_RESULT.ERROR;
        }
    }
}

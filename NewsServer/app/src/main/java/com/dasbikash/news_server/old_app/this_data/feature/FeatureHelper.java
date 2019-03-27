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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

import static com.dasbikash.news_server.old_app.database.NewsServerDBSchema.NULL_PARENT_FEATURE_ID;

public abstract class FeatureHelper {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "FeatureHelper";

    private static ArrayList<Feature> getFeaturesBySql(String sqlForFeatures){

        ArrayList<Feature> featureList = new ArrayList<>();

        if (sqlForFeatures == null) return featureList;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForFeatures,null)){
            //Log.d("StackTrace", "cursor.getCount(): "+cursor.getCount());
            if (cursor.getCount() == 0){
                return featureList;
            }
            cursor.moveToFirst();
            do {
                featureList.add(new FeatureCursorWrapper(cursor).getInstance());
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
        } catch (Exception ex){
            //Log.d("StackTrace", "getFeaturesBySql: Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        return featureList;

    }

    public static ArrayList<Feature> getAllActiveFeatures(){

        String sqlForAllFeature =
                "SELECT * FROM "+ NewsServerDBSchema.FeatureTable.NAME+
                " WHERE "+
                NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+
                NewsServerDBSchema.ITEM_ACTIVE_FLAG;
        return getFeaturesBySql(sqlForAllFeature);
    }

    public static ArrayList<Feature> getAllActiveFeaturesForNewspaper(Newspaper newspaper){
        if (newspaper==null) return null;

        String sqlForAllFeature =
                "SELECT * FROM "+ NewsServerDBSchema.FeatureTable.NAME+
                " WHERE "+NewsServerDBSchema.FeatureTable.Cols.NewsPaperId.NAME+
                " = "+
                newspaper.getId()+
                " AND "+
                NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+
                NewsServerDBSchema.ITEM_ACTIVE_FLAG;
        return getFeaturesBySql(sqlForAllFeature);
    }

    public static ArrayList<Feature> getActiveParentFeaturesForNewspaper(Newspaper newspaper){
        if (newspaper==null) return null;

        String sqlForParentFeatures =
                "SELECT * FROM "+ NewsServerDBSchema.FeatureTable.NAME+
                " WHERE "+
                NewsServerDBSchema.FeatureTable.Cols.NewsPaperId.NAME+
                " = "+
                newspaper.getId()+
                " AND "+
                NewsServerDBSchema.FeatureTable.Cols.ParentFeatureId.NAME+
                " = "+
                NULL_PARENT_FEATURE_ID+
                " AND "+
                NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+
                NewsServerDBSchema.ITEM_ACTIVE_FLAG;
        return getFeaturesBySql(sqlForParentFeatures);
    }

    public static ArrayList<Feature> getAllParentFeaturesForNewspaper(Newspaper newspaper){
        if (newspaper==null) return null;

        String sqlForParentFeatures =
                "SELECT * FROM "+ NewsServerDBSchema.FeatureTable.NAME+
                        " WHERE "+
                        NewsServerDBSchema.FeatureTable.Cols.NewsPaperId.NAME+
                        " = "+
                        newspaper.getId()+
                        " AND "+
                        NewsServerDBSchema.FeatureTable.Cols.ParentFeatureId.NAME+
                        " = "+
                        NULL_PARENT_FEATURE_ID;
        return getFeaturesBySql(sqlForParentFeatures);
    }

    public static ArrayList<Feature> getActiveChildFeatures(int parentFeatureId){

        String sqlForParentFeatures =
                "SELECT * FROM "+ NewsServerDBSchema.FeatureTable.NAME+
                " WHERE "+ NewsServerDBSchema.FeatureTable.Cols.ParentFeatureId.NAME+
                " = "+parentFeatureId+
                " AND "+
                NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+
                NewsServerDBSchema.ITEM_ACTIVE_FLAG+";";
        return getFeaturesBySql(sqlForParentFeatures);
    }

    public static ArrayList<Feature> getAllChildFeatures(int parentFeatureId){

        String sqlForParentFeatures =
                "SELECT * FROM "+ NewsServerDBSchema.FeatureTable.NAME+

                " WHERE "+ NewsServerDBSchema.FeatureTable.Cols.ParentFeatureId.NAME+
                " = "+parentFeatureId+";";
        return getFeaturesBySql(sqlForParentFeatures);
    }

    public static Feature findActiveFeatureById(int featureId){

        if (featureId < 1) return null;

        String sqlForParentFeatures =
                "SELECT * FROM "+ NewsServerDBSchema.FeatureTable.NAME+
                " WHERE "+ NewsServerDBSchema.FeatureTable.Cols.Id.NAME+
                " = "+featureId+
                " AND "+
                NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+
                NewsServerDBSchema.ITEM_ACTIVE_FLAG+
                ";";

        ArrayList<Feature> featureArrayList= getFeaturesBySql(sqlForParentFeatures);

        if (featureArrayList.size()==1){
            return featureArrayList.get(0);
        } else {
            return null;
        }
    }

    public static Feature findFeatureById(int featureId){

        if (featureId < 1) return null;

        String sqlForParentFeatures =
                "SELECT * FROM "+ NewsServerDBSchema.FeatureTable.NAME+
                        " WHERE "+ NewsServerDBSchema.FeatureTable.Cols.Id.NAME+
                        " = "+featureId+
                        ";";

        ArrayList<Feature> featureArrayList= getFeaturesBySql(sqlForParentFeatures);

        if (featureArrayList.size()==1){
            return featureArrayList.get(0);
        } else {
            return null;
        }
    }



    public static boolean activateFeature(Feature feature){
        if (feature == null) return false;
        if (feature.isActive()) return true;

        String sqlToActivateFeature =
                "UPDATE "+NewsServerDBSchema.FeatureTable.NAME+
                " SET "+NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+NewsServerDBSchema.ITEM_ACTIVE_FLAG+
                " WHERE "+NewsServerDBSchema.FeatureTable.Cols.Id.NAME+
                " = "+feature.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        //Log.d(TAG, "activateFeature: sqlToActivateFeature: "+sqlToActivateFeature);

        try{
            dbCon.execSQL(sqlToActivateFeature);
            return true;
        } catch (Exception ex){
            //Log.d(TAG, "activateNewspaper: Error: "+ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean deactivateFeature(Feature feature){
        if (feature==null) return false;
        if (!feature.isActive()) return true;

        String sqlToDeactivateNewspaper =
                "UPDATE "+NewsServerDBSchema.FeatureTable.NAME+
                " SET "+NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+NewsServerDBSchema.ITEM_INACTIVE_FLAG+
                " WHERE "+NewsServerDBSchema.FeatureTable.Cols.Id.NAME+
                " = "+feature.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        //Log.d(TAG, "deactivateFeature: sqlToDeactivateNewspaper: "+sqlToDeactivateNewspaper);

        try{
            dbCon.execSQL(sqlToDeactivateNewspaper);
            return true;
        } catch (Exception ex){
            //Log.d(TAG, "deactivateFeature: Error: "+ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static void incrementArticleReadCount(Feature feature){
        if (feature == null) return;

        String sqlToIncrementArticleReadCount =
                "UPDATE "+NewsServerDBSchema.FeatureTable.NAME+
                " SET "+NewsServerDBSchema.FeatureTable.Cols.ArticleReadCount.NAME+" = ("+
                NewsServerDBSchema.FeatureTable.Cols.ArticleReadCount.NAME+"+1) WHERE "+
                NewsServerDBSchema.FeatureTable.Cols.Id.NAME+" = "+ feature.getId();
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        //Log.d(TAG, "incrementArticleReadCount: sqlToIncrementArticleReadCount: "+sqlToIncrementArticleReadCount);

        try {
            dbCon.execSQL(sqlToIncrementArticleReadCount);
        } catch (SQLException e) {
            //Log.d(TAG, "incrementArticleReadCount: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static ArrayList<Feature> getFrequentlyViewdFeatures(){
        String sqlForFrequentlyViewedFeatures =
                "SELECT * FROM "+NewsServerDBSchema.FeatureTable.NAME+
                " WHERE "+NewsServerDBSchema.FeatureTable.Cols.ArticleReadCount.NAME+
                " > "+0+
                " AND "+NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+NewsServerDBSchema.ITEM_ACTIVE_FLAG+
                " ORDER BY "+ NewsServerDBSchema.FeatureTable.Cols.ArticleReadCount.NAME+
                " DESC ";

        return getFeaturesBySql(sqlForFrequentlyViewedFeatures);
    }

    public static boolean clearArticleReadCount() {

        String sqlToClearArticleReadCount =
            "UPDATE "+NewsServerDBSchema.FeatureTable.NAME+
            " SET "+NewsServerDBSchema.FeatureTable.Cols.ArticleReadCount.NAME+
            " = 0";
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        //Log.d(TAG, "incrementArticleReadCount: sqlToClearArticleReadCount: "+sqlToClearArticleReadCount);

        try {
            dbCon.execSQL(sqlToClearArticleReadCount);
            return true;
        } catch (SQLException e) {
            //Log.d(TAG, "incrementArticleReadCount: "+e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<Feature> getFavouriteFeatures(){
        String sqlForFavouriteFeatures =
                "SELECT * FROM "+NewsServerDBSchema.FeatureTable.NAME+
                " WHERE "+NewsServerDBSchema.FeatureTable.Cols.IsFavourite.NAME+
                " = "+NewsServerDBSchema.ITEM_ACTIVE_FLAG+
                " AND "+NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+NewsServerDBSchema.ITEM_ACTIVE_FLAG;

        return getFeaturesBySql(sqlForFavouriteFeatures);
    }

    public static boolean removeFeatureFromFavourites(Feature feature) {
        if (feature == null) return false;
        if (!feature.isFavourite()) return true;

        String sqlToRemoveFeatureFromFavourites =
                "UPDATE "+NewsServerDBSchema.FeatureTable.NAME+
                " SET "+NewsServerDBSchema.FeatureTable.Cols.IsFavourite.NAME+
                " = "+NewsServerDBSchema.ITEM_INACTIVE_FLAG+
                " WHERE "+NewsServerDBSchema.FeatureTable.Cols.Id.NAME+
                " = "+feature.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        //Log.d(TAG, "deactivateFeature: sqlToRemoveFeatureFromFavourites: "+sqlToRemoveFeatureFromFavourites);

        try{
            dbCon.execSQL(sqlToRemoveFeatureFromFavourites);
            return true;
        } catch (Exception ex){
            //Log.d(TAG, "deactivateFeature: Error: "+ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean addFeatureToFavourites(Feature feature) {
        if (feature == null) return false;
        if (feature.isFavourite()) return true;

        String sqlToAddFeatureToFavourites =
                "UPDATE "+NewsServerDBSchema.FeatureTable.NAME+
                " SET "+NewsServerDBSchema.FeatureTable.Cols.IsFavourite.NAME+
                " = "+NewsServerDBSchema.ITEM_ACTIVE_FLAG+
                " WHERE "+NewsServerDBSchema.FeatureTable.Cols.Id.NAME+
                " = "+feature.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        //Log.d(TAG, "deactivateFeature: sqlToAddFeatureToFavourites: "+sqlToAddFeatureToFavourites);

        try{
            dbCon.execSQL(sqlToAddFeatureToFavourites);
            return true;
        } catch (Exception ex){
            //Log.d(TAG, "deactivateFeature: Error: "+ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean activateAllFeatures(){

        String sqlToEnableAllFeatures =
                "UPDATE "+NewsServerDBSchema.FeatureTable.NAME+
                " SET "+NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+
                " = "+NewsServerDBSchema.ITEM_ACTIVE_FLAG;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try{
            dbCon.execSQL(sqlToEnableAllFeatures);
            return true;
        } catch (Exception ex){
            //Log.d(TAG, "deactivateFeature: Error: "+ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
}

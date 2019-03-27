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

package com.dasbikash.news_server.old_app.this_data.article;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

public abstract class ArticleHelper {

    private static final String TAG = "FeatureHelper";

    private static ArrayList<Article> getArticles(String sqlForArticles){

        ArrayList<Article> arrayList = new ArrayList<>();

        if (sqlForArticles == null) return arrayList;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForArticles,null)){
            if (cursor.getCount() == 0){
                return arrayList;
            }
            cursor.moveToFirst();
            do {
                arrayList.add(new ArticleCursorWrapper(cursor).getInstance());
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
        } catch (Exception ex){
            //Log.d("StaceTrace", "Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        return arrayList;
    }

    private static ArrayList<Integer> getArticleIds(String sqlForArticleIds){

        ArrayList<Integer> articleIdList = new ArrayList<>();

        if (sqlForArticleIds == null) return articleIdList;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForArticleIds,null)){
            if (cursor.getCount() == 0){
                return articleIdList;
            }
            cursor.moveToFirst();
            do {
                articleIdList.add(
                        cursor.getInt(cursor.getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.Id.NAME))
                        /*new ArticleCursorWrapper(cursor).getInstance().getId()*/
                );
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
        } catch (Exception ex){
            //Log.d("StaceTrace", "Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        return articleIdList;

    }

    public static ArrayList<Integer> findArticleIdsForFeature(Feature feature){

        ArrayList<Integer> articleIdList = new ArrayList<>();
        if (feature==null)  return articleIdList;

        String sqlForArticleIds = "SELECT * FROM "+ NewsServerDBSchema.ArticleTable.NAME+
                                    " WHERE "+ NewsServerDBSchema.ArticleTable.Cols.FeatureId.NAME+
                                    " = "+feature.getId()+
                                    " ORDER BY "+ NewsServerDBSchema.ArticleTable.Cols.PublicationTimeStamp.NAME+
                                    " DESC";//, "+NewsServerDBSchema.ArticleTable.Cols.Id.NAME+" ASC";
        return getArticleIds(sqlForArticleIds);

    }

    public static Article findArticleById(int articleId){

        String sqlForArticleById = "SELECT * FROM "+ NewsServerDBSchema.ArticleTable.NAME+
                " WHERE "+ NewsServerDBSchema.ArticleTable.Cols.Id.NAME+
                " = "+articleId+";";

        ArrayList<Article> articleArrayList= getArticles(sqlForArticleById);

        if (articleArrayList.size()==1){
            return articleArrayList.get(0);
        } else {
            return null;
        }
    }

    public static ArrayList<Article> findArticlesByHashCode(int hashCode){

        String sqlForArticlesByHashCode = "SELECT * FROM "+ NewsServerDBSchema.ArticleTable.NAME+
                " WHERE "+ NewsServerDBSchema.ArticleTable.Cols.LinkHashCode.NAME+
                " = "+hashCode+";";

        return getArticles(sqlForArticlesByHashCode);
    }
    
    public static int saveArticleDetails(int featureId, String link, String title,
                                             int previewImageId,long publicationTS, long lastModificationTS){
        
        ContentValues contentValues = new ContentValues();

        contentValues.put(NewsServerDBSchema.ArticleTable.Cols.FeatureId.NAME, featureId);
        contentValues.put(NewsServerDBSchema.ArticleTable.Cols.ArticleLink.NAME, link);
        contentValues.put(NewsServerDBSchema.ArticleTable.Cols.ArticleTitle.NAME, title);
        //contentValues.put(NewsServerDBSchema.ArticleTable.Cols.ArticleSummary.NAME, summary);
        contentValues.put(NewsServerDBSchema.ArticleTable.Cols.PreviewImageId.NAME, previewImageId);
        contentValues.put(NewsServerDBSchema.ArticleTable.Cols.LinkHashCode.NAME, link.hashCode());
        contentValues.put(NewsServerDBSchema.ArticleTable.Cols.PublicationTimeStamp.NAME, publicationTS);
        contentValues.put(NewsServerDBSchema.ArticleTable.Cols.LastModificationTimeStamp.NAME, lastModificationTS);

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try{
            return (int)dbCon.insert(NewsServerDBSchema.ArticleTable.NAME, null, contentValues);
        } catch (Exception ex){
            //Log.d("StaceTrace", "Error: "+ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public static void updateLastModificationTS(int hashCode, long currentArticleLastModificationTimeStamp) {

        String sqlForModTSUpdate = "UPDATE "+ NewsServerDBSchema.ArticleTable.NAME+
                                    " SET "+ NewsServerDBSchema.ArticleTable.Cols.LastModificationTimeStamp.NAME+ " = "+
                                    currentArticleLastModificationTimeStamp+
                                    " WHERE "+
                                    NewsServerDBSchema.ArticleTable.Cols.LinkHashCode.NAME+
                                    " = "+
                                    hashCode+";";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForModTSUpdate);
    }

    public static void updatePublicationTS(Article article, long currentArticlePublicationTimeStamp) {

        String sqlForModTSUpdate = "UPDATE "+ NewsServerDBSchema.ArticleTable.NAME+
                                    " SET "+ NewsServerDBSchema.ArticleTable.Cols.PublicationTimeStamp.NAME+ " = "+
                                    currentArticlePublicationTimeStamp+
                                    " WHERE "+
                                    NewsServerDBSchema.ArticleTable.Cols.Id.NAME+
                                    " = "+
                                    article.getId()+";";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForModTSUpdate);
    }

    public static void updateLastModificationTS(Article article, long currentArticlePublicationTimeStamp) {

        String sqlForModTSUpdate = "UPDATE "+ NewsServerDBSchema.ArticleTable.NAME+
                                    " SET "+ NewsServerDBSchema.ArticleTable.Cols.LastModificationTimeStamp.NAME+ " = "+
                                    currentArticlePublicationTimeStamp+
                                    " WHERE "+
                                    NewsServerDBSchema.ArticleTable.Cols.Id.NAME+
                                    " = "+
                                    article.getId()+";";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForModTSUpdate);
    }

    public static void deleteArticlesForFeature(Feature feature){

        String sqlForArticleDeletion = "DELETE FROM "+ NewsServerDBSchema.ArticleTable.NAME+
                                        " WHERE "+
                                        NewsServerDBSchema.ArticleTable.Cols.FeatureId.NAME+
                                        " = "+
                                        feature.getId()+";";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForArticleDeletion);
    }

    public static void deleteAllArticles(){

        String sqlForArticleDeletion = "DELETE FROM "+ NewsServerDBSchema.ArticleTable.NAME+";";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForArticleDeletion);
    }

    public static void saveArticleLocally(Article article){

        String sqlForArticleSave =
                "UPDATE "+ NewsServerDBSchema.ArticleTable.NAME+
                " SET "+ NewsServerDBSchema.ArticleTable.Cols.SavedLocally.NAME+
                " = "+ NewsServerDBSchema.SAVED_LOCALLY_FLAG+
                " WHERE "+ NewsServerDBSchema.ArticleTable.Cols.Id.NAME+
                " = "+
                article.getId()+";";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForArticleSave);
    }

    public static void deleteArticleFromStorage(Article article){

        String sqlForArticleDelete =
                "UPDATE "+ NewsServerDBSchema.ArticleTable.NAME+
                " SET "+ NewsServerDBSchema.ArticleTable.Cols.SavedLocally.NAME+
                " = "+ NewsServerDBSchema.NOT_SAVED_LOCALLY_FLAG+
                " WHERE "+ NewsServerDBSchema.ArticleTable.Cols.Id.NAME+
                " = "+
                article.getId()+";";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForArticleDelete);
    }

    public static boolean deleteAllSavedArticles() {
        String sqlToDeleteAllSavedArticles =
                "UPDATE "+ NewsServerDBSchema.ArticleTable.NAME+
                " SET "+ NewsServerDBSchema.ArticleTable.Cols.SavedLocally.NAME+
                " = "+ NewsServerDBSchema.NOT_SAVED_LOCALLY_FLAG;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        try {
            dbCon.execSQL(sqlToDeleteAllSavedArticles);
            return true;
        } catch (Exception ex){
            //Log.d("Error", "saveArticleDetails: "+ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static ArrayList<Integer> findSavedArticleIds() {

        String sqlForArticleIds =
                "SELECT DISTINCT "+ NewsServerDBSchema.ArticleTable.Cols.Id.NAME+
                " FROM "+ NewsServerDBSchema.ArticleTable.NAME+
                " WHERE "+ NewsServerDBSchema.ArticleTable.Cols.SavedLocally.NAME+
                " = "+NewsServerDBSchema.SAVED_LOCALLY_FLAG+
                " ORDER BY "+NewsServerDBSchema.ArticleTable.Cols.FeatureId.NAME;

        return getArticleIds(sqlForArticleIds);
    }
}

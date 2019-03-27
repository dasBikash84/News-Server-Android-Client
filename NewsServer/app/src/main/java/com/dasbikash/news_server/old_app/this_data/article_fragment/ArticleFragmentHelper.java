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

package com.dasbikash.news_server.old_app.this_data.article_fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.article.Article;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;
import java.util.List;

public final class ArticleFragmentHelper {

    private static final String TAG = "ArticleFragmentHelper";

    public static List<ArticleFragment> findFragmentsForArticle( Article article){

       if (article==null || article.getId() <1)    return null;

       String sqlFindArticleFragments = "SELECT * FROM "+ NewsServerDBSchema.ArticleFragmentTable.NAME+
                                        " WHERE "+
                                        NewsServerDBSchema.ArticleFragmentTable.Cols.ArticleLinkHashCode.NAME+
                                        " = "+
                                        article.getLinkHashCode()+
                                        " ORDER BY "+
                                        NewsServerDBSchema.ArticleFragmentTable.Cols.Id.NAME + ";";

        //Log.d(TAG, "findFragmentsForArticle: sqlFindArticleFragments:"+sqlFindArticleFragments);

        final SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlFindArticleFragments,null)){

            if (cursor.getCount()>0){
                //Log.d(TAG, "findFragmentsForArticle: cursor.getCount()>0: "+cursor.getCount());
                List<ArticleFragment> articleFragmentList = new ArrayList<>();
                cursor.moveToFirst();

                do {
                    ArticleFragment articleFragment = new ArticleFragmentCursorWrapper(cursor).getInstance(article);

                    if (articleFragment != null){
                        articleFragmentList.add(articleFragment);
                    }
                    cursor.moveToNext();
                }while (!cursor.isAfterLast());

                if (articleFragmentList.size()>0){
                    return articleFragmentList;
                }
            } else {
                //Log.d(TAG, "findFragmentsForArticle: cursor.getCount()<=0: ");
            }
        } catch (Exception ex){
            //Log.d(TAG, "findFragmentsForArticle: Error:"+ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    public static int saveArticleFragmentData(int linkHashCode,Integer textDataId,Integer imageDataId){

        if (linkHashCode == 0) return -1;
        if (textDataId == null && imageDataId==null) return -1;

        ContentValues contentValues = new ContentValues();

        contentValues.put(NewsServerDBSchema.ArticleFragmentTable.Cols.ArticleLinkHashCode.NAME, linkHashCode);
        if (textDataId != null){
            contentValues.put(NewsServerDBSchema.ArticleFragmentTable.Cols.TextId.NAME, textDataId.intValue());
        }
        if (imageDataId != null){
            contentValues.put(NewsServerDBSchema.ArticleFragmentTable.Cols.ImageId.NAME, imageDataId.intValue());
        }


        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try{
            return (int)dbCon.insert(NewsServerDBSchema.ArticleFragmentTable.NAME, null, contentValues);
        } catch (Exception ex){
            NewsServerUtility.logErrorMessage(TAG+":"+ex.getMessage());
            //Log.d("Error", "saveArticleDetails: "+ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }
}

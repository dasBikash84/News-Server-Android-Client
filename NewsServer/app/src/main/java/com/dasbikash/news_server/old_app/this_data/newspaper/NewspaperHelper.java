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

package com.dasbikash.news_server.old_app.this_data.newspaper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.language.Language;
import com.dasbikash.news_server.old_app.this_data.language.LanguageHelper;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

public abstract class NewspaperHelper {

    private static final String TAG = "NewspaperHelper";
    //private static final String TAG = "StackTrace";

    private static ArrayList<Newspaper> getNewspapersBySql(String sqlForNewspapers){

        //Log.d(TAG, "getNewspapersBySql: ");

        ArrayList<Newspaper> newspapers = new ArrayList<>();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForNewspapers,null)){
            if (cursor.getCount() == 0){
                //Log.d(TAG, "getNewspapersBySql: cursor.getCount() == 0");
                return newspapers;
            }
            cursor.moveToFirst();
            do {
                newspapers.add(new NewspaperCursorWrapper(cursor).getInstance());
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
        } catch (Exception ex){
            NewsServerUtility.logErrorMessage(TAG+":"+ex.getMessage());
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
        }

        return newspapers;
    }

    public static ArrayList<Newspaper> getAllActiveNewspapers(){

        String sqlForNewspapers = "SELECT * FROM "+
                                    NewsServerDBSchema.NewsPaperTable.NAME+
                                    " WHERE "+
                                    NewsServerDBSchema.NewsPaperTable.Cols.IsActive.NAME+
                                    " = "+
                                    NewsServerDBSchema.ITEM_ACTIVE_FLAG+
                                    " ORDER BY "+NewsServerDBSchema.NewsPaperTable.Cols.LanguageId.NAME+
                                    " ASC;";
        //Log.d(TAG, "getAllActiveNewspapers: sqlForNewspapers: "+sqlForNewspapers);

        return getNewspapersBySql(sqlForNewspapers);
    }

    public static ArrayList<Newspaper> getAllNewspapers(){

        String sqlForNewspapers = "SELECT * FROM "+
                NewsServerDBSchema.NewsPaperTable.NAME+
                " ORDER BY "+NewsServerDBSchema.NewsPaperTable.Cols.Name.NAME+
                " DESC;";
        //Log.d(TAG, "getAllActiveNewspapers: sqlForNewspapers: "+sqlForNewspapers);

        return getNewspapersBySql(sqlForNewspapers);
    }

    public static Newspaper findNewspaperById(int newspaperId){

        String sqlForNewspaper = "SELECT * FROM "+NewsServerDBSchema.NewsPaperTable.NAME+
                                    " WHERE "+
                                    NewsServerDBSchema.NewsPaperTable.Cols.Id.NAME+
                                    " = "+
                                    newspaperId;

        ArrayList<Newspaper> newspapers = getNewspapersBySql(sqlForNewspaper);

        if (newspapers.size()==1){
            return newspapers.get(0);
        } else {
            return null;
        }
    }

    public static String getNewspaperHomePageTitle(Newspaper newspaper){

        if (newspaper == null) return "Home Page";
        Language language = LanguageHelper.findLanguageForNewspaper(newspaper);
        if (language == null) return "Home Page";

        if (language.getName().matches("Bangla.+")){
            return NewsServerUtility.getContext().getResources().getString(R.string.newspaper_home_page_menu_text_bangla);
        } else {
            return NewsServerUtility.getContext().getResources().getString(R.string.newspaper_home_page_menu_text_english);
        }

    }

    public static boolean activateNewspaper(Newspaper newspaper){
        if (newspaper==null) return false;
        if (newspaper.isActive()) return true;

        String sqlToActivateNewspaper =
                "UPDATE "+NewsServerDBSchema.NewsPaperTable.NAME+
                " SET "+NewsServerDBSchema.NewsPaperTable.Cols.IsActive.NAME+
                " = "+NewsServerDBSchema.ITEM_ACTIVE_FLAG+
                " WHERE "+NewsServerDBSchema.NewsPaperTable.Cols.Id.NAME+
                " = "+newspaper.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        //Log.d(TAG, "activateNewspaper: sqlToActivateNewspaper: "+sqlToActivateNewspaper);

        try{
            dbCon.execSQL(sqlToActivateNewspaper);
            return true;
        } catch (Exception ex){
            //Log.d(TAG, "activateNewspaper: Error: "+ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean deactivateNewspaper(Newspaper newspaper){
        if (newspaper==null) return false;
        if (!newspaper.isActive()) return true;

        String sqlToDeactivateNewspaper =
                "UPDATE "+NewsServerDBSchema.NewsPaperTable.NAME+
                " SET "+NewsServerDBSchema.NewsPaperTable.Cols.IsActive.NAME+
                " = "+NewsServerDBSchema.ITEM_INACTIVE_FLAG+
                " WHERE "+NewsServerDBSchema.NewsPaperTable.Cols.Id.NAME+
                " = "+newspaper.getId();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        //Log.d(TAG, "deactivateNewspaper: sqlToDeactivateNewspaper: "+sqlToDeactivateNewspaper);

        try{
            dbCon.execSQL(sqlToDeactivateNewspaper);
            return true;
        } catch (Exception ex){
            //Log.d(TAG, "deactivateNewspaper: Error: "+ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean activateAllNewspapers(){

        String sqlToEnableAllNewspapers =
                "UPDATE "+NewsServerDBSchema.NewsPaperTable.NAME+
                " SET "+NewsServerDBSchema.NewsPaperTable.Cols.IsActive.NAME+
                " = "+NewsServerDBSchema.ITEM_ACTIVE_FLAG;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try{
            dbCon.execSQL(sqlToEnableAllNewspapers);
            return true;
        } catch (Exception ex){
            //Log.d(TAG, "deactivateFeature: Error: "+ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

}

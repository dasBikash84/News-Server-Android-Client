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

package com.dasbikash.news_server.old_app.this_data.language;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

public abstract class LanguageHelper {

    private static final String TAG = "LanguageHelper";

    private static ArrayList<Language> getLanguagesBySql(String sqlForLanguages){

        ArrayList<Language> languages = new ArrayList<>();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForLanguages,null)){
            if (cursor.getCount() == 0){
                return languages;
            }
            cursor.moveToFirst();
            do {
                languages.add(new LanguageCursorWrapper(cursor).getInstance());
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
        } catch (Exception ex){
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
        }

        return languages;
    }

    public static ArrayList<Language> getAllLanguages(){

        String sqlForLanguages = "SELECT * FROM "+NewsServerDBSchema.LanguageTable.NAME;

        return getLanguagesBySql(sqlForLanguages);
    }

    public static Language findLanguageForNewspaper(Newspaper newspaper){

        String sqlForLanguage = "SELECT * FROM "+NewsServerDBSchema.LanguageTable.NAME+
                                    " WHERE "+
                                    NewsServerDBSchema.LanguageTable.Cols.Id.NAME+
                                    " = "+
                                    newspaper.getLanguageId();

        ArrayList<Language> languages = getLanguagesBySql(sqlForLanguage);

        if (languages.size()==1){
            return languages.get(0);
        } else {
            return null;
        }
    }


}

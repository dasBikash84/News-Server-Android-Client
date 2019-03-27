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

package com.dasbikash.news_server.old_app.this_data.text_data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

public class TextDataHelper {

    private static final String TAG = "TextDataHelper";

    private static final String REGEX_TO_LOCATE_LINK_TAG = "<a.+?>|</a>";
    private static final String REGEX_TO_REPLACE_LINK_TAG = " ";

    private static String stripLinkTag(String content){
        if (content == null || content.length()==0) return content;

        return content.replaceAll(REGEX_TO_LOCATE_LINK_TAG,REGEX_TO_REPLACE_LINK_TAG);

    }

    public static TextData findTextDataById(int textDataId){

        if (textDataId<1)   return null;

        final SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        String sqlFindTextData = "SELECT * FROM "+
                                    NewsServerDBSchema.TextTable.NAME +
                                    " WHERE "+
                                    NewsServerDBSchema.TextTable.Cols.Id.NAME +
                                    " = "+
                                    textDataId;

        try (Cursor cursor = dbCon.rawQuery(sqlFindTextData,null)){
            if (cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return new TextDataCursorWrapper(cursor).getInstance();
        } catch (Exception ex){
            //NewsServerUtility.logErrorMessage(TAG+":"+ex.getMessage());
            //Log.d(TAG, "Error: "+ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    public static int saveTextData(String content){

        if (content == null) return -1;

        content = stripLinkTag(content);
        content = stripExtraBRTag(content);
        if (checkIfEmptyDisplayText(content)){
            return -1;
        }

        ContentValues contentValues = new ContentValues();

        contentValues.put(NewsServerDBSchema.TextTable.Cols.Content.NAME, content);

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try{
            return (int)dbCon.insert(NewsServerDBSchema.TextTable.NAME, null, contentValues);
        } catch (Exception ex){
            NewsServerUtility.logErrorMessage(TAG+":"+ex.getMessage());
            //Log.d("Error", "saveArticleDetails: "+ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    private static boolean checkIfEmptyDisplayText(String content) {
        content = content.replaceAll("<.+?>","");
        return content.trim().length() == 0;
    }

    private static String stripExtraBRTag(String content) {
        content = content.replaceAll("(<br>)+","<br>");
        content = content.replaceAll("(<br/>)+","<br>");
        return content;
    }
}

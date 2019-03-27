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

package com.dasbikash.news_server.old_app.this_data.image_data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageDataHelper {

    private static final String TAG = "ImageDataHelper";


    public static ImageData findImageDataById(int imageDataId){

        if ((imageDataId == 0) || (imageDataId == -1))   return null;

        final SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        String sqlFindImageData = "SELECT * FROM "+
                                    NewsServerDBSchema.ImageTable.NAME +
                                    " WHERE "+
                                    NewsServerDBSchema.ImageTable.Cols.Id.NAME +
                                    " = "+
                                    imageDataId;

        try (Cursor cursor = dbCon.rawQuery(sqlFindImageData,null)){
            if (cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return new ImageDataCursorWrapper(cursor).getInstance();
        } catch (Exception ex){
            //NewsServerUtility.logErrorMessage(TAG+":"+ex.getMessage());
            //Log.d(TAG, "Error: "+ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    public static boolean checkIfImageExists(int imageDataId) {

        String sqlFindImageEntry = "SELECT * FROM "+
                                    NewsServerDBSchema.ImageTable.NAME + " WHERE "+
                                    NewsServerDBSchema.ImageTable.Cols.Id.NAME + " = "+
                                    imageDataId;

        final SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try(Cursor cursor = dbCon.rawQuery(sqlFindImageEntry,null)) {
            if (cursor.getCount() == 1){
                return true;
            }
        } catch (Exception ex){
            NewsServerUtility.logErrorMessage(TAG+":"+ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    public static int saveImageData(int hashId,String link,String altText){

        String sqlCheckIfImageExists =
            "SELECT * FROM "+NewsServerDBSchema.ImageTable.NAME+
            " WHERE "+ NewsServerDBSchema.ImageTable.Cols.Id.NAME+
            " = "+hashId;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlCheckIfImageExists,null)){
            if (cursor.getCount() == 1){
                return hashId;
            }
        } catch (Exception ex){
            //Log.d(TAG, "saveImageData: Error: "+ex.getMessage());
        }

        ContentValues contentValues = new ContentValues();

        contentValues.put(NewsServerDBSchema.ImageTable.Cols.Id.NAME, hashId);
        contentValues.put(NewsServerDBSchema.ImageTable.Cols.WebLink.NAME, link);
        contentValues.put(NewsServerDBSchema.ImageTable.Cols.AltText.NAME, altText);


        try{
            return (int)dbCon.insert(NewsServerDBSchema.ImageTable.NAME, null, contentValues);
        } catch (Exception ex){
            //Log.d(TAG, "saveArticleDetails: "+ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    public static HashMap<Integer, String> getSavedImageDiskLocations() {

        String sqlForSavedImageLocations =
                "SELECT * FROM "+NewsServerDBSchema.ImageTable.NAME+
                " WHERE "+NewsServerDBSchema.ImageTable.Cols.DiskLocation.NAME+
                " IS NOT NULL";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        HashMap<Integer, String> imageDiskLocationMap = new HashMap<>();

        try(Cursor cursor=dbCon.rawQuery(sqlForSavedImageLocations,null)){
            if (cursor.getCount() > 0){
                cursor.moveToFirst();
                do {
                    int imageId = -1;
                    String imageLocation = null;
                    try {
                        imageId = cursor.getInt(cursor.getColumnIndex(NewsServerDBSchema.ImageTable.Cols.Id.NAME));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        imageLocation = cursor.getString(cursor.getColumnIndex(NewsServerDBSchema.ImageTable.Cols.DiskLocation.NAME));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (imageId != -1 || imageId != 0 || imageLocation != null) {
                        imageDiskLocationMap.put(imageId,imageLocation);
                    }
                    cursor.moveToNext();
                }while (!cursor.isAfterLast());
            }
        } catch (Exception ex){
            //Log.d(TAG, "getSavedImageDiskLocations: Error: "+ex.getMessage());
            ex.printStackTrace();
        }

        return imageDiskLocationMap;
    }

    public static void resetSavedImagesSize() {

        String sqlForSavedImageSizeReset =
                "UPDATE "+NewsServerDBSchema.ImageTable.NAME+
                " SET "+NewsServerDBSchema.ImageTable.Cols.DiskLocation.NAME+
                " = NULL,"+
                NewsServerDBSchema.ImageTable.Cols.SizeKB.NAME+
                " = 0 "+
                " WHERE "+NewsServerDBSchema.ImageTable.Cols.DiskLocation.NAME+
                " IS NOT NULL";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForSavedImageSizeReset);
    }

    private static ArrayList<Integer> getArticleImageIds(){

        String sqlForArticleImageIds =
                "SELECT "+NewsServerDBSchema.ArticleFragmentTable.Cols.ImageId.NAME+
                " FROM "+NewsServerDBSchema.ArticleFragmentTable.NAME;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        ArrayList<Integer> articleImageIdList = new ArrayList<>();

        try(Cursor cursor = dbCon.rawQuery(sqlForArticleImageIds,null)){
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    articleImageIdList.add(
                            cursor.getInt(0)
                    );
                    cursor.moveToNext();
                }while (!cursor.isAfterLast());
            }
        } catch (Exception ex){
            //Log.d(TAG, "getArticleImageIds: Error: "+ex.getMessage());
            ex.printStackTrace();
        }

        return articleImageIdList;
    }

    public static boolean deleteNonArticleImages(){

        ArrayList<Integer> articleImageIdList = getArticleImageIds();

        StringBuilder sqlBuilder = new StringBuilder(
                "DELETE FROM "+NewsServerDBSchema.ImageTable.NAME
        );

        if (articleImageIdList.size()>0){
            sqlBuilder.append(
                " WHERE "+NewsServerDBSchema.ImageTable.Cols.Id.NAME+
                " NOT IN("
            );
            for (int i = 0; i < articleImageIdList.size(); i++) {
                sqlBuilder.append(articleImageIdList.get(i));
                if (i!=articleImageIdList.size()-1){
                    sqlBuilder.append(",");
                }
            }
            sqlBuilder.append(")");
        }
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        try {
            //Log.d("NSUtility", "deleteNonArticleImages: "+sqlBuilder.toString());
            dbCon.execSQL(sqlBuilder.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        HashMap<Integer, String> imageDiskLocationMap = getSavedImageDiskLocations();
        File imageDirectory = new File(NewsServerUtility.getContext().getFilesDir().getPath());
        File[] imageFiles = imageDirectory.listFiles();
        for (File imageFile :
                imageFiles) {
            if (!imageDiskLocationMap.containsValue(imageFile.getAbsolutePath())){
                //Log.d("NSUtility", "deleteNonArticleImages: imageLocation:"+imageFile);
                //new File(imageFile).delete();
                imageFile.delete();
            }
        }
        return true;
    }

    public static String getManualImageDownloadPrompt(){
        String sqlToReadCount =
                "SELECT "+NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME+
                " FROM "+NewsServerDBSchema.SettingsDataTable.NAME+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+"="+
                NewsServerDBSchema.MANUAL_IMAGE_DOWNLOAD_COUNT_SETTINGS_ENTRY_ID;
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try(Cursor cursor = dbCon.rawQuery(sqlToReadCount,null)){
            if (cursor.getCount()==1){
                cursor.moveToFirst();
                int value = cursor.getInt(0);
                //Log.d("StackTrace", "getManualImageDownloadPrompt: "+value);
                if (value> NewsServerDBSchema.MIN_MANUAL_IMAGE_DL_COUNT_FOR_PROMPT2){
                    return NewsServerUtility.getContext().getResources().
                            getString(R.string.image_dl_disabled_on_data_net_message2);
                }
            }
        }catch (Exception ex){
            //Log.d(TAG, "getManualImageDownloadPrompt: Error: "+ex.getMessage());
        }
        return "";
    }

    public static boolean incrementManualImageDownloadCount() {
        String sqlToIncrementCount =
                "UPDATE "+NewsServerDBSchema.SettingsDataTable.NAME+
                " SET "+NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME+"="+
                NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME+"+1"+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+"="+
                NewsServerDBSchema.MANUAL_IMAGE_DOWNLOAD_COUNT_SETTINGS_ENTRY_ID;
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        try{
            dbCon.execSQL(sqlToIncrementCount);
            return true;
        }catch (Exception ex){
            //Log.d(TAG, "getManualImageDownloadPrompt: Error: "+ex.getMessage());
            return false;
        }
    }
}

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

package com.dasbikash.news_server.old_app.this_utility;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

import java.io.File;
import java.util.ArrayList;

@SuppressWarnings({"StatementWithEmptyBody", "UnusedAssignment"})
public abstract class DiskCleaner {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "DiskCleaner";

    private static final double MAX_ALLOWED_DISK_SPACE_MB = 40;
    private static final double MIN_BUFFER_SPACE_MB = 10;

    private static final double DELETION_EXECUTION_PERCENTAGE = 0.8;
    private static final int MAX_DELETION_ITERATION = 3;

    private static ArrayList<Integer> getLocallySavedArticleHashcodes(){
        ArrayList<Integer> localArticleHashcodeList = new ArrayList<>();
        String sqlForArticleInfo =
                "SELECT DISTINCT "+NewsServerDBSchema.ArticleTable.Cols.LinkHashCode.NAME+
                " FROM "+NewsServerDBSchema.ArticleTable.NAME+
                " WHERE "+NewsServerDBSchema.ArticleTable.Cols.SavedLocally.NAME+" = "+
                NewsServerDBSchema.SAVED_LOCALLY_FLAG;
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        try(Cursor cursor = dbCon.rawQuery(sqlForArticleInfo,null)){
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    localArticleHashcodeList.add(
                            cursor.getInt(0)
                    );
                    cursor.moveToNext();
                }while (!cursor.isAfterLast());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return localArticleHashcodeList;
    }

    private static ArrayList<Integer> getAllArticlePreviewImageIdList(){
        ArrayList<Integer> articlePreviewImageIdList = new ArrayList<>();
        String sqlForArticlePreviewImageId =
                "SELECT DISTINCT "+NewsServerDBSchema.ArticleTable.Cols.PreviewImageId.NAME+
                " FROM "+NewsServerDBSchema.ArticleTable.NAME;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        try(Cursor cursor = dbCon.rawQuery(sqlForArticlePreviewImageId,null)){
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    int previewImageId = cursor.getInt(0);
                    if (previewImageId !=0 && previewImageId!= -1) {
                        articlePreviewImageIdList.add(previewImageId);
                    }
                    cursor.moveToNext();
                }while (!cursor.isAfterLast());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return articlePreviewImageIdList;
    }

    private static ArrayList<Integer> getLocalArticleTextDataIds(ArrayList<Integer> localArticleHashCodeList){

        ArrayList<Integer> localTextIdList = new ArrayList<>();

        StringBuilder sqlForLocalTextIdsBuilder = new StringBuilder(
                "SELECT "+NewsServerDBSchema.ArticleFragmentTable.Cols.TextId.NAME+
                        " FROM "+NewsServerDBSchema.ArticleFragmentTable.NAME+
                        " WHERE "+NewsServerDBSchema.ArticleFragmentTable.Cols.TextId.NAME+" IS NOT NULL "
        );
        sqlForLocalTextIdsBuilder.append(
                " AND "+NewsServerDBSchema.ArticleFragmentTable.Cols.ArticleLinkHashCode.NAME+
                        " IN("
        );
        if (localArticleHashCodeList.size()>0){

            for (int i = 0; i < localArticleHashCodeList.size(); i++) {
                sqlForLocalTextIdsBuilder.append(localArticleHashCodeList.get(i));
                if (i != localArticleHashCodeList.size()-1){
                    sqlForLocalTextIdsBuilder.append(",");
                }
            }
        }

        sqlForLocalTextIdsBuilder.append(");");

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try(Cursor cursor = dbCon.rawQuery(sqlForLocalTextIdsBuilder.toString(),null)){
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    localTextIdList.add(cursor.getInt(0));
                    cursor.moveToNext();
                }while (!cursor.isAfterLast());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return localTextIdList;
    }

    private static ArrayList<Integer> getLocalArticleImageIds(ArrayList<Integer> localArticleHashCodeList){

        ArrayList<Integer> localImageIdList = new ArrayList<>();

        StringBuilder sqlForLocalImageIdsBuilder = new StringBuilder(
                "SELECT "+NewsServerDBSchema.ArticleFragmentTable.Cols.ImageId.NAME+
                " FROM "+NewsServerDBSchema.ArticleFragmentTable.NAME+
                " WHERE "+NewsServerDBSchema.ArticleFragmentTable.Cols.ImageId.NAME+" IS NOT NULL "
        );
        sqlForLocalImageIdsBuilder.append(
                " AND "+NewsServerDBSchema.ArticleFragmentTable.Cols.ArticleLinkHashCode.NAME+
                        " IN("
        );
        if (localArticleHashCodeList.size()>0){

            for (int i = 0; i < localArticleHashCodeList.size(); i++) {
                sqlForLocalImageIdsBuilder.append(localArticleHashCodeList.get(i));
                if (i != localArticleHashCodeList.size()-1){
                    sqlForLocalImageIdsBuilder.append(",");
                }
            }
        }

        sqlForLocalImageIdsBuilder.append(");");

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try(Cursor cursor = dbCon.rawQuery(sqlForLocalImageIdsBuilder.toString(),null)){
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    localImageIdList.add(cursor.getInt(0));
                    cursor.moveToNext();
                }while (!cursor.isAfterLast());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return localImageIdList;
    }

    private static void deleteArticleFragment(ArrayList<Integer> localArticleHashCodeList) {

        StringBuilder sqlForLocalFragmentDeleteBuilder = new StringBuilder(
                "DELETE FROM "+NewsServerDBSchema.ArticleFragmentTable.NAME
        );
        sqlForLocalFragmentDeleteBuilder.append(
                " WHERE "+NewsServerDBSchema.ArticleFragmentTable.Cols.ArticleLinkHashCode.NAME+" NOT IN("
        );
        if (localArticleHashCodeList.size()>0){

            for (int i = 0; i < localArticleHashCodeList.size(); i++) {
                sqlForLocalFragmentDeleteBuilder.append(localArticleHashCodeList.get(i));
                if (i != localArticleHashCodeList.size()-1){
                    sqlForLocalFragmentDeleteBuilder.append(",");
                }
            }
        }
        sqlForLocalFragmentDeleteBuilder.append(");");

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        dbCon.execSQL(sqlForLocalFragmentDeleteBuilder.toString());
    }

    private static void deleteTextData(ArrayList<Integer> localTextIdList) {

        StringBuilder sqlForLocalTextDataDeleteBuilder = new StringBuilder(
                "DELETE FROM "+NewsServerDBSchema.TextTable.NAME
        );
        sqlForLocalTextDataDeleteBuilder.append(
                " WHERE "+NewsServerDBSchema.TextTable.Cols.Id.NAME+" NOT IN("
        );
        if (localTextIdList.size()>0){

            for (int i = 0; i < localTextIdList.size(); i++) {
                sqlForLocalTextDataDeleteBuilder.append(localTextIdList.get(i));
                if (i != localTextIdList.size()-1){
                    sqlForLocalTextDataDeleteBuilder.append(",");
                }
            }
        }

        sqlForLocalTextDataDeleteBuilder.append(");");

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        dbCon.execSQL(sqlForLocalTextDataDeleteBuilder.toString());
    }

    private static void deleteImageDataExceptInList(ArrayList<Integer> localImageIdList) {

        StringBuilder sqlForLocalTextDataDeleteBuilder = new StringBuilder(
                "DELETE FROM "+NewsServerDBSchema.ImageTable.NAME
        );

        sqlForLocalTextDataDeleteBuilder.append(
                " WHERE "+NewsServerDBSchema.ImageTable.Cols.Id.NAME+" NOT IN("
        );
        if (localImageIdList.size()>0){

            for (int i = 0; i < localImageIdList.size(); i++) {
                sqlForLocalTextDataDeleteBuilder.append(localImageIdList.get(i));
                if (i != localImageIdList.size()-1){
                    sqlForLocalTextDataDeleteBuilder.append(",");
                }
            }
        }

        sqlForLocalTextDataDeleteBuilder.append(");");

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        dbCon.execSQL(sqlForLocalTextDataDeleteBuilder.toString());
    }

    private static ArrayList<String> getLocalImageDiskLocationList() {

        ArrayList<String> localImageDiskLocationList = new ArrayList<>();

        String sqlForImageDiskLocation =
                "SELECT "+NewsServerDBSchema.ImageTable.Cols.DiskLocation.NAME+
                " FROM "+NewsServerDBSchema.ImageTable.NAME+
                " WHERE "+NewsServerDBSchema.ImageTable.Cols.DiskLocation.NAME+
                " IS NOT NULL;";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForImageDiskLocation,null)){
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    localImageDiskLocationList.add(cursor.getString(0));
                    cursor.moveToNext();
                }while (!cursor.isAfterLast());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return localImageDiskLocationList;
    }

    private static void deleteImageExceptInList(ArrayList<String> localImageDiskLocationList) {
        File imageDirectory = new File(NewsServerUtility.getContext().getFilesDir().getPath());

        File[] imageFiles = imageDirectory.listFiles();

        for (File imageFile :
                imageFiles) {
            if (!localImageDiskLocationList.contains(imageFile.getAbsolutePath())){
                imageFile.delete();
            }
        }
    }

    private static void clearArticleDataAcceptGivenInfoMap(ArrayList<Integer> locallySavedArticleHashCodeList){

        //ArrayList<Integer> allArticlePreviewImageIdList = getAllArticlePreviewImageIdList();

        ArrayList<Integer> locallySavedArticlePreviewImageIdList = getLocalArticlePreviewImageIds(locallySavedArticleHashCodeList);
        ArrayList<Integer> locallySavedArticleTextIdList = getLocalArticleTextDataIds(locallySavedArticleHashCodeList);
        ArrayList<Integer> locallySavedArticleImageIdList = getLocalArticleImageIds(locallySavedArticleHashCodeList);

        ArrayList<Integer> allImageIdsRelatedToLocallySavedArticles = new ArrayList<>(locallySavedArticlePreviewImageIdList);
        allImageIdsRelatedToLocallySavedArticles.addAll(locallySavedArticleImageIdList);

        /*ArrayList<Integer> allImageIdsToBeKept = new ArrayList<>(allArticlePreviewImageIdList);
        allImageIdsToBeKept.addAll(locallySavedArticleImageIdList);*/

        deleteArticleFragment(locallySavedArticleHashCodeList);
        deleteTextData(locallySavedArticleTextIdList);
        //deleteImageDataExceptInList(allImageIdsToBeKept);
        deleteImageDataExceptInList(allImageIdsRelatedToLocallySavedArticles);


        removeDiskLocationExceptOnList(allImageIdsRelatedToLocallySavedArticles);
        ArrayList<String> localImageDiskLocationList = getLocalImageDiskLocationList();

        deleteImageExceptInList(localImageDiskLocationList);
    }

    private static ArrayList<Integer> getLocalArticlePreviewImageIds(ArrayList<Integer> localArticleHashCodeList) {

        ArrayList<Integer> localArticlePreviewImageIdList = new ArrayList<>();

        StringBuilder sqlForLocalImageIdsBuilder = new StringBuilder(
                "SELECT DISTINCT "+NewsServerDBSchema.ArticleTable.Cols.PreviewImageId.NAME+
                " FROM "+NewsServerDBSchema.ArticleTable.NAME
        );
        sqlForLocalImageIdsBuilder.append(
                " WHERE "+NewsServerDBSchema.ArticleTable.Cols.LinkHashCode.NAME+
                        " IN("
        );
        if (localArticleHashCodeList.size()>0){

            for (int i = 0; i < localArticleHashCodeList.size(); i++) {
                sqlForLocalImageIdsBuilder.append(localArticleHashCodeList.get(i));
                if (i != localArticleHashCodeList.size()-1){
                    sqlForLocalImageIdsBuilder.append(",");
                }
            }
        }
        sqlForLocalImageIdsBuilder.append(");");


        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try(Cursor cursor = dbCon.rawQuery(sqlForLocalImageIdsBuilder.toString(),null)){
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                do {
                    localArticlePreviewImageIdList.add(cursor.getInt(0));
                    cursor.moveToNext();
                }while (!cursor.isAfterLast());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return localArticlePreviewImageIdList;
    }

    private static void removeDiskLocationExceptOnList(ArrayList<Integer> imageIdListOfImagesToBeKept) {

        StringBuilder sqlForImagesLocalDiskInfoDeleteBuilder = new StringBuilder(
                "UPDATE "+NewsServerDBSchema.ImageTable.NAME+
                        " SET "+NewsServerDBSchema.ImageTable.Cols.DiskLocation.NAME+
                        " = NULL,"+NewsServerDBSchema.ImageTable.Cols.SizeKB.NAME+
                        " = 0 "
        );
        sqlForImagesLocalDiskInfoDeleteBuilder.append(
                " WHERE "+NewsServerDBSchema.ImageTable.Cols.Id.NAME+" NOT IN("
        );
        if (imageIdListOfImagesToBeKept.size()>0){

            for (int i = 0; i < imageIdListOfImagesToBeKept.size(); i++) {
                sqlForImagesLocalDiskInfoDeleteBuilder.append(imageIdListOfImagesToBeKept.get(i));
                if (i != imageIdListOfImagesToBeKept.size()-1){
                    sqlForImagesLocalDiskInfoDeleteBuilder.append(",");
                }
            }
        }

        sqlForImagesLocalDiskInfoDeleteBuilder.append(");");
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForImagesLocalDiskInfoDeleteBuilder.toString());
    }

    public static void clearCachedDataAction(){

        ArrayList<Integer> localArticleHashcodeList = getLocallySavedArticleHashcodes();
        clearArticleDataAcceptGivenInfoMap(localArticleHashcodeList);
    }

    public static boolean deleteUnsavedArticleInfo(){

        long timeStampBefore24Hours = System.currentTimeMillis() - 24*60*60*1000;

        String sqlForUnsavedArticleDelete =
                "DELETE FROM "+NewsServerDBSchema.ArticleTable.NAME+
                " WHERE "+NewsServerDBSchema.ArticleTable.Cols.SavedLocally.NAME+
                " = "+NewsServerDBSchema.NOT_SAVED_LOCALLY_FLAG;/*+
                " AND "+NewsServerDBSchema.ArticleTable.Cols.PublicationTimeStamp.NAME+
                "<"+ timeStampBefore24Hours;*/

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        dbCon.execSQL(sqlForUnsavedArticleDelete);
        return false;
    }

}

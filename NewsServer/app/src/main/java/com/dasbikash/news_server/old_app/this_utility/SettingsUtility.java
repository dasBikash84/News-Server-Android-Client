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

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AlertDialog;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderService;
import com.dasbikash.news_server.old_app.database.NewsServerDBOpenHelper;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderService;
import com.dasbikash.news_server.old_app.this_data.article.ArticleHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

public class SettingsUtility {

    private static final String TAG="SettingsUtility";

    private static final CharSequence DELETE_SAVED_ARTICLES_PROMPT = "Delete all saved articles?";
    private static final CharSequence FAILURE_TOAST_MESSAGE = "Error occurred, please retry.";
    private static final CharSequence SAVED_ARTICLES_DELETED_TOAST_MESSAGE = "All saved articles deleted.";

    private static final long PARENT_FEATURE_CHECK_INTERVAL_MILLIS = 12 * 60 * 60 * 1000;

    public static boolean getDNImageDownloadSetting(){

        String sqlForSettingRead =
                "SELECT * FROM "+ NewsServerDBSchema.SettingsDataTable.NAME+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                " = "+NewsServerDBSchema.IMAGE_DOWNLOAD_ON_DN_SETTINGS_ENTRY_ID;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try(Cursor cursor = dbCon.rawQuery(sqlForSettingRead,null)){
            if (cursor.getCount() == 1){
                cursor.moveToFirst();
                int settingsValue=-1;
                try {
                    settingsValue = cursor.getInt(cursor.getColumnIndex(
                        NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (settingsValue == NewsServerDBSchema.IMAGE_DOWNLOAD_ON_DN_ENABLE_FLAG){
                    return true;
                }
            }
        }catch (Exception ex){
            NewsServerUtility.logErrorMessage("SettingsUtility: "+ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean getNavMenuDisplaySetting(){

        String sqlForSettingRead =
                "SELECT * FROM "+ NewsServerDBSchema.SettingsDataTable.NAME+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                " = "+NewsServerDBSchema.NAVIGATION_MENU_DISPLAY_SETTINGS_ENTRY_ID;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try(Cursor cursor = dbCon.rawQuery(sqlForSettingRead,null)){
            if (cursor.getCount() == 1){
                cursor.moveToFirst();
                int settingsValue=-1;
                try {
                    settingsValue = cursor.getInt(cursor.getColumnIndex(
                        NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (settingsValue == NewsServerDBSchema.NAVIGATION_MENU_DISPLAY_ENABLE_FLAG){
                    return true;
                }
            }
        }catch (Exception ex){
            NewsServerUtility.logErrorMessage("SettingsUtility: "+ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean enableImageDownloadOnDN(){

        String sqlForEnableImageDownloadOnDn =
                "UPDATE "+ NewsServerDBSchema.SettingsDataTable.NAME+
                " SET "+NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME+
                " = "+NewsServerDBSchema.IMAGE_DOWNLOAD_ON_DN_ENABLE_FLAG+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                " = "+NewsServerDBSchema.IMAGE_DOWNLOAD_ON_DN_SETTINGS_ENTRY_ID;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForEnableImageDownloadOnDn);
        } catch (SQLException e) {
            NewsServerUtility.logErrorMessage("SettingsUtility: "+e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static boolean canDlImageOnDataNet(){

        String sqlCheckIfCanDlOnDataNet =
                "SELECT * FROM "+ NewsServerDBSchema.SettingsDataTable.NAME+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                " = "+NewsServerDBSchema.IMAGE_DOWNLOAD_ON_DN_SETTINGS_ENTRY_ID;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        try (Cursor cursor = dbCon.rawQuery(sqlCheckIfCanDlOnDataNet,null)){
            if (cursor.getCount() == 0){
                return true;
            }
            cursor.moveToFirst();
            if (cursor.getInt(cursor.getColumnIndex(NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME)) ==
                    NewsServerDBSchema.IMAGE_DOWNLOAD_ON_DN_ENABLE_FLAG){
                return true;
            }
        }catch (Exception ex){
            //Log.d(TAG, "canDlImageOnDataNet: Error: "+ex.getMessage());
            return true;
        }

        return false;
    }

    public static boolean disableImageDownloadOnDN(){

        String sqlForDisableImageDownloadOnDn =
                "UPDATE "+ NewsServerDBSchema.SettingsDataTable.NAME+
                " SET "+NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME+
                " = "+NewsServerDBSchema.IMAGE_DOWNLOAD_ON_DN_DISABLE_FLAG+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                " = "+NewsServerDBSchema.IMAGE_DOWNLOAD_ON_DN_SETTINGS_ENTRY_ID;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForDisableImageDownloadOnDn);
        } catch (SQLException e) {
            NewsServerUtility.logErrorMessage("SettingsUtility: "+e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean enableNavigationMenuDisplay(){

        String sqlForEnableImageDownloadOnDn =
                "UPDATE "+ NewsServerDBSchema.SettingsDataTable.NAME+
                " SET "+NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME+
                " = "+NewsServerDBSchema.NAVIGATION_MENU_DISPLAY_ENABLE_FLAG+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                " = "+NewsServerDBSchema.NAVIGATION_MENU_DISPLAY_SETTINGS_ENTRY_ID;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForEnableImageDownloadOnDn);
        } catch (SQLException e) {
            NewsServerUtility.logErrorMessage("SettingsUtility: "+e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean disableNavigationMenuDisplay(){

        String sqlForDisableImageDownloadOnDn =
                "UPDATE "+ NewsServerDBSchema.SettingsDataTable.NAME+
                " SET "+NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME+
                " = "+NewsServerDBSchema.NAVIGATION_MENU_DISPLAY_DISABLE_FLAG+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                " = "+NewsServerDBSchema.NAVIGATION_MENU_DISPLAY_SETTINGS_ENTRY_ID;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForDisableImageDownloadOnDn);
        } catch (SQLException e) {
            NewsServerUtility.logErrorMessage("SettingsUtility: "+e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static int getFrequentlyViewedListSizeValueSetting(){

        String sqlForSettingRead =
                "SELECT * FROM "+ NewsServerDBSchema.SettingsDataTable.NAME+
                        " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                        " = "+NewsServerDBSchema.FREQUENTLY_VIEWED_LIST_SIZE_SETTINGS_ENTRY_ID;

        //Log.d("StackTrace", "getFrequentlyViewedListSizeValueSetting: sqlForSettingRead: "+sqlForSettingRead);

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        int settingsValue = NewsServerDBSchema.FREQUENTLY_VIEWED_LIST_SIZE_DEFAULT_VALUE;

        try(Cursor cursor = dbCon.rawQuery(sqlForSettingRead,null)){
            if (cursor.getCount() == 1){
                cursor.moveToFirst();
                try {
                    settingsValue = cursor.getInt(cursor.getColumnIndex(
                            NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception ex){
            NewsServerUtility.logErrorMessage("SettingsUtility: "+ex.getMessage());
            ex.printStackTrace();
        }
        //Log.d("StackTrace", "getFrequentlyViewedListSizeValueSetting: settingsValue: "+settingsValue);

        return settingsValue;
    }

    public static void stopDownLoaders(){

        EditionLoaderService.stop();

        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while (EditionLoaderService.isRunning());

        ArticleLoaderService.stopArticleLoaderService();

        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while (ArticleLoaderService.isRunning());

    }

    public static boolean restoreFactoySettings() {
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        //PowerManager.WakeLock wakeLock = NewsServerUtility.getWakeLock();
        NewsServerUtility.setWakeLock();
        try {
            dbCon.beginTransaction();

            String sqlForNewspaperReset =
                    "UPDATE "+NewsServerDBSchema.NewsPaperTable.NAME+
                    " SET "+NewsServerDBSchema.NewsPaperTable.Cols.IsActive.NAME+
                    " = "+NewsServerDBSchema.ITEM_ACTIVE_FLAG;
            //Log.d(TAG, "sqlForNewspaperReset:"+sqlForNewspaperReset);
            dbCon.execSQL(sqlForNewspaperReset);

            String sqlForFeatureReset =
                    "UPDATE "+NewsServerDBSchema.FeatureTable.NAME+
                    " SET "+NewsServerDBSchema.FeatureTable.Cols.IsActive.NAME+" = "+
                    NewsServerDBSchema.ITEM_ACTIVE_FLAG+","+
                    NewsServerDBSchema.FeatureTable.Cols.ArticleReadCount.NAME+" =0,"+
                    NewsServerDBSchema.FeatureTable.Cols.IsFavourite.NAME+" = "+
                    NewsServerDBSchema.ITEM_INACTIVE_FLAG;
            //Log.d(TAG, "sqlForFeatureReset:"+sqlForFeatureReset);
            dbCon.execSQL(sqlForFeatureReset);

            String sqlToClearFeatureGroupEntryTable=
                    "DELETE FROM "+NewsServerDBSchema.FeatureGroupEntryTable.NAME;
            //Log.d(TAG, "sqlToClearFeatureGroupEntryTable:"+sqlToClearFeatureGroupEntryTable);
            dbCon.execSQL(sqlToClearFeatureGroupEntryTable);

            String sqlToClearFeatureGroupTable=
                    "DELETE FROM "+NewsServerDBSchema.FeatureGroupTable.NAME;
            //Log.d(TAG, "sqlToClearFeatureGroupTable:"+sqlToClearFeatureGroupTable);
            dbCon.execSQL(sqlToClearFeatureGroupTable);

            String sqlToClearSettingsValueTable=
                    "DELETE FROM "+NewsServerDBSchema.SettingsDataTable.NAME;
            //Log.d(TAG, "sqlToClearSettingsValueTable:"+sqlToClearSettingsValueTable);
            dbCon.execSQL(sqlToClearSettingsValueTable);

            String sqlToClearNotificationInfoTable =
                    "DELETE FROM "+NewsServerDBSchema.NotificationInfoTable.NAME;
            //Log.d(TAG, "sqlToClearNotificationInfoTable:"+sqlToClearNotificationInfoTable);
            dbCon.execSQL(sqlToClearNotificationInfoTable);

            dbCon.setTransactionSuccessful();

        } catch (Exception ex){
            ex.printStackTrace();
            //Log.d(TAG, "restoreFactoySettings: Error: "+ex.getMessage());
            return false;
        } finally {
            if (dbCon.inTransaction()){
                dbCon.endTransaction();
            }
        }
        try {
            NewsServerDBOpenHelper.loadDataFromSqlFile(R.raw.news_server_config_data);
        }catch (Exception ex){
            ex.printStackTrace();
            //Log.d(TAG, "restoreFactoySettings: Error: "+ex.getMessage());
            return false;
        }
        NewsServerUtility.releaseWakeLock();
        return true;
    }
    public static void clearCacheData(){

        //PowerManager.WakeLock wakeLock = NewsServerUtility.getWakeLock();
        NewsServerUtility.setWakeLock();

        try {
            SettingsUtility.stopDownLoaders();
            DiskCleaner.deleteUnsavedArticleInfo();
            DiskCleaner.clearCachedDataAction();
        }catch (Exception ex){
            ex.printStackTrace();
            //Log.d(TAG, "clearCacheData: Error: "+ex.getMessage());
        }

        NewsServerUtility.releaseWakeLock();
    }


    public static void deleteSavedArticlesAction(Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(DELETE_SAVED_ARTICLES_PROMPT)
                .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                    if (ArticleHelper.deleteAllSavedArticles()){
                        DisplayUtility.showShortToast(SAVED_ARTICLES_DELETED_TOAST_MESSAGE);
                    }else {
                        DisplayUtility.showShortToast(FAILURE_TOAST_MESSAGE);
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }



    public static int getArticleTextFontSizeSettingValue(){

        String sqlForSettingRead =
                "SELECT * FROM "+ NewsServerDBSchema.SettingsDataTable.NAME+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                " = "+NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_SETTINGS_ENTRY_ID;

        //Log.d("SettingsUtility", "getArticleTextFontSizeSettingValue: sqlForSettingRead: "+sqlForSettingRead);

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        int settingsValue = 0;

        try(Cursor cursor = dbCon.rawQuery(sqlForSettingRead,null)){
            if (cursor.getCount() == 1){
                cursor.moveToFirst();
                try {
                    settingsValue = cursor.getInt(cursor.getColumnIndex(
                            NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception ex){
            //Log.d("SettingsUtility","Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        //Log.d("SettingsUtility", "settingsValue: "+settingsValue);
        return settingsValue;
    }

    public static boolean setArticleTextFontSizeSettingValue(int desired_article_text_font_size){

        String sqlForSetArticleTextFontSizeSettingValue =
                "UPDATE "+ NewsServerDBSchema.SettingsDataTable.NAME+
                " SET "+NewsServerDBSchema.SettingsDataTable.Cols.ItemValue.NAME+
                " = "+desired_article_text_font_size+
                " WHERE "+NewsServerDBSchema.SettingsDataTable.Cols.Id.NAME+
                " = "+NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_SETTINGS_ENTRY_ID;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForSetArticleTextFontSizeSettingValue);
        } catch (SQLException e) {
            //Log.d("SettingsUtility","Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean saveCheckConfigIntegrityResult(String reportText) {

        String sqlForInsertingConfigIntegrityCheckResult =
            "INSERT INTO "+ NewsServerDBSchema.ConfigIntegrityCheckReportTable.NAME+
            " ("+NewsServerDBSchema.ConfigIntegrityCheckReportTable.Cols.EntryTs.NAME+","+
            NewsServerDBSchema.ConfigIntegrityCheckReportTable.Cols.ReportDetails.NAME+")"+
            " VALUES ("+System.currentTimeMillis()+",'"+reportText+"');";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForInsertingConfigIntegrityCheckResult);
        } catch (SQLException e) {
            //Log.d("SettingsUtility","Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static long getLastSuccessCheckedTimeForParentFeature(Feature parentFeature){
        if (parentFeature.getParentFeatureId() != NewsServerDBSchema.NULL_PARENT_FEATURE_ID){
            return 0L;
        }
        String sqlForLastCheckedTime =
                "SELECT "+NewsServerDBSchema.ParentFeatureCheckLog.Cols.EntryTs.NAME+
                " FROM "+NewsServerDBSchema.ParentFeatureCheckLog.NAME+
                " WHERE "+NewsServerDBSchema.ParentFeatureCheckLog.Cols.ParentFeatureId.NAME+
                " = "+parentFeature.getId()+
                " AND "+NewsServerDBSchema.ParentFeatureCheckLog.Cols.CheckStatus.NAME+
                " = "+NewsServerDBSchema.POSITIVE_STATUS+
                " ORDER BY "+NewsServerDBSchema.ParentFeatureCheckLog.Cols.Id.NAME+
                " DESC LIMIT 1;";
        //Log.d("CheckConfigIntegrity", "getLastSuccessCheckedTimeForParentFeature: sqlForLastCheckedTime: "+sqlForLastCheckedTime);
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        try (Cursor cursor = dbCon.rawQuery(sqlForLastCheckedTime,null)){
            if (cursor.getCount()==1){
                //Log.d("CheckConfigIntegrity", "getLastSuccessCheckedTimeForParentFeature: cursor.getCount()==1");
                cursor.moveToFirst();
                return cursor.getLong(0);
            }
        }catch (Exception ex){
            //Log.d(TAG, "getLastCheckedTimeForParentFeature: Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        return 0L;
    }

    private static long getLastFailueCheckedTimeForParentFeature(Feature parentFeature){
        if (parentFeature.getParentFeatureId() != NewsServerDBSchema.NULL_PARENT_FEATURE_ID){
            return 0L;
        }
        String sqlForLastCheckedTime =
                "SELECT "+NewsServerDBSchema.ParentFeatureCheckLog.Cols.EntryTs.NAME+
                " FROM "+NewsServerDBSchema.ParentFeatureCheckLog.NAME+
                " WHERE "+NewsServerDBSchema.ParentFeatureCheckLog.Cols.ParentFeatureId.NAME+
                " = "+parentFeature.getId()+
                " AND "+NewsServerDBSchema.ParentFeatureCheckLog.Cols.CheckStatus.NAME+
                " = "+NewsServerDBSchema.NEGETIVE_STATUS+
                " ORDER BY "+NewsServerDBSchema.ParentFeatureCheckLog.Cols.Id.NAME+
                " DESC LIMIT 1;";
        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();
        try (Cursor cursor = dbCon.rawQuery(sqlForLastCheckedTime,null)){
            if (cursor.getCount()==1){
                cursor.moveToFirst();
                return cursor.getLong(0);
            }
        }catch (Exception ex){
            //Log.d(TAG, "getLastCheckedTimeForParentFeature: Error: "+ex.getMessage());
            ex.printStackTrace();
        }
        return 0L;
    }

    public static boolean entryParentFeatureCheckSucess(Feature checkedFeature,Feature parentFeature){
        if (    parentFeature==null||
                (parentFeature.getParentFeatureId() != NewsServerDBSchema.NULL_PARENT_FEATURE_ID) ||
                checkedFeature==null){
            return false;
        }
        String sqlForEntry =
                "INSERT INTO "+NewsServerDBSchema.ParentFeatureCheckLog.NAME+" ("+
                NewsServerDBSchema.ParentFeatureCheckLog.Cols.EntryTs.NAME+","+
                NewsServerDBSchema.ParentFeatureCheckLog.Cols.ParentFeatureId.NAME+","+
                NewsServerDBSchema.ParentFeatureCheckLog.Cols.CheckedFeatureId.NAME+")"+
                " VALUES ("+
                System.currentTimeMillis()+","+
                parentFeature.getId()+","+
                checkedFeature.getId()+");";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForEntry);
        } catch (SQLException e) {
            //Log.d("SettingsUtility","Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean entryParentFeatureCheckFailure(Feature checkedFeature,Feature parentFeature){
        if (    parentFeature==null||
                (parentFeature.getParentFeatureId() != NewsServerDBSchema.NULL_PARENT_FEATURE_ID) ||
                checkedFeature==null){
            return false;
        }
        String sqlForEntry =
                "INSERT INTO "+NewsServerDBSchema.ParentFeatureCheckLog.NAME+" ("+
                NewsServerDBSchema.ParentFeatureCheckLog.Cols.EntryTs.NAME+","+
                NewsServerDBSchema.ParentFeatureCheckLog.Cols.CheckStatus.NAME+","+
                NewsServerDBSchema.ParentFeatureCheckLog.Cols.ParentFeatureId.NAME+","+
                NewsServerDBSchema.ParentFeatureCheckLog.Cols.CheckedFeatureId.NAME+")"+
                " VALUES ("+
                System.currentTimeMillis()+","+
                NewsServerDBSchema.NEGETIVE_STATUS+","+
                parentFeature.getId()+","+
                checkedFeature.getId()+");";

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForEntry);
        } catch (SQLException e) {
            //Log.d("SettingsUtility","Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean needToCheckParentFeature(Feature currentParentFeature) {
        long lastSuccessCheckTime = getLastSuccessCheckedTimeForParentFeature(currentParentFeature);
        long lastFailureCheckTime = getLastFailueCheckedTimeForParentFeature(currentParentFeature);
        if (lastSuccessCheckTime == 0L && lastFailureCheckTime==0L){
            return true;
        } else if (lastFailureCheckTime>lastSuccessCheckTime){
            return true;
        } else if (System.currentTimeMillis() - lastSuccessCheckTime >
                    SettingsUtility.PARENT_FEATURE_CHECK_INTERVAL_MILLIS){
            return true;
        }
        return false;
    }

    public static boolean clearConfigIntegrityCheckReportSummaryData() {

        String sqlForDataDelete =
                "DELETE FROM "+NewsServerDBSchema.ConfigIntegrityCheckReportTable.NAME;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForDataDelete);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean clearParentFeatureCheckData() {

        String sqlForDataDelete =
                "DELETE FROM "+NewsServerDBSchema.ParentFeatureCheckLog.NAME;

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try {
            dbCon.execSQL(sqlForDataDelete);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

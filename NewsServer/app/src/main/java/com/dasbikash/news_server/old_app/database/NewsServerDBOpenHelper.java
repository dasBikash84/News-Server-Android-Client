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

package com.dasbikash.news_server.old_app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.RawRes;
import android.util.Log;


import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.dasbikash.news_server.old_app.database.NewsServerDBSchema.*;

public class NewsServerDBOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "StackTrace";

    private static final int VERSION = 4;
    private static final String DATABASE_NAME = "news_server.db";
    public static final int FILE_BUFFER_WRITER_SIZE = 1024 * 1024 * 20;

    private static boolean sNewDatabaseCreated=false;

    public NewsServerDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    public static boolean isNewDatabaseCreated() {
        if (sNewDatabaseCreated){
            sNewDatabaseCreated = false;
            return true;
        }
        return false;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        createCoreTables(db);
        createDeveloperTables(db);

        loadDataFromSqlFile(db,R.raw.news_server_core_data);
        loadDataFromSqlFile(db,R.raw.news_server_config_data);
        sNewDatabaseCreated = true;
    }

    private void createCoreTables(SQLiteDatabase db) {
        // Create continent table
        //Log.d(TAG, "createCoreTables: ");

        db.execSQL(
                "create table IF NOT EXISTS "+ ContinentTable.NAME + "("+
                    ContinentTable.Cols.Id.NAME + " " + ContinentTable.Cols.Id.TYPE + ", "+
                    ContinentTable.Cols.Name.NAME + " " + ContinentTable.Cols.Name.TYPE +
                ");"
        );

        // Create country table

        db.execSQL(
                "create table IF NOT EXISTS "+ CountryTable.NAME + "("+
                    CountryTable.Cols.Name.NAME + " " + CountryTable.Cols.Name.TYPE +", "+
                    CountryTable.Cols.CodeName.NAME + " " + CountryTable.Cols.CodeName.TYPE +", "+
                    CountryTable.Cols.ContinentId.NAME + " " + CountryTable.Cols.ContinentId.TYPE +", "+
                    CountryTable.Cols.TimeZoneId.NAME + " " + CountryTable.Cols.TimeZoneId.TYPE +", "+
                    "foreign key ("+CountryTable.Cols.ContinentId.NAME+") references "+CountryTable.Cols.ContinentId.FOREIGN_KEY +
                ");"
        );

        // Create language table

        db.execSQL(
                "create table IF NOT EXISTS "+ LanguageTable.NAME + "("+
                    LanguageTable.Cols.Id.NAME + " " + LanguageTable.Cols.Id.TYPE + ", "+
                    LanguageTable.Cols.Name.NAME + " " + LanguageTable.Cols.Name.TYPE +
                ");"
        );

        // Create news_paper table

        db.execSQL(
                "create table IF NOT EXISTS "+ NewsPaperTable.NAME + "("+
                    NewsPaperTable.Cols.Id.NAME + " " + NewsPaperTable.Cols.Id.TYPE +", "+
                    NewsPaperTable.Cols.Name.NAME + " " + NewsPaperTable.Cols.Name.TYPE +", "+
                    NewsPaperTable.Cols.CountryName.NAME + " " + NewsPaperTable.Cols.CountryName.TYPE +", "+
                    NewsPaperTable.Cols.LanguageId.NAME + " " + NewsPaperTable.Cols.LanguageId.TYPE +", "+
                    NewsPaperTable.Cols.IsActive.NAME + " " + NewsPaperTable.Cols.IsActive.TYPE +", "+
                    "foreign key ("+NewsPaperTable.Cols.CountryName.NAME+") references "+NewsPaperTable.Cols.CountryName.FOREIGN_KEY +", "+
                    "foreign key ("+NewsPaperTable.Cols.LanguageId.NAME+") references "+NewsPaperTable.Cols.LanguageId.FOREIGN_KEY +
                ");"
        );


        // Create features table
        db.execSQL(
                "create table IF NOT EXISTS "+ FeatureTable.NAME + "("+
                        FeatureTable.Cols.Id.NAME + " " + FeatureTable.Cols.Id.TYPE + ", "+
                        FeatureTable.Cols.NewsPaperId.NAME + " " + FeatureTable.Cols.NewsPaperId.TYPE + ", "+
                        FeatureTable.Cols.ParentFeatureId.NAME + " " + FeatureTable.Cols.ParentFeatureId.TYPE + ", "+
                        FeatureTable.Cols.Title.NAME + " " + FeatureTable.Cols.Title.TYPE + ", "+
                        FeatureTable.Cols.IsWeekly.NAME + " " +FeatureTable.Cols.IsWeekly.TYPE +", "+
                        FeatureTable.Cols.WeeklyPublicationDay.NAME + " " +FeatureTable.Cols.WeeklyPublicationDay.TYPE +", "+
                        FeatureTable.Cols.LinkFormat.NAME + " " + FeatureTable.Cols.LinkFormat.TYPE +", "+
                        FeatureTable.Cols.LinkVariablePartFormat.NAME + " " + FeatureTable.Cols.LinkVariablePartFormat.TYPE +", "+
                        FeatureTable.Cols.FirstEditionDateString.NAME + " " +FeatureTable.Cols.FirstEditionDateString.TYPE +", "+
                        FeatureTable.Cols.ArticleReadCount.NAME + " " +FeatureTable.Cols.ArticleReadCount.TYPE +", "+
                        FeatureTable.Cols.IsActive.NAME + " " + FeatureTable.Cols.IsActive.TYPE +", "+
                        FeatureTable.Cols.IsFavourite.NAME + " " + FeatureTable.Cols.IsFavourite.TYPE +", "+
                        "foreign key ("+FeatureTable.Cols.NewsPaperId.NAME+") references "+FeatureTable.Cols.NewsPaperId.FOREIGN_KEY +
                ");"
        );

        // Create feature_group table

        db.execSQL(
                "create table IF NOT EXISTS "+ FeatureGroupTable.NAME + "("+
                    FeatureGroupTable.Cols.Id.NAME + " " + FeatureGroupTable.Cols.Id.TYPE +", "+
                    FeatureGroupTable.Cols.Title.NAME + " " + FeatureGroupTable.Cols.Title.TYPE +", "+
                    FeatureGroupTable.Cols.GroupCategoryidentifier.NAME + " " + FeatureGroupTable.Cols.GroupCategoryidentifier.TYPE +", "+
                    FeatureGroupTable.Cols.IsActive.NAME + " " + FeatureGroupTable.Cols.IsActive.TYPE +", "+
                    "UNIQUE ("+FeatureGroupTable.Cols.Title.NAME+")"+
                ");"
        );

        // Create feature_group_entry table

        db.execSQL(
                "create table IF NOT EXISTS "+ FeatureGroupEntryTable.NAME + "("+
                        FeatureGroupEntryTable.Cols.Id.NAME + " " + FeatureGroupEntryTable.Cols.Id.TYPE +", "+
                        FeatureGroupEntryTable.Cols.GroupId.NAME + " " + FeatureGroupEntryTable.Cols.GroupId.TYPE +", "+
                        FeatureGroupEntryTable.Cols.MemberFeatureId.NAME + " " + FeatureGroupEntryTable.Cols.MemberFeatureId.TYPE +", "+
                        "foreign key ("+FeatureGroupEntryTable.Cols.GroupId.NAME+") references "+FeatureGroupEntryTable.Cols.GroupId.FOREIGN_KEY +", "+
                        "foreign key ("+ FeatureGroupEntryTable.Cols.MemberFeatureId.NAME+") references "+ FeatureGroupEntryTable.Cols.MemberFeatureId.FOREIGN_KEY +", "+
                        "UNIQUE ("+FeatureGroupEntryTable.Cols.GroupId.NAME+","+FeatureGroupEntryTable.Cols.MemberFeatureId.NAME+")"+
                ");"
        );

        // Create texts table

        db.execSQL(
                "create table IF NOT EXISTS "+ TextTable.NAME + "("+
                        TextTable.Cols.Id.NAME + " " + TextTable.Cols.Id.TYPE + ", "+
                        TextTable.Cols.Content.NAME + " " + TextTable.Cols.Content.TYPE +
                ");"
        );

        // Create images table

        db.execSQL(
                "create table IF NOT EXISTS "+ ImageTable.NAME + "("+
                        ImageTable.Cols.Id.NAME + " " + ImageTable.Cols.Id.TYPE + ", "+
                        ImageTable.Cols.WebLink.NAME + " " + ImageTable.Cols.WebLink.TYPE + ", "+
                        ImageTable.Cols.DiskLocation.NAME + " " + ImageTable.Cols.DiskLocation.TYPE + ", "+
                        ImageTable.Cols.AltText.NAME + " " + ImageTable.Cols.AltText.TYPE + ", "+
                        //ImageTable.Cols.LastUpdateTimeStamp.NAME + " " + ImageTable.Cols.LastUpdateTimeStamp.TYPE + ", "+
                        ImageTable.Cols.SizeKB.NAME + " " + ImageTable.Cols.SizeKB.TYPE +
                ");"
        );

        // Create ArticleTable table

        db.execSQL(
                "create table IF NOT EXISTS "+ ArticleTable.NAME + "("+
                        ArticleTable.Cols.Id.NAME + " " + ArticleTable.Cols.Id.TYPE + ", "+
                        ArticleTable.Cols.FeatureId.NAME + " " + ArticleTable.Cols.FeatureId.TYPE + ", "+
                        ArticleTable.Cols.ArticleLink.NAME + " " + ArticleTable.Cols.ArticleLink.TYPE + ", "+
                        ArticleTable.Cols.ArticleTitle.NAME + " " + ArticleTable.Cols.ArticleTitle.TYPE + ", "+
                        //ArticleTable.Cols.ArticleSummary.NAME + " " + ArticleTable.Cols.ArticleSummary.TYPE + ", "+
                        ArticleTable.Cols.PreviewImageId.NAME + " " + ArticleTable.Cols.PreviewImageId.TYPE +", "+
                        ArticleTable.Cols.LinkHashCode.NAME + " " + ArticleTable.Cols.LinkHashCode.TYPE +", "+
                        ArticleTable.Cols.PublicationTimeStamp.NAME + " " + ArticleTable.Cols.PublicationTimeStamp.TYPE +", "+
                        ArticleTable.Cols.LastModificationTimeStamp.NAME + " " + ArticleTable.Cols.LastModificationTimeStamp.TYPE +", "+
                        ArticleTable.Cols.SavedLocally.NAME + " " + ArticleTable.Cols.SavedLocally.TYPE + ", "+
                        "foreign key ("+ArticleTable.Cols.FeatureId.NAME+") references "+ArticleTable.Cols.FeatureId.FOREIGN_KEY +
                ");"
        );

        // Create ArticleFragmentTable table

        db.execSQL(
                "create table IF NOT EXISTS "+ ArticleFragmentTable.NAME + "("+
                        ArticleFragmentTable.Cols.Id.NAME + " " + ArticleFragmentTable.Cols.Id.TYPE +", "+
                        ArticleFragmentTable.Cols.ArticleLinkHashCode.NAME + " " + ArticleFragmentTable.Cols.ArticleLinkHashCode.TYPE + ", "+
                        ArticleFragmentTable.Cols.TextId.NAME + " " + ArticleFragmentTable.Cols.TextId.TYPE + ", "+
                        ArticleFragmentTable.Cols.ImageId.NAME + " " + ArticleFragmentTable.Cols.ImageId.TYPE + ", "+
                        "foreign key ("+ArticleFragmentTable.Cols.TextId.NAME+") references "+ArticleFragmentTable.Cols.TextId.FOREIGN_KEY +","+
                        "foreign key ("+ArticleFragmentTable.Cols.ImageId.NAME+") references "+ArticleFragmentTable.Cols.ImageId.FOREIGN_KEY +
                ");"
        );

        // Create Settings table

        db.execSQL(
                "create table IF NOT EXISTS "+ SettingsDataTable.NAME + "("+
                        SettingsDataTable.Cols.Id.NAME + " " + ArticleFragmentTable.Cols.Id.TYPE +", "+
                        SettingsDataTable.Cols.ItemDescription.NAME + " " + SettingsDataTable.Cols.ItemDescription.TYPE + ", "+
                        SettingsDataTable.Cols.ItemValue.NAME + " " + SettingsDataTable.Cols.ItemValue.TYPE+
                ");"
        );

        db.execSQL(
                "create table IF NOT EXISTS "+ NotificationInfoTable.NAME + "("+
                        NotificationInfoTable.Cols.FeatureId.NAME + " " + NotificationInfoTable.Cols.FeatureId.TYPE +", "+
                        NotificationInfoTable.Cols.IsActive.NAME + " " + NotificationInfoTable.Cols.IsActive.TYPE +", "+
                        NotificationInfoTable.Cols.InclusionFilter.NAME + " " + NotificationInfoTable.Cols.ExclusionFilter.TYPE +", "+
                        NotificationInfoTable.Cols.ExclusionFilter.NAME + " " + NotificationInfoTable.Cols.ExclusionFilter.TYPE +", "+
                        "foreign key ("+NotificationInfoTable.Cols.FeatureId.NAME+") references "+NotificationInfoTable.Cols.FeatureId.FOREIGN_KEY +
                ");"
        );
    }

    private void createDeveloperTables(SQLiteDatabase db) {
        if (NewsServerUtility.isDeveloperModeOn()){
            db.execSQL(
                    "create table IF NOT EXISTS "+ ConfigIntegrityCheckReportTable.NAME + "("+
                            ConfigIntegrityCheckReportTable.Cols.Id.NAME + " " + ConfigIntegrityCheckReportTable.Cols.Id.TYPE +", "+
                            ConfigIntegrityCheckReportTable.Cols.EntryTs.NAME + " " + ConfigIntegrityCheckReportTable.Cols.EntryTs.TYPE +", "+
                            ConfigIntegrityCheckReportTable.Cols.ReportDetails.NAME + " " + ConfigIntegrityCheckReportTable.Cols.ReportDetails.TYPE+
                            ");"
            );
            db.execSQL(
                    "create table IF NOT EXISTS "+ ParentFeatureCheckLog.NAME + "("+
                            ParentFeatureCheckLog.Cols.Id.NAME + " " + ParentFeatureCheckLog.Cols.Id.TYPE +", "+
                            ParentFeatureCheckLog.Cols.EntryTs.NAME + " " + ParentFeatureCheckLog.Cols.EntryTs.TYPE +", "+
                            ParentFeatureCheckLog.Cols.ParentFeatureId.NAME + " " + ParentFeatureCheckLog.Cols.ParentFeatureId.TYPE +", "+
                            ParentFeatureCheckLog.Cols.CheckedFeatureId.NAME + " " + ParentFeatureCheckLog.Cols.CheckedFeatureId.TYPE +", "+
                            ParentFeatureCheckLog.Cols.CheckStatus.NAME + " " + ParentFeatureCheckLog.Cols.CheckStatus.TYPE+
                            ");"
            );
        }
    }

    public static void loadDataFromSqlFile(@RawRes int rawResId) {
        loadDataFromSqlFile(NewsServerUtility.getDatabaseCon(),rawResId);
    }

    public static void loadDataFromSqlFile(SQLiteDatabase sqLiteDatabase,@RawRes int rawResId) {

        try(InputStream inputStream = NewsServerUtility.getContext().getResources().openRawResource(rawResId);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(FILE_BUFFER_WRITER_SIZE)
        ){
            int readByteCount;
            do {
                byte[] readBytes = new byte[1024];
                readByteCount = inputStream.read(readBytes);
                byteArrayOutputStream.write(readBytes);
            }while (readByteCount != -1);

            String sql = byteArrayOutputStream.toString();
            sql=sql.trim();
            Log.d(TAG, "loadDataFromSqlFile: "+sql);
            if (sql.trim().length() == 0) return;
            sqLiteDatabase.beginTransaction();
            try{
                for (String str:
                        sql.split(";\n")) {
                    if (str.trim().length() == 0) break;
                    sqLiteDatabase.execSQL(str);
                }
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception ex){
                Log.d(TAG, "Error: "+ ex.getMessage());
            } finally {
                if (sqLiteDatabase.inTransaction()){
                    sqLiteDatabase.endTransaction();
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
            Log.d(TAG, "Error: "+ ex.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersionId, int newVersionId) {

        createCoreTables(sqLiteDatabase);
        createDeveloperTables(sqLiteDatabase);

        if (oldVersionId == 1 && newVersionId==4){
            loadDataFromSqlFile(sqLiteDatabase,R.raw.news_server_core_data_1_to_4);
            loadDataFromSqlFile(sqLiteDatabase,R.raw.news_server_config_data_1_to_4);
            //Log.d(TAG, "onUpgrade: news_server_config_data_1_to_4");
        }else if (oldVersionId == 2 && newVersionId==4){
            loadDataFromSqlFile(sqLiteDatabase,R.raw.news_server_core_data_2_to_4);
            loadDataFromSqlFile(sqLiteDatabase,R.raw.news_server_config_data_2_to_4);
            //Log.d(TAG, "onUpgrade: news_server_config_data_2_to_4");
        }else if (oldVersionId == 3 && newVersionId==4){
            loadDataFromSqlFile(sqLiteDatabase,R.raw.news_server_core_data_3_to_4);
            loadDataFromSqlFile(sqLiteDatabase,R.raw.news_server_config_data_3_to_4);
            //Log.d(TAG, "onUpgrade: news_server_config_data_2_to_4");
        }
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}

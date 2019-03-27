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
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.PowerManager;
import android.view.MenuItem;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderService;
import com.dasbikash.news_server.old_app.database.NewsServerDBOpenHelper;
import com.dasbikash.news_server.old_app.image_downloader.ImageDownloader;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_view.AboutAppActivity;
import com.dasbikash.news_server.old_app.this_view.ArticleListActivity;
import com.dasbikash.news_server.old_app.this_view.SettingsActivity;
import com.dasbikash.news_server.R;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.POWER_SERVICE;

public final class NewsServerUtility {

    private static final String TAG = "NSUtility";
    private static SQLiteDatabase sSQLiteDatabase;
    private static Context sContext;
    private static boolean sAppOnForeGround = false;

    ////////////// Developer mode enabler
    private static final boolean DEVELOPER_MODE_ON = false;
    ///////////////////////////////////////////////


    public static final long DEFAULT_MINIMUM_SITE_ACCESS_DELAY_MILLIS = 500L;
    private static final long[][] DEC_MINIMUM_SITE_ACCESS_DELAY_MILLIS_FOR_NP=
            {   {NewsServerDBSchema.NEWSPAPER_ID_PROTHOM_ALO,2000L}
            };

    private static final HashMap<Integer,Long> sLastAccessTsForNewspaper = new HashMap<>();
    private static final HashMap<Integer,Long>
                                            sMinimumAccesDelayMillisForNps =new HashMap<>();


    private static final String sSeperateDomainIdentifierRegex = "^//.+";
    private static final String sInvalidHTTPProtocol = "http:";
    private static PowerManager.WakeLock sWakeLock=null;

    static {
        for (int i = 0; i < DEC_MINIMUM_SITE_ACCESS_DELAY_MILLIS_FOR_NP.length; i++) {
            sMinimumAccesDelayMillisForNps.put(
                    (int)DEC_MINIMUM_SITE_ACCESS_DELAY_MILLIS_FOR_NP[i][0],
                    DEC_MINIMUM_SITE_ACCESS_DELAY_MILLIS_FOR_NP[i][1]
            );
        }
    }

    private static String getSiteHTTPProtocol(String siteBaseAddress){

        if (siteBaseAddress.matches("^https:.+")){
            return "https:";
        } else  if(siteBaseAddress.matches("^http:.+")){
            return "http:";
        } else {
            return null;
        }
    }

    public static String processLink(String linkText,String siteBaseAddress){

        String siteHTTPString = getSiteHTTPProtocol(siteBaseAddress);

        if (siteHTTPString == null) return null;

        if (linkText.contains(siteHTTPString)) return linkText;

        if (linkText.contains(sInvalidHTTPProtocol)){
            return linkText.replace(sInvalidHTTPProtocol,siteHTTPString);
        }

        if (linkText.matches(sSeperateDomainIdentifierRegex)){
            return siteHTTPString+linkText;
        }
        if(!linkText.matches("^/.+")){
            linkText = "/"+linkText;
        }
        return siteBaseAddress+linkText;
    }

    public static boolean checkIfCanAccessSite(Newspaper newspaper){

        if (newspaper != null) {
            synchronized (sLastAccessTsForNewspaper) {

                long currentSystemTimeMillis = System.currentTimeMillis();

                if (!sLastAccessTsForNewspaper.containsKey(newspaper.getId())) {
                    sLastAccessTsForNewspaper.put(newspaper.getId(), currentSystemTimeMillis);
                    return true;
                } else {
                    if (currentSystemTimeMillis >= sLastAccessTsForNewspaper.get(newspaper.getId()) +
                            sMinimumAccesDelayMillisForNps.get(newspaper.getId())) {
                        sLastAccessTsForNewspaper.put(newspaper.getId(), currentSystemTimeMillis);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isAppOnForeGround() {
        return sAppOnForeGround;
    }

    public static void setAppOnForeGround(boolean appOnForeGround) {
        sAppOnForeGround = appOnForeGround;
    }

    public static boolean isDeveloperModeOn() {
        return DEVELOPER_MODE_ON &&
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }
    /*public static boolean isNotificationEnabled() {
        return sNotificationEnabled;
    }

    public static boolean isOnBackGround(){
        return sNotificationEnabled;
    }

    public static void enableNotification() {
        sNotificationEnabled = true;
    }

    public static void disableNotification() {
        sNotificationEnabled = false;
    }*/

    public static SQLiteDatabase getDatabaseCon(){
        if (sSQLiteDatabase == null || !sSQLiteDatabase.isOpen()){
            sSQLiteDatabase = new NewsServerDBOpenHelper(sContext).getWritableDatabase();
        }
        return sSQLiteDatabase;
    }

    public static void init(Context context){

        setContext(context);
        getDatabaseCon();

        NetConnectivityUtility.init();
        SettingsUtility.clearCacheData();

        ImageDownloader.reset();
        EditionLoaderBase.reset();
        EditionLoaderService.reset();

        ArrayList<Newspaper> newspapers = NewspaperHelper.getAllNewspapers();
        for (Newspaper newspaper :
                newspapers) {
            if (!sMinimumAccesDelayMillisForNps.containsKey(newspaper.getId())) {
                sMinimumAccesDelayMillisForNps.put(newspaper.getId(), DEFAULT_MINIMUM_SITE_ACCESS_DELAY_MILLIS);
            }
        }

        if (isDeveloperModeOn() && NewsServerUtility.isAppOnForeGround() &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CheckConfigIntegrity.init(context);
        }
    }

    public static Context getContext(){
        return sContext;
    }

    public static void setContext(Context context){
        if (sContext == null && context!=null){
            sContext = context;
        }
    }

    public static boolean handleBasicOptionMenuItemActions(MenuItem item, Activity activity) {

        switch (item.getItemId()){
            case R.id.settings_menu_item:
                settingsMenuItemAction(activity);
                //Log.d(TAG, "handleBasicOptionMenuItemActions: settings_menu_item");
                return true;
            case R.id.share_app_menu_item:
                shareAppMenuItemAction(activity);
                //Log.d(TAG, "handleBasicOptionMenuItemActions: share_menu_item");
                return true;
            /*case R.id.about_app_menu_item:
                aboutAppMenuItemAction(activity);
                //Log.d(TAG, "handleBasicOptionMenuItemActions: about_app_menu_item");
                return true;*/
            case R.id.email_developer_menu_item:
                emailDeveloperMenuItemAction(activity);
                //Log.d(TAG, "handleBasicOptionMenuItemActions: about_app_menu_item");
                return true;
            default:
                return false;
        }
    }

    public static void emailDeveloperMenuItemAction(Activity activity){
        activity.startActivity(OptionsIntentBuilderUtility.getEmailDeveloperIntent(activity));
    }

    public static void aboutAppMenuItemAction(Activity activity) {
        Intent intent = new Intent(activity, AboutAppActivity.class);
        activity.startActivity(intent);
    }

    public static void shareAppMenuItemAction(Activity activity) {
        activity.startActivity(OptionsIntentBuilderUtility.getShareAppIntent(activity));
    }

    public static void settingsMenuItemAction(Activity activity) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(intent, ArticleListActivity.DEFAULT_REQUEST_CODE);
    }

    public static void logErrorMessage(String message){
        //Log.d(sContext.getString(R.string.app_name)+" Error:", message);
    }

    /*private static PowerManager.WakeLock getWakeLock(){
        return powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
    }*/
    public static void setWakeLock(){
        PowerManager powerManager = (PowerManager) getContext().getSystemService(POWER_SERVICE);
        sWakeLock =  powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                            "MyApp::MyWakelockTag");

        if (sWakeLock !=null) {
            sWakeLock.acquire();
        }
    }
    public static void releaseWakeLock(){
        if (sWakeLock !=null) {
            sWakeLock.release();
        }
        sWakeLock = null;
    }
}

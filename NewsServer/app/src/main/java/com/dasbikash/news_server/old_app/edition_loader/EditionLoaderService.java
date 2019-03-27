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

package com.dasbikash.news_server.old_app.edition_loader;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.edition_loader.anando_bazar.AnandaBazarEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.bd_pratidin.BdPratidinEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.bhorer_kagoj.BhorerKagojEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.bonik_barta.BonikBartaEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.daily_mirror.DailyMirrorEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.daily_sun.DailySunEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.dawn_pak.DawnPakEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.dhaka_tribune.DhakaTribuneEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.doinick_ittefaq.DoinickIttefaqEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.jugantor.JugantorEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.kaler_kantho.KalerKanthoEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.new_age.NewAgeEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.the_financial_express.TheFinancialExpressEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.the_indian_express.TheIndianExpressEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.prothom_alo.ProthomaloEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.the_daily_star.TheDailyStarEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.the_gurdian.TheGurdianEditionLoader;
import com.dasbikash.news_server.old_app.edition_loader.the_times_of_india.TheTimesOfIndiaEditionLoader;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.NetConnectivityUtility;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class EditionLoaderService extends IntentService {

    private static final String TAG = "EdLoaderBase";
    //private static final String TAG = "EditionLoaderService";

    static final String ACTION_EDITION_DOWNLOAD_THE_GURDIAN =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_THE_GURDIAN";
    static final String ACTION_EDITION_DOWNLOAD_PROTHOM_ALO =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_PROTHOM_ALO";
    static final String ACTION_EDITION_DOWNLOAD_ANANDA_BAZAR =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_ANANDA_BAZAR";
    static final String ACTION_EDITION_DOWNLOAD_THE_DAILY_STAR =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_THE_DAILY_STAR";
    static final String ACTION_EDITION_DOWNLOAD_THE_INDIAN_EXPRESS =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_THE_INDIAN_EXPRESS";
    static final String ACTION_EDITION_DOWNLOAD_DOINICK_ITTEFAQ =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_DOINICK_ITTEFAQ";
    static final String ACTION_EDITION_DOWNLOAD_THE_TIMES_OF_INDIA =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_THE_TIMES_OF_INDIA";
    static final String ACTION_EDITION_DOWNLOAD_DAILY_MIRROR =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_DAILY_MIRROR";
    static final String ACTION_EDITION_DOWNLOAD_DHAKA_TRIBUNE =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_DAHAKA_TRIBUNE";
    static final String ACTION_EDITION_DOWNLOAD_BD_PRATIDIN =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_BD_PRATIDIN";
    static final String ACTION_EDITION_DOWNLOAD_DAWN_PAK =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_DAWN_PAK";
    static final String ACTION_EDITION_DOWNLOAD_KALER_KANTHO =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_KALER_KANTHO";
    static final String ACTION_EDITION_DOWNLOAD_JUGANTOR =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_JUGANTOR";
    static final String ACTION_EDITION_DOWNLOAD_THE_FINANCIAL_EXPRESS =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_THE_FINANCIAL_EXPRESS";
    static final String ACTION_EDITION_DOWNLOAD_BONIK_BARTA =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_BONIK_BARTA";
    static final String ACTION_EDITION_DOWNLOAD_BHORER_KAGOJ =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_BHORER_KAGOJ";
    static final String ACTION_EDITION_DOWNLOAD_NEW_AGE =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_NEW_AGE";
    static final String ACTION_EDITION_DOWNLOAD_DAILY_SUN =
            "com.dasbikash.news_server.action.EDITION_DOWNLOAD_DAILY_SUN";


    static final String EXTRA_FEATURE = "com.dasbikash.news_server.extra.FEATURE";
    static final String EXTRA_FIRST_EDITION = "com.dasbikash.news_server.extra.FIRST_EDITION";
    public static final int MAX_EXECUTOR_THREAD_COUNT = 3;

    private static EditionLoaderService sEditionLoaderService;
    private ExecutorService mExecutorService;
    private static final ArrayList<Feature> sCurrentlyActiveFeatureList = new ArrayList<>();

    private static final ArrayList<Intent> sIntentArrayList = new ArrayList<>();

    public static void removeFeatureFromActiveList(Feature feature){
        synchronized (sCurrentlyActiveFeatureList){
            sCurrentlyActiveFeatureList.
                    remove(feature);
        }
    }

    public static void stop(){
        if (sEditionLoaderService!=null){
            sEditionLoaderService.stopSelf();
        }
        sIntentArrayList.clear();
        sCurrentlyActiveFeatureList.clear();
    }

    public static void reset(){
        stop();
    }

    private boolean addFeatureToActiveList(Feature feature){
        synchronized (sCurrentlyActiveFeatureList){
            if (sCurrentlyActiveFeatureList.contains(feature)){
                //Log.d(TAG, "addFeatureToActiveList: "+feature.getTitle()+" is already in active list.");
                return false;
            } else {
                sCurrentlyActiveFeatureList.add(feature);
                //Log.d(TAG, "addFeatureToActiveList: "+feature.getTitle()+" added to active list");
                return true;
            }
        }
    }

    private ExecutorService getThreadExecutor(){
        if ((mExecutorService == null) || mExecutorService.isShutdown() || mExecutorService.isTerminated()){
            int maxThreadCount = MAX_EXECUTOR_THREAD_COUNT;//(Runtime.getRuntime().availableProcessors()>1)? (Runtime.getRuntime().availableProcessors()-1):1;
            mExecutorService = Executors.newFixedThreadPool(maxThreadCount);
        }
        return mExecutorService;
    }

    public EditionLoaderService() {
        super("EditionLoaderService");
    }

    private static Intent makeBaseIntent(Feature feature, Newspaper newspaper){

        Context context = NewsServerUtility.getContext();
        Intent intent = new Intent(context, EditionLoaderService.class);

        switch (newspaper.getId()){
            case NewsServerDBSchema.NEWSPAPER_ID_THE_GURDIAN:
                intent.setAction(ACTION_EDITION_DOWNLOAD_THE_GURDIAN);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_PROTHOM_ALO:
                intent.setAction(ACTION_EDITION_DOWNLOAD_PROTHOM_ALO);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_ANANDO_BAZAR:
                intent.setAction(ACTION_EDITION_DOWNLOAD_ANANDA_BAZAR);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_THE_DAILY_STAR:
                intent.setAction(ACTION_EDITION_DOWNLOAD_THE_DAILY_STAR);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_THE_INDIAN_EXPRESS:
                intent.setAction(ACTION_EDITION_DOWNLOAD_THE_INDIAN_EXPRESS);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_DOINICK_ITTEFAQ:
                intent.setAction(ACTION_EDITION_DOWNLOAD_DOINICK_ITTEFAQ);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_THE_TIMES_OF_INDIA:
                intent.setAction(ACTION_EDITION_DOWNLOAD_THE_TIMES_OF_INDIA);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_DAILY_MIRROR:
                intent.setAction(ACTION_EDITION_DOWNLOAD_DAILY_MIRROR);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_DHAKA_TRIBUNE:
                intent.setAction(ACTION_EDITION_DOWNLOAD_DHAKA_TRIBUNE);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_BD_PROTIDIN:
                intent.setAction(ACTION_EDITION_DOWNLOAD_BD_PRATIDIN);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_DAWN_PAK:
                intent.setAction(ACTION_EDITION_DOWNLOAD_DAWN_PAK);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_KALER_KANTHO:
                intent.setAction(ACTION_EDITION_DOWNLOAD_KALER_KANTHO);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_JUGANTOR:
                intent.setAction(ACTION_EDITION_DOWNLOAD_JUGANTOR);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_THE_FINANCIAL_EXPRESS:
                intent.setAction(ACTION_EDITION_DOWNLOAD_THE_FINANCIAL_EXPRESS);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_BONIK_BARTA:
                intent.setAction(ACTION_EDITION_DOWNLOAD_BONIK_BARTA);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_BHORER_KAGOJ:
                intent.setAction(ACTION_EDITION_DOWNLOAD_BHORER_KAGOJ);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_NEW_AGE:
                intent.setAction(ACTION_EDITION_DOWNLOAD_NEW_AGE);
                break;
            case NewsServerDBSchema.NEWSPAPER_ID_DAILY_SUN:
                intent.setAction(ACTION_EDITION_DOWNLOAD_DAILY_SUN);
                break;
            default:
                return null;
        }

        intent.putExtra(EXTRA_FEATURE, feature);
        return intent;
    }

    public static boolean placeFirstEditionDownloadRequest(Feature feature){
        //Log.d("CheckConfigIntegrity", "placeFirstEditionDownloadRequest: "+feature.getTitle());

        if (feature == null || !NetConnectivityUtility.isConnected()){
            return false;
        }

        Newspaper newspaper = NewspaperHelper.findNewspaperById(feature.getNewsPaperId());
        if (newspaper == null) return false;
        //Log.d(TAG, "placeFirstEditionDownloadRequest: for NP: "+newspaper.getName());
        Intent intent = makeBaseIntent(feature,newspaper);
        if (intent == null) return false;
        intent.putExtra(EXTRA_FIRST_EDITION, "get_first_edition");

        synchronized (sIntentArrayList){
            sIntentArrayList.add(intent);
        }

        NewsServerUtility.getContext().startService(intent);
        //Log.d(TAG, "placeFirstEditionDownloadRequest: For Feature: "+feature.getTitle());
        return true;
    }

    public static boolean placeEditionDownloadRequest(Feature feature){
        //Log.d(TAG, "placeEditionDownloadRequest: ");

        if (feature == null || !NetConnectivityUtility.isConnected()){
            return false;
        }

        Newspaper newspaper = NewspaperHelper.findNewspaperById(feature.getNewsPaperId());
        if (newspaper == null) return false;

        Intent intent = makeBaseIntent(feature,newspaper);
        if (intent == null) return false;

        synchronized (sIntentArrayList){
            sIntentArrayList.add(intent);
        }

        NewsServerUtility.getContext().startService(intent);
        //Log.d(TAG, "placeEditionDownloadRequest: For feature: "+feature.getTitle());

        return true;
    }

    private Intent getIntentForIndex(int intentIndex){
        synchronized (sIntentArrayList){
            if (intentIndex>(sIntentArrayList.size()-1)){
                return null;
            } else {
                return sIntentArrayList.get(intentIndex);
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            intent = null;
            do {
                int intentIndex = 0;
                //Log.d(TAG, "onHandleIntent: going to find valid intent.");
                Intent tempIntent = null;
                do {
                    tempIntent = getIntentForIndex(intentIndex);
                    if (tempIntent !=null){
                        Feature feature = getIntentFeature(tempIntent);
                        //Log.d(TAG, "onHandleIntent: Found intent for feature: "+feature.getTitle()+
                        //" & now will add to active list.");
                        if (addFeatureToActiveList(feature)){
                            intent = tempIntent;
                            //Log.d(TAG, "onHandleIntent: found valid intent for feature: "+feature.getTitle()+
                            //" and now will remove that from sIntentArrayList");
                            synchronized (sIntentArrayList){
                                sIntentArrayList.remove(intent);
                            }
                            //tempIntent = null;
                            break;
                        }
                    }
                    intentIndex++;
                }while (tempIntent != null);
                if (intent != null) break;
                try {
                    //Log.d(TAG, "onHandleIntent: didn't get valid intent. Going to sleep before next iteration.");
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (true);

            final Intent intentForExecution = intent;

            final String action = intentForExecution.getAction();

            EditionLoaderBase editionLoader = null;

            switch (action){
                case ACTION_EDITION_DOWNLOAD_THE_GURDIAN:
                    editionLoader = new TheGurdianEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_PROTHOM_ALO:
                    editionLoader = new ProthomaloEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_ANANDA_BAZAR:
                    editionLoader = new AnandaBazarEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_THE_DAILY_STAR:
                    editionLoader = new TheDailyStarEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_THE_INDIAN_EXPRESS:
                    editionLoader = new TheIndianExpressEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_DOINICK_ITTEFAQ:
                    editionLoader = new DoinickIttefaqEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_THE_TIMES_OF_INDIA:
                    editionLoader = new TheTimesOfIndiaEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_DAILY_MIRROR:
                    editionLoader = new DailyMirrorEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_DHAKA_TRIBUNE:
                    editionLoader = new DhakaTribuneEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_BD_PRATIDIN:
                    editionLoader = new BdPratidinEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_DAWN_PAK:
                    editionLoader = new DawnPakEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_KALER_KANTHO:
                    editionLoader = new KalerKanthoEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_JUGANTOR:
                    editionLoader = new JugantorEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_THE_FINANCIAL_EXPRESS:
                    editionLoader = new TheFinancialExpressEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_BONIK_BARTA:
                    editionLoader = new BonikBartaEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_BHORER_KAGOJ:
                    editionLoader = new BhorerKagojEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_NEW_AGE:
                    editionLoader = new NewAgeEditionLoader();
                    break;
                case ACTION_EDITION_DOWNLOAD_DAILY_SUN:
                    editionLoader = new DailySunEditionLoader();
                    break;
            }

            final EditionLoaderBase editionLoaderBaseFor = editionLoader;
            if (editionLoaderBaseFor !=null){
                getThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        editionLoaderBaseFor.loadEdition(intentForExecution);
                    }
                });
            }

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sEditionLoaderService = EditionLoaderService.this;
        //Log.d(TAG, "onCreate: ");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        sEditionLoaderService = null;
        mExecutorService = null;
        //Log.d(TAG, "onDestroy: ");
    }
    public static synchronized boolean isRunning(){
        //Log.d(TAG, "isRunning: ");
        return sCurrentlyActiveFeatureList.size()>0;//!(sEditionLoaderService == null);
    }
    /*public static void stopEditionLoaderService(){
        if (sEditionLoaderService!=null) {
            sEditionLoaderService.stopSelf();
            //Log.d(TAG, "stopEditionLoaderService: ");
        }
    }*/

    public static Feature getIntentFeature(Intent intent){
        if (intent == null) return null;
        return (Feature) intent.getSerializableExtra(EXTRA_FEATURE);
    }

    static boolean checkIfFirstEditionDownloadRequest(Intent intent){
        if (intent == null) return false;
        return intent.hasExtra(EXTRA_FIRST_EDITION);
    }
}
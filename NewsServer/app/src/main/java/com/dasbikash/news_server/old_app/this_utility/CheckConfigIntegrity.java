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


import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderHelper;
import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderService;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderService;
import com.dasbikash.news_server.old_app.this_data.article.Article;
import com.dasbikash.news_server.old_app.this_data.article.ArticleHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CheckConfigIntegrity extends JobService {

    private static final String TAG = "CheckConfigIntegrity";

    public static final int JOB_ID = 362984705;
    private static final long JOB_INTERVAL_MINS = 60;
    private static final long JOB_INTERVAL_MILLIS = 60*1000*JOB_INTERVAL_MINS;
    public static final long MAX_JOB_RUNNING_TIME_MILLIS =  5 * 60 * 1000;

    private static final long MAX_HALT_SECS_FOR_DOWNLOAD = 60;
    private static final int MAX_WAIT_SECS_FOR_NETWORK_LOSS = 30;
    public static final long DELAY_MILLIS_FOR_SAME_NP_CHECK = 5000L;
    private static final int MAX_ARTICLE_CHECK_COUNT = 3;

    public static final String BR_STRING = "<br>";
    public static final String LOG_STRING_STARTER = "Starting at: ";
    public static final String HALT_BY_ADMIN_MESSAGE = "<strong>Exit due to halt by admin</strong>";
    private static final String NOTIFICATION_TITLE_FAILURE = "Config failure";
    public static final String APP_FOREGROUND_EXIT_MESSAGE = "Exiting because app on foreground.";
    private static final String HALT_BY_NETWORK_LOSS_MESSAGE = "<strong>Exit due to halt by Network Loss</strong>";
    public static final String NO_FEATURE_TO_CHECK_MESSAGE = "No f eature to check";
    public static final String FEATURES_CHECKED_WITH_NO_FAILURE_MESSAGE = " features checked with no failure.";

    private static boolean sRunning = false;
    private JobParameters mJobParameters;
    //private Context mContext;
    private CheckIntegrity mCheckIntegrity = null;
    private StringBuilder mResultSrtingBuilder = new StringBuilder();

    private final BroadcastReceiver mEditionLoadBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEditionLoadBrodcastMessage(intent);
        }
    };

    private final BroadcastReceiver mArticleLoaderBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent !=null){
                handleArticleDownloadBroadcastMessage(context,intent);
            }
        }
    };
    private boolean mWaitingForEditionDownload=false;
    private boolean mWaitingForArticleDownload=false;
    private EditionLoaderBase.BroadcastStatus mEditionDownloadResult;
    private boolean mArticleDownloadResult;
    private int mEditionDownloadFailureCount =0;
    private int mArticleDownloadFailureCount=0;
    private int mFeatureCheckedCount = 0;

    private static ArrayList<Newspaper>
            sNewspapers = new ArrayList<>();
    private static HashMap<Newspaper,ArrayList<Feature>>
            sParentFeatureMap = new HashMap<>();

    public static void stop() {
        sRunning = false;
    }

    private void handleArticleDownloadBroadcastMessage(Context context, Intent intent) {
        //Log.d(TAG, "handleArticleDownloadBroadcastMessage: ");
        mArticleDownloadResult = ArticleLoaderService.getArticleDownloadStatus(intent);
        mWaitingForArticleDownload = false;
    }

    private void handleEditionLoadBrodcastMessage(Intent intent) {
        //Log.d(TAG, "handleEditionLoadBrodcastMessage: ");
        mEditionDownloadResult = EditionLoaderBase.getBroadcastStatus(intent);
        mWaitingForEditionDownload = false;
    }

    public CheckConfigIntegrity() {}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        registerBrodcastReceivers(getApplicationContext());
        sRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        unregisterBrodcastReceivers(getApplicationContext());
        if (mCheckIntegrity !=null){
            mCheckIntegrity.cancel(true);
            mCheckIntegrity = null;
        }
        sRunning = false;
        NewsServerUtility.releaseWakeLock();
    }

    private void registerBrodcastReceivers(Context context) {
        //Log.d(TAG, "registerBrodcastReceivers: ");
        LocalBroadcastManager.getInstance(context).registerReceiver(
                mArticleLoaderBroadcastReceiver, ArticleLoaderService.getIntentFilterForArticleLoaderBroadcastMessage()
        );

        LocalBroadcastManager.getInstance(context).registerReceiver(
                mEditionLoadBroadcastReceiver, EditionLoaderBase.getIntentFilterForEditionDownloadBroadcastMessage()
        );
    }
    private void unregisterBrodcastReceivers(Context context) {
        //Log.d(TAG, "unregisterBrodcastReceivers: ");
        LocalBroadcastManager.getInstance(context).unregisterReceiver(
                mArticleLoaderBroadcastReceiver
        );
        LocalBroadcastManager.getInstance(context).unregisterReceiver(
                mEditionLoadBroadcastReceiver
        );
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob: ");

        if (!NewsServerUtility.isAppOnForeGround()) {
            Log.d(TAG, "Going to init app");
            NewsServerUtility.init(getApplicationContext());
            mJobParameters = jobParameters;
            new CheckIntegrity().execute();
            return true;
        } else {
            mResultSrtingBuilder.append(APP_FOREGROUND_EXIT_MESSAGE);
            houseKeepingOnExit();
        }
        return false;
    }

    private static void initData() {
        sNewspapers =
                NewspaperHelper.getAllNewspapers();
        for (Newspaper newspaper :
                sNewspapers) {
            ArrayList<Feature> parentFeatures =
                    FeatureHelper.getAllParentFeaturesForNewspaper(newspaper);
            sParentFeatureMap.put(newspaper,parentFeatures);
        }
    }

    private class CheckIntegrity extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            NewsServerUtility.setWakeLock();
            mCheckIntegrity = CheckIntegrity.this;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            long jobStartTimeMillis = System.currentTimeMillis();

            if (sNewspapers.size() == 0){
                initData();
            }

            Newspaper lastCheckedNewspaper=null;
            ArrayList<Newspaper> skippedNewspaperList =
                    new ArrayList<>();
            int iterationCount = 0;

            do {
                if ((System.currentTimeMillis() - jobStartTimeMillis) >
                        MAX_JOB_RUNNING_TIME_MILLIS){
                    return null;
                }

                newspaperCheckLabel:

                for (Newspaper newspaper :
                        sNewspapers) {
                    Log.d(TAG, "Newspaper:" + newspaper.getName());

                    if (!sRunning){
                        recordExitDueToHalt();
                        return null;
                    } else if (!NetConnectivityUtility.checkConnection()){
                        int loopCounter = 0;
                        do {
                            try {
                                Log.d(TAG, "Waiting for network recovery.");
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            loopCounter++;
                            if (loopCounter >= MAX_WAIT_SECS_FOR_NETWORK_LOSS){
                                recordExitDueToNetworkLoss();
                                return null;
                            }
                        }while (!NetConnectivityUtility.checkConnection());
                    }

                    if (
                        (newspaper.getId() == NewsServerDBSchema.NEWSPAPER_ID_PROTHOM_ALO &&
                        !NetConnectivityUtility.isOnMobileDataNetwork())
                                ||
                        (lastCheckedNewspaper != null &&
                        lastCheckedNewspaper.getId() == newspaper.getId())
                            ){
                        try {
                            Thread.sleep(DELAY_MILLIS_FOR_SAME_NP_CHECK);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    lastCheckedNewspaper = newspaper;

                    ArrayList<Feature> parentFeatures = sParentFeatureMap.get(newspaper);

                    if (skippedNewspaperList.contains(newspaper)){
                        continue newspaperCheckLabel;
                    } else if (iterationCount>=parentFeatures.size()){
                        skippedNewspaperList.add(newspaper);
                        continue newspaperCheckLabel;
                    }

                    Feature currentParentFeature = parentFeatures.get(iterationCount);
                    if (!SettingsUtility.needToCheckParentFeature(currentParentFeature)){
                        Log.d(TAG, "too early to check again:"+currentParentFeature.getTitle());
                        continue newspaperCheckLabel;
                    }
                    mFeatureCheckedCount++;
                    Feature featureToCheck;

                    ArrayList<Feature> childFeatures =
                            FeatureHelper.getAllChildFeatures(currentParentFeature.getId());

                    if (childFeatures == null || childFeatures.size() == 0) {
                        featureToCheck = currentParentFeature;
                    } else {
                        if (currentParentFeature.getLinkFormat() != null){
                            childFeatures.add(0,currentParentFeature);
                        }
                        int childIndex =
                                new Random().nextInt(childFeatures.size());
                        featureToCheck = childFeatures.get(childIndex);
                    }

                    Log.d(TAG, "featureToCheck:" + featureToCheck.getTitle());

                    if (!EditionLoaderService.placeFirstEditionDownloadRequest(featureToCheck)){
                        recordEditionDownloadRequestFailure(newspaper,currentParentFeature,featureToCheck);
                        continue newspaperCheckLabel;
                    }

                    mEditionDownloadResult = null;
                    mWaitingForEditionDownload = true;

                    long haltSleepCount = 0;
                    while (mWaitingForEditionDownload){
                        //Log.d(TAG, "doInBackground: while (mWaitingForEditionDownload)");
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        haltSleepCount++;
                        if (haltSleepCount> 10* MAX_HALT_SECS_FOR_DOWNLOAD){
                            recordEmptyEditionDownloadEvent(newspaper,featureToCheck,currentParentFeature);
                            mWaitingForEditionDownload = false;
                            continue newspaperCheckLabel;
                        }
                    }

                    switch (mEditionDownloadResult){
                        case EMPTY:
                            recordEmptyEditionDownloadEvent(newspaper,featureToCheck,currentParentFeature);
                            continue newspaperCheckLabel;
                        case INVALID_ARG:
                        case FAILURE:
                            recordEditionDownloadRequestFailure(newspaper,currentParentFeature,featureToCheck);
                            continue newspaperCheckLabel;
                    }
                    /*Log.d(TAG, "doInBackground: edition load success: "+" for: "+ currentParentFeature.getTitle()+
                            " | "+newspaper.getName());*/

                    ArrayList<Integer> articleIdList = ArticleHelper.findArticleIdsForFeature(featureToCheck);
                    if (articleIdList.size() == 0){
                        recordEmptyEditionDownloadEvent(newspaper,featureToCheck,currentParentFeature);
                        continue newspaperCheckLabel;
                    }
                    int i=0;
                    for (;i<MAX_ARTICLE_CHECK_COUNT;i++) {

                        if (i>=articleIdList.size()){
                            break;
                        }

                        Article article = ArticleHelper.findArticleById(articleIdList.get(i));

                        if (article == null ||
                                !ArticleLoaderHelper.placeArticleDownloadRequest(article, newspaper)) {
                            recordArticleDownloadRequestFailure(newspaper,featureToCheck,currentParentFeature,article);
                            continue;
                        }
                        mWaitingForArticleDownload = true;
                        haltSleepCount=0;
                        while (mWaitingForArticleDownload) {
                            //Log.d(TAG, "doInBackground: while (mWaitingForArticleDownload)");
                            try {
                                Thread.sleep(100L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            haltSleepCount++;
                            if (haltSleepCount > 10 * MAX_HALT_SECS_FOR_DOWNLOAD) {
                                recordArticleDownloadFailure(newspaper,featureToCheck,currentParentFeature,article);
                                mWaitingForArticleDownload = false;
                                continue;
                            }
                        }
                        if (mArticleDownloadResult) {
                            /*Log.d(TAG, "doInBackground: article load success: "+article.getTitle()+" for: "+ currentParentFeature.getTitle()+
                            " | "+newspaper.getName());*/
                            SettingsUtility.entryParentFeatureCheckSucess(featureToCheck,currentParentFeature);
                            continue newspaperCheckLabel;
                        }
                        recordArticleDownloadFailure(newspaper,featureToCheck,currentParentFeature,article);
                        //i++;
                    }
                    mArticleDownloadFailureCount +=i;
                }

                iterationCount++;

            }while (skippedNewspaperList.size()<sNewspapers.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: ");
            houseKeepingOnExit();
            jobFinished(mJobParameters,false);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(TAG, "onCancelled: ");
            houseKeepingOnExit();
            jobFinished(mJobParameters,false);
        }
    }

    private void recordExitDueToNetworkLoss() {
        mResultSrtingBuilder.append(BR_STRING+HALT_BY_NETWORK_LOSS_MESSAGE);
        Log.d(TAG, "recordExitDueToNetworkLoss: ");
    }

    private void recordExitDueToHalt() {
        mResultSrtingBuilder.append(BR_STRING+HALT_BY_ADMIN_MESSAGE);
        Log.d(TAG, "recordExitDueToHalt");
    }

    private void recordArticleDownloadFailure(Newspaper newspaper,Feature featureToCheck,
                                              Feature parentFeature, Article article) {
        SettingsUtility.entryParentFeatureCheckFailure(featureToCheck,parentFeature);
        mResultSrtingBuilder.append(
                BR_STRING+ "<strong>Article download failure for "+
                featureToCheck.getTitle()+" | "+parentFeature.getTitle()+" | "+newspaper.getName()+
                " | "+article.getTitle()+
                "</strong>"
        );
        Log.d(TAG, "recordArticleDownloadFailure: "+featureToCheck.getTitle()+" | "+
                parentFeature.getTitle()+" | "+newspaper.getName()+" | "+article.getTitle());
    }

    private void recordArticleDownloadRequestFailure(Newspaper newspaper,Feature featureToCheck,
                                                     Feature parentFeature,Article article) {
        SettingsUtility.entryParentFeatureCheckFailure(featureToCheck,parentFeature);
        mResultSrtingBuilder.append(
                BR_STRING+"Article download request failure for "+
                featureToCheck.getTitle()+" | "+parentFeature.getTitle()+" | "+newspaper.getName()+
                (article!=null? " | "+article.getTitle(): "")
        );
        Log.d(TAG, "recordArticleDownloadRequestFailure: "+
                featureToCheck.getTitle()+" | "+parentFeature.getTitle()+" | "+newspaper.getName()+
                        (article!=null? " | "+article.getTitle(): ""));
    }

    private void recordEmptyEditionDownloadEvent(Newspaper newspaper,Feature featureToCheck,Feature parentFeature) {
        mEditionDownloadFailureCount++;
        mResultSrtingBuilder.append(
                BR_STRING+"<strong>Edition download failure for "+
                featureToCheck.getTitle()+" | "+parentFeature.getTitle()+" | "+newspaper.getName()+
                "</strong>"
        );
        SettingsUtility.entryParentFeatureCheckFailure(featureToCheck,parentFeature);
        Log.d(TAG, "recordEmptyEditionDownloadEvent: "+
                featureToCheck.getTitle()+" | "+parentFeature.getTitle()+" | "+newspaper.getName());
    }

    private void recordEditionDownloadRequestFailure(Newspaper newspaper,Feature parentFeature,Feature featureToCheck) {
        mEditionDownloadFailureCount++;
        mResultSrtingBuilder.append(
                BR_STRING+"Edition download request failure for "+
                        featureToCheck.getTitle()+" | "+parentFeature.getTitle()+" | "+newspaper.getName()
        );
        Log.d(TAG, "recordEditionDownloadRequestFailure: "+
                featureToCheck.getTitle()+" | "+parentFeature.getTitle()+" | "+newspaper.getName());
    }

    private void houseKeepingOnExit() {
        Log.d(TAG, "houseKeepingOnExit");
        if (mFeatureCheckedCount > 0) {
            if (mEditionDownloadFailureCount > 0 || mArticleDownloadFailureCount > 0) {
                NotificationHelper.generateNotification(
                        NOTIFICATION_TITLE_FAILURE,
                        mEditionDownloadFailureCount + " empty edition & " + mArticleDownloadFailureCount +
                                " article download failure."
                );
                Log.d(TAG, "Notification: "+mEditionDownloadFailureCount + " empty edition & " + mArticleDownloadFailureCount +
                        " article download failure.");
            }else {
                mResultSrtingBuilder.append(
                        BR_STRING+ mFeatureCheckedCount+ FEATURES_CHECKED_WITH_NO_FAILURE_MESSAGE
                );
            }
        }else {
            mResultSrtingBuilder.append(
                    BR_STRING+ NO_FEATURE_TO_CHECK_MESSAGE
            );
        }
        SettingsUtility.saveCheckConfigIntegrityResult(mResultSrtingBuilder.toString());
        NewsServerUtility.releaseWakeLock();
        mCheckIntegrity = null;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "onStopJob: ");
        return false;
    }

    public static void init(Context context){
        //Log.d(TAG, "init: ");

        if (! CheckConfigIntegrity.isRunning()) {

            if (isConfigCheckJobEnabled()){
                return;
            }
            Log.d(TAG, "init: Going to init job");

            JobInfo jobInfo =
                    new JobInfo.Builder(JOB_ID,new ComponentName(context,CheckConfigIntegrity.class))
                    .setPeriodic(JOB_INTERVAL_MILLIS)
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING)
                    .build();

            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(jobInfo);
        }
    }


    public static boolean isConfigCheckJobEnabled() {
        JobScheduler jobScheduler = (JobScheduler) NewsServerUtility.getContext().
                getSystemService(Context.JOB_SCHEDULER_SERVICE);

        for (JobInfo jobInfo :
                jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == CheckConfigIntegrity.JOB_ID) {
                Log.d(TAG, "isConfigCheckJobEnabled: Job Already initiated");
                return true;
            }
        }
        Log.d(TAG, "isConfigCheckJobEnabled: Job not initiated");
        return false;
    }

    public static void cancelJob(){
        JobScheduler jobScheduler = (JobScheduler) NewsServerUtility.getContext().
                getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (isConfigCheckJobEnabled()){
            jobScheduler.cancel(JOB_ID);
            Log.d(TAG, "cancelJob: Jon Canceled");
        }
    }



    public static boolean isRunning() {
        //Log.d(TAG, "isRunning: ");
        return sRunning;
    }

}

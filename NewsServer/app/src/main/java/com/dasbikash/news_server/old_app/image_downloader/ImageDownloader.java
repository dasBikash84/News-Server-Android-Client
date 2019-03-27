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

package com.dasbikash.news_server.old_app.image_downloader;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_utility.SettingsUtility;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageData;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageDataHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_utility.NetConnectivityUtility;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({"SynchronizeOnNonFinalField", "UnusedReturnValue", "SameReturnValue", "CanBeFinal"})
public final class ImageDownloader implements Runnable {

    private static final String TAG = "ImageDownloader";
    //private static final String TAG = "StackTrace";

    private static final int CONNECT_TIMEOUT = 1000;
    private static final int READ_TIMEOUT=10000;

    private static final String FILE_SAVE_LOCATION_FIRST_PART =
            NewsServerUtility.getContext().getFilesDir().getPath()+"/image_";
    private static final String FILE_SAVE_LOCATION_LAST_PART =".jpg";
    private static final String IMAGE_DOWNLOAD_INTENT_FILTER =
            "com.dasbikash.news_server.image_download_broadcast_intent_filter";
    private static final String EXTRA_DOWNLOADED_IMAGE_ID =
            "com.dasbikash.news_server.downloaded_image_id_extra";
    private static final String EXTRA_IMAGE_DOWNLOAD_FLAG =
            "com.dasbikash.news_server.image_download_flag";

    public enum DOWNLOAD_REQUEST_RESPONSE{
        DL_IN_PROGRESS,
        REQUEST_PLACED,
        ALREADY_DOWNLOADED,
        INVALID_IMAGE_PARAM,
        CANT_DL_ON_DATA_NETWORK,
        NO_NETWORK_CONNECTION
    }

    private static ExecutorService sExecutorService;
    private static final List<Integer> sCurrentImageIdList = new ArrayList<>();

    private static final HashMap<Integer,Long> sLastAccessTsForNewspaper = new HashMap<>();
    public static final long DEFAULT_MINIMUM_SITE_ACCESS_DELAY_MILLIS = 200L;
    public static final long MINIMUM_SITE_ACCESS_DELAY_MILLIS_PALO = 500L;

    public static boolean checkIfCanAccessSite(Newspaper newspaper){

        if (newspaper != null) {
            //Log.d(TAG, "checkIfCanAccessSite: from: "+newspaper.getName());
            synchronized (sLastAccessTsForNewspaper) {
                long currentSystemTimeMillis = System.currentTimeMillis();
                //Log.d(TAG, "checkIfCanAccessSite: currentSystemTimeMillis:"+currentSystemTimeMillis);

                long minimumSiteAccessDelayMillis = DEFAULT_MINIMUM_SITE_ACCESS_DELAY_MILLIS;
                if (newspaper.getId() == NewsServerDBSchema.NEWSPAPER_ID_PROTHOM_ALO){
                    minimumSiteAccessDelayMillis = MINIMUM_SITE_ACCESS_DELAY_MILLIS_PALO;
                }
                //Log.d(TAG, "checkIfCanAccessSite: minimumSiteAccessDelayMillis:"+minimumSiteAccessDelayMillis);

                if (!sLastAccessTsForNewspaper.containsKey(newspaper.getId())) {
                    //Log.d(TAG, "checkIfCanAccessSite: First access");
                    sLastAccessTsForNewspaper.put(newspaper.getId(), currentSystemTimeMillis);
                    //Log.d(TAG, "checkIfCanAccessSite: access granted");
                    return true;
                } else {
                    if (currentSystemTimeMillis >= sLastAccessTsForNewspaper.get(newspaper.getId()) +
                                                    minimumSiteAccessDelayMillis) {
                        //Log.d(TAG, "checkIfCanAccessSite: Last accessed on:"+sLastAccessTsForNewspaper.get(newspaper.getId()));
                        sLastAccessTsForNewspaper.put(newspaper.getId(), currentSystemTimeMillis);
                        //Log.d(TAG, "checkIfCanAccessSite: access granted");
                        return true;
                    }
                }
            }
        }
        //Log.d(TAG, "checkIfCanAccessSite: no access");
        return false;
    }

    private String mWebUrl;
    private String mDiskUri;
    private int mImageId;
    private int mImageByteSize=0;
    private Newspaper mNewspaper;

    private ImageDownloader(int imageId,Newspaper newspaper){
        mImageId = imageId;
        mNewspaper = newspaper;
    }

    public static void reset(){
        sCurrentImageIdList.clear();
        sExecutorService=null;
    }

    public static ExecutorService getThreadExecutor(){
        if ((sExecutorService == null) || sExecutorService.isShutdown() || sExecutorService.isTerminated()){
            int maxThreadCount = (Runtime.getRuntime().availableProcessors()>1)? (Runtime.getRuntime().availableProcessors()-1):1;
            sExecutorService = Executors.newFixedThreadPool(maxThreadCount);
        }
        return sExecutorService;
    }

    private static DOWNLOAD_REQUEST_RESPONSE checkIfNeedToDownload(int imageId){

        synchronized (sCurrentImageIdList){
            if (sCurrentImageIdList.contains(imageId)){
                return DOWNLOAD_REQUEST_RESPONSE.DL_IN_PROGRESS;
            }
        }
        ImageData imageData = ImageDataHelper.findImageDataById(imageId);
        if (imageData == null) return DOWNLOAD_REQUEST_RESPONSE.INVALID_IMAGE_PARAM;

        if (imageData.getSizeKB()>0){
            generateBrodcast(imageId,true);
            return DOWNLOAD_REQUEST_RESPONSE.ALREADY_DOWNLOADED;
        }
        //return !checkIfAlreadyDownloaded(imageId);
        return DOWNLOAD_REQUEST_RESPONSE.REQUEST_PLACED;
    }

    private static boolean checkIfAlreadyDownloaded(int imageId){
        ImageData imageData = ImageDataHelper.findImageDataById(imageId);
        if (imageData!=null && imageData.getSizeKB()>0){
            generateBrodcast(imageId,true);
            return true;
        }
        return false;
    }

    private static String generateFileUri(int imageId) {
        String diskUri = FILE_SAVE_LOCATION_FIRST_PART +
                imageId +
                "_" +
                Calendar.getInstance().getTimeInMillis() +
                FILE_SAVE_LOCATION_LAST_PART;
        return diskUri;
    }

    public static DOWNLOAD_REQUEST_RESPONSE placeUrgentFileDownloadRequest(int imageId, Newspaper newspaper){
        ImageDataHelper.incrementManualImageDownloadCount();
        return checkSettingAndPlaceRequest(imageId,newspaper,false);
    }

    public static DOWNLOAD_REQUEST_RESPONSE placeFileDownloadRequest(int imageId, Newspaper newspaper){
        return checkSettingAndPlaceRequest(imageId,newspaper,true);
    }

    private static DOWNLOAD_REQUEST_RESPONSE checkSettingAndPlaceRequest(
            int imageId, Newspaper newspaper,boolean needToCheckSetting
    ){
        if (newspaper == null ||
                imageId == 0 ||
                imageId == -1) {
            return DOWNLOAD_REQUEST_RESPONSE.INVALID_IMAGE_PARAM;
        }
        if (!NetConnectivityUtility.isConnected()){
            return DOWNLOAD_REQUEST_RESPONSE.NO_NETWORK_CONNECTION;
        }
        DOWNLOAD_REQUEST_RESPONSE checkIfNeedToDl =checkIfNeedToDownload(imageId);

        if (checkIfNeedToDl != DOWNLOAD_REQUEST_RESPONSE.REQUEST_PLACED){
            return checkIfNeedToDl;
        }

        if (needToCheckSetting &&
            NetConnectivityUtility.isOnMobileDataNetwork() &&
            !SettingsUtility.canDlImageOnDataNet()){
            //generateBrodcast(imageId,false);
            return DOWNLOAD_REQUEST_RESPONSE.CANT_DL_ON_DATA_NETWORK;
        }
        synchronized (sCurrentImageIdList) {
            getThreadExecutor().execute(new ImageDownloader(imageId, newspaper));
            sCurrentImageIdList.add(imageId);
        }
        return DOWNLOAD_REQUEST_RESPONSE.REQUEST_PLACED;
    }

    @Override
    public void run() {
        //Log.d(TAG, "goOn before wait: imageId:"+mImageId);
        //Log.d(TAG, "goOn before wait: newspaper:"+mNewspaper.getName());
        while (!checkIfCanAccessSite(mNewspaper)) {
            try {
                Thread.sleep(DEFAULT_MINIMUM_SITE_ACCESS_DELAY_MILLIS / 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(checkIfAlreadyDownloaded(mImageId)){
            return;
        }

        ImageData imageData = ImageDataHelper.findImageDataById(mImageId);
        if (imageData == null) return;
        mWebUrl = imageData.getLink();

        if (mWebUrl == null) return;

        try {
            mDiskUri = generateFileUri(mImageId);

            //Log.d(TAG,"Going to download Image from: "+mWebUrl);

            FileUtils.copyURLToFile(
                    new URL(mWebUrl),
                    new File(mDiskUri),
                    CONNECT_TIMEOUT,
                    READ_TIMEOUT);

            File newImageFile = new File(mDiskUri);
            mImageByteSize = (int) (newImageFile.length());

            if(checkIfAlreadyDownloaded(mImageId)){
                return;
            }

            final SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

            String sqlUpdateDiskLocation = "UPDATE "+
                    NewsServerDBSchema.ImageTable.NAME +
                    " SET "+
                    NewsServerDBSchema.ImageTable.Cols.DiskLocation.NAME +
                    " = '"+
                    mDiskUri +
                    "',"+
                    NewsServerDBSchema.ImageTable.Cols.SizeKB.NAME+
                    " = "+
                    (mImageByteSize/1024.00)+
                    " WHERE " +
                    NewsServerDBSchema.ImageTable.Cols.Id.NAME +
                    " = "+
                    mImageId + " ;";

            dbCon.execSQL(sqlUpdateDiskLocation);
            //Log.d(TAG, "goOn: "+sqlUpdateDiskLocation);
            generateBrodcast(mImageId,true);

        } catch (Throwable e) {
            //Log.d(TAG, "goOn: Error: "+e.getMessage());
            generateBrodcast(mImageId,false);
            e.printStackTrace();
        } finally {
            if (sCurrentImageIdList.contains(mImageId)){
                sCurrentImageIdList.remove((Integer)mImageId);
            }
        }
    }

    public static int getBrodcastedImageId(Intent intent) {
        if (intent!=null) {
            return intent.getIntExtra(EXTRA_DOWNLOADED_IMAGE_ID, 0);
        }
        return 0;
    }

    public static IntentFilter getIntentFilterForImageDownloadBroadcastMessage(){
        return new IntentFilter(IMAGE_DOWNLOAD_INTENT_FILTER);
    }

    private static void generateBrodcast(int imageId,boolean downloadResult) {
        Intent imageDlBrodcastIntent = new Intent(IMAGE_DOWNLOAD_INTENT_FILTER);
        imageDlBrodcastIntent.putExtra(EXTRA_DOWNLOADED_IMAGE_ID,imageId);
        if (downloadResult){
            imageDlBrodcastIntent.putExtra(EXTRA_IMAGE_DOWNLOAD_FLAG,"Success");
        }
        LocalBroadcastManager.getInstance(NewsServerUtility.getContext()).sendBroadcast(imageDlBrodcastIntent);
    }

    public static boolean getImageDownloadStatus(Intent intent){
        if (intent!=null){
            return intent.hasExtra(EXTRA_IMAGE_DOWNLOAD_FLAG);
        }
        return false;
    }
}

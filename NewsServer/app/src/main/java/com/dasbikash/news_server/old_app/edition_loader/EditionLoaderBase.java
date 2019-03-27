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

import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.article.Article;
import com.dasbikash.news_server.old_app.this_data.article.ArticleHelper;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageDataHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.CalenderUtility;
import com.dasbikash.news_server.old_app.this_utility.URLConnectionHelper;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public abstract class EditionLoaderBase {

    //private static final String TAG = "EdLoaderBase";
    private static final String TAG = "StackTrace";

    private static final String EXTRA_SUCCESS_BROADCAST =
            "EditionLoaderBase.extra.SUCCESS_BROADCAST";

    private static final String EXTRA_EMPTY_BROADCAST =
            "EditionLoaderBase.extra.EMPTY_BROADCAST";

    private static final String EXTRA_FAILURE_BROADCAST =
            "EditionLoaderBase.extra.FAILURE_BROADCAST";

    private static final String EXTRA_END_OF_EDITION_BROADCAST =
            "EditionLoaderBase.extra.END_OF_EDITION_BROADCAST";

    private static final String EDITION_DOWNLOAD_INTENT_FILTER =
            "EditionLoaderBase.edition_download_intent_filter";
    private static final String EXTRA_DOWNLOADED_FEATURE_ID =
            "EditionLoaderBase.downloaded_feature_id";

    protected static final int DEFAULT_MAX_RERUN_COUNT_FOR_EMPTY_WITH_REPEAT = 2;
    protected static final int DEFAULT_MAX_RERUN_COUNT_FOR_EMPTY = 0;

    private int mReRunCountOnEmptyWithRepeat = 0;
    private int mReRunCountOnEmpty = 0;
    private boolean mFirstEditionDownloadRequest = false;

    protected static HashMap<Integer,Integer> sLastCheckedPageNumberMap = new HashMap<>();

    private static int[] newspaperIdsForWithoutPreviewDate = {
            NewsServerDBSchema.NEWSPAPER_ID_THE_INDIAN_EXPRESS,
            NewsServerDBSchema.NEWSPAPER_ID_DOINICK_ITTEFAQ,
            NewsServerDBSchema.NEWSPAPER_ID_ANANDO_BAZAR,
            NewsServerDBSchema.NEWSPAPER_ID_DAILY_MIRROR,
            NewsServerDBSchema.NEWSPAPER_ID_DAWN_PAK,
            NewsServerDBSchema.NEWSPAPER_ID_THE_FINANCIAL_EXPRESS,
            NewsServerDBSchema.NEWSPAPER_ID_BD_PROTIDIN,
            NewsServerDBSchema.NEWSPAPER_ID_BONIK_BARTA,
            NewsServerDBSchema.NEWSPAPER_ID_KALER_KANTHO,
            NewsServerDBSchema.NEWSPAPER_ID_DAILY_SUN,
            NewsServerDBSchema.NEWSPAPER_ID_BHORER_KAGOJ
    };

    private static final ArrayList<Integer> newspaperIdListForWithoutPreviewDate = new ArrayList<>();

    static {
        for (Integer newspaperId :
                newspaperIdsForWithoutPreviewDate) {
            newspaperIdListForWithoutPreviewDate.add(newspaperId);
        }
    }

    public static void reset(){
        sLastCheckedPageNumberMap.clear();
    }

    protected enum EditionSummarySaveStatus {
        SUCCESS,
        SUCCESS_WITH_REPEAT,
        EMPTY,
        EMPTY_WITH_REPEAT,
        FAILURE,
        END_OF_EDITION
    }

    public enum BroadcastStatus {
        SUCCESS,
        EMPTY,
        FAILURE,
        END_OF_EDITION,
        INVALID_ARG;
    }

    private EditionSummary mEditionSummary = new EditionSummary();
    private Elements mPreviewBlocks = new Elements();

    protected Feature mFeature;
    protected String mPageLink;
    protected Newspaper mNewspaper;
    protected Country mCountry;
    protected SimpleDateFormat mSimpleDateFormat;
    protected abstract String getSiteBaseAddress();
    protected Document mEditionDocument;

    protected abstract String getArticlePublicationDatetimeFormat();
    protected abstract Elements getPreviewBlocks();

    protected abstract String getArticleLink(Element previewBlock);
    protected abstract String getArticlePreviewImageLink(Element previewBlock);
    protected abstract String getArticleTitle(Element previewBlock);
    protected abstract String getArticlePublicationDateString(Element previewBlock);

    protected int getMaxReRunCountOnEmptyWithRepeat(){
        return DEFAULT_MAX_RERUN_COUNT_FOR_EMPTY_WITH_REPEAT;
    }

    protected int getMaxReRunCountOnEmpty(){
        return DEFAULT_MAX_RERUN_COUNT_FOR_EMPTY;
    }

    protected String getPageLink(Feature feature,int pageNumber) {

        //Log.d(TAG, "getPageLink: ");

        if (feature == null || feature.getLinkFormat() == null){
            return null;
        }

        if (feature.getLinkVariablePartFormat() == null){
            return feature.getLinkFormat();
        }

        if (feature.getLinkVariablePartFormat().equals(NewsServerDBSchema.DEFAULT_LINK_TRAILING_FORMAT)){
            return feature.getLinkFormat().replace(feature.getLinkVariablePartFormat(),""+pageNumber);
        } else {

            Newspaper newspaper = NewspaperHelper.findNewspaperById(feature.getNewsPaperId());
            if (newspaper == null) return null;

            Country country = CountryHelper.findCountryByName(newspaper.getCountryName());
            if (country == null) return null;

            Calendar currentTime = CalenderUtility.getCurrentTime();
            currentTime.setTimeZone(TimeZone.getTimeZone(country.getTimeZone()));

            if (feature.isWeekly()){
                do {
                    if (feature.getWeeklyPublicationDay() == currentTime.get(Calendar.DAY_OF_WEEK)) {
                        break;
                    }
                    currentTime.add(Calendar.DAY_OF_MONTH, -1);
                } while (true);
                currentTime.add(Calendar.DAY_OF_YEAR,-1*(pageNumber-1)*7);
            } else {
                currentTime.add(Calendar.DAY_OF_YEAR, -1 * (pageNumber - 1));
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(feature.getLinkVariablePartFormat());
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(country.getTimeZone()));

            return feature.getLinkFormat().replace(
                    feature.getLinkVariablePartFormat(),
                    simpleDateFormat.format(currentTime.getTime())
            );
        }
    }

    protected Long getArticleModificationTimeStamp(Element previewBlock){
        return null;
    }

    protected String getArticleModificationDateString(Element previewBlock){
        return null;
    }

    protected Long getArticlePublicationTimeStamp(Element previewBlock) {
        return null;
    }

    protected String processArticlePreviewImageLink(String previewImageLink){
        return processLink(previewImageLink);
    }

    protected String processArticleLink(String articleLink){
        return processLink(articleLink);
    }

    private String processLink(String linkText){
        if (linkText == null) return null;
        String siteBaseAddress = getSiteBaseAddress();
        return NewsServerUtility.processLink(linkText,siteBaseAddress);
    }

    public static IntentFilter getIntentFilterForEditionDownloadBroadcastMessage(){
        return new IntentFilter(EDITION_DOWNLOAD_INTENT_FILTER);
    }

    public static int getBrodcastedFeatureId(Intent intent) {
        if (intent!=null) {
            return intent.getIntExtra(EXTRA_DOWNLOADED_FEATURE_ID, 0);
        }
        return 0;
    }

    public static BroadcastStatus getBroadcastStatus(Intent intent){

        if (intent == null) return BroadcastStatus.INVALID_ARG;

        if (intent.hasExtra(EXTRA_SUCCESS_BROADCAST)){
            return BroadcastStatus.SUCCESS;
        } else if(intent.hasExtra(EXTRA_EMPTY_BROADCAST)){
            return BroadcastStatus.EMPTY;
        } else if(intent.hasExtra(EXTRA_FAILURE_BROADCAST)){
            return BroadcastStatus.FAILURE;
        } else if (intent.hasExtra(EXTRA_END_OF_EDITION_BROADCAST)){
            return BroadcastStatus.END_OF_EDITION;
        } else {
            return BroadcastStatus.INVALID_ARG;
        }
    }

    private static Intent getBaseIntentForBroadcast(int featureId){

        Intent intent = new Intent(EDITION_DOWNLOAD_INTENT_FILTER);
        intent.putExtra(EXTRA_DOWNLOADED_FEATURE_ID,featureId);

        return intent;
    }

    protected static void generateSuccessBrodcast(int featureId){
        //Log.d("CheckConfigIntegrity", "generateSuccessBrodcast: ");

        Intent successBrodcastIntent = getBaseIntentForBroadcast(featureId);
        successBrodcastIntent.putExtra(EXTRA_SUCCESS_BROADCAST,"Success");

        LocalBroadcastManager.getInstance(NewsServerUtility.getContext()).sendBroadcast(successBrodcastIntent);
    }

    protected static void generateEmptyBrodcast(int featureId){

        //Log.d("CheckConfigIntegrity", "generateEmptyBrodcast: ");
        Intent emptyBrodcastIntent = getBaseIntentForBroadcast(featureId);
        emptyBrodcastIntent.putExtra(EXTRA_EMPTY_BROADCAST,"Empty");

        LocalBroadcastManager.getInstance(NewsServerUtility.getContext()).sendBroadcast(emptyBrodcastIntent);
    }

    protected static void generateFailureBrodcast(int featureId){

        //Log.d("CheckConfigIntegrity", "generateFailureBrodcast: ");

        Intent failureBrodcastIntent = getBaseIntentForBroadcast(featureId);
        failureBrodcastIntent.putExtra(EXTRA_FAILURE_BROADCAST,"Failure");

        LocalBroadcastManager.getInstance(NewsServerUtility.getContext()).sendBroadcast(failureBrodcastIntent);
    }

    protected static void generateEndOfEditionBrodcast(int featureId){

        //Log.d("CheckConfigIntegrity", "generateEndOfEditionBrodcast: ");

        Intent endOfEditionBrodcastIntent = getBaseIntentForBroadcast(featureId);
        endOfEditionBrodcastIntent.putExtra(EXTRA_END_OF_EDITION_BROADCAST,"End_of_edition");

        LocalBroadcastManager.getInstance(NewsServerUtility.getContext()).sendBroadcast(endOfEditionBrodcastIntent);
    }

    public void loadEdition(Intent intent) {

        mFeature = EditionLoaderService.getIntentFeature(intent);

        mNewspaper = NewspaperHelper.findNewspaperById(mFeature.getNewsPaperId());
        if (mNewspaper == null) {
            EditionLoaderService.removeFeatureFromActiveList(mFeature);
            return;
        }

        int pageNumber = 0;

        if (EditionLoaderService.checkIfFirstEditionDownloadRequest(intent)){
            pageNumber = 1;
            mFirstEditionDownloadRequest =true;
        } else {
            if (sLastCheckedPageNumberMap.containsKey(mFeature.getId())){
                pageNumber = sLastCheckedPageNumberMap.get(mFeature.getId())+1;
            } else {
                pageNumber = 1;
            }
        }

        loadEdition(pageNumber,false);
    }

    private void loadEdition(int currentPageNumber, boolean internalRequest){

        mPageLink = getPageLink(mFeature,currentPageNumber);
        //Log.d(TAG, "loadEdition: mPageLink: "+mPageLink);
        if (mPageLink == null) {
            generateEndOfEditionBrodcast(mFeature.getId());
            EditionLoaderService.removeFeatureFromActiveList(mFeature);
            return;
        }

        try {
            while (!NewsServerUtility.checkIfCanAccessSite(mNewspaper)) {

                try {
                    Thread.sleep(NewsServerUtility.DEFAULT_MINIMUM_SITE_ACCESS_DELAY_MILLIS / 2);
                    //Log.d(TAG, "loadEdition: Going to sleep");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            mEditionDocument = URLConnectionHelper.getJsopDocument(mPageLink);


            if (mEditionDocument == null) {
                generateFailureBrodcast(mFeature.getId());
                EditionLoaderService.removeFeatureFromActiveList(mFeature);
                return;
            }

            parseDocument();

            if (mEditionSummary == null)   {
                generateFailureBrodcast(mFeature.getId());
                EditionLoaderService.removeFeatureFromActiveList(mFeature);
                return;
            }

            EditionSummarySaveStatus saveStatus = saveEditionSummary();

            if (internalRequest){
                switch (saveStatus){
                    case SUCCESS:
                        //Log.d(TAG, "loadEdition: internalRequest from internalRequest");
                        loadEdition(currentPageNumber+1,true);
                        break;
                    default:
                        sLastCheckedPageNumberMap.put(
                            mFeature.getId(),sLastCheckedPageNumberMap.get(mFeature.getId())+currentPageNumber-1
                        );
                        generateSuccessBrodcast(mFeature.getId());
                        break;
                }
            } else {
                switch (saveStatus){
                    case SUCCESS:
                        if (mFirstEditionDownloadRequest && sLastCheckedPageNumberMap.containsKey(mFeature.getId())){
                            //Log.d(TAG, "loadEdition: internalRequest from gen");
                            loadEdition(currentPageNumber+1,true);
                        } else {
                            generateSuccessBrodcast(mFeature.getId());
                            sLastCheckedPageNumberMap.put(
                                    mFeature.getId(),currentPageNumber
                            );
                        }
                        break;
                    case SUCCESS_WITH_REPEAT:
                        generateSuccessBrodcast(mFeature.getId());
                        if (!(mFirstEditionDownloadRequest && sLastCheckedPageNumberMap.containsKey(mFeature.getId()))) {
                            sLastCheckedPageNumberMap.put(
                                    mFeature.getId(), currentPageNumber
                            );
                        }
                        break;
                    case EMPTY_WITH_REPEAT:
                        //Log.d(TAG, "loadEdition: EMPTY_WITH_REPEAT");
                        if (mFirstEditionDownloadRequest && sLastCheckedPageNumberMap.containsKey(mFeature.getId())) {
                            generateEmptyBrodcast(mFeature.getId());
                        } else {
                            if(needToCheckForEmptyWithRepeat() && /*||(mFirstEditionDownloadRequest &&*/
                                    mReRunCountOnEmptyWithRepeat < getMaxReRunCountOnEmptyWithRepeat()/*)*/){
                                mReRunCountOnEmptyWithRepeat++;
                                loadEdition(currentPageNumber+1,false);
                            } else {
                                //Log.d(TAG, "loadEdition: generateEndOfEditionBrodcast mReRunCountOnEmptyWithRepeat: "+
                                        //mReRunCountOnEmptyWithRepeat);
                                generateEndOfEditionBrodcast(mFeature.getId());
                            }
                        }
                        break;
                    case EMPTY:
                        //getBroadcastMessageForEmptyPage();
                        if (mReRunCountOnEmpty < getMaxReRunCountOnEmpty()){
                            mReRunCountOnEmpty++;
                            loadEdition(currentPageNumber+1,false);
                            break;
                        }
                    case END_OF_EDITION:
                        generateEndOfEditionBrodcast(mFeature.getId());
                        break;
                }
            }
        } catch (Exception e) {
            NewsServerUtility.logErrorMessage(TAG+":"+e.getMessage());
            e.printStackTrace();
            generateSuccessBrodcast(mFeature.getId());
        }

        EditionLoaderService.removeFeatureFromActiveList(mFeature);
    }

    protected boolean needToCheckForEmptyWithRepeat() {
        //return false;
        return true;
    }

    private void parseDocument(){

        mCountry = CountryHelper.findCountryByName(mNewspaper.getCountryName());
        if (mCountry == null) return;

        if (getArticlePublicationDatetimeFormat() !=null) {
            mSimpleDateFormat = new SimpleDateFormat(getArticlePublicationDatetimeFormat());
            mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone(mCountry.getTimeZone()));
        }

        mPreviewBlocks = getPreviewBlocks();

        if (mPreviewBlocks==null || mPreviewBlocks.size()==0) return;

        int articleIndex = 0;

        for (Element previewBlock: mPreviewBlocks){
            try {
                String articleLink = getArticleLink(previewBlock);
                if (articleLink == null) continue;
                articleLink = processArticleLink(articleLink);
                mEditionSummary.getArticleLinks().put(articleIndex, articleLink);
            } catch (Exception e) {
                continue;
            }

            try {
                String previewImageLink = getArticlePreviewImageLink(previewBlock);
                //Log.d(TAG, "parseDocument: previewImageLink:"+previewImageLink);
                previewImageLink = processArticlePreviewImageLink(previewImageLink);
                //Log.d(TAG, "parseDocument: previewImageLink:"+previewImageLink);
                mEditionSummary.getArticlePreviewImageLinks().put(articleIndex, previewImageLink);
            } catch (Exception e) {
                mEditionSummary.getArticlePreviewImageLinks().put(articleIndex,null);
            }

            try {
                String articleTitle = getArticleTitle(previewBlock);
                if (articleTitle == null) continue;
                mEditionSummary.getArticleTitles().put(articleIndex,articleTitle);
            } catch (Exception e) {
                continue;
            }

            try {
                String articlePublicationDateString = getArticlePublicationDateString(previewBlock);
                //Log.d(TAG, "parseDocument: articlePublicationDateString:"+articlePublicationDateString);
                Long articlePublicationDateTimeStamp = getArticlePublicationTimeStamp(previewBlock);
                if (articlePublicationDateTimeStamp==null) {
                    articlePublicationDateTimeStamp = mSimpleDateFormat.parse(articlePublicationDateString).getTime();
                }
                //Log.d(TAG, "parseDocument: articlePublicationDateTimeStamp:"+articlePublicationDateTimeStamp);
                mEditionSummary.getArticlePublicationTimeStamp().
                        put(articleIndex,articlePublicationDateTimeStamp);
            } catch (Exception e) {
                //Log.d(TAG, "parseDocument: Error: "+e.getMessage());
                mEditionSummary.getArticlePublicationTimeStamp().put(articleIndex,0L);
            }

            try {
                String articleModificationDateString = getArticleModificationDateString(previewBlock);
                Long articleModificationDateTimeStamp = getArticleModificationTimeStamp(previewBlock);
                if (articleModificationDateTimeStamp==null) {
                    articleModificationDateTimeStamp = mSimpleDateFormat.parse(articleModificationDateString).getTime();
                }
                mEditionSummary.getArticleLastModificationTimeStamp().
                        put(articleIndex,articleModificationDateTimeStamp);
            } catch (Exception e) {
                mEditionSummary.getArticleLastModificationTimeStamp().put(articleIndex,0L);
            }
            //mEditionSummary.getArticleLastModificationTimeStamp().put(articleIndex,0L);
            articleIndex++;
        }

    }

    private EditionSummarySaveStatus saveEditionSummary() {

        if (!EditionLoaderService.isRunning()){
            return EditionSummarySaveStatus.EMPTY;
        }
        //Log.d("StackTrace", "saveEditionSummary: mEditionSummary.getArticleLinks().size(): "+mEditionSummary.getArticleLinks().size());
        //Log.d(TAG, "saveEditionSummary: mNewspaper.getName(): "+mNewspaper.getName());
        //Log.d(TAG, "saveEditionSummary: mNewspaper.getId(: "+mNewspaper.getId());

        int newArticleCount=0;
        int firstArticleId = 0;
        int repeatArticleCount = 0;

        //final SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        int insertedArticleId;

        label:
        for (int i = 0; i< mEditionSummary.getArticleLinks().size(); i++) {

            try {

                if (mEditionSummary.getArticlePublicationTimeStamp().get(i) == 0L){
                    if (!newspaperIdListForWithoutPreviewDate.contains(mNewspaper.getId())) {
                        continue;
                    }
                }

                //Log.d(TAG, "saveEditionSummary: mEditionSummary.getArticleTitles(): "+mEditionSummary.getArticleTitles().get(i));

                int hashCode = mEditionSummary.getArticleLinks().get(i).hashCode();
                ArrayList<Article> articleArrayList = ArticleHelper.findArticlesByHashCode(hashCode);

                if (articleArrayList.size() != 0) {
                    for (Article article :
                            articleArrayList) {
                        if (article.getFeatureId() == mFeature.getId()){
                            repeatArticleCount++;
                            continue label;
                        }
                    }
                }

                int insertedImageId = -1;
                String previewImageLink = mEditionSummary.getArticlePreviewImageLinks().get(i);

                if (previewImageLink !=null){
                    int imageLinkHash = previewImageLink.hashCode();
                    if (!ImageDataHelper.checkIfImageExists(imageLinkHash)){
                        insertedImageId = ImageDataHelper.saveImageData(
                                imageLinkHash,previewImageLink,null
                        );
                    } else {
                        insertedImageId = imageLinkHash;
                    }
                }

                insertedArticleId = ArticleHelper.saveArticleDetails(
                        mFeature.getId(),
                        mEditionSummary.getArticleLinks().get(i),
                        mEditionSummary.getArticleTitles().get(i),
                        insertedImageId,
                        mEditionSummary.getArticlePublicationTimeStamp().get(i),
                        mEditionSummary.getArticleLastModificationTimeStamp().get(i)
                );
                if (insertedArticleId>0){
                    //Log.d(TAG, "saveEditionSummary: insertedArticleId: "+insertedArticleId);
                    if(newArticleCount == 0) {
                        firstArticleId = insertedArticleId;
                    }
                    newArticleCount++;
                }
            } catch (Exception ex){
                //Log.d(TAG, "saveEditionSummary: Error: "+ex.getMessage());
                continue ;
            }
        }

        if (newArticleCount>0){
            houseKeepingOnSuccessfulEditionLoad();
            /*if (NewsServerUtility.isNotificationEnabled()) {
                generateNotification(mFeature, firstArticleId);
            }*/
            if (repeatArticleCount>0){
                return EditionSummarySaveStatus.SUCCESS_WITH_REPEAT;
            } else {
                return EditionSummarySaveStatus.SUCCESS;
            }
        } else {
            if (repeatArticleCount>0){
                return EditionSummarySaveStatus.EMPTY_WITH_REPEAT;
            }else {
                return EditionSummarySaveStatus.EMPTY;
            }
        }
    }

    protected void houseKeepingOnSuccessfulEditionLoad(){}
    /*private void generateNotification(Feature feature, int firstArticleId) {}*/
}

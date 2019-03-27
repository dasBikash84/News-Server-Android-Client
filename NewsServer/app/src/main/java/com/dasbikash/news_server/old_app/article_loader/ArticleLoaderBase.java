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

package com.dasbikash.news_server.old_app.article_loader;

import android.os.Build;

import com.dasbikash.news_server.old_app.image_downloader.ImageDownloader;
import com.dasbikash.news_server.old_app.this_data.article.Article;
import com.dasbikash.news_server.old_app.this_data.article.ArticleHelper;
import com.dasbikash.news_server.old_app.this_data.article_fragment.ArticleFragmentHelper;
import com.dasbikash.news_server.old_app.this_data.article_fragment_payload.ArticleFragmentPayload;
import com.dasbikash.news_server.old_app.this_data.article_fragment_payload.ArticleFragmentPayloadHelper;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageDataHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.text_data.TextDataHelper;
import com.dasbikash.news_server.old_app.this_utility.CheckConfigIntegrity;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;
import com.dasbikash.news_server.old_app.this_utility.URLConnectionHelper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public abstract class ArticleLoaderBase {

    //protected static final String TAG = "ArticleLoaderBase";
    protected static final String TAG = "StackTrace";

    private static final String ARTICLE_IMAGE_BLOCK_REMOVER_REGEX = "<img.+?>";
    private static final String ARTICLE_IMAGE_BLOCK_REPLACER_REGEX = "";
    private static final int DEFAULT_REQUIRED_FEATURED_IMAGE_COUNT = 1;
    private static final int DEFAULT_FEATURED_IMAGE_INDEX = 0;

    protected Article mArticle;
    protected Newspaper mNewspaper;

    private final ArrayList<ArticleFragmentPayload>
            mArticleFragmentPayloads = new ArrayList<>();
    protected Document mDocument;

    protected final ArrayList<String> mParagraphInvalidatorText =
            new ArrayList<>();

    protected final ArrayList<String> mUnwantedArticleText =
            new ArrayList<>();

    protected final ArrayList<String> mParagraphQuiterText =
            new ArrayList<>();

    protected abstract Elements getArticleFragmentBlocks();

    protected abstract String getParagraphImageCaptionSelector();/* {
        return null;
    }*/

    protected abstract String getParagraphImageCaptionSelectorAttr();/* {
        return null;
    }*/

    protected abstract String getParagraphImageLinkSelectorAttr();/* {
        return "";
    }*/

    protected abstract String getParagraphImageSelector();/* {
        return null;
    }*/

    protected abstract String getFeaturedImageCaptionSelectorAttr();/* {
        return null;
    }*/

    protected abstract String getFeaturedImageCaptionSelector();/* {
        return null;
    }*/

    protected abstract String getFeaturedImageLinkSelectorAttr();/* {
        return null;
    }*/

    protected abstract String  getFeaturedImageSelector();/* {
        return null;
    }*/

    protected abstract String getArticleModificationDateString();/* {
        return null;
    }*/

    protected abstract String[] getArticleModificationDateStringFormats();/* {
        return null;
    }*/

    {
        mParagraphInvalidatorText.add("আরও পড়ুন");
        mParagraphInvalidatorText.add("আরও পড়ুন");
        mParagraphInvalidatorText.add("Read |");
        mParagraphInvalidatorText.add("READ |");
        mParagraphInvalidatorText.add("READ More |");
        mParagraphInvalidatorText.add("Read More |");
        mParagraphInvalidatorText.add("Also Read");
        mParagraphInvalidatorText.add("Also read");
        mParagraphInvalidatorText.add("Read Also");
        mParagraphInvalidatorText.add("Read also");
        mUnwantedArticleText.add("&nbsp;");
        mUnwantedArticleText.add("\\<div.+?\\>\\<\\/div\\>");
        mUnwantedArticleText.add("READ\\s+?ALSO.+?\\</a\\>");
        mUnwantedArticleText.add("<br>");
    }

    protected abstract String getSiteBaseAddress();

    protected String processLink(String linkText){
        String siteBaseAddress = getSiteBaseAddress();
        return NewsServerUtility.processLink(linkText,siteBaseAddress);
    }

    protected int getReqFeaturedImageCount(){
        return DEFAULT_REQUIRED_FEATURED_IMAGE_COUNT;
    }
    protected int getReqFeaturedImageIndex(){
        return DEFAULT_FEATURED_IMAGE_INDEX;
    }


    private void parseArticle() {

        //Log.d(TAG, "parseArticle: "+mArticle.getLink());

        mDocument = URLConnectionHelper.getJsopDocument(mArticle.getLink());

        if (mDocument == null) {
            return;
        }

        if (getArticleModificationDateStringFormats()!= null){

            String modificationDateString = getArticleModificationDateString();

            if (modificationDateString !=null && modificationDateString.length()>0){

                Country country = CountryHelper.findCountryByName(mNewspaper.getCountryName());
                Calendar articleModificationTime = Calendar.getInstance(TimeZone.getTimeZone(country.getTimeZone()));
                articleModificationTime.setTimeInMillis(0L);
                SimpleDateFormat simpleDateFormat;

                String[] modificationDateStringFormats = getArticleModificationDateStringFormats();

                for (int i = 0; i < modificationDateStringFormats.length; i++) {
                    try {
                        simpleDateFormat = new SimpleDateFormat(modificationDateStringFormats[i]);
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(country.getTimeZone()));
                        articleModificationTime.setTime(simpleDateFormat.parse(modificationDateString));
                        if (articleModificationTime.getTimeInMillis() !=0L) {
                            ArticleHelper.updateLastModificationTS(mArticle, articleModificationTime.getTimeInMillis());
                            break;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        if (getFeaturedImageSelector()!=null) {

            try{
                Elements featuredImageElements = mDocument.select(getFeaturedImageSelector());
                //Log.d(TAG, "parseArticle: mArticle.getTitle():"+mArticle.getTitle());
                //Log.d(TAG, "parseArticle: featuredImageElements.size():"+featuredImageElements.size());

                if (featuredImageElements!=null && featuredImageElements.size() == getReqFeaturedImageCount()) {
                    Element featuredImage = featuredImageElements.get(getReqFeaturedImageIndex());
                    //Log.d(TAG, "parseArticle:featuredImageLink: "+featuredImage.html());
                        if (getFeaturedImageLinkSelectorAttr() !=null) {

                            String featuredImageLink = featuredImage.attr(getFeaturedImageLinkSelectorAttr());
                           //Log.d(TAG, "parseArticle:featuredImageLink: "+featuredImageLink);
                            if (featuredImageLink.trim().length() > 0) {
                                featuredImageLink = processLink(featuredImageLink);
                                //Log.d(TAG, "parseArticle:featuredImageLink: "+featuredImageLink);

                                String imageCaption = "";
                                try {
                                    if (getFeaturedImageCaptionSelectorAttr() !=null) {
                                        //Log.d(TAG, "parseArticle:getFeaturedImageCaptionSelectorAttr() !=null:  "+getFeaturedImageCaptionSelectorAttr());
                                        imageCaption = featuredImage.attr(getFeaturedImageCaptionSelector());
                                        //Log.d(TAG, "parseArticle:imageCaption from attr: "+imageCaption);
                                    } else if(getFeaturedImageCaptionSelector() !=null) {
                                        //Log.d(TAG, "parseArticle: getFeaturedImageCaptionSelector() !=null");

                                            Elements featuredImageCaptionElements = mDocument.select(getFeaturedImageCaptionSelector());
                                            if (featuredImageCaptionElements.size()>0){
                                                imageCaption = featuredImageCaptionElements.get(getReqFeaturedImageIndex()).text();
                                                //Log.d(TAG, "parseArticle:imageCaption: "+imageCaption);
                                        }
                                    }
                                } catch (Exception ex){
                                    //Log.d(TAG, "parseArticle: Error: "+ex.getMessage());
                                    ex.printStackTrace();
                                }
                                //Log.d(TAG, "parseArticle: ");

                                int featuredImageDataId =
                                        ImageDataHelper.saveImageData(
                                                featuredImageLink.hashCode(),
                                                featuredImageLink,
                                                imageCaption
                                        );
                                ArticleFragmentPayload articleFragmentPayload = ArticleFragmentPayloadHelper.getInstanceByImageDataId(featuredImageDataId);
                                if (articleFragmentPayload != null) {
                                    mArticleFragmentPayloads.add(articleFragmentPayload);
                                }
                            }
                        }
                }

            } catch (Exception ex){
                //Log.d(TAG, "parseArticle: Error: "+ex.getMessage());
                ex.printStackTrace();
            }
        }

        Elements articleFragments = getArticleFragmentBlocks();//mDocument.select(getArticleFragmentBlockSelector());

        if (articleFragments!=null && articleFragments.size()>0) {

            articleFragmentLooper:

            for (Element articleFragment :
                    articleFragments) {

                String paraText = articleFragment.html();

                if (paraText.trim().length() == 0) continue;
                //Log.d(TAG, "parseArticle: paraText:"+paraText);

                if (getParagraphImageSelector() !=null) {

                    try {

                        Elements imageChildren = articleFragment.select(getParagraphImageSelector());
                        boolean imageCaptionFound = false;

                        if (imageChildren.size() > 0) {

                            for (Element imageChild :
                                    imageChildren) {

                                if (getParagraphImageLinkSelectorAttr() == null ||
                                        getParagraphImageLinkSelectorAttr().trim().length() == 0) continue;

                                String articleImageLink = imageChild.attr(getParagraphImageLinkSelectorAttr());

                                if (articleImageLink.length() > 0) {
                                    articleImageLink = processLink(articleImageLink);
                                    String imageCaption = "";
                                    try {

                                        if (getParagraphImageCaptionSelectorAttr() != null) {
                                            imageCaption = imageChild.attr(getParagraphImageCaptionSelectorAttr());
                                        } else if (getParagraphImageCaptionSelector() != null) {
                                            Elements imageCaptionElements = articleFragment.select(getParagraphImageCaptionSelector());
                                            if (imageCaptionElements.size() > 0) {
                                                imageCaption = imageCaptionElements.get(0).text();
                                                imageCaptionFound = true;
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                    int articleImageDataId =
                                        ImageDataHelper.saveImageData(
                                                articleImageLink.hashCode(),
                                                articleImageLink,
                                                imageCaption
                                        );

                                    ArticleFragmentPayload articleFragmentPayload = ArticleFragmentPayloadHelper.getInstanceByImageDataId(articleImageDataId);

                                    if (articleFragmentPayload != null) {
                                        mArticleFragmentPayloads.add(articleFragmentPayload);
                                    }
                                }
                            }
                        }
                        if (imageCaptionFound) {
                            //Log.d(TAG, "parseArticle: imageCaptionFound");
                            continue;
                        }
                        paraText = paraText.replaceAll(
                                ARTICLE_IMAGE_BLOCK_REMOVER_REGEX,
                                ARTICLE_IMAGE_BLOCK_REPLACER_REGEX
                        );
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }

                if (paraText.trim().length()==0) continue;

                for (String paragraphQuiterText:
                        mParagraphQuiterText) {
                    if (paraText.contains(paragraphQuiterText)) break articleFragmentLooper;
                }

                for (String paragraphInvalidatorText:
                        mParagraphInvalidatorText) {
                    if (paraText.contains(paragraphInvalidatorText)) continue articleFragmentLooper;
                }

                for (String unwantedArticleText:
                        mUnwantedArticleText) {
                    paraText = paraText.replaceAll(unwantedArticleText,"");
                }

                int textDataId = TextDataHelper.saveTextData(paraText);

                ArticleFragmentPayload articleFragmentPayload = ArticleFragmentPayloadHelper.getInstanceByTextDataId(textDataId);

                if (articleFragmentPayload != null) {
                    mArticleFragmentPayloads.add(articleFragmentPayload);
                }
            }
        }
    }

    private boolean saveArticleFragmentPayloads(){

        if (!ArticleLoaderService.isRunning()){
            return false;
        }

        ArrayList<Integer> savedImageIds = new ArrayList<>();

        for (ArticleFragmentPayload articleFragmentPayload :
                mArticleFragmentPayloads) {

            if (articleFragmentPayload.getImageDataId() !=null){
                if (savedImageIds.contains(articleFragmentPayload.getImageDataId())){
                    articleFragmentPayload.setImageDataId(null);
                } else {
                    savedImageIds.add(articleFragmentPayload.getImageDataId());
                }
            }

            ArticleFragmentHelper.saveArticleFragmentData(
                    mArticle.getLinkHashCode(),
                    articleFragmentPayload.getTextDataId(),
                    articleFragmentPayload.getImageDataId()
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (NewsServerUtility.isDeveloperModeOn() && CheckConfigIntegrity.isRunning()){
                    continue;
                }
            }
            if (articleFragmentPayload.getImageDataId() !=null){
                ImageDownloader.placeFileDownloadRequest(
                        articleFragmentPayload.getImageDataId(),mNewspaper
                );
            }
        }
        return true;
    }

    public boolean downloadArticle(Article article, Newspaper newspaper){

        if (!ArticleLoaderService.isRunning()){
            return false;
        }

        mArticle = article;
        mNewspaper = newspaper;

        while (true){

            if(NewsServerUtility.checkIfCanAccessSite(mNewspaper)){
                break;
            }

            try {
                Thread.sleep(NewsServerUtility.DEFAULT_MINIMUM_SITE_ACCESS_DELAY_MILLIS /2);
                //Log.d(TAG, "Going to sleep");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        parseArticle();

        if (mArticleFragmentPayloads == null || mArticleFragmentPayloads.size() == 0) {
            return false;
        }

        return saveArticleFragmentPayloads();
    }
}

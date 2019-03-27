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

package com.dasbikash.news_server.old_app.edition_loader.jugantor;

import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;
import com.dasbikash.news_server.old_app.this_utility.CalenderUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;



public class JugantorEditionLoader extends EditionLoaderBase {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "JugEdLoader";

    private static final String MINUTES_AGO_TIME_STRING_BANGLA = "মি. আগে";
    private static final String HOURS_AGO_TIME_STRING_BANGLA = "ঘ. আগে";
    public static final int ONE_HOUR_IN_MILLIS = 3600000;
    public static final int ONE_MINUTE_IN_MILLIS = 60000;

    private final String mSiteBaseAddress = "https://www.jugantor.com";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getArticlePublicationDatetimeFormat() {
        return JugantorEditionParserInfo.ARTICLE_PUBLICATION_DATE_TIME_FORMAT;
    }

    @Override
    protected Elements getPreviewBlocks() {
        return mEditionDocument.select(JugantorEditionParserInfo.ARTICLE_PREVIEW_BLOCK_SELECTOR);
    }

    @Override
    protected String getArticleLink(Element previewBlock) {
        return previewBlock.select(JugantorEditionParserInfo.ARTICLE_LINK_ELEMENT_SELECTOR).get(0).
                                    attr(JugantorEditionParserInfo.ARTICLE_LINK_TEXT_SELECTOR_TAG);
    }

    @Override
    protected String getArticlePreviewImageLink(Element previewBlock) {
        return previewBlock.select(JugantorEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_ELEMENT_SELECTOR).get(0).
                                    attr(JugantorEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_TEXT_SELECTOR_TAG);
    }

    @Override
    protected String getArticleTitle(Element previewBlock) {
        return previewBlock.select(JugantorEditionParserInfo.ARTICLE_TITLE_ELEMENT_SELECTOR).
                                    get(0).text();
    }

    @Override
    protected String getArticlePublicationDateString(Element previewBlock) {
        return null;
    }

    @Override
    protected String processArticlePreviewImageLink(String previewImageLink) {
        if (!previewImageLink.matches(".+?url\\(.+?\\).+")){
            return null;
        }
        previewImageLink = (previewImageLink.split("url\\("))[1];
        previewImageLink = (previewImageLink.split("\\)"))[0];
        //return previewImageLink;
        return super.processArticlePreviewImageLink(previewImageLink);
    }

    @Override
    protected Long getArticlePublicationTimeStamp(Element previewBlock) {
        String articlePublicationDateString = previewBlock.select(JugantorEditionParserInfo.ARTICLE_PUBLICATION_DATE_ELEMENT_SELECTOR).
                                                get(0).text();
        return processDateString(articlePublicationDateString,mSimpleDateFormat);
    }

    private long processDateString(String articlePublicationDateString, SimpleDateFormat simpleDateFormat) {
        //Log.d(TAG, "processDateString: articlePublicationDateString: "+articlePublicationDateString);
        if (articlePublicationDateString.contains(HOURS_AGO_TIME_STRING_BANGLA)){
            articlePublicationDateString =
                    articlePublicationDateString.replace(HOURS_AGO_TIME_STRING_BANGLA,"").trim();
            int agoHour = 0;
            try {
                agoHour = Integer.decode(articlePublicationDateString);
                //Log.d(TAG, "processDateString: agoHour: " + agoHour);
                Calendar publicationTime = CalenderUtility.getCurrentTime();
                publicationTime.setTimeInMillis(publicationTime.getTimeInMillis() - agoHour* ONE_HOUR_IN_MILLIS);
                return publicationTime.getTimeInMillis();
            }catch (Exception ex){
                //Log.d(TAG, "processDateString: Error: "+ex.getMessage());
                return 0L;
            }
        }else if(articlePublicationDateString.contains(MINUTES_AGO_TIME_STRING_BANGLA)){
            articlePublicationDateString =
                    articlePublicationDateString.replace(MINUTES_AGO_TIME_STRING_BANGLA,"").trim();
            int agoMinutes = 0;
            try {
                agoMinutes = Integer.decode(articlePublicationDateString);
                //Log.d(TAG, "processDateString: agoMinutes: " + agoMinutes);
                Calendar publicationTime = CalenderUtility.getCurrentTime();
                publicationTime.setTimeInMillis(publicationTime.getTimeInMillis() - agoMinutes* ONE_MINUTE_IN_MILLIS);
                return publicationTime.getTimeInMillis();
            }catch (Exception ex){
                //Log.d(TAG, "processDateString: Error: "+ex.getMessage());
                return 0L;
            }
        } else{
            //Log.d(TAG, "processDateString: GeneralTimeString: "+articlePublicationDateString);
            articlePublicationDateString = DisplayUtility.banglaToEnglishDateString(articlePublicationDateString).trim();
            //Log.d(TAG, "processDateString: inEnglish: "+articlePublicationDateString);
            Calendar publicationTime = Calendar.getInstance();
            publicationTime.setTimeZone(simpleDateFormat.getTimeZone());
            try {
                publicationTime.setTime(simpleDateFormat.parse(articlePublicationDateString));
                return publicationTime.getTimeInMillis();
            } catch (ParseException e) {
                e.printStackTrace();
                return 0L;
            }
        }
    }

}
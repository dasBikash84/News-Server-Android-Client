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

package com.dasbikash.news_server.old_app.article_loader.anando_bazar;

import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderBase;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import org.jsoup.select.Elements;

public class AnandoBazarArticleLoader extends ArticleLoaderBase {

    private static final String TAG = "ABArticleLoader";

    private final String mSiteBaseAddress = "https://www.anandabazar.com";

    {
        mParagraphQuiterText.add("ভ্রম সংশোধন");
    }

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getFeaturedImageCaptionSelector() {
        return AnandoBazarArticleParserInfo.FEATURED_IMAGE_CAPTION_SELECTOR;
    }

    @Override
    protected String getFeaturedImageLinkSelectorAttr() {
        return AnandoBazarArticleParserInfo.FEATURED_IMAGE_LINK_SELECTOR;
    }

    @Override
    protected String getFeaturedImageSelector() {
        return AnandoBazarArticleParserInfo.FEATURED_IMAGE_SELECTOR;
    }

    @Override
    protected String getArticleModificationDateString() {
        Elements dateStringElements =
                mDocument.select(AnandoBazarArticleParserInfo.ARTICLE_MODIFICATION_DATE_STRING_SELECTOR);
        //Log.d(TAG, "parseArticle: dateStringElements.size(): "+dateStringElements.size());
        if (dateStringElements != null && dateStringElements.size()>0) {
            String dateString = "";
            if (dateStringElements.size() > 1) {
                dateString = dateStringElements.get(1).text();
            } else {
                dateString = dateStringElements.get(0).text();
            }
            //Log.d(TAG, "parseArticle: dateString: "+dateString);
            if (dateString.trim().length() > 0) {
                if (dateString.split("শেষ আপডেট\\s?:").length == 2) {
                    dateString = dateString.split("শেষ আপডেট\\s?:\\s?")[1];
                }
            }
            //Log.d(TAG, "parseArticle: dateString: "+dateString);
            if (dateString.trim().length() > 0) {
                dateString = DisplayUtility.banglaToEnglishDateString(dateString.trim());
            }
            return dateString;
        }
        return null;
    }

    @Override
    protected String[] getArticleModificationDateStringFormats() {
        return AnandoBazarArticleParserInfo.ARTICLE_MODIFICATION_DATE_STRING_FORMATS;
    }

    private int mArticleLayoutType = 0;

    @Override
    protected Elements getArticleFragmentBlocks() {
        Elements articleFragments = mDocument.select(
                AnandoBazarArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR[mArticleLayoutType]
        );
        if (articleFragments==null || articleFragments.size() == 0){
            mArticleLayoutType = 1;
            articleFragments = mDocument.select(
                    AnandoBazarArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR[mArticleLayoutType]
            );
        }

        return articleFragments;
    }

    @Override
    protected String getParagraphImageCaptionSelector() {
        return null;
    }

    @Override
    protected String getParagraphImageSelector() {
        return AnandoBazarArticleParserInfo.PARAGRAPH_IMAGE_SELECTOR[mArticleLayoutType];
    }

    @Override
    protected String getFeaturedImageCaptionSelectorAttr() {
        return null;
    }

    @Override
    protected String getParagraphImageLinkSelectorAttr() {
        return AnandoBazarArticleParserInfo.PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR[mArticleLayoutType];
    }

    @Override
    protected String getParagraphImageCaptionSelectorAttr() {
        return AnandoBazarArticleParserInfo.PARAGRAPH_IMAGE_CAPTION_SELECTOR_ATTR[mArticleLayoutType];
    }
}

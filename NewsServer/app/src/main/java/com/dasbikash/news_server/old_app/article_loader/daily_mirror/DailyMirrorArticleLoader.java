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

package com.dasbikash.news_server.old_app.article_loader.daily_mirror;

import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderBase;

import org.jsoup.select.Elements;

public class DailyMirrorArticleLoader extends ArticleLoaderBase {

    private static final String TAG = "DMALoader";

    private final String mSiteBaseAddress = "https://www.mirror.co.uk";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getArticleModificationDateString() {

        Elements dateStringElements =
                mDocument.select(DailyMirrorArticleParserInfo.ARTICLE_MODIFICATION_DATE_STRING_BLOCK_SELECTOR);
        //Log.d(TAG, "parseArticle: dateStringElements.size(): "+dateStringElements.size());
        if (dateStringElements != null && dateStringElements.size()>0) {
            String dateString = "";
            if (dateStringElements.size() == 2) {
                dateString = dateStringElements.get(1).
                        attr(DailyMirrorArticleParserInfo.ARTICLE_MODIFICATION_DATE_STRING_SELECTOR_ATTR).trim();
            } else {
                dateString = dateStringElements.get(0).
                        attr(DailyMirrorArticleParserInfo.ARTICLE_MODIFICATION_DATE_STRING_SELECTOR_ATTR).trim();
            }
            return dateString;
        }
        return null;
    }

    @Override
    protected String[] getArticleModificationDateStringFormats() {
        return new String[]{DailyMirrorArticleParserInfo.ARTICLE_MODIFICATION_DATE_STRING_FORMATS};
    }

    @Override
    protected Elements getArticleFragmentBlocks() {

        Elements articleDataBloacks = mDocument.select(DailyMirrorArticleParserInfo.ARTICLE_DATA_BLOCK_SELECTOR);
        if (articleDataBloacks.size() !=1) return null;
        return articleDataBloacks.get(0).select(DailyMirrorArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR);
    }

    @Override
    protected String getParagraphImageSelector() {
        return DailyMirrorArticleParserInfo.PARAGRAPH_IMAGE_SELECTOR;
    }

    @Override
    protected String getFeaturedImageCaptionSelectorAttr() {
        return null;
    }

    @Override
    protected String getFeaturedImageCaptionSelector() {
        return null;
    }

    @Override
    protected String getFeaturedImageLinkSelectorAttr() {
        return null;
    }

    @Override
    protected String getFeaturedImageSelector() {
        return null;
    }

    @Override
    protected String getParagraphImageLinkSelectorAttr() {
        return DailyMirrorArticleParserInfo.PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR;
    }

    @Override
    protected String getParagraphImageCaptionSelector() {
        return DailyMirrorArticleParserInfo.PARAGRAPH_IMAGE_CAPTION_SELECTOR;
    }

    @Override
    protected String getParagraphImageCaptionSelectorAttr() {
        return null;
    }
}

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

package com.dasbikash.news_server.old_app.article_loader.the_times_of_india;

import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderBase;

import org.jsoup.select.Elements;

public class TheTimesOfIndiaArticleLoader extends ArticleLoaderBase {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "TTOIArticleLoader";

    private final String mSiteBaseAddress = "https://timesofindia.indiatimes.com";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getFeaturedImageSelector() {
        return TheTimesOfIndiaArticleParserInfo.FEATURED_IMAGE_SELECTOR;
    }

    @Override
    protected String getArticleModificationDateString() {
        return null;
    }

    @Override
    protected String[] getArticleModificationDateStringFormats() {
        return new String[0];
    }

    @Override
    protected String getFeaturedImageLinkSelectorAttr() {
        return TheTimesOfIndiaArticleParserInfo.FEATURED_IMAGE_LINK_SELECTOR_ATTR;
    }

    @Override
    protected String getFeaturedImageCaptionSelectorAttr() {
        return TheTimesOfIndiaArticleParserInfo.FEATURED_IMAGE_CAPTION_SELECTOR_ATTR;
    }

    @Override
    protected String getFeaturedImageCaptionSelector() {
        return null;
    }

    private int mParagraphLayoutType = 0;

    @Override
    protected Elements getArticleFragmentBlocks() {

        Elements articleFragmentBlocks =
                mDocument.select(TheTimesOfIndiaArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR[mParagraphLayoutType]);

        if (articleFragmentBlocks.size()>0){
            return articleFragmentBlocks;
        }
        mParagraphLayoutType++;

        articleFragmentBlocks =
                mDocument.select(TheTimesOfIndiaArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR[mParagraphLayoutType]);

        if (articleFragmentBlocks.size()>0){
            return articleFragmentBlocks;
        }
        mParagraphLayoutType++;

        articleFragmentBlocks =
                mDocument.select(TheTimesOfIndiaArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR[mParagraphLayoutType]);

        if (articleFragmentBlocks.size()>0){
            return articleFragmentBlocks;
        }
        return null;
    }

    @Override
    protected String getParagraphImageCaptionSelector() {
        return null;
    }

    @Override
    protected String getParagraphImageSelector() {
        return TheTimesOfIndiaArticleParserInfo.PARAGRAPH_IMAGE_SELECTOR[mParagraphLayoutType];
    }

    @Override
    protected String getParagraphImageLinkSelectorAttr() {
        return TheTimesOfIndiaArticleParserInfo.PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR[mParagraphLayoutType];
    }

    @Override
    protected String getParagraphImageCaptionSelectorAttr() {
        return TheTimesOfIndiaArticleParserInfo.PARAGRAPH_IMAGE_CAPTION_SELECTOR_ATTR[mParagraphLayoutType];
    }
}

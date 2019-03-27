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

package com.dasbikash.news_server.old_app.article_loader.the_gurdian;

import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderBase;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TheGurdianArticleLoader extends ArticleLoaderBase {

    private static final String TAG = "TGArtLoader";

    private final String mSiteBaseAddress = "https://www.theguardian.com";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getFeaturedImageSelector() {
        return TheGurdianArticleParserInfo.FEATURED_IMAGE_SELECTOR;
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
        return TheGurdianArticleParserInfo.FEATURED_IMAGE_LINK_SELECTOR_ATTR;
    }

    @Override
    protected String getFeaturedImageCaptionSelector() {
        return TheGurdianArticleParserInfo.FEATURED_IMAGE_CAPTION_SELECTOR;
    }

    @Override
    protected Elements getArticleFragmentBlocks() {

        Elements articleDataBlocks = mDocument.select(TheGurdianArticleParserInfo.ARTICLE_DATA_BLOCK_SELECTOR);

        if (articleDataBlocks != null && articleDataBlocks.size() == 1) {
            Element articleDataBlock = articleDataBlocks.get(0);
            return articleDataBlock.select(TheGurdianArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR);
        }
        return null;
    }

    @Override
    protected String getParagraphImageSelector() {
        return TheGurdianArticleParserInfo.PARAGRAPH_IMAGE_SELECTOR;
    }

    @Override
    protected String getFeaturedImageCaptionSelectorAttr() {
        return null;
    }

    @Override
    protected String getParagraphImageLinkSelectorAttr() {
        return TheGurdianArticleParserInfo.PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR;
    }

    @Override
    protected String getParagraphImageCaptionSelector() {
        return TheGurdianArticleParserInfo.PARAGRAPH_IMAGE_CAPTION_SELECTOR;
    }

    @Override
    protected String getParagraphImageCaptionSelectorAttr() {
        return null;
    }
}

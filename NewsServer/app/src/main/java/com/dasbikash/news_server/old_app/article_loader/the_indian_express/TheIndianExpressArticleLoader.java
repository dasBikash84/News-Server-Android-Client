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

package com.dasbikash.news_server.old_app.article_loader.the_indian_express;

import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderBase;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TheIndianExpressArticleLoader extends ArticleLoaderBase {

    private static final String TAG = "TheIndianExpress";
    //private static final String TAG = "StackTrace";

    private final String mSiteBaseAddress = "https://indianexpress.com";
    private Element mArticleDataBlock;

    private final String[] PARA_FILTER_TEXT =
            {
                    "Indian Express App",
                    "Express Editorial",
                    "Express Opinion",
                    "READ |",
                    "Express Explained"
            };

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    {
        for (String filterText:
                PARA_FILTER_TEXT) {
            mParagraphInvalidatorText.add(filterText);
        }
    };

    @Override
    protected String getArticleModificationDateString() {
        Elements articleDataBlocks =
                mDocument.select(TheIndianExpressArticleParserInfo.ARTICLE_DATA_BLOCK_SELECTOR);

        if (articleDataBlocks.size() == 1) {
            mArticleDataBlock = articleDataBlocks.get(0);
            if (mArticleDataBlock!=null && mArticle.getPublicationTS() == 0L) {
                Elements dateTimeBlocks = mArticleDataBlock.select(
                        TheIndianExpressArticleParserInfo.ARTICLE_MODIFICATION_DATE_SELECTOR
                );
                Element dateTimeBlock = null;
                if (dateTimeBlocks.size() == 1) {
                    dateTimeBlock = dateTimeBlocks.first();
                }
                if (dateTimeBlock != null) {
                    return dateTimeBlock.attr(TheIndianExpressArticleParserInfo.ARTICLE_MODIFICATION_DATE_SELECTOR_ATTR);
                }
            }
        }
        return null;
    }

    @Override
    protected String[] getArticleModificationDateStringFormats() {
        return new String[]{TheIndianExpressArticleParserInfo.ARTICLE_MODIFICATION_DATE_STRING_FORMATS};
    }

    @Override
    protected Elements getArticleFragmentBlocks() {
        if (mArticleDataBlock !=null){
            return mArticleDataBlock.select(TheIndianExpressArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR);
        }
        return null;
    }

    @Override
    protected String getParagraphImageSelector() {
        return TheIndianExpressArticleParserInfo.PARAGRAPH_IMAGE_SELECTOR;
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
        return TheIndianExpressArticleParserInfo.PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR;
    }

    @Override
    protected String getParagraphImageCaptionSelector() {
        return TheIndianExpressArticleParserInfo.PARAGRAPH_IMAGE_CAPTION_SELECTOR;
    }

    @Override
    protected String getParagraphImageCaptionSelectorAttr() {
        return null;
    }
}

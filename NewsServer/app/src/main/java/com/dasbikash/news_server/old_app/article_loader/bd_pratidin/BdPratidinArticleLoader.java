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

package com.dasbikash.news_server.old_app.article_loader.bd_pratidin;

import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderBase;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import org.jsoup.select.Elements;

public class BdPratidinArticleLoader extends ArticleLoaderBase {

    private static final String TAG = "BdPArtLoader";
    public static final String DATE_STRING_SPLITTER_REGEX = "প্রকাশ\\s:\\s";

    private final String mSiteBaseAddress = "http://www.bd-pratidin.com";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String processLink(String linkText) {
        if (linkText.matches("^\\./.+")){
            linkText = linkText.substring(1);
        }
        return super.processLink(linkText);
    }

    @Override
    protected String getArticleModificationDateString() {

        Elements dateStringElements =
                mDocument.select(BdPratidinArticleParserInfo.ARTICLE_MODIFICATION_DATE_STRING_SELECTOR);

        if (dateStringElements != null && dateStringElements.size()>=1) {
            //Log.d(TAG, "parseArticle: dateText: "+document.select(".detail-poauthor li").get(0).text().trim());
            String dateTextFromPage = dateStringElements.get(0).text().trim();

            if (dateTextFromPage.split(DATE_STRING_SPLITTER_REGEX).length == 2) {
                String dateString = ((dateTextFromPage.split(DATE_STRING_SPLITTER_REGEX))[1]).trim();
                //Log.d(TAG, "parseArticle: dateString:"+dateString);
                dateString = DisplayUtility.banglaToEnglishDateString(dateString);
                return dateString;
            }
        }
        return null;
    }

    @Override
    protected String[] getArticleModificationDateStringFormats() {
        return new String[]{BdPratidinArticleParserInfo.ARTICLE_MODIFICATION_DATE_STRING_FORMATS};
    }

    @Override
    protected String getFeaturedImageCaptionSelector() {
        return BdPratidinArticleParserInfo.FEATURED_IMAGE_CAPTION_SELECTOR;
    }

    @Override
    protected String getFeaturedImageLinkSelectorAttr() {
        return BdPratidinArticleParserInfo.FEATURED_IMAGE_LINK_SELECTOR;
    }

    @Override
    protected String getFeaturedImageSelector() {
        return BdPratidinArticleParserInfo.FEATURED_IMAGE_SELECTOR;
    }

    @Override
    protected Elements getArticleFragmentBlocks() {
        return mDocument.select(BdPratidinArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR);
    }

    @Override
    protected String getParagraphImageCaptionSelector() {
        return null;
    }

    @Override
    protected String getParagraphImageSelector() {
        return BdPratidinArticleParserInfo.PARAGRAPH_IMAGE_SELECTOR;
    }

    @Override
    protected String getFeaturedImageCaptionSelectorAttr() {
        return null;
    }

    @Override
    protected String getParagraphImageLinkSelectorAttr() {
        return BdPratidinArticleParserInfo.PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR;
    }

    @Override
    protected String getParagraphImageCaptionSelectorAttr() {
        return BdPratidinArticleParserInfo.PARAGRAPH_IMAGE_CAPTION_SELECTOR_ATTR;
    }
}

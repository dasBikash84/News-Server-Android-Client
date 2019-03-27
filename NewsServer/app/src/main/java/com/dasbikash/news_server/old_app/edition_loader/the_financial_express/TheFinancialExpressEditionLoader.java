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

package com.dasbikash.news_server.old_app.edition_loader.the_financial_express;


import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class TheFinancialExpressEditionLoader extends EditionLoaderBase {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "TFEEdLoader";

    private final String mSiteBaseAddress = "http://thefinancialexpress.com.bd";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getArticlePublicationDatetimeFormat() {
        return null;
    }

    int mPreviewBlockType=0;

    @Override
    protected Elements getPreviewBlocks() {
        return mEditionDocument.select(TheFinancialExpressEditionParserInfo.ARTICLE_PREVIEW_BLOCK_SELECTOR);
    }

    @Override
    protected String getArticleLink(Element previewBlock) {
        if (previewBlock.is(TheFinancialExpressEditionParserInfo.ARTICLE_PREVIEW_BLOCK_SELECTOR2)){
            mPreviewBlockType = 1;
        }
        String articleLink;
        if (mPreviewBlockType == 0) {
            articleLink = previewBlock.select(TheFinancialExpressEditionParserInfo.ARTICLE_LINK_ELEMENT_SELECTOR[mPreviewBlockType]).
                                                get(0).attr(TheFinancialExpressEditionParserInfo.ARTICLE_LINK_TEXT_SELECTOR_TAG[mPreviewBlockType]);
        }else {
            articleLink = previewBlock.attr(TheFinancialExpressEditionParserInfo.ARTICLE_LINK_TEXT_SELECTOR_TAG[mPreviewBlockType]);
        }

        if (articleLink ==null || articleLink.trim().length()==0){
            return null;
        }
        return articleLink;
    }

    @Override
    protected String getArticlePreviewImageLink(Element previewBlock) {
        return previewBlock.select(
                TheFinancialExpressEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_ELEMENT_SELECTOR).get(0).
                attr(TheFinancialExpressEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_TEXT_SELECTOR_TAG
        );
    }

    @Override
    protected String getArticleTitle(Element previewBlock) {
        return previewBlock.select(TheFinancialExpressEditionParserInfo.ARTICLE_TITLE_ELEMENT_SELECTOR[mPreviewBlockType]).
                                    get(0).text();
    }

    @Override
    protected String getArticlePublicationDateString(Element previewBlock) {
        return null;
    }

    @Override
    protected String processArticleLink(String articleLink) {
        if (!articleLink.contains(mSiteBaseAddress) &&
                !articleLink.matches("^//.+")&&
                !articleLink.matches("^/.+")){
            articleLink = "/"+articleLink;
        }
        return super.processArticleLink(articleLink);
    }

    @Override
    protected String processArticlePreviewImageLink(String previewImageLink) {
        if (!previewImageLink.contains(mSiteBaseAddress) &&
                !previewImageLink.matches("^//.+")&&
                !previewImageLink.matches("^/.+")){
            previewImageLink = "/"+previewImageLink;
        }
        return super.processArticlePreviewImageLink(previewImageLink);
    }
}
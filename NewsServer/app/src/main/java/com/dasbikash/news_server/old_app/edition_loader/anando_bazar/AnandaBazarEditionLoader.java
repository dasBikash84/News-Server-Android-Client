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

package com.dasbikash.news_server.old_app.edition_loader.anando_bazar;

import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class AnandaBazarEditionLoader extends EditionLoaderBase {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "ABEdLoader";

    private final String mSiteBaseAddress = "https://www.anandabazar.com";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getArticlePublicationDatetimeFormat() {
        return AnandoBazarEditionParserInfo.ARTICLE_PUBLICATION_DATE_TIME_FORMAT;
    }

    int mPageLayoutType =0;

    @Override
    protected Elements getPreviewBlocks() {
        Elements previewBlocks = new Elements();
        previewBlocks = mEditionDocument.select(AnandoBazarEditionParserInfo.ARTICLE_PREVIEW_BLOCK_SELECTOR[mPageLayoutType]);
        if (previewBlocks.size() == 0){
            mPageLayoutType = 1;
            previewBlocks = mEditionDocument.select(AnandoBazarEditionParserInfo.ARTICLE_PREVIEW_BLOCK_SELECTOR[mPageLayoutType]);
        }
        return previewBlocks;
    }

    @Override
    protected String getArticleLink(Element previewBlock) {
        return previewBlock.select(AnandoBazarEditionParserInfo.ARTICLE_LINK_ELEMENT_SELECTOR[mPageLayoutType]).get(0).
                                    attr(AnandoBazarEditionParserInfo.ARTICLE_LINK_TEXT_SELECTOR_TAG[mPageLayoutType]);
    }

    @Override
    protected String getArticlePreviewImageLink(Element previewBlock) {
        return previewBlock.select(AnandoBazarEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_ELEMENT_SELECTOR[mPageLayoutType]).get(0).
                                    attr(AnandoBazarEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_TEXT_SELECTOR_TAG[mPageLayoutType]);
    }

    @Override
    protected String getArticleTitle(Element previewBlock) {
        return previewBlock.select(AnandoBazarEditionParserInfo.ARTICLE_TITLE_ELEMENT_SELECTOR[mPageLayoutType]).
                                    get(0).text();
    }

    @Override
    protected String getArticlePublicationDateString(Element previewBlock) {
        if (mPageLayoutType == 0) {

            Elements dateTimeElements = previewBlock.select(AnandoBazarEditionParserInfo.ARTICLE_PUBLICATION_DATE_ELEMENT_SELECTOR[mPageLayoutType]);

            if (dateTimeElements.size() > 0) {
                Element dateTimeElement = dateTimeElements.get(0);
                String articlePublicationDateString = dateTimeElement.text().trim();
                articlePublicationDateString = DisplayUtility.banglaToEnglishDateString(articlePublicationDateString);
                return articlePublicationDateString;
            }
        }
        return null;
    }
}
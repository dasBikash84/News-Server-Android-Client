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

package com.dasbikash.news_server.old_app.edition_loader.the_indian_express;


import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class TheIndianExpressEditionLoader extends EditionLoaderBase {

    private static final String TAG = "ProthomaloEditionLoader";
    //private static final String TAG = "StackTrace";

    private static final String REGEX_FOR_CITY_LINK = ".+?/cities/.+?/page.+";
    private static final String REGEX_FOR_OP_ED_LINK = ".+?/opinion/.+?";
    private static final String REGEX_FOR_TRENDING_LINK = ".+?/trending/.+?";
    private static final String REGEX_FOR_LINK_WITH_HTTPS = "^https:.+";
    private static int GENERAL_ARTICLE_PARSER_INDEX = 0;
    private static int OP_ED_ARTICLE_PARSER_INDEX = 1;
    private static int TRENDING_ARTICLE_PARSER_INDEX = 2;
    private int mArticleParserIndex = GENERAL_ARTICLE_PARSER_INDEX;
    private static final int MAX_RERUN_COUNT_FOR_EMPTY_WITH_REPEAT_FOR_REGULAR_FEATURE = 3;

    @Override
    protected int getMaxReRunCountOnEmptyWithRepeat() {
        return MAX_RERUN_COUNT_FOR_EMPTY_WITH_REPEAT_FOR_REGULAR_FEATURE;
    }

    private final String mSiteBaseAddress = "https://indianexpress.com";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }


    @Override
    protected String getPageLink(Feature feature, int pageNumber) {

        if (feature.getLinkFormat().matches(REGEX_FOR_OP_ED_LINK)){
            mArticleParserIndex = OP_ED_ARTICLE_PARSER_INDEX;
        } else if (feature.getLinkFormat().matches(REGEX_FOR_TRENDING_LINK)){
            mArticleParserIndex = TRENDING_ARTICLE_PARSER_INDEX;
        }

        if (feature.getLinkFormat().matches(REGEX_FOR_CITY_LINK)){
            pageNumber++;
        }

        return super.getPageLink(feature, pageNumber);
    }

    @Override
    protected String getArticlePublicationDatetimeFormat() {
        return TheIndianExpressEditionParserInfo.ARTICLE_PUBLICATION_DATE_TIME_FORMAT[mArticleParserIndex];
    }

    @Override
    protected Elements getPreviewBlocks() {
        return mEditionDocument.select(TheIndianExpressEditionParserInfo.ARTICLE_PREVIEW_BLOCK_SELECTOR[mArticleParserIndex]);
    }

    @Override
    protected String getArticleLink(Element previewBlock) {
        return previewBlock.select(
                TheIndianExpressEditionParserInfo.ARTICLE_TITLE_ELEMENT_SELECTOR[mArticleParserIndex]).
                get(0).select(TheIndianExpressEditionParserInfo.ARTICLE_LINK_ELEMENT_SELECTOR[mArticleParserIndex]).
                get(0).attr(TheIndianExpressEditionParserInfo.ARTICLE_LINK_TEXT_SELECTOR_TAG[mArticleParserIndex]
        );
    }

    @Override
    protected String getArticlePreviewImageLink(Element previewBlock) {
        return previewBlock.select(TheIndianExpressEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_ELEMENT_SELECTOR[mArticleParserIndex]).
                                    get(0).attr(TheIndianExpressEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_TEXT_SELECTOR_TAG[mArticleParserIndex]);
    }

    @Override
    protected String getArticleTitle(Element previewBlock) {
        return previewBlock.select(TheIndianExpressEditionParserInfo.ARTICLE_TITLE_ELEMENT_SELECTOR[mArticleParserIndex]).
                                    get(0).text();
    }

    @Override
    protected String getArticlePublicationDateString(Element previewBlock) {
        return previewBlock.select(TheIndianExpressEditionParserInfo.ARTICLE_PUBLICATION_DATE_ELEMENT_SELECTOR[mArticleParserIndex]).
                                    get(0).text();
    }

}
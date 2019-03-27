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

package com.dasbikash.news_server.old_app.edition_loader.the_times_of_india;


import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class TheTimesOfIndiaEditionLoader extends EditionLoaderBase {

    private static final String TAG = "ProthomaloEditionLoader";
    //private static final String TAG = "StackTrace";

    private static final String REGEX_FOR_SPORTS_LINK = ".+?/sports/.+?/page.+";
    private static final String REGEX_FOR_ENT_LIFESTYLE_LINK = ".+?/(entertainment|life\\-style)/.+?";
    private static final String REGEX_FOR_LINK_WITH_HTTPS = "^https:.+";

    private static int GENERAL_ARTICLE_PARSER_INDEX = 0;
    private static int ENT_LIFESTYLE_ARTICLE_PARSER_INDEX = 1;
    private static int SPORTS_ARTICLE_PARSER_INDEX = 2;
    private int mArticleParserIndex = GENERAL_ARTICLE_PARSER_INDEX;

    private final String mSiteBaseAddress = "https://timesofindia.indiatimes.com";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getPageLink(Feature feature, int pageNumber) {

        if (feature.getLinkFormat().matches(REGEX_FOR_ENT_LIFESTYLE_LINK)){
            mArticleParserIndex = ENT_LIFESTYLE_ARTICLE_PARSER_INDEX;
        } else if (feature.getLinkFormat().matches(REGEX_FOR_SPORTS_LINK)){
            mArticleParserIndex = SPORTS_ARTICLE_PARSER_INDEX;
        }

        if ((mArticleParserIndex == GENERAL_ARTICLE_PARSER_INDEX || mArticleParserIndex == SPORTS_ARTICLE_PARSER_INDEX)
            && pageNumber == 1){
            if (feature.getLinkVariablePartFormat() !=null) {
                return feature.getLinkFormat().replace(feature.getLinkVariablePartFormat(), "");
            }
        }
        return super.getPageLink(feature, pageNumber);
    }

    @Override
    protected String getArticlePublicationDatetimeFormat() {
        return TheTimesOfIndiaEditionParserInfo.ARTICLE_PUBLICATION_DATE_TIME_FORMAT[mArticleParserIndex];
    }

    @Override
    protected Elements getPreviewBlocks() {
        return mEditionDocument.select(TheTimesOfIndiaEditionParserInfo.ARTICLE_PREVIEW_BLOCK_SELECTOR[mArticleParserIndex]);
    }

    @Override
    protected String getArticleLink(Element previewBlock) {
        return previewBlock.select(
                TheTimesOfIndiaEditionParserInfo.ARTICLE_TITLE_ELEMENT_SELECTOR[mArticleParserIndex]).
                get(0).select(TheTimesOfIndiaEditionParserInfo.ARTICLE_LINK_ELEMENT_SELECTOR[mArticleParserIndex]).
                get(0).attr(TheTimesOfIndiaEditionParserInfo.ARTICLE_LINK_TEXT_SELECTOR_TAG[mArticleParserIndex]
        );
    }

    @Override
    protected String getArticlePreviewImageLink(Element previewBlock) {
        return previewBlock.select(
                TheTimesOfIndiaEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_ELEMENT_SELECTOR[mArticleParserIndex]).
                get(0).attr(TheTimesOfIndiaEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_TEXT_SELECTOR_TAG[mArticleParserIndex]
        );
    }

    @Override
    protected String getArticleTitle(Element previewBlock) {
        return previewBlock.select(TheTimesOfIndiaEditionParserInfo.ARTICLE_TITLE_ELEMENT_SELECTOR[mArticleParserIndex]).
                                    get(0).text();
    }

    @Override
    protected String getArticlePublicationDateString(Element previewBlock) {
        if (mArticleParserIndex == ENT_LIFESTYLE_ARTICLE_PARSER_INDEX) {
            return previewBlock.select(TheTimesOfIndiaEditionParserInfo.ARTICLE_PUBLICATION_DATE_ELEMENT_SELECTOR[mArticleParserIndex]).
                    get(0).attr(TheTimesOfIndiaEditionParserInfo.ARTICLE_PUBLICATION_DATE_TEXT_SELECTOR_TAG[mArticleParserIndex]);
        }else {
            return previewBlock.select(TheTimesOfIndiaEditionParserInfo.ARTICLE_PUBLICATION_DATE_ELEMENT_SELECTOR[mArticleParserIndex]).
                    get(0).text();
        }
    }
}
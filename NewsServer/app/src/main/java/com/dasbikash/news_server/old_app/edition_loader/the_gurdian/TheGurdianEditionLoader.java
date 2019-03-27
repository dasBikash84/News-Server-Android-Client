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

package com.dasbikash.news_server.old_app.edition_loader.the_gurdian;


import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class TheGurdianEditionLoader extends EditionLoaderBase {

    private static final String TAG = "TheGurdianEditionLoader";

    private final String mSiteBaseAddress = "https://www.theguardian.com";

    private final String TITLE_FILTER_REGEX = "(?i).+?(" +
                                            "(–\\s?podcast\\s?)|" +
                                            "(-\\s?podcast\\s?)|" +
                                            "(–\\s?video\\s?)|" +
                                            "(-\\s?video\\s?)|" +
                                            "(–.+?live updates\\s?)|" +
                                            "(-.+?live updates\\s?)|" +
                                            "(–.+?live\\s?)|" +
                                            "(-.+?live\\s?)|" +
                                            "(–\\s?cartoon\\s?)|" +
                                            "(-\\s?cartoon\\s?)|" +
                                            "(–\\s?in pictures\\s?)|" +
                                            "(-\\s?in pictures\\s?)|" +
                                            "(\\s?best photos\\s?)|" +
                                            "(–\\s?as it happened\\s?)|" +
                                            "(-\\s?as it happened\\s?)" +
                                            ")\\s?$";

    private static final int MAX_RERUN_COUNT_FOR_EMPTY_WITH_REPEAT_FOR_REGULAR_FEATURE = 3;

    @Override
    protected int getMaxReRunCountOnEmptyWithRepeat() {
        return MAX_RERUN_COUNT_FOR_EMPTY_WITH_REPEAT_FOR_REGULAR_FEATURE;
    }

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getArticlePublicationDatetimeFormat() {
        return TheGurdianEditionParserInfo.ARTICLE_PUBLICATION_DATE_TIME_FORMAT;
    }

    @Override
    protected Elements getPreviewBlocks() {
        return mEditionDocument.select(TheGurdianEditionParserInfo.ARTICLE_PREVIEW_BLOCK_SELECTOR);
    }

    @Override
    protected String getArticleLink(Element previewBlock) {
        return previewBlock.select(TheGurdianEditionParserInfo.ARTICLE_LINK_ELEMENT_SELECTOR).get(0).
                                    attr(TheGurdianEditionParserInfo.ARTICLE_LINK_TEXT_SELECTOR_TAG);
    }

    @Override
    protected String getArticlePreviewImageLink(Element previewBlock) {
        return previewBlock.select(TheGurdianEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_ELEMENT_SELECTOR).get(0).
                                    attr(TheGurdianEditionParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_TEXT_SELECTOR_TAG);
    }

    @Override
    protected String getArticleTitle(Element previewBlock) {
        String articleTitle = previewBlock.select(TheGurdianEditionParserInfo.ARTICLE_TITLE_ELEMENT_SELECTOR).
                                    get(0).text();
        if (articleTitle.matches(TITLE_FILTER_REGEX)){
            return null;
        }
        return articleTitle;
    }

    @Override
    protected String getArticlePublicationDateString(Element previewBlock) {
        return previewBlock.select(
                TheGurdianEditionParserInfo.ARTICLE_PUBLICATION_DATE_ELEMENT_SELECTOR).
                get(0).attr(TheGurdianEditionParserInfo.ARTICLE_PUBLICATION_DATE_TEXT_SELECTOR_TAG
        );
    }
}
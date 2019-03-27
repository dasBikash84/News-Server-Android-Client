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

package com.dasbikash.news_server.old_app.edition_loader.new_age;

class NewAgeEditionParserInfo {

    static final String ARTICLE_PREVIEW_BLOCK_CONTAINER = "article ul";
    static final String ARTICLE_PREVIEW_BLOCK_SELECTOR = "li";

    static final String ARTICLE_LINK_ELEMENT_SELECTOR = "a";
    static final String ARTICLE_LINK_TEXT_SELECTOR_TAG = "href";

    static final String ARTICLE_PREVIEW_IMAGE_LINK_ELEMENT_SELECTOR = "img";
    static final String ARTICLE_PREVIEW_IMAGE_LINK_TEXT_SELECTOR_TAG = "src";

    static final String ARTICLE_TITLE_ELEMENT_SELECTOR = "a";

    static final String ARTICLE_PUBLICATION_DATE_ELEMENT_SELECTOR = "p span";

    static final String ARTICLE_PUBLICATION_DATE_TIME_FORMAT = "hh:mm a MMMM dd, yyyy";
    static final String ARTICLE_PUBLICATION_DATE_TIME_CLEANER_REGEX = "^.+?(\\d+:\\d+)(am|pm)\\son(\\s.+)$";
    static final String ARTICLE_PUBLICATION_DATE_TIME_REPLACER = "$1 $2$3";

    static final int ARTICLE_PREVIEW_COUNT_PER_PAGE = 10;
}

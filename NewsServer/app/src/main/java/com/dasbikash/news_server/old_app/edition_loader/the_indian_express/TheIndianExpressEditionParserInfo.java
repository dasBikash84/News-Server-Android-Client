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

class TheIndianExpressEditionParserInfo {

    static final String ARTICLE_PREVIEW_BLOCK_SELECTOR[] = {".articles",".leadstory,.opi-story",".articles ul li"};

    static final String ARTICLE_TITLE_ELEMENT_SELECTOR[] = {".title","h6","h3"};

    static final String ARTICLE_LINK_ELEMENT_SELECTOR[] = {"a","a","a"};
    static final String ARTICLE_LINK_TEXT_SELECTOR_TAG[] = {"href","href","href"};

    static final String ARTICLE_PREVIEW_IMAGE_LINK_ELEMENT_SELECTOR[] = {"img[data-lazy-src]","img[data-lazy-src]","img[data-lazy-src]"};
    static final String ARTICLE_PREVIEW_IMAGE_LINK_TEXT_SELECTOR_TAG[] = {"data-lazy-src","data-lazy-src","data-lazy-src"};

    static final String ARTICLE_PUBLICATION_DATE_ELEMENT_SELECTOR[] = {".date","",""};
    static final String ARTICLE_PUBLICATION_DATE_TIME_FORMAT[] = {"MMMMM dd, yyyy hh:mm:ss a","",""};
}

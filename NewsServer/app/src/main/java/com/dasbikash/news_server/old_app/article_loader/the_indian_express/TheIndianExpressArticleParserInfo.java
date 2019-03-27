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

abstract class TheIndianExpressArticleParserInfo {

    static final String ARTICLE_DATA_BLOCK_SELECTOR = ".articles";

    static final String ARTICLE_FRAGMENT_BLOCK_SELECTOR = "p:not(p:has(span.custom-caption),.dnews p),span.custom-caption";

    static final String PARAGRAPH_IMAGE_SELECTOR = "img[data-lazy-src]";
    static final String PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR = "data-lazy-src";
    static final String PARAGRAPH_IMAGE_CAPTION_SELECTOR = ":root";

    static final String ARTICLE_MODIFICATION_DATE_SELECTOR = "[itemprop='dateModified']";
    static final String ARTICLE_MODIFICATION_DATE_SELECTOR_ATTR = "content";
    static final String ARTICLE_MODIFICATION_DATE_STRING_FORMATS = "yyyy-MM-dd'T'HH:mm:ssZZZ";
}

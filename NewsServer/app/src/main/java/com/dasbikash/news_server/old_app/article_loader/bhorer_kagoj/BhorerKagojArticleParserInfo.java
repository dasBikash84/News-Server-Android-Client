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

package com.dasbikash.news_server.old_app.article_loader.bhorer_kagoj;

public abstract class BhorerKagojArticleParserInfo {

    static final String FEATURED_IMAGE_SELECTOR = "#content-p .content-p-feature img";
    static final String FEATURED_IMAGE_LINK_SELECTOR = "data-src";
    static final String FEATURED_IMAGE_CAPTION_SELECTOR = ".wp-caption-text.gallery-caption";

    static final String ARTICLE_FRAGMENT_BLOCK_SELECTOR = "#content-p p:not(p[class]),#content-p div:not(div[class])";

    static final String ARTICLE_MODIFICATION_DATE_STRING_SELECTOR = ".post_bar";
    static final String ARTICLE_MODIFICATION_DATE_STRING_FORMATS = "MMM d, yyyy , hh:mm a";

    static final String DATE_STRING_SPLITTER_REGEX = "\\|";
    static final String ARTICLE_MODIFICATION_DATE_CLEANER_SELECTOR = "প্রকাশিত হয়েছে:";
    static final String ARTICLE_MODIFICATION_DATE_CLEANER_REPLACEMENT = "";

    static final String PARAGRAPH_IMAGE_SELECTOR = "img";
    static final String PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR = "data-src";
    static final String PARAGRAPH_IMAGE_CAPTION_SELECTOR_ATTR = "alt";
}

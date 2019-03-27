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

package com.dasbikash.news_server.old_app.article_loader.bonik_barta;

public abstract class BonikBartaArticleParserInfo {

    static final String FEATURED_IMAGE_SELECTOR = "figure img";
    static final String FEATURED_IMAGE_LINK_SELECTOR = "src";
    static final String FEATURED_IMAGE_CAPTION_SELECTOR_ATTR = "alt";

    static final String ARTICLE_FRAGMENT_BLOCK_SELECTOR = ".content p,.content div:not(div[class])";

    static final String ARTICLE_MODIFICATION_DATE_STRING_SELECTOR = ".date-pub";
    static final String ARTICLE_MODIFICATION_DATE_STRING_FORMATS = "HH:mm:ss, MMM dd, yyyy";

    static final String ARTICLE_MODIFICATION_DATE_CLEANER_SELECTOR = " মিনিট";
    static final String ARTICLE_MODIFICATION_DATE_CLEANER_REPLACEMENT = "";
}

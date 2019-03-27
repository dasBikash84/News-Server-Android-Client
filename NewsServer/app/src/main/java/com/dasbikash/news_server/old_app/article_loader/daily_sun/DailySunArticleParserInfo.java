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

package com.dasbikash.news_server.old_app.article_loader.daily_sun;

abstract class DailySunArticleParserInfo {

    static final String FEATURED_IMAGE_SELECTOR = "img.main_img";
    static final String FEATURED_IMAGE_LINK_SELECTOR_ATTR = "src";
    static final String FEATURED_IMAGE_CAPTION_SELECTOR_ATTR = "alt";

    static final String ARTICLE_FRAGMENT_BLOCK_SELECTOR =   "article p:not(p[class])";

    static final String PARAGRAPH_IMAGE_SELECTOR = "img";
    static final String PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR = "src";
    static final String PARAGRAPH_IMAGE_CAPTION_SELECTOR_ATTR = "alt";

    static final String ARTICLE_MODIFICATION_DATE_STRING_SELECTOR = "h3:not(h3[class])";
    static final String ARTICLE_MODIFICATION_DATE_STRING_REPLACEMENT_SELECTOR = "^.+?(\\d.+)$";
    static final String ARTICLE_MODIFICATION_DATE_STRING_REPLACEMENT_STRING = "$1";
    static final String[] ARTICLE_MODIFICATION_DATE_STRING_FORMATS =
            {
                "d'st' MMMM, yyyy HH:mm:ss",
                "d'nd' MMMM, yyyy HH:mm:ss",
                "d'rd' MMMM, yyyy HH:mm:ss",
                "d'th' MMMM, yyyy HH:mm:ss"
            };
}

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

package com.dasbikash.news_server.old_app.article_loader.jugantor;

public abstract class JugantorArticleParserInfo {

    static final String FEATURED_IMAGE_SELECTOR = ".dtl_section .dtl_img_section img";
    static final String FEATURED_IMAGE_LINK_SELECTOR_ATTR = "src";
    static final String FEATURED_IMAGE_CAPTION_SELECTOR = ".dtl_img_caption";

    static final String ARTICLE_FRAGMENT_BLOCK_SELECTOR = "#myText p";

    static final String PARAGRAPH_IMAGE_SELECTOR = "img";
    static final String PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR = "src";
    static final String PARAGRAPH_IMAGE_CAPTION_SELECTOR_ATTR = "alt";

    static final String ARTICLE_MODIFICATION_DATE_STRING_SELECTOR = ".rpt_name p";

    static final String ARTICLE_MODIFICATION_DATE_CLEANER_SELECTOR = "^.+?(\\d{2}.+?\\d{4},\\s\\d{2}:\\d{2}).+";
    static final String ARTICLE_MODIFICATION_DATE_CLEANER_REPLACEMENT = "$1";

    static final String ARTICLE_MODIFICATION_DATE_STRING_FORMATS = "dd MMMM yyyy, HH:mm";
}

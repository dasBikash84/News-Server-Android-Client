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

package com.dasbikash.news_server.old_app.article_loader.anando_bazar;

public abstract class AnandoBazarArticleParserInfo {

    static final String FEATURED_IMAGE_SELECTOR = ".textwrap_left img";
    static final String FEATURED_IMAGE_LINK_SELECTOR = "src";
    static final String FEATURED_IMAGE_CAPTION_SELECTOR = ".text_below_img";

    static final String[] ARTICLE_MODIFICATION_DATE_STRING_FORMATS = {"dd MMM, yyyy, HH:mm:ss","dd, MMM, yyyy HH:mm:ss"};

    static final String ARTICLE_FRAGMENT_BLOCK_SELECTOR[] = {"#textbody p",".item .zoom"};

    static final String PARAGRAPH_IMAGE_SELECTOR[] = {"img","a"};
    static final String PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR[] = {"src","href"};
    static final String PARAGRAPH_IMAGE_CAPTION_SELECTOR_ATTR[] = {"alt","data-caption"};

    static final String ARTICLE_MODIFICATION_DATE_STRING_SELECTOR =
            "[class='abp-story-date-div2'],[class='abp-created-date hidden-sm hidden-xs'],[class='abp-story-date-div']";
}

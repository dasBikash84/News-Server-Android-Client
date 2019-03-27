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

package com.dasbikash.news_server.old_app.article_loader.daily_mirror;

abstract class DailyMirrorArticleParserInfo {

    static final String ARTICLE_DATA_BLOCK_SELECTOR = ".article-wrapper";

    static final String ARTICLE_FRAGMENT_BLOCK_SELECTOR = "p,.in-article-image";

    static final String PARAGRAPH_IMAGE_SELECTOR = "img[content]";
    static final String PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR = "content";
    static final String PARAGRAPH_IMAGE_CAPTION_SELECTOR = ".caption";

    static final String ARTICLE_MODIFICATION_DATE_STRING_BLOCK_SELECTOR = ".time-info [datetime]";
    static final String ARTICLE_MODIFICATION_DATE_STRING_SELECTOR_ATTR = "datetime";
    static final String ARTICLE_MODIFICATION_DATE_STRING_FORMATS = "yyyy-MM-dd'T'HH:mm:ss'Z'";
}

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

package com.dasbikash.news_server.old_app.article_loader.dawn_pak;

public abstract class DawnPakArticleParserInfo {

    static final String FEATURED_IMAGE_SELECTOR = ".slideshow__slide.slideshow__slide--first.slideshow__slide--horizontal img";
    static final String FEATURED_IMAGE_LINK_SELECTOR = "src";
    static final String FEATURED_IMAGE_CAPTION_SELECTOR_ATTR = "alt";

    static final int REQUIRED_FEATURED_IMAGE_COUNT = 3;
    static final int REQUIRED_FEATURED_IMAGE_INDEX = 0;

    static final String ARTICLE_FRAGMENT_BLOCK_SELECTOR =       ".story__content p," +
                                                                ".story__content figure," +
                                                                ".story__content h1," +
                                                                ".story__content h2," +
                                                                ".story__content h3," +
                                                                ".story__content h4";
    static final String PARAGRAPH_IMAGE_SELECTOR = "img";
    static final String PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR = "src";
    static final String PARAGRAPH_IMAGE_CAPTION_SELECTOR = ".media__caption";
}

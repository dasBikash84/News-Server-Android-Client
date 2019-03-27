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

package com.dasbikash.news_server.old_app.article_loader.the_times_of_india;

abstract class TheTimesOfIndiaArticleParserInfo {

    static final String FEATURED_IMAGE_SELECTOR = ".highlight.clearfix img";
    static final String FEATURED_IMAGE_LINK_SELECTOR_ATTR = "src";
    static final String FEATURED_IMAGE_CAPTION_SELECTOR_ATTR = "alt";

    static final String[] ARTICLE_FRAGMENT_BLOCK_SELECTOR = {
            ".highlight.clearfix .txt1,arttextxml",
            ".photosty_container_box.clearfix h3,.photosty_container_box.clearfix .clearfix.height .readmore_span,.photosty_container_box.clearfix .imagebox_bg",
            ".photo_block .clearfix.height,.photo_block .img_cptn,.photo_block .imgblock"
    };

    static final String[] PARAGRAPH_IMAGE_SELECTOR = {
            "img",
            "img[data-src-new]",
            "img[data-src-new]"
    };
    static final String[] PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR = {
            "src",
            "data-src-new",
            "data-src-new"
    };
    static final String[] PARAGRAPH_IMAGE_CAPTION_SELECTOR_ATTR = {
            "alt",
            "alt-alt",
            "alt-alt"
    };
}

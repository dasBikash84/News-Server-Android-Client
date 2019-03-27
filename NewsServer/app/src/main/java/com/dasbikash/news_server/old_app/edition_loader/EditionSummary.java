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

package com.dasbikash.news_server.old_app.edition_loader;

import android.annotation.SuppressLint;

import java.util.HashMap;

public final class EditionSummary {

    @SuppressWarnings("unused")
    private String mEditionURL;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,String> mArticleLinks = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,String> mArticleTitles = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,String> mArticlePreviewImageLinks = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,Long> mArticlePublicationTimeStamp = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,Long> mArticleLastModificationTimeStamp = new HashMap<>();

    public void setEditionURL(String editionURL) {
        mEditionURL = editionURL;
    }

    public HashMap<Integer,String> getArticleLinks() {
        return mArticleLinks;
    }

    public HashMap<Integer,String> getArticleTitles() {
        return mArticleTitles;
    }

    public HashMap<Integer,String> getArticlePreviewImageLinks() {
        return mArticlePreviewImageLinks;
    }

    public HashMap<Integer, Long> getArticlePublicationTimeStamp() {
        return mArticlePublicationTimeStamp;
    }

    public HashMap<Integer, Long> getArticleLastModificationTimeStamp() {
        return mArticleLastModificationTimeStamp;
    }
}

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

package com.dasbikash.news_server.old_app.this_data.newspaper;

import java.io.Serializable;

public class Newspaper implements Serializable{
    private int mId;
    private String mName;
    private String mCountryName;
    private int mLanguageId;
    private boolean mActive;

    Newspaper(int id, String name, String countryName, int languageId, boolean active) {
        mId = id;
        mName = name;
        mCountryName = countryName;
        mLanguageId = languageId;
        mActive = active;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getCountryName() {
        return mCountryName;
    }

    public int getLanguageId() {
        return mLanguageId;
    }

    public boolean isActive() {
        return mActive;
    }
}

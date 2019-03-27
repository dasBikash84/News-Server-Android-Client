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

package com.dasbikash.news_server.old_app.this_data.country;

public class Country {

    private String mName;
    private String mCountryCode;
    private int mContinentId;
    private String mTimeZone;

    Country(String name, String countryCode, int continentId, String timeZone) {
        mName = name;
        mCountryCode = countryCode;
        mContinentId = continentId;
        mTimeZone = timeZone;
    }

    public String getName() {
        return mName;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public int getContinentId() {
        return mContinentId;
    }

    public String getTimeZone() {
        return mTimeZone;
    }
}

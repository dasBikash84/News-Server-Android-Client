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

package com.dasbikash.news_server.display_models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Country {
    @PrimaryKey
    @NonNull
    private String mName;
    private String mCountryCode;
    private String mTimeZone;

    public Country() {
    }

    @Ignore
    public Country(String name, String countryCode, String timeZone) {
        mName = name;
        mCountryCode = countryCode;
        mTimeZone = timeZone;
    }

    public String getName() {
        return mName;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setCountryCode(String countryCode) {
        mCountryCode = countryCode;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }

    @Override
    public String toString() {
        return "Country{" +
                "mName='" + mName + '\'' +
                ", mCountryCode='" + mCountryCode + '\'' +
                ", mTimeZone='" + mTimeZone + '\'' +
                '}';
    }
}

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

import android.database.Cursor;
import android.database.CursorWrapper;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

class CountryCursorWrapper extends CursorWrapper {

    private static final String TAG = "CountryCursorWrapper";

    CountryCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    Country getInstance() {

        try {

            String name = getString(getColumnIndex(NewsServerDBSchema.CountryTable.Cols.Name.NAME));
            String countryCode = getString(getColumnIndex(NewsServerDBSchema.CountryTable.Cols.CodeName.NAME));
            int continentId = getInt(getColumnIndex(NewsServerDBSchema.CountryTable.Cols.ContinentId.NAME));
            String timeZoneId = getString(getColumnIndex(NewsServerDBSchema.CountryTable.Cols.TimeZoneId.NAME));

            if (continentId<1||
                name.trim().length()<1||
                timeZoneId.trim().length()<1 )   return null;

            return new Country(name,countryCode,continentId,timeZoneId) ;

        } catch (Exception ex){
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}

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
import android.database.sqlite.SQLiteDatabase;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

public abstract class CountryHelper {

    private static final String TAG = "CountryHelper";

    private static ArrayList<Country> getCountriesBySql(String sqlForCountries){

        ArrayList<Country> countries = new ArrayList<>();

        SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        try (Cursor cursor = dbCon.rawQuery(sqlForCountries,null)){
            if (cursor.getCount() == 0){
                return countries;
            }
            cursor.moveToFirst();
            do {
                countries.add(new CountryCursorWrapper(cursor).getInstance());
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
        } catch (Exception ex){
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
        }

        return countries;
    }

    public static ArrayList<Country> getAllCountries(){

        String sqlForCountries = "SELECT * FROM " + NewsServerDBSchema.CountryTable.NAME;

        return getCountriesBySql(sqlForCountries);
    }

    public static Country findCountryByName(String countryName){

        String sqlForCountry = "SELECT * FROM " + NewsServerDBSchema.CountryTable.NAME+
                                " WHERE "+NewsServerDBSchema.CountryTable.Cols.Name.NAME+
                                " = '"+
                                countryName+
                                "'";
        ArrayList<Country> countries = getCountriesBySql(sqlForCountry);

        if (countries.size()==1){
            return countries.get(0);
        } else {
            return null;
        }
    }

}

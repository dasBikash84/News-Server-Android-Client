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

package com.dasbikash.news_server;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.dasbikash.news_server.database.NewsServerDatabase;
import com.dasbikash.news_server.database.daos.CountryDao;
import com.dasbikash.news_server.display_models.Country;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import androidx.room.Room;
import androidx.test.runner.AndroidJUnit4;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RoomDatabaseInstrumentedTest {
    private NewsServerDatabase mDatabase;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        mDatabase = Room.inMemoryDatabaseBuilder(context, NewsServerDatabase.class).build();
    }

    @After
    public void closeDb() throws IOException {
        mDatabase.close();
    }

    @Test
    public void testCountryClass(){
        Country country = new Country("Bangladesh","BD","Asia/Dhaka");
        Log.d("DbTest", "In data:"+country);
        CountryDao countryDao = mDatabase.getCountryDao();
        countryDao.addCountry(country);

        List<Country> countries = countryDao.findAll();

        for (Country c :
                countries) {
            Log.d("DbTest", "Read Data:"+c);
        }
    }

}

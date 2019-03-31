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

import com.dasbikash.news_server.display_models.entity.Country;
import com.dasbikash.news_server.display_models.entity.Language;
import com.dasbikash.news_server.display_models.entity.Newspaper;
import com.dasbikash.news_server.display_models.entity.Page;
import com.dasbikash.news_server.utils.AppSettingsBootStrap;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    public static final String TAG = "DbTest";

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private Context appContext = InstrumentationRegistry.getTargetContext();
    
    @Test
    public void useAppContext() {
        // Context of the app under test.
        

        assertEquals("com.dasbikash.news_server", appContext.getPackageName());
    }

    @Before
    public void init(){
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
    }

    @Test
    public void realtimeDbTest(){
        HashMap<String,Boolean> map =
                new HashMap<>();
        map.put("bikashdaseee@gmail.com",true);
        map.put("bikash2daseee@gmail.com",true);
        map.put("bikash3daseee@gmail.com",true);
        Task<Void> task = mReference.child("admin_list").setValue(Arrays.asList(
                "bikashdaseee@gmail.com",
                "bikash2daseee@gmail.com",
                "bikash3daseee@gmail.com"
        ));
        while (!task.isComplete());
    }
    
    /*@Test
    public void testFileReader(){
        for (Iterator<String> it = new AppSettingsBootStrap(appContext).loadDataFromSqlFile(R.raw.country_data); it.hasNext(); ) {
            String line = it.next();
            Log.d(TAG, "testFileReader: "+line);

        }
    }*/
    /*@Test
    public void testCountrylistReader(){
        for (Country country :
                new AppSettingsBootStrap(appContext).getCountries()) {
            Log.d(TAG, "testCountrylistReader: "+country);
        }
    }
    @Test
    public void testLanguageListReader(){
        for (Language language :
                new AppSettingsBootStrap(appContext).getLanguages()) {
            Log.d(TAG, "testLanguageListReader: "+language);
        }
    }
    @Test
    public void testNewspaperListReader(){
        for (Newspaper newspaper :
                new AppSettingsBootStrap(appContext).getNewspapers()) {
            Log.d(TAG, "testNewspaperListReader: "+newspaper);
        }
    }
    @Test
    public void testPageListReader(){
        for (Page page :
                new AppSettingsBootStrap(appContext).getPages()) {
            Log.d(TAG, "testNewspaperListReader: "+page);
        }
    }
    @Test
    public void testPageGroupReader(){
        Log.d(TAG, "testPageGroupReader: "+new AppSettingsBootStrap(appContext).getPageGroupData());
    }*/
    @Test
    public void testLoadSetingsData(){
        AppSettingsBootStrap.getInstance(appContext).loadData();//new AppSettingsBootStrap(appContext).loadData();
    }
}

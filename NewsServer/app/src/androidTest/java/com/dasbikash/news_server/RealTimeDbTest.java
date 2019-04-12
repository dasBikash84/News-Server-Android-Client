/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
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



import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RealTimeDbTest {

    /*public static final String TAG = "DbTest";

    private String APP_SETTINGS_NODE = "app_settings";
    private String COUNTRIES_NODE = "countries";
    private String LANGUAGES_NODE = "languages";
    private String NEWSPAPERS_NODE = "newspapers";
    private String PAGES_NODE = "pages";
    private String PAGE_GROUPS_NODE = "page_groups";

    private Context appContext;// = InstrumentationRegistry.getTargetContext();
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRootReference;
    private DatabaseReference mAppSettingsReference;
    private DatabaseReference mCountriesSettingsReference;
    private DatabaseReference mLanguagesSettingsReference;
    private DatabaseReference mNewspaperSettingsReference;
    private DatabaseReference mPagesSettingsReference;
    private DatabaseReference mPageGroupsSettingsReference;

    @Test
    public void useAppContext() {
        // Context of the app under test.
        

        assertEquals("com.dasbikash.news_server", appContext.getPackageName());
    }

    @Before
    public void init(){
        appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = FirebaseDatabase.getInstance();
        mRootReference = mDatabase.getReference();
        mAppSettingsReference = mRootReference.child(APP_SETTINGS_NODE);
        mCountriesSettingsReference = mAppSettingsReference.child(COUNTRIES_NODE);
        mLanguagesSettingsReference = mAppSettingsReference.child(LANGUAGES_NODE);
        mNewspaperSettingsReference = mAppSettingsReference.child(NEWSPAPERS_NODE);
        mPagesSettingsReference = mAppSettingsReference.child(NEWSPAPERS_NODE);
        mPagesSettingsReference = mAppSettingsReference.child(PAGES_NODE);
        mPageGroupsSettingsReference = mAppSettingsReference.child(PAGE_GROUPS_NODE);
    }

    @Test
    public void loadSetingsDataToRealTimeDb(){
        AppSettingsBootStrapToRTDb.INSTANCE.loadAppSettingsDataToServer(appContext);
    }

    @Test
    public void readLastUpdateTime(){
       *//* FirebaseRealtimeDBUtils.INSTANCE.getServerAppSettingsUpdateTime().
                subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.d(TAG, "Time: "+aLong);
                    }

                });
        SystemClock.sleep(5000);*//*
    }

//    @Test
//    public void readAppSrttings(){
//        FirebaseRealtimeDBUtils.INSTANCE.getServerAppSettingsData().
//                subscribe(new Consumer<DefaultAppSettings>() {
//                    @Override
//                    public void accept(DefaultAppSettings defaultAppSettings) throws Exception {
//                        Log.d(TAG, "Time: "+defaultAppSettings);
//                        for (Country country :
//                                defaultAppSettings.getCountries()) {
//                            Log.d(TAG, "country: "+country);
//                        }
//                        for (Language language :
//                                defaultAppSettings.getLanguages()) {
//                            Log.d(TAG, "language: "+language);
//                        }
//                        for (Newspaper newspaper :
//                                defaultAppSettings.getNewspapers()) {
//                            Log.d(TAG, "newspaper: "+newspaper);
//                        }
//                        for (Page page :
//                                defaultAppSettings.getPages()) {
//                            Log.d(TAG, "page: "+page);
//                        }
//                        for (PageGroup pageGroup :
//                                defaultAppSettings.getPageGroups()) {
//                            Log.d(TAG, "pageGroup: "+pageGroup);
//                        }
//                    }
//
//                });
//        SystemClock.sleep(7000);
//    }

    @Test
    public void writeCountryData(){
        HashMap<String,Country> countryHashMap =
        AppSettingsBootStrapToRTDb.INSTANCE.getCountries(appContext);

        Task<Void> task = mCountriesSettingsReference.setValue(countryHashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                }else {
                    Log.d(TAG, "onComplete Error: "+task.getException().getMessage());
                }
            }
        });
        while (!task.isComplete());
    }

    @Test
    public void readCountryData(){
        AtomicBoolean waitFlag = new AtomicBoolean(false);
        mCountriesSettingsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot);
                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {
                    Log.d(TAG, "key:"+snapshot.getKey());
                    Log.d(TAG, "Country: "+snapshot.getValue(Country.class));
                }
                waitFlag.set(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        while(!waitFlag.get());
    }

    @Test
    public void writeLanguageData(){
        HashMap<String,Language> languageHashMap =
                AppSettingsBootStrapToRTDb.INSTANCE.getLanguages(appContext);

        Log.d(TAG, "writeLanguageData: "+languageHashMap);

        Task<Void> task = mLanguagesSettingsReference.setValue(languageHashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                        }else {
                            Log.d(TAG, "onComplete Error: "+task.getException().getMessage());
                        }
                    }
                });
        while (!task.isComplete());
    }

    @Test
    public void readLanguageData(){

        AtomicBoolean waitFlag = new AtomicBoolean(false);

        mLanguagesSettingsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot);
                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {
                    Log.d(TAG, "key:"+snapshot.getKey());
                    Log.d(TAG, "Language: "+snapshot.getValue(Language.class));
                }
                waitFlag.set(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        while(!waitFlag.get());
    }

    @Test
    public void writeNewspaperData(){
        HashMap<String,Newspaper> newspaperHashMap =
                AppSettingsBootStrapToRTDb.INSTANCE.getNewspapers(appContext);

        Log.d(TAG, "writeNewspaperData: "+newspaperHashMap);

        Task<Void> task = mNewspaperSettingsReference.setValue(newspaperHashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                        }else {
                            Log.d(TAG, "onComplete Error: "+ Arrays.asList(task.getException().getStackTrace()));
                        }
                    }
                });
        while (!task.isComplete());
    }

    @Test
    public void readNewspaperData(){

        AtomicBoolean waitFlag = new AtomicBoolean(false);

        mNewspaperSettingsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot);
                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {
                    Log.d(TAG, "key:"+snapshot.getKey());
                    Log.d(TAG, "Newspaper: "+snapshot.getValue(Newspaper.class));
                }
                waitFlag.set(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        while(!waitFlag.get());
    }

    @Test
    public void writePageData(){
        HashMap<String, AppSettingsBootStrapToRTDb.ServerPage> pageHashMap =
                AppSettingsBootStrapToRTDb.INSTANCE.getPages(appContext);

        Log.d(TAG, "writePageData: "+pageHashMap);

        Task<Void> task = mPagesSettingsReference.setValue(pageHashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                        }else {
                            Log.d(TAG, "onComplete Error: "+ Arrays.asList(task.getException().getStackTrace()));
                        }
                    }
                });
        while (!task.isComplete());
    }

    @Test
    public void readPageData(){

        AtomicBoolean waitFlag = new AtomicBoolean(false);

        mPagesSettingsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot);
                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {
                    Log.d(TAG, "key:"+snapshot.getKey());
                    Log.d(TAG, "Page: "+snapshot.getValue(Page.class));
                }
                waitFlag.set(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        while(!waitFlag.get());
    }

    @Test
    public void writePageGroupData(){
        HashMap<String,PageGroup> pageGroupHashMap =
                AppSettingsBootStrapToRTDb.INSTANCE.getPageGroupData(appContext);

        Log.d(TAG, "writePageGroupData: "+pageGroupHashMap);

        Task<Void> task = mPageGroupsSettingsReference.setValue(pageGroupHashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                        }else {
                            Log.d(TAG, "onComplete Error: "+ Arrays.asList(task.getException().getStackTrace()));
                        }
                    }
                });
        while (!task.isComplete());
    }

    @Test
    public void readPageGroupData(){

        AtomicBoolean waitFlag = new AtomicBoolean(false);

        mPageGroupsSettingsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot);
                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {
                    Log.d(TAG, "key:"+snapshot.getKey());
                    Log.d(TAG, "PageGroup: "+snapshot.getValue(PageGroup.class));
                }
                waitFlag.set(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        while(!waitFlag.get());
    }
*/
}

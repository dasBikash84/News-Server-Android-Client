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
import android.os.SystemClock;
import android.util.Log;

import com.dasbikash.news_server.data_sources.firebase.FirebaseRealtimeDBUtils;
import com.dasbikash.news_server.display_models.entity.Country;
import com.dasbikash.news_server.display_models.entity.DefaultAppSettings;
import com.dasbikash.news_server.display_models.entity.Language;
import com.dasbikash.news_server.display_models.entity.Newspaper;
import com.dasbikash.news_server.display_models.entity.Page;
import com.dasbikash.news_server.display_models.entity.PageGroup;
import com.dasbikash.news_server.utils.AppSettingsBootStrap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.annotation.NonNull;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RealTimeDbTest {

    public static final String TAG = "DbTest";

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
        AppSettingsBootStrap.INSTANCE.loadData(appContext);//new AppSettingsBootStrap(appContext).loadData();
    }

    private Observable<DataSnapshot> getDataSnapshotObservableForRef(DatabaseReference reference){
        return Observable.create(new ObservableOnSubscribe<DataSnapshot>() {
            @Override
            public void subscribe(ObservableEmitter<DataSnapshot> emitter) throws Exception {

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        emitter.onNext(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        throw databaseError.toException();
                    }
                });

            }
        })
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io());
    }

    private void showDataAtDbReference(DatabaseReference reference){
        getDataSnapshotObservableForRef(reference)
                .subscribe(new Consumer<DataSnapshot>() {
                    @Override
                    public void accept(DataSnapshot dataSnapshot) throws Exception {
                        if (dataSnapshot.hasChildren()){
                            for (DataSnapshot snapshot:
                                    dataSnapshot.getChildren()
                                 ) {
                                Log.d(TAG, reference.getKey()+": "+snapshot.getValue().toString());
                            }
                        }else {
                            Log.d(TAG, reference.getKey()+": "+dataSnapshot.getValue().toString());
                        }
                    }
                });
    }

    @Test
    public void readCountriesData(){
        showDataAtDbReference(mCountriesSettingsReference);
        SystemClock.sleep(5000);
    }

    @Test
    public void readLanguagesData(){
        showDataAtDbReference(mLanguagesSettingsReference);
        SystemClock.sleep(5000);
    }

    @Test
    public void readNewspaperData(){
        showDataAtDbReference(mNewspaperSettingsReference);
        SystemClock.sleep(5000);
    }

    @Test
    public void readPagesData(){
        showDataAtDbReference(mPagesSettingsReference);
        SystemClock.sleep(5000);
    }

    @Test
    public void readPageGroupsData(){
        showDataAtDbReference(mPageGroupsSettingsReference);
        SystemClock.sleep(5000);
    }

    @Test
    public void readLastUpdateTime(){
       /* FirebaseRealtimeDBUtils.INSTANCE.getServerAppSettingsUpdateTime().
                subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.d(TAG, "Time: "+aLong);
                    }

                });
        SystemClock.sleep(5000);*/
    }

    @Test
    public void readLanguageData(){
       /* FirebaseRealtimeDBUtils.INSTANCE.getLanguageSettingsData().
                subscribe(new Consumer<ArrayList<Language>>() {
                    @Override
                    public void accept(ArrayList<Language> languages) throws Exception {
                        Log.d(TAG, "Time: "+languages);
                    }

                });
        SystemClock.sleep(5000);*/
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
//                                defaultAppSettings.getPage_groups()) {
//                            Log.d(TAG, "pageGroup: "+pageGroup);
//                        }
//                    }
//
//                });
//        SystemClock.sleep(7000);
//    }

}

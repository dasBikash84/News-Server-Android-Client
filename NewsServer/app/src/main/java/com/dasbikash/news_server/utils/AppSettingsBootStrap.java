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

package com.dasbikash.news_server.utils;

import android.content.Context;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.display_models.entity.Country;
import com.dasbikash.news_server.display_models.entity.Language;
import com.dasbikash.news_server.display_models.entity.Newspaper;
import com.dasbikash.news_server.display_models.entity.Page;
import com.dasbikash.news_server.display_models.entity.PageGroup;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.RawRes;

final public class AppSettingsBootStrap {

    public static final String TAG = "DbTest";
    private static final String APP_SETTINGS_NODE = "app_settings";
    private static final String COUNTRIES_NODE = "countries";
    private static final String LANGUAGES_NODE = "languages";
    private static final String NEWSPAPERS_NODE = "newspapers";
    private static final String PAGES_NODE = "pages";
    private static final String PAGE_GROUPS_NODE = "page_groups";

    private Context mContext;
    private FirebaseDatabase mFirebaseDatabase;

    private static AppSettingsBootStrap INSTANCE;

    public static AppSettingsBootStrap getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = new AppSettingsBootStrap(context);
        }
        return INSTANCE;
    }

    private AppSettingsBootStrap(Context context) {
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    private Iterator<String> loadDataFromSqlFile(@RawRes int rawResId) {
        return FileLineIterator.getFileLineIteratorFromRawResource(mContext,rawResId);
    }


    private Iterable<Country> getCountries(){
        List<Country> countries = new ArrayList<>();
        Iterator<String> countryData = loadDataFromSqlFile(R.raw.country_data);
        int countryId = 0;
        while (countryData.hasNext()){
            String[] datam = getStrings(countryData.next());
            countries.add(new Country(datam[0],datam[1],datam[2]));
        }
        return countries;
    }


    private Iterable<Language> getLanguages(){
        List<Language> languages = new ArrayList<>();
        Iterator<String> languageData = loadDataFromSqlFile(R.raw.language_data);
        while (languageData.hasNext()){
            String[] datam = getStrings(languageData.next());
            languages.add(new Language(Integer.parseInt(datam[0]),datam[1]));
        }
        return languages;
    }

    private Iterable<Newspaper> getNewspapers(){
        List<Newspaper> newspapers = new ArrayList<>();

        Iterator<String> newsPaperData = loadDataFromSqlFile(R.raw.newspaper_data);
        while (newsPaperData.hasNext()){
            String[] datam = getStrings(newsPaperData.next());
            newspapers.add(new Newspaper(Integer.parseInt(datam[0]),datam[1],datam[2],Integer.parseInt(datam[3]),true));
        }

        return newspapers;
    }

    private Iterable<Page> getPages(){
        List<Page> pages = new ArrayList<>();

        Iterator<String> pageData = loadDataFromSqlFile(R.raw.page_data);

        while (pageData.hasNext()){
            String[] datam = getStrings(pageData.next());

            Page page = new Page(Integer.parseInt(datam[0]),
                    Integer.parseInt(datam[1]),
                    Integer.parseInt(datam[2]),
                    datam[3],false);
            if (page.getParentPageId() == 0){
                page.setActive(true);
            }

            pages.add(page);
        }

        return pages;
    }

    private List<PageGroup> getPageGroupData() {
        BufferedReader reader =  new BufferedReader(new InputStreamReader(mContext.getResources().openRawResource(R.raw.page_group_data)));
        Gson gson = new Gson();
        PageGroups pageGroups = gson.fromJson(reader,PageGroups.class);

        return pageGroups.page_groups;
    }

    public void loadData(){
        DatabaseReference rootReference = mFirebaseDatabase.getReference();
        DatabaseReference appSettingsRef= rootReference.child(APP_SETTINGS_NODE);
        DatabaseReference countriesSettingsRef= appSettingsRef.child(COUNTRIES_NODE);
        DatabaseReference languagesSettingsRef= appSettingsRef.child(LANGUAGES_NODE);
        DatabaseReference newspapersSettingsRef= appSettingsRef.child(NEWSPAPERS_NODE);
        DatabaseReference pagesSettingsRef= appSettingsRef.child(PAGES_NODE);
        DatabaseReference pageGroupsSettingsRef= appSettingsRef.child(PAGE_GROUPS_NODE);

        Task<Void> task = countriesSettingsRef.setValue(getCountries());
        while (!task.isComplete());

        task = languagesSettingsRef.setValue(getLanguages());
        while (!task.isComplete());

        task = newspapersSettingsRef.setValue(getNewspapers());
        while (!task.isComplete());

        task = pagesSettingsRef.setValue(getPages());
        while (!task.isComplete());

        task = pageGroupsSettingsRef.setValue(getPageGroupData());
        while (!task.isComplete());
    }

    @NotNull
    private String[] getStrings(String countryData) {
        return countryData.replace("'","").split(",");
    }

    private static class PageGroups{
        final List<PageGroup> page_groups = new ArrayList<>();

        @Override
        public String toString() {
            return "PageGroups{" +
                    "page_groups=" + page_groups +
                    '}';
        }
    }
}

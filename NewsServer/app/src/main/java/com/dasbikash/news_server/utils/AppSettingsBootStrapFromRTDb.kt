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

package com.dasbikash.news_server.utils

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import com.dasbikash.news_server.R
import com.dasbikash.news_server.data_sources.firebase.FirebaseRealtimeDBUtils
import com.dasbikash.news_server.display_models.entity.*
import com.google.firebase.database.ServerValue
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.HashMap

object AppSettingsBootStrapFromRTDb {

    private val TAG = "DbTest"

    fun getCountries(context: Context): HashMap<String,Country> {

        val reader = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.country_data)))
        val gson = Gson()
        val countries = gson.fromJson(reader, Countries::class.java)

        val countryMap: HashMap<String,Country> = HashMap();
        for (country in countries.countries){
            countryMap.put(country.name,country)
        }

        Log.d(TAG,"pageGroupMap Data: "+countryMap)

        return countryMap


    }

    fun getLanguages(context: Context): HashMap<String,Language> {

        val reader = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.language_data)))
        val gson = Gson()
        val languages = gson.fromJson(reader, Languages::class.java)

        val languageMap: HashMap<String,Language> = HashMap();
        for (language in languages.languages){
            languageMap.put(language.id,language)
        }

        Log.d(TAG,"languageMap Data: "+languageMap)

        return languageMap
    }

    fun getNewspapers(context: Context): HashMap<String,Newspaper> {

        val reader = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.newspaper_data)))
        val gson = Gson()
        val newspapers = gson.fromJson(reader, Newspapers::class.java)

        val newspaperMap: HashMap<String,Newspaper> = HashMap();
        for (newspaper in newspapers.newspapers){
            newspaperMap.put(newspaper.id,newspaper)
        }

        Log.d(TAG,"newspaperMap Data: "+newspaperMap)

        return newspaperMap
    }

    fun getPages(context: Context): HashMap<String,Page> {

        val reader = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.page_data)))
        val gson = Gson()
        val pages = gson.fromJson(reader, PageList::class.java)

        val pageMap: HashMap<String,Page> = HashMap();
        for (page in pages.pages){
            pageMap.put(page.id,page)
        }

        Log.d(TAG,"pageMap Data: "+pageMap)

        return pageMap

    }

    fun getPageGroupData(context: Context): HashMap<String,PageGroup> {
        val reader = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.page_group_data)))
        val gson = Gson()
        val pageGroups = gson.fromJson(reader, PageGroups::class.java)

        val pageGroupMap: HashMap<String,PageGroup> = HashMap();
        for (pageGroup in pageGroups.pageGroups){
            pageGroupMap.put(pageGroup.id,pageGroup)
        }

        Log.d(TAG,"pageGroupMap Data: "+pageGroupMap)

        return pageGroupMap

    }

    fun loadAppSettingsData(context: Context) {

        var task = FirebaseRealtimeDBUtils.mCountriesSettingsReference
                .setValue(getCountries(context))
                .addOnCompleteListener {
                    it.exception?.let {
                        Log.d(TAG,"Error: ${it?.message }" +" in countries saving")
                    } ?: Log.d(TAG,"Success in countries savin")
                }
        while (!task.isComplete);

        task = FirebaseRealtimeDBUtils.mLanguagesSettingsReference
                .setValue(getLanguages(context))
                .addOnCompleteListener {
                    it.exception?.let {
                        Log.d(TAG,"Error: ${it?.message }" +" in Languages saving")
                    } ?: Log.d(TAG,"Success in Languages savin")
                }
        while (!task.isComplete);

        task = FirebaseRealtimeDBUtils.mNewspaperSettingsReference
                .setValue(getNewspapers(context))
                .addOnCompleteListener {
                    it.exception?.let {
                        Log.d(TAG,"Error: ${it?.message }" +" in Newspapers saving")
                    } ?: Log.d(TAG,"Success in Newspapers saving")
                }
        while (!task.isComplete);

        task = FirebaseRealtimeDBUtils.mPagesSettingsReference
                .setValue(getPages(context))
                .addOnCompleteListener {
                    it.exception?.let {
                        Log.d(TAG,"Error: ${it?.message }" +" in Pages saving")
                    } ?: Log.d(TAG,"Success in Pages saving")
                }
        while (!task.isComplete);

        task = FirebaseRealtimeDBUtils.mPageGroupsSettingsReference
                .setValue(getPageGroupData(context))
                .addOnCompleteListener {
                    it.exception?.let {
                        Log.d(TAG,"Error: ${it?.message }" +" in PageGroupData saving")
                    } ?: Log.d(TAG,"Success in PageGroupData savin")
                }
        while (!task.isComplete);

        task = FirebaseRealtimeDBUtils.mSettingsUpdateTimeReference
                .push()
                .setValue(ServerValue.TIMESTAMP)
                .addOnCompleteListener {
                    it.exception?.let {
                        Log.d(TAG,"Error: ${it?.message }" +" in SettingsUpdateTime saving")
                    } ?: Log.d(TAG,"Success in SettingsUpdateTime savin")
                }
        while (!task.isComplete);
    }

    private class PageGroups {
        internal val pageGroups: List<PageGroup> = ArrayList()

        override fun toString(): String {
            return "PageGroups{" +
                    "page_groups=" + pageGroups +
                    '}'.toString()
        }
    }

    private class Countries {
        internal val countries: List<Country> = ArrayList()
        override fun toString(): String {
            return "Countries(countries=$countries)"
        }
    }

    private class Languages {
        internal val languages: List<Language> = ArrayList()
        override fun toString(): String {
            return "Languages(languages=$languages)"
        }

    }

    private class Newspapers {
        internal val newspapers: List<Newspaper> = ArrayList()
        override fun toString(): String {
            return "Newspapers(newspapers=$newspapers)"
        }
    }

    private class PageList {
        internal val pages: List<Page> = ArrayList()
        override fun toString(): String {
            return "PageList(pages=$pages)"
        }
    }
}

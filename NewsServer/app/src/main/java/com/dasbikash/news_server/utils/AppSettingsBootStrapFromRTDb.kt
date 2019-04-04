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

    private fun loadDataFromSqlFile(context: Context, @RawRes rawResId: Int): Iterator<String>? {
        return FileLineIterator.getFileLineIteratorFromRawResource(context, rawResId)
    }

    fun getCountries(context: Context): HashMap<String,Country> {
        val countries = ArrayList<Country>()
        val countryData = loadDataFromSqlFile(context, R.raw.country_data)
        while (countryData!!.hasNext()) {
            val datam = getStrings(countryData.next())
            countries.add(Country(datam[0], datam[1], datam[2]))
        }
        val countryMap: HashMap<String,Country> = HashMap();
        for (country in countries){
            countryMap.put(country.name,country)
        }
        return countryMap
    }

    fun getLanguages(context: Context): HashMap<String,Language> {
        val languages = ArrayList<Language>()
        val languageData = loadDataFromSqlFile(context, R.raw.language_data)
        while (languageData!!.hasNext()) {
            val datam = getStrings(languageData.next())
            languages.add(Language(datam[0], datam[1]))
        }
        val languageMap: HashMap<String,Language> = HashMap();
        for (language in languages){
            languageMap.put(language.id,language)
        }
        return languageMap
    }

    fun getNewspapers(context: Context): HashMap<String,Newspaper> {
        val newspapers = ArrayList<Newspaper>()

        val newsPaperData = loadDataFromSqlFile(context, R.raw.newspaper_data)
        while (newsPaperData!!.hasNext()) {
            val datam = getStrings(newsPaperData.next())
            newspapers.add(Newspaper(datam[0], datam[1], datam[2], datam[3], true))
        }
        val newspaperMap: HashMap<String,Newspaper> = HashMap();
        for (newsPaper in newspapers){
            newspaperMap.put(newsPaper.id,newsPaper)
        }
//        Log.d(TAG,"NP data:"+newspaperMap)
        return newspaperMap
    }

    fun getPages(context: Context): HashMap<String,Page> {
        val pages = ArrayList<Page>()

        val pageData = loadDataFromSqlFile(context, R.raw.page_data)

        while (pageData!!.hasNext()) {
            val datam = getStrings(pageData.next())

            val page = Page(datam[0],datam[1],datam[2],datam[3], true)

            pages.add(page)
        }

        val pageMap: HashMap<String,Page> = HashMap();
        for (page in pages){
            pageMap.put(page.id,page)
        }

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

    private fun getStrings(countryData: String): Array<String> {
        return countryData.replace("'", "").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    private class PageGroups {
        internal val pageGroups: List<PageGroup> = ArrayList()

        override fun toString(): String {
            return "PageGroups{" +
                    "page_groups=" + pageGroups +
                    '}'.toString()
        }
    }
}

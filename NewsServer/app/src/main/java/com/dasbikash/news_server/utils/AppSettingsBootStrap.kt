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
import androidx.annotation.RawRes
import com.dasbikash.news_server.R
import com.dasbikash.news_server.display_models.entity.*
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

object AppSettingsBootStrap {

    private val TAG = "DbTest"
    private val APP_SETTINGS_NODE = "app_settings"
    private val COUNTRIES_NODE = "countries"
    private val LANGUAGES_NODE = "languages"
    private val NEWSPAPERS_NODE = "newspapers"
    private val PAGES_NODE = "pages"
    private val PAGE_GROUPS_NODE = "page_groups"

    private val mFirebaseDatabase = FirebaseDatabase.getInstance()

    private fun loadDataFromSqlFile(context: Context, @RawRes rawResId: Int): Iterator<String>? {
        return FileLineIterator.getFileLineIteratorFromRawResource(context, rawResId)
    }

    private fun getCountries(context: Context): Iterable<Country> {
        val countries = ArrayList<Country>()
        val countryData = loadDataFromSqlFile(context, R.raw.country_data)
        while (countryData!!.hasNext()) {
            val datam = getStrings(countryData.next())
            countries.add(Country(datam[0], datam[1], datam[2]))
        }
        return countries
    }

    private fun getLanguages(context: Context): Iterable<Language> {
        val languages = ArrayList<Language>()
        val languageData = loadDataFromSqlFile(context, R.raw.language_data)
        while (languageData!!.hasNext()) {
            val datam = getStrings(languageData.next())
            languages.add(Language(Integer.parseInt(datam[0]), datam[1]))
        }
        return languages
    }

    private fun getNewspapers(context: Context): Iterable<Newspaper> {
        val newspapers = ArrayList<Newspaper>()

        val newsPaperData = loadDataFromSqlFile(context, R.raw.newspaper_data)
        while (newsPaperData!!.hasNext()) {
            val datam = getStrings(newsPaperData.next())
            newspapers.add(Newspaper(Integer.parseInt(datam[0]), datam[1], datam[2], Integer.parseInt(datam[3]), true))
        }

        return newspapers
    }

    private fun getPages(context: Context): Iterable<Page> {
        val pages = ArrayList<Page>()

        val pageData = loadDataFromSqlFile(context, R.raw.page_data)

        while (pageData!!.hasNext()) {
            val datam = getStrings(pageData.next())

            val page = Page(Integer.parseInt(datam[0]),
                    Integer.parseInt(datam[1]),
                    Integer.parseInt(datam[2]),
                    datam[3], true)

            pages.add(page)
        }

        return pages
    }

    private fun getPageGroupData(context: Context): List<PageGroup> {
        val reader = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.page_group_data)))
        val gson = Gson()
        val pageGroups = gson.fromJson(reader, PageGroups::class.java)

        return pageGroups.page_groups
    }

    fun loadData(context: Context) {
        val rootReference = mFirebaseDatabase.reference
        val appSettingsRef = rootReference.child(APP_SETTINGS_NODE)
        val countriesSettingsRef = appSettingsRef.child(COUNTRIES_NODE)
        val languagesSettingsRef = appSettingsRef.child(LANGUAGES_NODE)
        val newspapersSettingsRef = appSettingsRef.child(NEWSPAPERS_NODE)
        val pagesSettingsRef = appSettingsRef.child(PAGES_NODE)
        val pageGroupsSettingsRef = appSettingsRef.child(PAGE_GROUPS_NODE)

        var task = countriesSettingsRef.setValue(getCountries(context))
        while (!task.isComplete);

        task = languagesSettingsRef.setValue(getLanguages(context))
        while (!task.isComplete);

        task = newspapersSettingsRef.setValue(getNewspapers(context))
        while (!task.isComplete);

        task = pagesSettingsRef.setValue(getPages(context))
        while (!task.isComplete);

        task = pageGroupsSettingsRef.setValue(getPageGroupData(context))
        while (!task.isComplete);
    }

    private fun getStrings(countryData: String): Array<String> {
        return countryData.replace("'", "").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    private class PageGroups {
        internal val page_groups: List<PageGroup> = ArrayList()

        override fun toString(): String {
            return "PageGroups{" +
                    "page_groups=" + page_groups +
                    '}'.toString()
        }
    }
}

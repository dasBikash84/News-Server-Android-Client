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

package com.dasbikash.news_server_data.repositories.room_impls

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.AppSettingsRepository

internal class AppSettingsRepositoryRoomImpl internal constructor(context: Context): AppSettingsRepository() {

    private val mDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    override fun getCountryCount(): Int {
        return mDatabase.countryDao.findAll().size
    }
    override fun getLanguageCount(): Int {
        return mDatabase.languageDao.findAll().size
    }
    override fun getNewsPaperCount(): Int {
        return mDatabase.newsPaperDao.findAll().size
    }
    override fun getPageCount(): Int {
        return mDatabase.pageDao.findAll().size
    }

    override fun getNewsCategories(): List<NewsCategory> {
        return mDatabase.newsCategoryDao.findAll()
    }

    override fun nukeAppSettings() {
        mDatabase.nukeAppSettings()
    }

    override fun addLanguages(languages: List<Language>) {
        mDatabase.languageDao.addLanguages(languages)
    }

    override fun addCountries(countries: List<Country>) {
        mDatabase.countryDao.addCountries(countries)
    }

    override fun addNewsPapers(newspapers: List<Newspaper>) {
        mDatabase.newsPaperDao.addNewsPapers(newspapers)
    }

    override fun addPages(pages: List<Page>) {
        mDatabase.pageDao.addPages(ArrayList(pages))
    }

    override fun addNewsCategories(newsCategories: List<NewsCategory>) {
        mDatabase.newsCategoryDao.addNewsCategories(newsCategories)
    }

    override fun addPageGroups(pageGroups:List<PageGroup>) {
        mDatabase.pageGroupDao.addPageGroups(pageGroups)
    }

    override fun getNewsPapersLiveData():LiveData<List<Newspaper>>{
        return mDatabase.newsPaperDao.findAllLiveData()
    }

    override fun getTopPagesForNewspaper(newspaper: Newspaper): List<Page> {
        return mDatabase.pageDao.getTopPagesByNewsPaperId(newspaper.id)
    }

    override fun getPagesForNewspaper(newspaper: Newspaper): List<Page> {
        return mDatabase.pageDao.getPagesByNewsPaperId(newspaper.id)
    }

    override fun getChildPagesForTopLevelPage(topLevelPage: Page):List<Page>{
        return mDatabase.pageDao.getChildPagesByTopLevelPageId(topLevelPage.id)
    }

    override fun getLanguageByPage(page: Page): Language {
        val newspaper = mDatabase.newsPaperDao.findById(page.newspaperId!!)
        return mDatabase.languageDao.findByLanguageId(newspaper.languageId!!)
    }

    override fun getNewspaperByPage(page: Page): Newspaper {
        return mDatabase.newsPaperDao.findById(page.newspaperId ?: "")
    }

    override fun getLanguageByNewspaper(newspaper: Newspaper): Language {
        return mDatabase.languageDao.findByLanguageId(newspaper.languageId!!)
    }

    override fun findMatchingPages(it: String): List<Page> {
        return mDatabase.pageDao.findByNameContent("%"+it+"%")
    }

    override fun findPageById(pageId:String): Page? {
        return mDatabase.pageDao.findById(pageId)
    }

    override fun findNewsCategoryById(newsCategoryId: String): NewsCategory? {
        return mDatabase.newsCategoryDao.findById(newsCategoryId)
    }
}
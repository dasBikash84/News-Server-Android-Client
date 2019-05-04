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
import com.dasbikash.news_server_data.utills.ExceptionUtils

class AppSettingsRepositoryRoomImpl internal constructor(context: Context): AppSettingsRepository() {

    private val mDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    override fun getCountryCount(): Int {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.countryDao.count
    }
    override fun getLanguageCount(): Int {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.languageDao.count
    }
    override fun getNewsPaperCount(): Int {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.newsPaperDao.count
    }
    override fun getPageCount(): Int {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageDao.count
    }
    override fun getPageGroupCount(): Int {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageGroupDao.count
    }

    override fun nukeAppSettings() {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        mDatabase.nukeAppSettings()
    }

    override fun addLanguages(languages: List<Language>) {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        mDatabase.languageDao.addLanguages(languages)
    }

    override fun addCountries(countries: List<Country>) {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        mDatabase.countryDao.addCountries(countries)
    }

    override fun addNewsPapers(newspapers: List<Newspaper>) {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        mDatabase.newsPaperDao.addNewsPapers(newspapers)
    }

    override fun addPages(pages: List<Page>) {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        mDatabase.pageDao.addPages(ArrayList(pages))
    }

    override fun addPageGroups(pageGroups:List<PageGroup>) {
        mDatabase.pageGroupDao.addPageGroups(pageGroups)
    }

    override fun getNewsPapers():LiveData<List<Newspaper>>{
        return mDatabase.newsPaperDao.findAll()
    }

    override fun getTopPagesForNewspaper(newspaper: Newspaper): List<Page> {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageDao.getTopPagesByNewsPaperId(newspaper.id)
    }

    override fun getChildPagesForTopLevelPage(topLevelPage: Page):List<Page>{
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageDao.getChildPagesByTopLevelPageId(topLevelPage.id)
    }

    override fun getLanguageByPage(page: Page): Language {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        val newspaper = mDatabase.newsPaperDao.findById(page.newspaperId!!)
        return mDatabase.languageDao.findByLanguageId(newspaper.languageId!!)
    }

    override fun getNewspaperByPage(page: Page): Newspaper {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.newsPaperDao.findById(page.newspaperId ?: "")
    }

    override fun findMatchingPages(it: String): List<Page> {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageDao.findByNameContent("%"+it+"%")
    }

    override fun findPageById(pageId:String): Page? {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        return mDatabase.pageDao.findById(pageId)
    }

}
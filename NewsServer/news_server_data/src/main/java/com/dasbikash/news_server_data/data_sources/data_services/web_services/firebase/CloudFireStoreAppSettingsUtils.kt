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

package com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase

import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.models.DefaultAppSettings
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query


internal object CloudFireStoreAppSettingsUtils{

    private const val SETTINGS_UPDATE_TIME_FIELD_NAME = "updateTime"
    private const val WAITING_MS_FOR_NET_RESPONSE = 30000L

    fun getServerAppSettingsUpdateTime(): Long{

        val lock = Object()
        var lastSettingsUpdateTime:Long? = null
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getSettingsUpdateTimeCollectionRef()
                .orderBy(SETTINGS_UPDATE_TIME_FIELD_NAME, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    //Log.d(TAG,"getServerAppSettingsUpdateTime")
                    for (document in documents) {
                        if (document.exists()){
                            lastSettingsUpdateTime = (document.get(SETTINGS_UPDATE_TIME_FIELD_NAME) as Timestamp).toDate().time
                        }
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    //Log.d(TAG,"getServerAppSettingsUpdateTime. Eror msg: ${exception.message}")
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        //Log.d(TAG,"getServerAppSettingsUpdateTime before wait")
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        //Log.d(TAG,"getServerAppSettingsUpdateTime before throw it")
        dataServerException?.let { throw it }

        //Log.d(TAG,"getServerAppSettingsUpdateTime before throw DataNotFoundException()")
        if (lastSettingsUpdateTime == null ){
            throw DataNotFoundException()
        }

        //Log.d(TAG,"getServerAppSettingsUpdateTime before return")
        return lastSettingsUpdateTime!!
    }

    fun getServerAppSettingsData(): DefaultAppSettings {
        val countries: HashMap<String, Country> = getCountries()
        val languages: HashMap<String, Language> = getLanguages()
        val newspapers: HashMap<String, Newspaper> = getNewspapers()
        val pages: HashMap<String, Page> = getPages()
        val pageGroups: HashMap<String, PageGroup> = getPageGroups()
        val updateTime = mutableMapOf<String, Long>()
        updateTime.put("key1", getServerAppSettingsUpdateTime())
        return DefaultAppSettings(
                countries, languages, newspapers, pages, pageGroups, HashMap(updateTime))
    }

    private fun getCountries(): HashMap<String, Country> {

        val lock = Object()
        val countries = mutableMapOf<String,Country>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getCountrySettingsCollectionRef()
                .get()
                .addOnSuccessListener { documents ->
                    //Log.d(TAG,"getCountries")
                    for (document in documents) {
                        val country = document.toObject(Country::class.java)
                        countries.put(country.name,country)
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    //Log.d(TAG,"getCountries. Eror msg: ${exception.message}")
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        //Log.d(TAG,"getCountries before wait")
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        //Log.d(TAG,"getCountries before throw it")
        dataServerException?.let { throw it }

        //Log.d(TAG,"getCountries before throw DataNotFoundException()")
        if (countries.isEmpty()){
            throw DataNotFoundException()
        }

        //Log.d(TAG,"getCountries before return")
        return HashMap(countries)
    }

    private fun getLanguages(): HashMap<String, Language> {

        val lock = Object()
        val languages = mutableMapOf<String,Language>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getLanguageSettingsCollectionRef()
                .get()
                .addOnSuccessListener { documents ->
                    //Log.d(TAG,"getLanguages")
                    for (document in documents) {
                        val language = document.toObject(Language::class.java)
                        languages.put(language.id,language)
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    //Log.d(TAG,"getLanguages. Eror msg: ${exception.message}")
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        //Log.d(TAG,"getLanguages before wait")
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        //Log.d(TAG,"getLanguages before throw it")
        dataServerException?.let { throw it }

        //Log.d(TAG,"getLanguages before throw DataNotFoundException()")
        if (languages.isEmpty()){
            throw DataNotFoundException()
        }

        //Log.d(TAG,"getLanguages before return")
        return HashMap(languages)
    }

    private fun getNewspapers(): HashMap<String, Newspaper> {

        val lock = Object()
        val newspapers = mutableMapOf<String,Newspaper>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getNewspaperSettingsCollectionRef()
                .get()
                .addOnSuccessListener { documents ->
                    //Log.d(TAG,"getNewspapers")
                    for (document in documents) {
                        val newspaper = document.toObject(Newspaper::class.java)
                        newspapers.put(newspaper.id,newspaper)
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    //Log.d(TAG,"getNewspapers. Eror msg: ${exception.message}")
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        //Log.d(TAG,"getNewspapers before wait")
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        //Log.d(TAG,"getNewspapers before throw it")
        dataServerException?.let { throw it }

        //Log.d(TAG,"getNewspapers before throw DataNotFoundException()")
        if (newspapers.isEmpty()){
            throw DataNotFoundException()
        }

        //Log.d(TAG,"getNewspapers before return")
        return HashMap(newspapers)
    }

    private fun getPages(): HashMap<String, Page> {

        val lock = Object()
        val pages = mutableMapOf<String,Page>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getPageSettingsCollectionRef()
                .get()
                .addOnSuccessListener { documents ->
                    //Log.d(TAG,"getPages")
                    for (document in documents) {
                        val page = document.toObject(Page::class.java)
                        pages.put(page.id,page)
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    //Log.d(TAG,"getPages. Eror msg: ${exception.message}")
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        //Log.d(TAG,"getPages before wait")
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        //Log.d(TAG,"getPages before throw it")
        dataServerException?.let { throw it }

        //Log.d(TAG,"getPages before throw DataNotFoundException()")
        if (pages.isEmpty()){
            throw DataNotFoundException()
        }

        //Log.d(TAG,"getPages before return")
        return HashMap(pages)
    }

    private fun getPageGroups(): HashMap<String, PageGroup> {

        val lock = Object()
        val pageGroups = mutableMapOf<String,PageGroup>()
        var dataServerException: DataServerException? = null

        CloudFireStoreConUtils.getPageGroupSettingsCollectionRef()
                .get()
                .addOnSuccessListener { documents ->
                    //Log.d(TAG,"getPageGroups")
                    for (document in documents) {
                        val pageGroup = document.toObject(PageGroup::class.java)
                        pageGroups.put(pageGroup.name,pageGroup)
                    }
                    synchronized(lock) { lock.notify() }
                }
                .addOnFailureListener { exception ->
                    //Log.d(TAG,"getPageGroups. Eror msg: ${exception.message}")
                    dataServerException = DataNotFoundException(exception)
                    synchronized(lock) { lock.notify() }
                }

        //Log.d(TAG,"getPageGroups before wait")
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        //Log.d(TAG,"getPageGroups before throw it")
        dataServerException?.let { throw it }

        //Log.d(TAG,"getPageGroups before throw DataNotFoundException()")

        //Log.d(TAG,"getPageGroups before return")
        return HashMap(pageGroups)

    }

    fun ping(): Boolean {
        try {
            LoggerUtils.debugLog("ping",this::class.java)
            getLanguages()
            return true
        }catch (ex:Exception){
            return false
        }
    }

}
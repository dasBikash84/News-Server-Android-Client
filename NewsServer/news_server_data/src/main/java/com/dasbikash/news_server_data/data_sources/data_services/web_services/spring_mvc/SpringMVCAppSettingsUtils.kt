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

package com.dasbikash.news_server_data.data_sources.data_services.web_services.spring_mvc

import android.util.Log
import com.dasbikash.news_server_data.data_sources.NewsDataService
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.exceptions.DataServerNotAvailableExcepption
import com.dasbikash.news_server_data.models.DefaultAppSettings
import com.dasbikash.news_server_data.models.room_entity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

internal object SpringMVCAppSettingsUtils {

    private val TAG = "DataService"
    private val springMVCWebService = SpringMVCWebService.RETROFIT.create(SpringMVCWebService::class.java)

    fun getServerAppSettingsUpdateTime(): Long {

        val lock = Object()
        var serverAppSettingsUpdateTime: Long? = null

        var dataServerException: DataServerException? = null

        Log.d(TAG, "getServerAppSettingsUpdateTime")

        springMVCWebService
                .getSettingsUpdateLogs()
                .enqueue(object : Callback<SpringMVCWebService.SettingsUpdateLogs?> {
                    override fun onFailure(call: Call<SpringMVCWebService.SettingsUpdateLogs?>, throwable: Throwable) {
                        Log.d(TAG, "getServerAppSettingsUpdateTime onFailure")
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.SettingsUpdateLogs?>, response: Response<SpringMVCWebService.SettingsUpdateLogs?>) {
                        Log.d(TAG, "getServerAppSettingsUpdateTime onResponse")
                        if (response.isSuccessful) {

                            Log.d(TAG, "getServerAppSettingsUpdateTime onResponse isSuccessful")
                            response.body()?.let {
//                                Log.d(TAG, "getServerAppSettingsUpdateTime" + it.settingsUpdateLogs.get(0).updateTime)
                                serverAppSettingsUpdateTime = it.settingsUpdateLogs.get(0).updateTime.time
                            }
                        } else {

                            Log.d(TAG, "getServerAppSettingsUpdateTime onResponse not Successful")
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        Log.d(TAG, "getServerAppSettingsUpdateTime before wait")
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        Log.d(TAG, "getServerAppSettingsUpdateTime before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG, "getServerAppSettingsUpdateTime before throw DataNotFoundException()")
        if (serverAppSettingsUpdateTime == null) {
            throw DataNotFoundException()
        }

        Log.d(TAG, "getServerAppSettingsUpdateTime before return")
        return serverAppSettingsUpdateTime!!
    }

    fun getRawAppsettings(): DefaultAppSettings {
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

    private fun getPageGroups(): HashMap<String, PageGroup> {

        val lock = Object()

        var pageGroups: HashMap<String, PageGroup>? = null

        var dataServerException: DataServerException? = null

        Log.d(TAG, "getPageGroups")

        springMVCWebService
                .getPageGroups()
                .enqueue(object : Callback<SpringMVCWebService.PageGroups?> {
                    override fun onFailure(call: Call<SpringMVCWebService.PageGroups?>, throwable: Throwable) {
                        Log.d(TAG, "getPageGroups onFailure")
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.PageGroups?>,
                                            response: Response<SpringMVCWebService.PageGroups?>) {
                        Log.d(TAG, "getPageGroups onResponse")
                        if (response.isSuccessful) {

                            Log.d(TAG, "getPageGroups onResponse isSuccessful")
                            response.body()?.let {
                                pageGroups = HashMap(it.pageGroupMap)
                            }
                        } else {

                            Log.d(TAG, "getPageGroups onResponse not Successful")
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        Log.d(TAG, "getPageGroups before wait")
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        Log.d(TAG, "getPageGroups before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG, "getPageGroups before return")
        return pageGroups!!
    }

    private fun getPages(): HashMap<String, Page> {

        val lock = Object()
        val pages = mutableMapOf<String, Page>()

        var dataServerException: DataServerException? = null

        Log.d(TAG, "getPages")

        springMVCWebService
                .getPages()
                .enqueue(object : Callback<SpringMVCWebService.Pages?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Pages?>, throwable: Throwable) {
                        Log.d(TAG, "getPages onFailure")
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Pages?>,
                                            response: Response<SpringMVCWebService.Pages?>) {
                        Log.d(TAG, "getPages onResponse")
                        if (response.isSuccessful) {

                            Log.d(TAG, "getPages onResponse isSuccessful")
                            response.body()?.let {
                                it.pages.asSequence().forEach {
                                    pages.put(it.id, it)
                                }
                            }
                        } else {

                            Log.d(TAG, "getPages onResponse not Successful")
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        Log.d(TAG, "getPages before wait")
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        Log.d(TAG, "getPages before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG, "getPages before throw DataNotFoundException()")
        if (pages.keys.isEmpty()) {
            throw DataNotFoundException()
        }

        Log.d(TAG, "getPages before return")
        return HashMap(pages)
    }

    private fun getNewspapers(): HashMap<String, Newspaper> {

        val lock = Object()
        val newspapers = mutableMapOf<String, Newspaper>()

        var dataServerException: DataServerException? = null

        Log.d(TAG, "getNewspapers")

        springMVCWebService
                .getNewspapers()
                .enqueue(object : Callback<SpringMVCWebService.Newspapers?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Newspapers?>, throwable: Throwable) {
                        Log.d(TAG, "getNewspapers onFailure")
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Newspapers?>,
                                            response: Response<SpringMVCWebService.Newspapers?>) {
                        Log.d(TAG, "getNewspapers onResponse")
                        if (response.isSuccessful) {

                            Log.d(TAG, "getNewspapers onResponse isSuccessful")
                            response.body()?.let {
                                it.newspapers.asSequence().forEach {
                                    newspapers.put(it.id, it)
                                }
                            }
                        } else {

                            Log.d(TAG, "getNewspapers onResponse not Successful")
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        Log.d(TAG, "getNewspapers before wait")
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        Log.d(TAG, "getNewspapers before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG, "getNewspapers before throw DataNotFoundException()")
        if (newspapers.keys.isEmpty()) {
            throw DataNotFoundException()
        }

        Log.d(TAG, "getNewspapers before return")
        return HashMap(newspapers)
    }

    private fun getLanguages(): HashMap<String, Language> {

        val lock = Object()
        val languages = mutableMapOf<String, Language>()

        var dataServerException: DataServerException? = null

        Log.d(TAG, "getLanguages")

        springMVCWebService
                .getLanguages()
                .enqueue(object : Callback<SpringMVCWebService.Languages?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Languages?>, throwable: Throwable) {
                        Log.d(TAG, "getLanguages onFailure")
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Languages?>,
                                            response: Response<SpringMVCWebService.Languages?>) {
                        Log.d(TAG, "getLanguages onResponse")
                        if (response.isSuccessful) {

                            Log.d(TAG, "getLanguages onResponse isSuccessful")
                            response.body()?.let {
                                it.languages.asSequence().forEach {
                                    languages.put(it.id, it)
                                }
                            }
                        } else {

                            Log.d(TAG, "getLanguages onResponse not Successful")
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        Log.d(TAG, "getLanguages before wait")
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        Log.d(TAG, "getLanguages before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG, "getLanguages before throw DataNotFoundException()")
        if (languages.keys.isEmpty()) {
            throw DataNotFoundException()
        }

        Log.d(TAG, "getLanguages before return")
        return HashMap(languages)
    }

    private fun getCountries(): HashMap<String, Country> {

        val lock = Object()
        val countries = mutableMapOf<String, Country>()

        var dataServerException: DataServerException? = null

        Log.d(TAG, "getCountries")

        springMVCWebService
                .getCountries()
                .enqueue(object : Callback<SpringMVCWebService.Countries?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Countries?>, throwable: Throwable) {
                        Log.d(TAG, "getCountries onFailure")
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Countries?>,
                                            response: Response<SpringMVCWebService.Countries?>) {
                        Log.d(TAG, "getCountries onResponse")
                        if (response.isSuccessful) {

                            Log.d(TAG, "getCountries onResponse isSuccessful")
                            response.body()?.let {
                                it.countries.asSequence().forEach {
                                    countries.put(it.name, it)
                                }
                            }
                        } else {

                            Log.d(TAG, "getCountries onResponse not Successful")
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        Log.d(TAG, "getCountries before wait")
        synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }

        Log.d(TAG, "getCountries before throw it")
        dataServerException?.let { throw it }

        Log.d(TAG, "getCountries before throw DataNotFoundException()")
        if (countries.keys.isEmpty()) {
            throw DataNotFoundException()
        }

        Log.d(TAG, "getCountries before return")
        return HashMap(countries)
    }

    fun ping(): Boolean {
        try {
            getLanguages()
            return true
        }catch (ex:Exception){
            return false
        }
    }
}
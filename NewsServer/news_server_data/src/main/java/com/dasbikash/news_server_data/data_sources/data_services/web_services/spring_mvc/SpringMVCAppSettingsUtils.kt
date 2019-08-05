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
import com.dasbikash.news_server_data.utills.LoggerUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

internal object SpringMVCAppSettingsUtils {

    const val WAITING_MS_FOR_NET_RESPONSE = 5000L

    private val springMVCWebService = SpringMVCWebService.RETROFIT.create(SpringMVCWebService::class.java)

    fun getServerAppSettingsUpdateTime(): Long {

        val lock = Object()
        var serverAppSettingsUpdateTime: Long? = null

        var dataServerException: DataServerException? = null

        LoggerUtils.debugLog( "getServerAppSettingsUpdateTime",this::class.java)

        springMVCWebService
                .getSettingsUpdateLogs()
                .enqueue(object : Callback<SpringMVCWebService.SettingsUpdateLogs?> {
                    override fun onFailure(call: Call<SpringMVCWebService.SettingsUpdateLogs?>, throwable: Throwable) {
                        LoggerUtils.debugLog( "getServerAppSettingsUpdateTime onFailure",this::class.java)
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.SettingsUpdateLogs?>, response: Response<SpringMVCWebService.SettingsUpdateLogs?>) {
                        LoggerUtils.debugLog( "getServerAppSettingsUpdateTime onResponse",this::class.java)
                        if (response.isSuccessful) {

                            LoggerUtils.debugLog( "getServerAppSettingsUpdateTime onResponse isSuccessful",this::class.java)
                            response.body()?.let {
//                                Log.d(TAG, "getServerAppSettingsUpdateTime" + it.settingsUpdateLogs.get(0).updateTime)
                                serverAppSettingsUpdateTime = it.settingsUpdateLogs.get(0).updateTime.time
                            }
                        } else {

                            LoggerUtils.debugLog( "getServerAppSettingsUpdateTime onResponse not Successful",this::class.java)
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        LoggerUtils.debugLog( "getServerAppSettingsUpdateTime before wait",this::class.java)
        try {
            synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        LoggerUtils.debugLog( "getServerAppSettingsUpdateTime before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog( "getServerAppSettingsUpdateTime before throw DataNotFoundException()",this::class.java)
        if (serverAppSettingsUpdateTime == null) {
            throw DataNotFoundException()
        }

        LoggerUtils.debugLog( "getServerAppSettingsUpdateTime before return",this::class.java)
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
                countries, languages, newspapers, pages, pageGroups, HashMap<String,NewsCategory>(),HashMap(updateTime))
    }

    private fun getPageGroups(): HashMap<String, PageGroup> {

        val lock = Object()

        var pageGroups: HashMap<String, PageGroup>? = null

        var dataServerException: DataServerException? = null

        LoggerUtils.debugLog( "getPageGroups",this::class.java)

        springMVCWebService
                .getPageGroups()
                .enqueue(object : Callback<SpringMVCWebService.PageGroups?> {
                    override fun onFailure(call: Call<SpringMVCWebService.PageGroups?>, throwable: Throwable) {
                        LoggerUtils.debugLog( "getPageGroups onFailure",this::class.java)
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.PageGroups?>,
                                            response: Response<SpringMVCWebService.PageGroups?>) {
                        LoggerUtils.debugLog( "getPageGroups onResponse",this::class.java)
                        if (response.isSuccessful) {

                            LoggerUtils.debugLog( "getPageGroups onResponse isSuccessful",this::class.java)
                            response.body()?.let {
                                pageGroups = HashMap(it.pageGroupMap)
                            }
                        } else {

                            LoggerUtils.debugLog( "getPageGroups onResponse not Successful",this::class.java)
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        LoggerUtils.debugLog( "getPageGroups before wait",this::class.java)
        try {
            synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        LoggerUtils.debugLog( "getPageGroups before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog( "getPageGroups before return",this::class.java)
        return pageGroups!!
    }

    private fun getPages(): HashMap<String, Page> {

        val lock = Object()
        val pages = mutableMapOf<String, Page>()

        var dataServerException: DataServerException? = null

        LoggerUtils.debugLog( "getPages",this::class.java)

        springMVCWebService
                .getPages()
                .enqueue(object : Callback<SpringMVCWebService.Pages?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Pages?>, throwable: Throwable) {
                        LoggerUtils.debugLog( "getPages onFailure",this::class.java)
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Pages?>,
                                            response: Response<SpringMVCWebService.Pages?>) {
                        LoggerUtils.debugLog( "getPages onResponse",this::class.java)
                        if (response.isSuccessful) {

                            LoggerUtils.debugLog( "getPages onResponse isSuccessful",this::class.java)
                            response.body()?.let {
                                it.pages.asSequence().forEach {
                                    pages.put(it.id, it)
                                }
                            }
                        } else {

                            LoggerUtils.debugLog( "getPages onResponse not Successful",this::class.java)
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        LoggerUtils.debugLog( "getPages before wait",this::class.java)
        try {
            synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        LoggerUtils.debugLog( "getPages before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog( "getPages before throw DataNotFoundException()",this::class.java)
        if (pages.keys.isEmpty()) {
            throw DataNotFoundException()
        }

        LoggerUtils.debugLog( "getPages before return",this::class.java)
        return HashMap(pages)
    }

    private fun getNewspapers(): HashMap<String, Newspaper> {

        val lock = Object()
        val newspapers = mutableMapOf<String, Newspaper>()

        var dataServerException: DataServerException? = null

        LoggerUtils.debugLog( "getNewspapers",this::class.java)

        springMVCWebService
                .getNewspapers()
                .enqueue(object : Callback<SpringMVCWebService.Newspapers?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Newspapers?>, throwable: Throwable) {
                        LoggerUtils.debugLog( "getNewspapers onFailure",this::class.java)
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Newspapers?>,
                                            response: Response<SpringMVCWebService.Newspapers?>) {
                        LoggerUtils.debugLog( "getNewspapers onResponse",this::class.java)
                        if (response.isSuccessful) {

                            LoggerUtils.debugLog( "getNewspapers onResponse isSuccessful",this::class.java)
                            response.body()?.let {
                                it.newspapers.asSequence().forEach {
                                    newspapers.put(it.id, it)
                                }
                            }
                        } else {

                            LoggerUtils.debugLog( "getNewspapers onResponse not Successful",this::class.java)
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        LoggerUtils.debugLog( "getNewspapers before wait",this::class.java)
        try {
            synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        LoggerUtils.debugLog( "getNewspapers before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog( "getNewspapers before throw DataNotFoundException()",this::class.java)
        if (newspapers.keys.isEmpty()) {
            throw DataNotFoundException()
        }

        LoggerUtils.debugLog( "getNewspapers before return",this::class.java)
        return HashMap(newspapers)
    }

    private fun getLanguages(): HashMap<String, Language> {

        val lock = Object()
        val languages = mutableMapOf<String, Language>()

        var dataServerException: DataServerException? = null

        LoggerUtils.debugLog( "getLanguages",this::class.java)

        springMVCWebService
                .getLanguages()
                .enqueue(object : Callback<SpringMVCWebService.Languages?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Languages?>, throwable: Throwable) {
                        LoggerUtils.debugLog( "getLanguages onFailure",this::class.java)
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Languages?>,
                                            response: Response<SpringMVCWebService.Languages?>) {
                        LoggerUtils.debugLog( "getLanguages onResponse",this::class.java)
                        if (response.isSuccessful) {

                            LoggerUtils.debugLog( "getLanguages onResponse isSuccessful",this::class.java)
                            response.body()?.let {
                                it.languages.asSequence().forEach {
                                    languages.put(it.id, it)
                                }
                            }
                        } else {

                            LoggerUtils.debugLog( "getLanguages onResponse not Successful",this::class.java)
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        LoggerUtils.debugLog( "getLanguages before wait",this::class.java)
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        LoggerUtils.debugLog( "getLanguages before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog( "getLanguages before throw DataNotFoundException()",this::class.java)
        if (languages.keys.isEmpty()) {
            throw DataNotFoundException()
        }

        LoggerUtils.debugLog( "getLanguages before return",this::class.java)
        return HashMap(languages)
    }

    private fun getCountries(): HashMap<String, Country> {

        val lock = Object()
        val countries = mutableMapOf<String, Country>()

        var dataServerException: DataServerException? = null

        LoggerUtils.debugLog( "getCountries",this::class.java)

        springMVCWebService
                .getCountries()
                .enqueue(object : Callback<SpringMVCWebService.Countries?> {
                    override fun onFailure(call: Call<SpringMVCWebService.Countries?>, throwable: Throwable) {
                        LoggerUtils.debugLog( "getCountries onFailure",this::class.java)
                        dataServerException = DataServerNotAvailableExcepption(throwable)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onResponse(call: Call<SpringMVCWebService.Countries?>,
                                            response: Response<SpringMVCWebService.Countries?>) {
                        LoggerUtils.debugLog( "getCountries onResponse",this::class.java)
                        if (response.isSuccessful) {

                            LoggerUtils.debugLog( "getCountries onResponse isSuccessful",this::class.java)
                            response.body()?.let {
                                it.countries.asSequence().forEach {
                                    countries.put(it.name, it)
                                }
                            }
                        } else {

                            LoggerUtils.debugLog( "getCountries onResponse not Successful",this::class.java)
                            dataServerException = DataNotFoundException()

                        }
                        synchronized(lock) { lock.notify() }
                    }
                })


        LoggerUtils.debugLog( "getCountries before wait",this::class.java)
        try {
            synchronized(lock) { lock.wait(NewsDataService.WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        LoggerUtils.debugLog( "getCountries before throw it",this::class.java)
        dataServerException?.let { throw it }

        LoggerUtils.debugLog( "getCountries before throw DataNotFoundException()",this::class.java)
        if (countries.keys.isEmpty()) {
            throw DataNotFoundException()
        }

        LoggerUtils.debugLog( "getCountries before return",this::class.java)
        return HashMap(countries)
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
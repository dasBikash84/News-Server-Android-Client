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

import androidx.annotation.Keep
import com.dasbikash.news_server_data.models.room_entity.*

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

internal interface SpringMVCWebService {

    //    @GET(TOP_LEVEL_PAGE_ARTICLE_URL)
//    fun getLatestArticleByTopLevelPageId(@Path("topLevelPageId") topLevelPageId: String): Call<Article>
    @GET(LANGUAGES_PATH)
    fun getLanguages(): Call<Languages>

    @GET(COUNTRIES_PATH)
    fun getCountries(): Call<Countries>

    @GET(NEWSPAPERS_PATH)
    fun getNewspapers(): Call<Newspapers>

    @GET(PAGES_PATH)
    fun getPages(): Call<Pages>

    @GET(PAGE_GROUPS_PATH)
    fun getPageGroups(): Call<PageGroups>

    @GET(SETTINGS_UPDATE_LOG_PATH)
    fun getSettingsUpdateLogs(@Query("page-size") pageSize:Int = 1):Call<SettingsUpdateLogs>

    @GET(LATEST_ARTICLE_BY_PAGE_ID_PATH)
    fun getLatestArticlesByPageId(@Path("pageId") pageId: String,
                                  @Query("article_count") resultSize: Int?): Call<Articles>

    @GET(ARTICLES_BEFORE_LAST_ID_PATH)
    fun getArticlesBeforeLastId(@Path("pageId") pageId: String,
                                @Path("lastArticleId") lastArticleId: String,
                                @Query("article_count") resultSize: Int?): Call<Articles>

    @GET(ARTICLES_AFTER_LAST_ID_PATH)
    fun getArticlesAfterLastId(@Path("pageId") pageId: String,
                                @Path("lastArticleId") lastArticleId: String,
                                @Query("article_count") resultSize: Int?): Call<Articles>

    companion object {

        const val BASE_URL = "http://192.168.0.101:8097/"
        const val LANGUAGES_PATH = "languages"
        const val COUNTRIES_PATH = "countries"
        const val NEWSPAPERS_PATH = "newspapers"
        const val PAGES_PATH = "pages"
        const val PAGE_GROUPS_PATH = "page-groups"
        const val SETTINGS_UPDATE_LOG_PATH = "settings-update-logs"
        const val LATEST_ARTICLE_BY_PAGE_ID_PATH = "articles/page-id/{pageId}"
        const val ARTICLES_BEFORE_LAST_ID_PATH = "articles/page-id/{pageId}/before/last-article-id/{lastArticleId}"
        const val ARTICLES_AFTER_LAST_ID_PATH = "articles/page-id/{pageId}/after/last-article-id/{lastArticleId}"

        val RETROFIT = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Keep class Articles(val articles: List<Article>)
    @Keep class Languages(val languages: List<Language>)
    @Keep class Countries(val countries: List<Country>)
    @Keep class Newspapers(val newspapers: List<Newspaper>)
    @Keep class Pages(val pages: List<Page>)
    @Keep class PageGroups(val pageGroupMap: Map<String, PageGroup>)
    @Keep class SettingsUpdateLog(val updateTime:Date)
    @Keep class SettingsUpdateLogs(val settingsUpdateLogs:List<SettingsUpdateLog>)
}

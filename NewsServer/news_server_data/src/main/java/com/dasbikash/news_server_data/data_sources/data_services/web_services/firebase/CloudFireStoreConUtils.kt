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

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

internal object CloudFireStoreConUtils {

    private const val LANGUAGES_COLLECTION_LABEL= "languages"
    private const val COUNTRIES_COLLECTION_LABEL = "countries"
    private const val NEWSPAPERS_COLLECTION_LABEL = "newspapers"
    private const val PAGES_COLLECTION_LABEL = "pages"
    private const val PAGE_GROUPS_COLLECTION_LABEL = "page_groups"
    private const val APP_SETTINGS_UPDATE_TIME_COLLECTION_LABEL = "update_time"

    private const val ARTICLE_COLLECTION = "articles"

    private fun getDbConnection(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    internal fun getLanguageSettingsCollectionRef() =
            getDbConnection().collection(LANGUAGES_COLLECTION_LABEL)

    internal fun getCountrySettingsCollectionRef() =
            getDbConnection().collection(COUNTRIES_COLLECTION_LABEL)

    internal fun getNewspaperSettingsCollectionRef() =
            getDbConnection().collection(NEWSPAPERS_COLLECTION_LABEL)

    internal fun getPageSettingsCollectionRef() =
            getDbConnection().collection(PAGES_COLLECTION_LABEL)

    internal fun getPageGroupSettingsCollectionRef() =
            getDbConnection().collection(PAGE_GROUPS_COLLECTION_LABEL)

    internal fun getSettingsUpdateTimeCollectionRef() =
            getDbConnection().collection(APP_SETTINGS_UPDATE_TIME_COLLECTION_LABEL)

    internal fun getArticleCollectionRef(): CollectionReference {
        return getDbConnection().collection(ARTICLE_COLLECTION)
    }
}
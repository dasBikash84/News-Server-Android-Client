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

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


internal object RealtimeDBUtils {
    //App settings related nodes
    private const val APP_SETTINGS_NODE = "app_settings"
    //User settings related nodes
    private const val USER_SETTINGS_ROOT_NODE = "user_settings"
    private const val ARTICLE_DATA_ROOT_NODE = "article_data"
    private const val ADMION_LIST_NODE = "admin_list"
    private const val PAGE_DOWNLOAD_REQUEST_NODE = "page_download_request"
    private const val PAGE_DOWNLOAD_REQUEST_SETTINGS_NODE = "page_download_request_settings"
    private const val ADMIN_TASK_DATA_NODE = "admin_task_data"
    private const val KEY_WORD_SERACH_RESULT_NODE = "key_word_serach_result"
    private const val SERACH_KEY_WORDS_NODE = "serach_key_words"
    private const val ARTICLE_INFO_FOR_NEWS_CSTEGORIES_NODE = "article_info_for_news_categories"
    private const val APP_VERSION_HISTORY_NODE = "version_history"
    private const val ARTICLE_COMMENTS_NODE = "article_comments"

    private const val FCM_NOTIFICATION_GEN_REQ_NODE = "fcm_notification_gen_request"

    val mFBDataBase: FirebaseDatabase
    private const val CACHE_SIZE_BYTES = 5*1024*1024L //5MB

    init {
        mFBDataBase = FirebaseDatabase.getInstance()
//        mFBDataBase.setPersistenceEnabled(true)
//        mFBDataBase.setPersistenceCacheSizeBytes(CACHE_SIZE_BYTES)
    }

    val mRootReference = mFBDataBase.reference
    val mAppSettingsReference: DatabaseReference = mRootReference.child(APP_SETTINGS_NODE)
    val mUserSettingsRootReference: DatabaseReference = mRootReference.child(USER_SETTINGS_ROOT_NODE)
    val mArticleDataRootReference: DatabaseReference = mRootReference.child(ARTICLE_DATA_ROOT_NODE)
    val mAdminListReference: DatabaseReference = mRootReference.child(ADMION_LIST_NODE)
    val mPageDownLoadRequestReference: DatabaseReference = mRootReference.child(PAGE_DOWNLOAD_REQUEST_NODE)
    val mPageDownLoadRequestSettingsReference: DatabaseReference = mRootReference.child(PAGE_DOWNLOAD_REQUEST_SETTINGS_NODE)
    val mAdminTaskDataNode: DatabaseReference = mRootReference.child(ADMIN_TASK_DATA_NODE)
    val mKeyWordSerachResultNode: DatabaseReference = mRootReference.child(KEY_WORD_SERACH_RESULT_NODE)
    val mSerachKeyWordsNode: DatabaseReference = mRootReference.child(SERACH_KEY_WORDS_NODE)
    val mArticleInfoForNewsCategoriesNode: DatabaseReference = mRootReference.child(ARTICLE_INFO_FOR_NEWS_CSTEGORIES_NODE)
    val mAppVersionHistoryNode: DatabaseReference = mRootReference.child(APP_VERSION_HISTORY_NODE)
    val mFcmNotificationGenReqRef:DatabaseReference = mRootReference.child(FCM_NOTIFICATION_GEN_REQ_NODE)
    val mArticleCommentsRef:DatabaseReference = mRootReference.child(ARTICLE_COMMENTS_NODE)
}


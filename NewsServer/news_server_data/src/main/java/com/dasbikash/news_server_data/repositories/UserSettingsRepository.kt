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

package com.dasbikash.news_server_data.repositories

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.UserSettingsDataService
import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.display_models.entity.PageGroup
import com.dasbikash.news_server_data.display_models.entity.UserPreferenceData
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import com.firebase.ui.auth.IdpResponse
import java.util.*

class UserSettingsRepository private constructor(context: Context) {

    private val DISABLE_PROMPT_FOR_LOG_IN_FLAG_SP_KEY =
            "com.dasbikash.news_server_data.repositories.UserSettingsRepository.DISABLE_PROMPT_FOR_LOG_IN_FLAG_SP_KEY"

    private val mUserSettingsDataService: UserSettingsDataService = DataServiceImplProvider.getUserSettingsDataServiceImpl()
    private val mDatabase: NewsServerDatabase = NewsServerDatabase.getDatabase(context)

    fun shouldPromptForLogIn(context: Context):Boolean{
        return !(SharedPreferenceUtils
                .getData(context,SharedPreferenceUtils.DefaultValues.DEFAULT_BOOLEAN,DISABLE_PROMPT_FOR_LOG_IN_FLAG_SP_KEY) as Boolean)
    }

    fun disablePromptForLogIn(context: Context){
        SharedPreferenceUtils
                .saveData(context,true,DISABLE_PROMPT_FOR_LOG_IN_FLAG_SP_KEY)
    }

    fun getLogInStatus():Boolean{
        return mUserSettingsDataService.getLogInStatus()
    }

    fun getLogInIntent():Intent?{
        return mUserSettingsDataService.getLogInIntent()
    }

    fun doPostLogInProcessing(userLogInResponse: UserLogInResponse):Boolean{
        TODO()
    }

    fun checkIfOnFavList(mPage: Page): Boolean {
        val userPreferenceDataList = mDatabase.userPreferenceDataDao.findAll()
        if (userPreferenceDataList.size>0){
            return userPreferenceDataList.get(0).favouritePageIds.contains(mPage.id)
        }
        return false
    }

    fun addPageToFavList(page: Page):Boolean {
        val userPreferenceDataList = mDatabase.userPreferenceDataDao.findAll()
        val userPreferenceData : UserPreferenceData
        if (userPreferenceDataList.size ==0){
            userPreferenceData = UserPreferenceData(id = UUID.randomUUID().toString())
        }else{
            userPreferenceData = userPreferenceDataList.get(0)
        }
        if(! userPreferenceData.favouritePageIds.contains(page.id)){
            userPreferenceData.favouritePageIds.add(page.id)
        } else{
            return true
        }
        if (userPreferenceDataList.size ==0){
            mDatabase.userPreferenceDataDao.add(userPreferenceData)
        }else{
            mDatabase.userPreferenceDataDao.save(userPreferenceData)
        }
        return true
    }

    fun removePageFromFavList(page: Page): Boolean {
        val userPreferenceDataList = mDatabase.userPreferenceDataDao.findAll()
        val userPreferenceData : UserPreferenceData
        if (userPreferenceDataList.size ==0){
            return false
        }
        userPreferenceData = userPreferenceDataList.get(0)
        if(userPreferenceData.favouritePageIds.contains(page.id)){
            userPreferenceData.favouritePageIds.remove(page.id)
            mDatabase.userPreferenceDataDao.save(userPreferenceData)
            return true
        } else{
            return false
        }
    }

    fun getUserPreferenceData(): LiveData<UserPreferenceData> {
        return mDatabase.userPreferenceDataDao.findUserPreferenceData()
    }

    fun getPageGroupList(): List<PageGroup> {
        val pageGroups = mDatabase.pageGroupDao.findAll()
        pageGroups
                .asSequence()
                .forEach {
                    val thisPageGroup = it
                    it.pageList?.let {
                        it.asSequence().forEach {
                            thisPageGroup.pageEntityList.add(mDatabase.pageDao.findById(it))
                        }
                    }
                }

        return pageGroups
    }

    companion object{
        @Volatile
        private lateinit var  INSTANCE:UserSettingsRepository

        internal fun getInstance(context: Context):UserSettingsRepository{
            if (!::INSTANCE.isInitialized) {
                synchronized(UserSettingsRepository::class.java) {
                    if (!::INSTANCE.isInitialized) {
                        INSTANCE = UserSettingsRepository(context)
                    }
                }
            }
            return INSTANCE
        }
    }

}

class UserLogInResponse(val iDpResponse:IdpResponse){}
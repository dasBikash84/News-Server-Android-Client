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

package com.dasbikash.news_server_data.database.daos

import androidx.room.*
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageArticleFetchStatus

@Dao
internal interface PageDao {


    @get:Query("SELECT COUNT(*) FROM Page")
    val count: Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPages(pages: List<Page>)

    @Query("DELETE FROM Page")
    fun nukeTable()

    @Query("SELECT * FROM Page WHERE parentPageId = '${Page.TOP_LEVEL_PAGE_PARENT_ID}' AND newsPaperId=:newspaperId")
    fun getTopPagesByNewsPaperId(newspaperId: String):List<Page>

    @Query("SELECT * FROM Page WHERE id = :pageId")
    fun findById(pageId:String):Page

    @Query("SELECT * FROM Page WHERE parentPageId= :parentPageId")
    fun getChildPagesByTopLevelPageId(parentPageId: String):List<Page>

    @Query("SELECT * FROM Page WHERE name LIKE :nameContent")
    fun findByNameContent(nameContent: String): List<Page>

    @Query("UPDATE Page SET articleFetchStatus='${PageArticleFetchStatus.NOT_SYNCED}'")
    fun markAllNotSynced()

    @Update
    fun save(page: Page)
}

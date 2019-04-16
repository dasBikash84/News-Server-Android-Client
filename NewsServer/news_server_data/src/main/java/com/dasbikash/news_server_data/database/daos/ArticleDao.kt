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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dasbikash.news_server_data.display_models.entity.Article

@Dao
internal interface ArticleDao {

    @Query("SELECT * FROM Article WHERE pageId=:pageId")
    fun findAllByPageId(pageId: Int): List<Article>

    @Query("SELECT * FROM Article WHERE id=:id")
    fun findById(id: Int): Article

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addArticles(articles: List<Article>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addArticles(vararg articles: Article)

    @Query("DELETE FROM Article")
    fun nukeTable()

    @Query("SELECT * FROM Article WHERE pageId=:pageId ORDER BY publicationDate DESC,created DESC  LIMIT 1")
    fun getLatestArticleByPageId(pageId: String):Article
}

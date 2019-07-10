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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dasbikash.news_server_data.models.room_entity.ArticleSearchKeyWord

@Dao
internal interface ArticleSearchKeyWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addArticleSearchKeyWords(searchKeyWords: List<ArticleSearchKeyWord>)

    @Query("SELECT * FROM ArticleSearchKeyWord limit 1")
    fun getFirst(): ArticleSearchKeyWord?

    @Query("SELECT * FROM ArticleSearchKeyWord WHERE id like :searchKeyWordPortion")
    fun getMatchingSerachKeyWords(searchKeyWordPortion:String):List<ArticleSearchKeyWord>

    @Query("DELETE FROM ArticleSearchKeyWord")
    fun nukeTable()
}

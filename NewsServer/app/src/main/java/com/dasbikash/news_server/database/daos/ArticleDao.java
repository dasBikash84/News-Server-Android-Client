/*
 * Copyright 2019 www.dasbikash.org. All rights reserved.
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

package com.dasbikash.news_server.database.daos;

import com.dasbikash.news_server.display_models.Article;
import com.dasbikash.news_server.display_models.Country;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ArticleDao {

    @Query("SELECT * FROM Article WHERE pageId=:pageId")
    public List<Article> findAllByPageId(int pageId);

    @Query("SELECT * FROM Article WHERE id=:id")
    public Article findId(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addArticle(Article article);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addArticles(List<Article> articles);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addArticles(Article... articles);

    @Delete
    public void deleteArticle(Article article);

    @Query("SELECT COUNT(*) FROM Article WHERE pageId=:pageId")
    public int getArticleCountByPageId(int pageId);
}

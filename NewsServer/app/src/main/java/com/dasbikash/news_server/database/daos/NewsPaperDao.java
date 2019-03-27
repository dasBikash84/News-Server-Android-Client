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

import com.dasbikash.news_server.display_models.Newspaper;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface NewsPaperDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addNewsPapers(List<Newspaper> newspapers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addNewsPapers(Newspaper... newspapers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addNewsPaper(Newspaper newspaper);

    @Query("SELECT * FROM Newspaper")
    public List<Newspaper> findAll();

    @Query("SELECT * FROM Newspaper WHERE mId=:id")
    public Newspaper findById(int id);

    @Update
    public void updateNewsPapers(Newspaper... newspapers);
}

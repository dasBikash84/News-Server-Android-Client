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

package com.dasbikash.news_server.database;

import android.content.Context;

import com.dasbikash.news_server.database.daos.ArticleDao;
import com.dasbikash.news_server.database.daos.CountryDao;
import com.dasbikash.news_server.database.daos.LanguageDao;
import com.dasbikash.news_server.database.daos.NewsPaperDao;
import com.dasbikash.news_server.database.daos.PageDao;
import com.dasbikash.news_server.database.daos.PageGroupDao;
import com.dasbikash.news_server.database.daos.UserPreferenceDataDao;
import com.dasbikash.news_server.display_models.entity.Article;
import com.dasbikash.news_server.display_models.entity.ArticleVisitHistory;
import com.dasbikash.news_server.display_models.entity.Country;
import com.dasbikash.news_server.display_models.entity.Language;
import com.dasbikash.news_server.display_models.entity.Newspaper;
import com.dasbikash.news_server.display_models.entity.Page;
import com.dasbikash.news_server.display_models.entity.PageGroup;
import com.dasbikash.news_server.display_models.entity.UserPreferenceData;
import com.dasbikash.news_server.display_models.room_converters.DateConverter;
import com.dasbikash.news_server.display_models.room_converters.IntListConverter;
import com.dasbikash.news_server.display_models.room_converters.StringListConverter;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Country.class,Language.class,Newspaper.class,
                        Page.class, PageGroup.class,Article.class,
        UserPreferenceData.class, ArticleVisitHistory.class},
        version = 1,exportSchema = false)
@TypeConverters({
        //ImageLinkListConverter.class,
        //IntDataListConverter.class,
        DateConverter.class,
        IntListConverter.class,
        StringListConverter.class
})
public abstract class NewsServerDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "news_server_database";

    private static volatile NewsServerDatabase INSTANCE;

    public static NewsServerDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NewsServerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                NewsServerDatabase.class, DATABASE_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract CountryDao getCountryDao();
    public abstract LanguageDao getLanguageDao();
    public abstract NewsPaperDao getNewsPaperDao();
    public abstract PageDao getPageDao();
    public abstract PageGroupDao getPageGroupDao();
    public abstract ArticleDao getArticleDao();


    public abstract UserPreferenceDataDao getUserPreferenceDataDao();
}

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

package com.dasbikash.news_server_data.data_sources.data_services.news_data_services.spring_mvc

import com.dasbikash.news_server_data.database.NewsServerDatabase
import com.dasbikash.news_server_data.display_models.entity.Article
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.utills.ExceptionUtils
import java.util.*

internal object NewsDataServiceUtils {

    fun processFetchedArticleData(article: Article,database: NewsServerDatabase):Article{
        ExceptionUtils.thowExceptionIfOnMainThred()

        val page = database.pageDao.findById(article.pageId!!)

        return NewsDataServiceUtils.processFetchedArticleData(article,page)
    }

    fun processFetchedArticleData(article: Article,page: Page):Article{
        ExceptionUtils.thowExceptionIfOnMainThred()

        article.newsPaperId = page.newsPaperId

        val publicationDate = Calendar.getInstance()

        if (article.publicationTime != 0L){
            publicationDate.timeInMillis = article.publicationTime
        } else if (article.modificationTime != 0L){
            publicationDate.timeInMillis = article.modificationTime
        }

        article.publicationDate = publicationDate.time

        return article
    }
}
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

import com.dasbikash.news_server_data.data_sources.ArticleCommentService
import com.dasbikash.news_server_data.data_sources.DataServiceImplProvider
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.ArticleCommentLocal
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.utills.ExceptionUtils

object ArticleCommentRepository {
    private val mArticleCommentService: ArticleCommentService =
            DataServiceImplProvider.getArticleCommentServiceImpl()

    fun getCommentsForArticle(article: Article):List<ArticleCommentLocal> {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        return mArticleCommentService.getCommentsForArticle(article)
    }

    fun addNewComment(commentText: String,article: Article,postAnonymous:Boolean=false):ArticleCommentLocal? {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        return mArticleCommentService.addNewComment(commentText,article,postAnonymous)
    }

    fun deleteComment(articleCommentLocal: ArticleCommentLocal,article: Article):Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        return mArticleCommentService.deleteComment(articleCommentLocal,article)
    }

    fun editComment(newText: String, articleCommentLocal: ArticleCommentLocal, article: Article):Boolean {
        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()
        return mArticleCommentService.editComment(newText, articleCommentLocal, article)
    }

}
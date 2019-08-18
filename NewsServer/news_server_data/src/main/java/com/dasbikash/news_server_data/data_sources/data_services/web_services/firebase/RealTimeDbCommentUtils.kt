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

import androidx.annotation.Keep
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.Exclude
import java.util.*


@Keep
data class ArticleCommentServer(
        var commentText: String? = null,
        var timeStamp: Long? = null,
        var dislayName: String? = null,
        var imageUrl: String? = null
) {
    @Exclude
    fun getArticleCommentLocal(commentId: String,uid: String): ArticleCommentLocal? {
        if (commentText != null && commentText.isNullOrBlank() &&
                timeStamp != null && timeStamp!! > 0) {
            val commentDate = Calendar.getInstance()
            commentDate.timeInMillis = timeStamp!!
            return ArticleCommentLocal(commentId,commentText!!,commentDate.time,uid,
                                        dislayName, imageUrl)
        }
        return null
    }
}

@Keep
data class ArticleCommentLocal(
        val commentId: String,
        val commentText: String,
        val commentTime: Date,
        val userId: String,
        val dislayName: String? = null,
        val imageUrl: String? = null
){
    companion object{

        fun getgetArticleCommentServer(commentText: String,firebaseUser: FirebaseUser): ArticleCommentServer? {
            if (!firebaseUser.isAnonymous && commentText.isNotBlank()) {
                ArticleCommentLocal("",commentText, Date(), firebaseUser.uid,
                                    firebaseUser.displayName, firebaseUser.photoUrl?.path).apply {
                    return ArticleCommentServer(commentText,commentTime.time,dislayName,imageUrl)
                }
            }
            return null
        }
    }
}
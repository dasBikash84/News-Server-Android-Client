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
import com.dasbikash.news_server_data.models.room_entity.Article
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

internal object RealTimeDbArticleCommentUtils {

    private const val WAITING_MS_FOR_NET_RESPONSE = 10000L

    fun getCommentsForArticle(article: Article): List<ArticleCommentLocal> {

        val lock = Object()
        val articleComments = mutableListOf<ArticleCommentLocal>()

        RealtimeDBUtils.mArticleCommentsRef.child(article.id)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        synchronized(lock) { lock.notify()}
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            if (snapshot.hasChildren()){
                                snapshot.children.asSequence().forEach {
                                    val userId = it.key!!
                                    if (it.hasChildren()) {
                                        it.children.asSequence().forEach {
                                            val commentId = it.key!!
                                            val articleCommentServer = it.getValue(ArticleCommentServer::class.java)

                                            val articleCommentLocal = articleCommentServer?.getArticleCommentLocal(commentId,userId)
                                            articleCommentLocal?.let {
                                                articleComments.add(it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        synchronized(lock) { lock.notify()}
                    }
                })
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:Exception){}

        return articleComments.toList()
    }

    fun addNewComment(commentText: String,article: Article,postAnonymous:Boolean=false): ArticleCommentLocal? {
        FirebaseAuth.getInstance().currentUser?.let {
            if (!it.isAnonymous){
                val lock = Object()
                var articleCommentLocal:ArticleCommentLocal? = null
                val articleCommentServer = ArticleCommentServer.getInstance(commentText,it)
                if (postAnonymous){
                    articleCommentServer.makeAnonymous()
                }
                RealtimeDBUtils.mArticleCommentsRef
                        .child(article.id)
                        .child(it.uid)
                        .push()
                        .setValue(articleCommentServer,object : DatabaseReference.CompletionListener{
                            override fun onComplete(error: DatabaseError?, databaseReference: DatabaseReference) {
                                if (error == null){
                                    articleCommentLocal = articleCommentServer.getArticleCommentLocal(databaseReference.key!!,it.uid)
                                }
                                synchronized(lock) { lock.notify()}
                            }
                        })
                try {
                    synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
                }catch (ex:Exception){}

                return articleCommentLocal
            }
        }
        return null
    }

    fun deleteComment(articleCommentLocal: ArticleCommentLocal,article: Article): Boolean {

        val lock = Object()
        var result = false

        RealtimeDBUtils.mArticleCommentsRef
                .child(article.id)
                .child(articleCommentLocal.userId)
                .child(articleCommentLocal.commentId)
                .setValue(null,object : DatabaseReference.CompletionListener{
                    override fun onComplete(error: DatabaseError?, databaseReference: DatabaseReference) {
                        if (error == null){
                            result = true
                        }
                        synchronized(lock) { lock.notify()}
                    }
                })
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:Exception){}

        return result
    }

    fun editComment(newText: String, articleCommentLocal: ArticleCommentLocal, article: Article): Boolean {

        val lock = Object()
        var result = false

        RealtimeDBUtils.mArticleCommentsRef
                .child(article.id)
                .child(articleCommentLocal.userId)
                .child(articleCommentLocal.commentId)
                .setValue(articleCommentLocal.copy(commentText = newText.trim(),commentTime = Date()).getArticleCommentServer(),object : DatabaseReference.CompletionListener{
                    override fun onComplete(error: DatabaseError?, databaseReference: DatabaseReference) {
                        if (error == null){
                            result = true
                        }
                        synchronized(lock) { lock.notify()}
                    }
                })
        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:Exception){}

        return result
    }
}

@Keep
data class ArticleCommentServer(
        var commentText: String? = null,
        var timeStamp: Long? = null,
        var dislayName: String? = null,
        var imageUrl: String? = null
) {
    @Exclude
    fun getArticleCommentLocal(commentId: String,uid: String): ArticleCommentLocal? {
        if (commentText != null && commentText!!.isNotBlank() &&
                timeStamp != null && timeStamp!! > 0) {
            val commentDate = Calendar.getInstance()
            commentDate.timeInMillis = timeStamp!!
            return ArticleCommentLocal(commentId,commentText!!,commentDate.time,uid,
                                        dislayName, imageUrl)
        }

        return null
    }
    fun makeAnonymous(){
        dislayName = "Anonymous"
        imageUrl = null
    }
    companion object {
        fun getInstance(commentText: String, firebaseUser: FirebaseUser) =
                ArticleCommentServer(commentText, Date().time, firebaseUser.displayName, firebaseUser.photoUrl?.toString())
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
   fun getArticleCommentServer() =
           ArticleCommentServer(commentText = commentText,timeStamp = commentTime.time,
                                dislayName = dislayName,imageUrl = imageUrl)
}
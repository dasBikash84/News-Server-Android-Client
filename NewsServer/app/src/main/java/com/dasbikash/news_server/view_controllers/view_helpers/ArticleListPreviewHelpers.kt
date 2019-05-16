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

package com.dasbikash.news_server.view_controllers.view_helpers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.ImageUtils
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Language
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object ArticleDiffCallback: DiffUtil.ItemCallback<Article>(){
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}

class ArticlePreviewListAdapter(val mLanguage: Language,val clickAction:(position:Int)->Unit) :
        ListAdapter<Article, ArticlePreviewHolder>(ArticleDiffCallback) {

    override fun onBindViewHolder(holder: ArticlePreviewHolder, position: Int) {
        holder.bind(getItem(position),mLanguage)
        holder.itemView.setOnClickListener{
            clickAction(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlePreviewHolder {
        return ArticlePreviewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_article_preview_holder_without_page_title,parent,false))
    }
}

class ArticlePreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val articlePreviewImage: ImageView
    val articleTitle: TextView
    val articlePublicationTime: TextView
    lateinit var mLanguage: Language
    lateinit var mArticle: Article

    init {
        articlePreviewImage = itemView.findViewById(R.id.article_preview_image)
        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)
    }

    @SuppressLint("CheckResult")
    fun bind(article: Article, language: Language){
        mArticle = article
        mLanguage = language

        Observable.just(mArticle)
                .subscribeOn(Schedulers.computation())
                .map {
                    DisplayUtils.getArticlePublicationDateString(it, mLanguage, itemView.context)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    articleTitle.text = mArticle.title
                    articlePublicationTime.text = it

                    ImageUtils.customLoader(articlePreviewImage,mArticle.previewImageLink)
                }
    }
}

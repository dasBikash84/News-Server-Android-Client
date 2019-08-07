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
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.ImageUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

object ArticleDiffCallback: DiffUtil.ItemCallback<Article>(){
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}

class ArticlePreviewListAdapter(val articleClickAction: (Article) -> Unit, val requestMoreArticle: () -> Unit,
                                val showLoadingIfRequired: () -> Unit, val articleLoadChunkSize: Int,
                                val mShowPageTitle:Boolean=false) :
        ListAdapter<Article, ArticlePreviewHolder>(ArticleDiffCallback) {

    override fun onBindViewHolder(holder: ArticlePreviewHolder, position: Int) {
        holder.itemView.setOnClickListener { articleClickAction(getItem(position)) }
        if (position >= (itemCount - articleLoadChunkSize / 2)) {
            requestMoreArticle()
        }
        if (position == (itemCount - 1)) {
            showLoadingIfRequired()
        }
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlePreviewHolder {
        return ArticlePreviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.view_article_preview, parent, false),mShowPageTitle)
    }
}


class ArticlePreviewHolder(itemView: View, val mShowPageTitle:Boolean=false) : RecyclerView.ViewHolder(itemView) {

    val pageTitle: TextView
    val articlePreviewImage: ImageView
    val articleTitle: TextView
    val articlePublicationTime: TextView
    val articleTextPreview: TextView

    val articleTitlePlaceHolder: TextView
    val articlePublicationTimePlaceHolder: TextView
    val articleTextPreviewPlaceHolder: TextView

    lateinit var mdisposable: Disposable


    init {

        pageTitle = itemView.findViewById(R.id.page_title)
        articlePreviewImage = itemView.findViewById(R.id.article_preview_image)

        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)
        articleTextPreview = itemView.findViewById(R.id.article_text_preview)

        articleTitlePlaceHolder = itemView.findViewById(R.id.article_title_ph)
        articlePublicationTimePlaceHolder = itemView.findViewById(R.id.article_time_ph)
        articleTextPreviewPlaceHolder = itemView.findViewById(R.id.article_text_preview_ph)

        disableView()
    }

    private fun disableView() {
        pageTitle.visibility = View.GONE
        articleTitle.visibility = View.GONE
        articlePublicationTime.visibility = View.GONE
        articleTextPreview.visibility = View.GONE

        articleTitlePlaceHolder.visibility = View.VISIBLE
        articlePublicationTimePlaceHolder.visibility = View.VISIBLE
        articleTextPreviewPlaceHolder.visibility = View.VISIBLE
        ImageUtils.customLoader(imageView = articlePreviewImage,
                defaultImageResourceId = R.drawable.pc_bg,
                placeHolderImageResourceId = R.drawable.pc_bg)
    }

    private fun enableView() {

        articleTitlePlaceHolder.visibility = View.GONE
        articlePublicationTimePlaceHolder.visibility = View.GONE
        articleTextPreviewPlaceHolder.visibility = View.GONE

        if (mShowPageTitle){
            pageTitle.visibility = View.VISIBLE
        }
        articleTitle.visibility = View.VISIBLE
        articlePublicationTime.visibility = View.VISIBLE
        articleTextPreview.visibility = View.VISIBLE
    }

    @SuppressLint("CheckResult")
    fun bind(article: Article) {
        disableView()
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(itemView.context)
        Observable.just(article)
                .subscribeOn(Schedulers.io())
                .map {
                    val page = appSettingsRepository.findPageById(it.pageId!!)!!
                    val newspaper = appSettingsRepository.getNewspaperByPage(page)
                    val language = appSettingsRepository.getLanguageByNewspaper(newspaper)
                    Triple(language, newspaper, page)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    pageTitle.text = StringBuilder(it.third.name!!).append(" | ").append(it.second.name!!).toString()
                    articleTitle.text = article.title
                    articlePublicationTime.text = DisplayUtils.getArticlePublicationDateString(article, it.first, itemView.context)
                    DisplayUtils.displayHtmlText(articleTextPreview, article.articleText ?: "")
//                    debugLog(article.toString())
                    enableView()

                    ImageUtils.customLoader(articlePreviewImage, article.previewImageLink,
                            R.drawable.pc_bg, R.drawable.app_big_logo)
                }
    }
}
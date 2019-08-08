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
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server.view_controllers.ArticleWithParents
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.ImageUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

object ArticleWithParentsDiffCallback : DiffUtil.ItemCallback<ArticleWithParents>() {
    override fun areItemsTheSame(oldItem: ArticleWithParents, newItem: ArticleWithParents): Boolean {
        return oldItem.article.checkIfSameArticle(newItem.article)
    }

    override fun areContentsTheSame(oldItem: ArticleWithParents, newItem: ArticleWithParents): Boolean {
        return oldItem.article.checkIfSameArticle(newItem.article)
    }
}

object ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}

class ArticlePreviewListAdapter(val articleClickAction: (Article) -> Unit, val requestMoreArticle: () -> Unit,
                                val showLoadingIfRequired: () -> Unit, val articleLoadChunkSize: Int,
                                val mShowPageTitle: Boolean = false) :
        ListAdapter<ArticleWithParents, ArticlePreviewHolder>(ArticleWithParentsDiffCallback) {

    override fun onBindViewHolder(holder: ArticlePreviewHolder, position: Int) {
        holder.itemView.setOnClickListener { articleClickAction((getItem(position) as ArticleWithParents).article) }
        holder.bind(getItem(position))
        if (position >= (itemCount - articleLoadChunkSize / 2)) {
            requestMoreArticle()
        }
        if (position == (itemCount - 1)) {
            showLoadingIfRequired()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlePreviewHolder {
        return ArticlePreviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.view_article_preview, parent, false), mShowPageTitle)
    }
}


class ArticlePreviewHolder(itemView: View, val mShowPageTitle: Boolean = false) : RecyclerView.ViewHolder(itemView) {

    val pageTitle: TextView
    val articlePreviewImage: ImageView
    val articleTitle: TextView
    val articlePublicationTime: TextView
    val articleTextPreview: TextView

    val articleTitlePlaceHolder: TextView
    val articlePublicationTimePlaceHolder: TextView
    val articleTextPreviewPlaceHolder: TextView

    lateinit var mDisposable: Disposable


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
        if (::mDisposable.isInitialized){
            mDisposable.dispose()
        }
    }

    private fun enableView() {

        articleTitlePlaceHolder.visibility = View.GONE
        articlePublicationTimePlaceHolder.visibility = View.GONE
        articleTextPreviewPlaceHolder.visibility = View.GONE

        if (mShowPageTitle) {
            pageTitle.visibility = View.VISIBLE
        }
        articleTitle.visibility = View.VISIBLE
        articlePublicationTime.visibility = View.VISIBLE
        articleTextPreview.visibility = View.VISIBLE
    }

    @SuppressLint("CheckResult")
    fun bind(articleWithParents: ArticleWithParents) {
        disableView()

        articleWithParents.apply {
            loadData(article, language, newspaper, page)
        }
    }

    private fun loadData(article: Article, language: Language, newspaper: Newspaper, page: Page) {
        debugLog("loadData for: ${article.toString()}")

        pageTitle.text = StringBuilder(page.name!!).append(" | ").append(newspaper.name!!).toString()
        articleTitle.text = article.title
        articlePublicationTime.text = DisplayUtils.getArticlePublicationDateString(article, language, itemView.context)
        DisplayUtils.displayHtmlText(articleTextPreview, article.articleText ?: "")
        enableView()

        ImageUtils.customLoader(articlePreviewImage, article.previewImageLink,
                R.drawable.pc_bg, R.drawable.app_big_logo)

    }
}
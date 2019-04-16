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

package com.dasbikash.news_server.views.rv_helpers

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.RepositoryFactory
import com.dasbikash.news_server_data.display_models.entity.Article
import com.dasbikash.news_server_data.display_models.entity.Language
import com.dasbikash.news_server_data.display_models.entity.Page
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class PagePreviewListAdapter(@LayoutRes val holderResId: Int, val homeViewModel: HomeViewModel) :
        ListAdapter<Page, ArticlePreviewHolder>(PageDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlePreviewHolder {
        val view = LayoutInflater.from(parent.context).inflate(holderResId, parent, false)
        return ArticlePreviewHolder(view, homeViewModel)
    }

    override fun onBindViewHolder(holder: ArticlePreviewHolder, position: Int) {
        holder.disposable.clear()
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ArticlePreviewHolder) {
        super.onViewRecycled(holder)
        holder.disposable.clear()
    }

}

class ArticlePreviewHolder(itemView: View, val homeViewModel: HomeViewModel) : RecyclerView.ViewHolder(itemView) {

    companion object {
        val TAG = "ArticlePreviewHolder"
    }

    val pageTitle: TextView
    val articlePreviewImage: ImageView
    val articleTitle: TextView
    val articlePublicationTime: TextView

    lateinit var language: Language

    lateinit var mArticle: Article

    val disposable = CompositeDisposable()

    init {
        pageTitle = itemView.findViewById(R.id.page_title)
        articlePreviewImage = itemView.findViewById(R.id.article_preview_image)
        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)
    }

    fun bind(page: Page) {

        pageTitle.visibility = View.GONE
        articlePreviewImage.visibility = View.GONE
        articleTitle.visibility = View.GONE
        articlePublicationTime.visibility = View.GONE


        val settingsRepository = RepositoryFactory.getSettingsRepository(itemView.context)

        val uuid = UUID.randomUUID()

        disposable.add(

                homeViewModel.getLatestArticleProvider(Pair(uuid, page))
                        .filter { it.first == uuid }
                        .map {
                            language = settingsRepository.getLanguageByPage(page)
                            it.second?.let {
                                val dateString = DisplayUtils.getArticlePublicationDateString(it, language, itemView.context)
                                return@map Pair(dateString, it)
                            }
                            return@map Any()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Any>() {
                            override fun onComplete() {
//                                Log.d(TAG, "onComplete for page ${page.name} Np: ${page.newsPaperId} L2")
                            }

                            @Suppress("UNCHECKED_CAST")
                            override fun onNext(articleData: Any) {

                                pageTitle.text = page.name
                                pageTitle.visibility = View.VISIBLE

                                if (articleData is Pair<*, *>) {

                                    val articleDataResult = articleData as Pair<String, Article>

                                    mArticle = articleDataResult.second

//                                    Log.d(TAG, "page ${page.name} Np: ${page.newsPaperId} has article title: ${mArticle.title}")

                                    articleTitle.text = mArticle.title
                                    articlePublicationTime.text = articleDataResult.first
                                    articleTitle.visibility = View.VISIBLE
                                    articlePublicationTime.visibility = View.VISIBLE

                                    mArticle.previewImageLink?.let {
                                        Picasso.get().load(it).into(articlePreviewImage)
                                        articlePreviewImage.visibility = View.VISIBLE
                                    } ?: let {
                                        Picasso.get().load(R.drawable.app_big_logo).into(articlePreviewImage)
                                        articlePreviewImage.visibility = View.VISIBLE
                                    }

                                    //Add click listner
                                    itemView.setOnClickListener(View.OnClickListener {
                                        Log.d(TAG, "Article: ${mArticle.title} clicked")
                                    })
                                }
                            }

                            override fun onError(e: Throwable) {
//                                Log.d(TAG, e.message + " for page Np: ${page.newsPaperId} ${page.name} L2")
                            }
                        })
        )

    }
}
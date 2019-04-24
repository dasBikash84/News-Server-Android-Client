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
import com.dasbikash.news_server.views.PageViewActivity
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class PagePreviewListAdapter(@LayoutRes val holderResId: Int, val homeViewModel: HomeViewModel) :
        ListAdapter<Page, ArticlePreviewHolder>(PageDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlePreviewHolder {
        val view = LayoutInflater.from(parent.context).inflate(holderResId, parent, false)
        return ArticlePreviewHolder(view, homeViewModel)
    }

    override fun onBindViewHolder(holder: ArticlePreviewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ArticlePreviewHolder) {
        super.onViewRecycled(holder)
        holder.disposableObserver?.let {
            Log.d("ArticlePreviewHolder", "disposed in PagePreviewListAdapter for:${holder.mPage.name}")
            if (!it.isDisposed) {
                it.dispose()
            }
        }
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
    var disposableObserver: Disposable? = null

    init {
        pageTitle = itemView.findViewById(R.id.page_title)
        articlePreviewImage = itemView.findViewById(R.id.article_preview_image)
        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)
    }

    lateinit var mPage: Page

    fun bind(page: Page) {

        pageTitle.visibility = View.GONE
        articlePreviewImage.visibility = View.INVISIBLE
        articleTitle.visibility = View.INVISIBLE
        articlePublicationTime.visibility = View.INVISIBLE

        mPage = page
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(itemView.context)

        val uuid = UUID.randomUUID()

        disposableObserver?.let {
            Log.d(TAG, "disposed in bind for:${page.name}")
            if (!it.isDisposed) {
                it.dispose()
            }
        }

        pageTitle.text = page.name
        pageTitle.visibility = View.VISIBLE

        itemView.setOnClickListener(View.OnClickListener {

        })

        disposableObserver = homeViewModel.getLatestArticleProvider(Pair(uuid, page))
                .filter { it.first == uuid }
                .map {
                    language = appSettingsRepository.getLanguageByPage(page)
                    it.second?.let {
                        val dateString = DisplayUtils.getArticlePublicationDateString(it, language, itemView.context)

                        var displayImageLink: String? = null

                        if (it.previewImageLink !=null && it.previewImageLink!!.isNotBlank()){
                            displayImageLink = it.previewImageLink
                        } else if (it.imageLinkList!=null && it.imageLinkList!!.size>0) {
                            var i=0
                            it.imageLinkList?.forEach {
                                it.link?.let {
                                    if (it.isNotBlank()){
                                        displayImageLink = it
                                        return@forEach
                                    }
                                }
                            }
                        }
                        return@map Triple(dateString, it, displayImageLink)
                    }
                    return@map Any()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Any>() {
                    override fun onComplete() {
                        Log.d(TAG, "onComplete for page ${page.name} Np: ${page.newsPaperId} L2")
                    }

                    @Suppress("UNCHECKED_CAST")
                    override fun onNext(articleData: Any) {

                        if (articleData is Triple<*, *, *>) {

                            val articleDataResult = articleData as Triple<String, Article, String?>

                            mArticle = articleDataResult.second

                            articleTitle.text = mArticle.title
                            articlePublicationTime.text = articleDataResult.first
                            articleTitle.visibility = View.VISIBLE
                            articlePublicationTime.visibility = View.VISIBLE

                            articleDataResult.third?.let {
                                Picasso.get().load(it).into(articlePreviewImage)
                                articlePreviewImage.visibility = View.VISIBLE
                            } ?: let {
                                Picasso.get().load(R.drawable.app_big_logo).into(articlePreviewImage)
                                articlePreviewImage.visibility = View.VISIBLE
                            }

                            //Add click listner
                            itemView.setOnClickListener(View.OnClickListener {
                                itemView.context.startActivity(
                                        PageViewActivity.getIntentForPageDisplay(
                                                itemView.context, page, mArticle.id
                                        )
                                )
                            })
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d(TAG, e.message + " for page Np: ${page.newsPaperId} ${page.name} L2")
                    }
                })
    }
}
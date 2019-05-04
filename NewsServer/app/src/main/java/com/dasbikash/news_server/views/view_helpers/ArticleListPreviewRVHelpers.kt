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

package com.dasbikash.news_server.views.view_helpers

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server.views.NewspaperPerviewFragment
import com.dasbikash.news_server.views.PageViewActivity
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class PagePreviewListAdapter(@LayoutRes val holderResId: Int, val homeViewModel: HomeViewModel) :
        ListAdapter<Page, LatestArticlePreviewHolder>(PageDiffCallback),DefaultLifecycleObserver {

    val TAG = "ArticlePreviewHolder"

    val mDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestArticlePreviewHolder {
        val view = LayoutInflater.from(parent.context).inflate(holderResId, parent, false)
        return LatestArticlePreviewHolder(view, homeViewModel)
    }

    override fun onBindViewHolder(holder: LatestArticlePreviewHolder, position: Int) {
        val page = getItem(position)

        val uuid = UUID.randomUUID()
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(holder.itemView.context)
        holder.disableDisplay(page)
        mDisposable.add(
                homeViewModel.getLatestArticleProvider(Pair(uuid, page))
                        .filter { it.first == uuid }
                        .map {
                            it.second?.let {
                                val dateString = DisplayUtils
                                        .getArticlePublicationDateString(
                                                it, appSettingsRepository.getLanguageByPage(page), holder.itemView.context)
                                return@map Pair(dateString, it)
                            }
                            return@map Any()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Any>() {
                            override fun onComplete() {
                                Log.d(TAG, "onComplete for page ${page.name} Np: ${page.newspaperId} L2")
                            }

                            @Suppress("UNCHECKED_CAST")
                            override fun onNext(articleData: Any) {
                                if (articleData is Pair<*, *>) {
                                    Log.d(NewspaperPerviewFragment.TAG,"art displayed for page: ${page.name} Np: ${page.newspaperId}")
                                    val articleDataResult = articleData as Pair<String?, Article>
                                    holder.bind(page,articleDataResult.first,articleDataResult.second)
                                }
                            }

                            override fun onError(e: Throwable) {
                                Log.d(TAG, e.message + " for page Np: ${page.newspaperId} ${page.name} L2")
                            }
                        })
        )
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mDisposable.clear()
    }

    override fun onPause(owner: LifecycleOwner) {
        mDisposable.clear()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mDisposable.clear()
    }

}

class LatestArticlePreviewHolder(itemView: View, val homeViewModel: HomeViewModel) : RecyclerView.ViewHolder(itemView) {

    companion object {
        val TAG = "ArticlePreviewHolder"
    }

    val pageTitle: TextView
    val articlePreviewImage: ImageView
    val articleTitle: TextView
    val articlePublicationTime: TextView

    lateinit var mArticle: Article

    init {
        pageTitle = itemView.findViewById(R.id.page_title)
        articlePreviewImage = itemView.findViewById(R.id.article_preview_image)
        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)
        itemView.setOnClickListener{}
    }

    lateinit var mPage: Page

    fun disableDisplay(page: Page) {
        mPage = page

        pageTitle.text = page.name
        pageTitle.visibility = View.VISIBLE

        articlePreviewImage.visibility = View.INVISIBLE
        articleTitle.visibility = View.INVISIBLE
        articlePublicationTime.visibility = View.INVISIBLE
    }

    fun bind(page: Page, dateString: String?, article: Article) {

        mArticle = article

        articleTitle.text = mArticle.title
        articleTitle.visibility = View.VISIBLE

        articlePublicationTime.text = dateString
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
            itemView.context.startActivity(
                    PageViewActivity.getIntentForPageDisplay(
                            itemView.context, page
                    )
            )
        })
    }
}
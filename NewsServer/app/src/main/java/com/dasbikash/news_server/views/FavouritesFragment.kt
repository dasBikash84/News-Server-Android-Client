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

package com.dasbikash.news_server.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView

import com.dasbikash.news_server.R
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server.views.rv_helpers.PageDiffCallback
import com.dasbikash.news_server_data.RepositoryFactory
import com.dasbikash.news_server_data.display_models.entity.Article
import com.dasbikash.news_server_data.display_models.entity.Language
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.display_models.entity.UserPreferenceData
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class FavouritesFragment : Fragment() {

    private lateinit var mCoordinatorLayout: CoordinatorLayout
    private lateinit var mScroller: NestedScrollView
    private lateinit var mFavItemsHolder: RecyclerView

    lateinit var mFavouritePagesListAdapter : FavouritePagesListAdapter

    private val disposable = CompositeDisposable()

    private lateinit var mHomeViewModel: HomeViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCoordinatorLayout = view.findViewById(R.id.fav_frag_coor_layout)
        mScroller = view.findViewById(R.id.fav_frag_item_scroller)
        mFavItemsHolder = view.findViewById(R.id.fav_frag_item_holder)

        mFavouritePagesListAdapter = FavouritePagesListAdapter(context!!)

        mHomeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)

        mFavItemsHolder.adapter = mFavouritePagesListAdapter

        val settingsRepository = RepositoryFactory.getSettingsRepository(context!!)

        mHomeViewModel.getUserPreferenceData()
                .observe(activity!!,object : Observer<UserPreferenceData> {
                    override fun onChanged(userPreferenceData: UserPreferenceData) {
                        disposable.add(Observable.just(userPreferenceData.favouritePageIds)
                                .subscribeOn(Schedulers.io())
                                .map {
                                    it.asSequence().map {settingsRepository.findPageById(it)}.toList()
                                }
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(object : DisposableObserver<List<Page?>>() {
                                    override fun onComplete() {}
                                    override fun onNext(pageList: List<Page?>) {
                                        mFavouritePagesListAdapter.submitList(pageList)
                                    }
                                    override fun onError(e: Throwable) {}
                                }))
                    }
                })

    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }

    companion object {
        val TAG = "FavouritesFragment"
    }
}

class FavouritePagesListAdapter(val context: Context) :
        ListAdapter<Page, FavouritePagePreviewHolder>(PageDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritePagePreviewHolder {
        return FavouritePagePreviewHolder(LayoutInflater.from(context).inflate(
                R.layout.view_article_perview_parent_width, parent, false))
    }

    override fun onBindViewHolder(holder: FavouritePagePreviewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class FavouritePagePreviewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {

    private val pageTitle: AppCompatTextView
    private val pageTitleHolder: MaterialCardView
    private val articleTitle: AppCompatTextView
    private val articlePublicationTime: AppCompatTextView
    private val articleImage: AppCompatImageView

    private lateinit var mPage: Page
    private lateinit var mArticle: Article

    init {
        pageTitleHolder = itemview.findViewById(R.id.page_title_holder) as MaterialCardView
        pageTitle = itemview.findViewById(R.id.page_title)
        articleTitle = itemview.findViewById(R.id.article_title)
        articlePublicationTime = itemview.findViewById(R.id.article_time)
        articleImage = itemview.findViewById(R.id.article_preview_image)
    }

    fun bind(page: Page?) {

        Log.d("FavouritesFragment","Page: ${page?.name}")
        Log.d("FavouritesFragment","Page: ${page?.name}")

        pageTitleHolder.visibility = View.GONE
        hideChilds()

        pageTitle.setOnClickListener({})

        if (page == null) return

        mPage = page
        Log.d("FavouritesFragment","Page: ${mPage.name}")

        pageTitle.text = mPage.name
        pageTitleHolder.visibility = View.VISIBLE

        pageTitle.setOnClickListener {
            if (!::mArticle.isInitialized){
                showChilds()
                val settingsRepository = RepositoryFactory.getSettingsRepository(itemView.context)
                val newsDataRepository = RepositoryFactory.getNewsDataRepository(itemView.context)
                var language:Language
                Observable.just(mPage)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val article = newsDataRepository.getLatestArticleByPage(it)
                            article?.let {
                                language = settingsRepository.getLanguageByPage(mPage)
                                return@map Pair(it,DisplayUtils.getArticlePublicationDateString(article, language, itemView.context))
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Any>(){
                            override fun onComplete() {
                            }
                            override fun onNext(data:Any) {

                                if (data is Pair<*,*>) {
                                    @Suppress("UNCHECKED_CAST")
                                    mArticle = (data as Pair<Article,String>).first

                                    articleTitle.text = mArticle.title
                                    articlePublicationTime.text = data.second

                                    mArticle.previewImageLink?.let {
                                        Picasso.get().load(it).into(articleImage)
                                    } ?: let {
                                        Picasso.get().load(R.drawable.app_big_logo).into(articleImage)
                                    }
                                }
                            }
                            override fun onError(e: Throwable) {
                            }
                        })
            }else{
                if (articleTitle.visibility == View.GONE){
                    showChilds()

                } else{
                    hideChilds()
                }
            }
        }
    }

    private fun hideChilds() {
        articleTitle.visibility = View.GONE
        articlePublicationTime.visibility = View.GONE
        articleImage.visibility = View.GONE
    }

    private fun showChilds() {
        articleTitle.visibility = View.VISIBLE
        articlePublicationTime.visibility = View.VISIBLE
        articleImage.visibility = View.VISIBLE
    }

}
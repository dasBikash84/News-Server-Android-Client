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

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server.views.interfaces.NavigationHost
import com.dasbikash.news_server.views.view_helpers.PageDiffCallback
import com.dasbikash.news_server.views.view_helpers.PagePreviewListAdapter
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class NewspaperPerviewFragment : Fragment() {

    private lateinit var mNewspaper: Newspaper
    private lateinit var mHomeViewModel: HomeViewModel

    private lateinit var mPagePreviewList: RecyclerView
    private lateinit var mNestedScrollView: NestedScrollView
    private lateinit var mListAdapter: TopPagePreviewListAdapter


    private val mDisposable = CompositeDisposable()
    lateinit var mAppSettingsRepository: AppSettingsRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_newspaper_page_list_preview_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(activity!!)

        mNewspaper = arguments!!.getSerializable(ARG_NEWS_PAPAER) as Newspaper
        mPagePreviewList = view.findViewById(R.id.newspaper_page_preview_list)
        mNestedScrollView = view.findViewById(R.id.page_preview_scroller)
        mHomeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)

        (activity as NavigationHost)
                .showBottomNavigationView(true)

        mNestedScrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val remainingYOfRV = mPagePreviewList.height - scrollY - resources.displayMetrics.heightPixels
                if (remainingYOfRV < resources.displayMetrics.heightPixels / 5) { //Hides scroller if 1/5 off Child recycler view is below scroller
                    (activity as NavigationHost)
                            .showBottomNavigationView(false)
                } else {
                    (activity as NavigationHost)
                            .showBottomNavigationView(true)
                }
            }
        })

        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            mAppSettingsRepository
                                    .getTopPagesForNewspaper(mNewspaper)
                                    .sortedBy { it.id }
                                    .toCollection(mutableListOf())
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d(TAG, "newspaper: ${mNewspaper.name}, top page count: ${it.size}")
                            mListAdapter = TopPagePreviewListAdapter(this, mAppSettingsRepository, ViewModelProviders.of(activity!!).get(HomeViewModel::class.java))
                            mPagePreviewList.adapter = mListAdapter
                            mListAdapter.submitList(it.toList())
                        })
        )

    }

    override fun onPause() {
        super.onPause()
        mDisposable.clear()
        Log.d("NpPerviewFragment", "onPause():${mNewspaper.name}")
    }

    companion object {

        val ARG_NEWS_PAPAER = "com.dasbikash.news_server.views.NewspaperPerviewFragment.ARG_NEWS_PAPAER"
        val TAG = "NpPerviewFragment"

        fun getInstance(newspaper: Newspaper): NewspaperPerviewFragment {
            val args = Bundle()
            args.putSerializable(ARG_NEWS_PAPAER, newspaper)
            val fragment = NewspaperPerviewFragment()
            fragment.setArguments(args)
            return fragment
        }
    }
}

class TopPagePreviewListAdapter(val lifecycleOwner: LifecycleOwner,
                                val appSettingsRepository: AppSettingsRepository,
                                val homeViewModel: HomeViewModel) :
        ListAdapter<Page, PagePreviewHolder>(PageDiffCallback) {

    val childPageMap = mutableMapOf<Page, List<Page>>()

    val disposable = CompositeDisposable()

    override fun onCurrentListChanged(previousList: MutableList<Page>, currentList: MutableList<Page>) {
        super.onCurrentListChanged(previousList, currentList)
        if (currentList.size > 0) {
            disposable.add(
                Observable.fromIterable(currentList)
                    .subscribeOn(Schedulers.io())
                    .forEach {
                        it?.let {
                            val childPages =
                                    appSettingsRepository
                                            .getChildPagesForTopLevelPage(it)
                                            .asSequence()
                                            .filter { it.getHasData() }
                                            .sortedBy { it.id }
                                            .toCollection(mutableListOf<Page>())
                            if (it.getHasData()) {
                                childPages.add(0, it)
                            }
                            synchronized(childPageMap) {
                                childPageMap.put(it, childPages)
                            }
                        }
                    }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagePreviewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_child_page_list_preview, parent, false)
        val holder = PagePreviewHolder(view)
        lifecycleOwner.lifecycle.addObserver(holder)
        return holder
    }

    override fun onBindViewHolder(holder: PagePreviewHolder, position: Int) {

        val page = getItem(position)!!

        disposable.add(
                Observable.just(page)
                        .subscribeOn(Schedulers.io())
                        .map {
                            do {
                                synchronized(childPageMap) {
                                    if (childPageMap.containsKey(it)) {
                                        return@map childPageMap.get(it)
                                    }
                                }
                                Thread.sleep(100L)
                            } while (true)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Any>() {
                            override fun onComplete() {}
                            override fun onNext(childPageList: Any) {
                                if (childPageList is List<*>) {
                                    @Suppress("UNCHECKED_CAST")
                                    holder.bind(page, childPageList as List<Page>, homeViewModel)
                                }
                            }

                            override fun onError(e: Throwable) {}
                        })
        )
    }

    override fun onViewRecycled(holder: PagePreviewHolder) {
        super.onViewRecycled(holder)
        holder.active = false
    }

    override fun onFailedToRecycleView(holder: PagePreviewHolder): Boolean {
        lifecycleOwner.lifecycle.removeObserver(holder)
        return super.onFailedToRecycleView(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposable.clear()
    }

}

class PagePreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), DefaultLifecycleObserver {

    override fun onPause(owner: LifecycleOwner) {
        Log.d("NpPerviewFragment", "onPause: active = false for page:${mPage.name}")
        active = false
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("NpPerviewFragment", "onStop: active = false for page:${mPage.name}")
        active = false
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d("NpPerviewFragment", "onDestroy: active = false for page:${mPage.name}")
        active = false
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.d("NpPerviewFragment", "onResume: active = true at PagePreviewHolder")
        active = true
    }

    companion object {
        val TAG = "NpPerviewFragment"
    }

    var active = true

    lateinit var mPage: Page

    private val mPageListPreviewHolderRV: RecyclerView

    init {
        mPageListPreviewHolderRV = itemView.findViewById(R.id.mPageListPreviewHolder)
    }

    @SuppressLint("CheckResult")
    fun bind(page: Page, data: List<Page>, homeViewModel: HomeViewModel) {
        mPage = page
        val articlePreviewResId: Int
        if (data.size == 1) {
            articlePreviewResId = R.layout.view_article_preview_holder_parent_width
            mPageListPreviewHolderRV.minimumWidth = itemView.resources.displayMetrics.widthPixels
        } else if (data.size > 1) {
            articlePreviewResId = R.layout.view_article_preview_holder_custom_width
        } else {
            return
        }

        val pagePreviewListAdapter = PagePreviewListAdapter(articlePreviewResId, homeViewModel)
        mPageListPreviewHolderRV.adapter = pagePreviewListAdapter
        pagePreviewListAdapter.submitList(data)
    }
}
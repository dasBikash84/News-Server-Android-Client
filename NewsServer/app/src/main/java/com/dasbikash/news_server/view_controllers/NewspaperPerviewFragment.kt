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

package com.dasbikash.news_server.view_controllers

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.OnceSettableBoolean
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_controllers.view_helpers.PageDiffCallback
import com.dasbikash.news_server.view_controllers.view_helpers.PagePreviewListAdapter
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

class NewspaperPerviewFragment : Fragment() {

    private lateinit var mNewspaper: Newspaper
    private lateinit var mHomeViewModel: HomeViewModel

    private lateinit var mPagePreviewList: RecyclerView
    private lateinit var mListAdapter: TopPagePreviewListAdapter
    private lateinit var mTopPageList: List<Page>


    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)
    lateinit var mAppSettingsRepository: AppSettingsRepository

    private var mInitDone = OnceSettableBoolean()
    private var mInitInitiated = AtomicBoolean(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_newspaper_page_list_preview_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(activity!!)

        mNewspaper = arguments!!.getSerializable(ARG_NEWS_PAPAER) as Newspaper
        mPagePreviewList = view.findViewById(R.id.newspaper_page_preview_list)
        mHomeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)

        (activity as NavigationHost)
                .showBottomNavigationView(true)
        init()
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        if (!mInitDone.get()) {
            mDisposable.add(
                    Observable.just(true)
                            .subscribeOn(Schedulers.io())
                            .map {
                                if (!mInitInitiated.get()  && !mInitDone.get()) {
                                    mInitInitiated.set(true)
                                    mTopPageList =  mAppSettingsRepository
                                                        .getTopPagesForNewspaper(mNewspaper)
                                                        .sortedBy { it.id }
                                                        .toList()
                                }
                            }
                            .doOnDispose { mInitInitiated.set(false) }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                if(::mTopPageList.isInitialized && this.lifecycle.currentState==Lifecycle.State.RESUMED && !mInitDone.get()) {
                                    mInitDone.set()
                                    LoggerUtils.debugLog( "newspaper: ${mNewspaper.name}, top page count: ${mTopPageList.size}",this::class.java)
                                    mListAdapter = TopPagePreviewListAdapter(this, mAppSettingsRepository, ViewModelProviders.of(activity!!).get(HomeViewModel::class.java))
                                    mPagePreviewList.adapter = mListAdapter
                                    mListAdapter.submitList(mTopPageList)
                                }
                            })
            )
        }
    }

    companion object {

        val ARG_NEWS_PAPAER = "com.dasbikash.news_server.views.NewspaperPerviewFragment.ARG_NEWS_PAPAER"

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
        ListAdapter<Page, PagePreviewHolder>(PageDiffCallback), DefaultLifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    val childPageMap = mutableMapOf<Page, List<Page>>()

    val disposable = CompositeDisposable()

    override fun onCurrentListChanged(previousList: MutableList<Page>, currentList: MutableList<Page>) {
        super.onCurrentListChanged(previousList, currentList)
        if (currentList.size > 0) {
            disposable.add(
                    Observable.fromIterable(currentList)
                            .subscribeOn(Schedulers.io())
                            .forEach {
                                    it.let {
                                        val childPages =
                                                appSettingsRepository
                                                        .getChildPagesForTopLevelPage(it)
                                                        .asSequence()
                                                        .filter { it.hasData }
                                                        .sortedBy { it.id }
                                                        .toCollection(mutableListOf<Page>())
                                        if (it.hasData) {
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
        val holder = PagePreviewHolder(lifecycleOwner, view)
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
                                try {
                                    Thread.sleep(10L)
                                } catch (ex: InterruptedException) {
                                    LoggerUtils.printStackTrace(ex)
                                }
                            } while (true)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Any>() {
                            override fun onComplete() {}
                            @Suppress("UNCHECKED_CAST")
                            override fun onNext(childPageList: Any) {
                                if (childPageList is List<*>) {
                                    LoggerUtils.debugLog("bind for page: ${page.name} Np: ${page.newspaperId} with ${childPageList.size} childs",
                                                        this@TopPagePreviewListAdapter::class.java)
                                    holder.bind(page, childPageList as List<Page>, homeViewModel)
                                }
                            }

                            override fun onError(e: Throwable) {}
                        })
        )
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        LoggerUtils.debugLog("Disposing",this::class.java)
        disposable.clear()
    }

    override fun onPause(owner: LifecycleOwner) {
        LoggerUtils.debugLog("Disposing",this::class.java)
        disposable.clear()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        LoggerUtils.debugLog("Disposing",this::class.java)
        disposable.clear()
    }

}

class PagePreviewHolder(val lifecycleOwner: LifecycleOwner, itemView: View) : RecyclerView.ViewHolder(itemView) {

    lateinit var mPage: Page

    private val mPageListPreviewHolderRV: RecyclerView

    init {
        mPageListPreviewHolderRV = itemView.findViewById(R.id.mPageListPreviewHolder)
        mPageListPreviewHolderRV.minimumWidth = itemView.resources.displayMetrics.widthPixels
    }

    @SuppressLint("CheckResult")
    fun bind(page: Page, data: List<Page>, homeViewModel: HomeViewModel) {
        mPage = page
        val articlePreviewResId: Int
        if (data.size == 1) {
            articlePreviewResId = R.layout.view_article_preview_holder_parent_width
        } else if (data.size > 1) {
            articlePreviewResId = R.layout.view_article_preview_holder_custom_width
        } else {
            return
        }

        val pagePreviewListAdapter = PagePreviewListAdapter(lifecycleOwner, articlePreviewResId, homeViewModel)
        mPageListPreviewHolderRV.adapter = pagePreviewListAdapter
        pagePreviewListAdapter.submitList(data)
    }
}
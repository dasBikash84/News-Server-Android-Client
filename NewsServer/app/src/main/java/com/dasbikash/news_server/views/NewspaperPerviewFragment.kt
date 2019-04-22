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
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server.views.interfaces.NavigationHost
import com.dasbikash.news_server.views.rv_helpers.PageDiffCallback
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
                            mListAdapter = TopPagePreviewListAdapter(activity!!.supportFragmentManager,this)
                            mPagePreviewList.adapter = mListAdapter
                            mListAdapter.submitList(it)
                        })
        )

    }

    override fun onPause() {
        super.onPause()
        mDisposable.clear()
        Log.d("NpPerviewFragment","onPause():${mNewspaper.name}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("NpPerviewFragment","onDestroy():${mNewspaper.name}")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("NpPerviewFragment","onDetach():${mNewspaper.name}")
    }

    override fun onStop() {
        super.onStop()
        Log.d("NpPerviewFragment","onStop():${mNewspaper.name}")
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

class TopPagePreviewListAdapter(val fragmentManager: FragmentManager,val lifecycleOwner: LifecycleOwner) :
        ListAdapter<Page, PagePreviewHolder>(PageDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagePreviewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_top_page_child_list_preview_holder, parent, false)
        val holder = PagePreviewHolder(view)
        lifecycleOwner.lifecycle.addObserver(holder)
        return holder
    }

    override fun onBindViewHolder(holder: PagePreviewHolder, position: Int) {
        holder.disposable.clear()
        holder.bind(getItem(position)!!, fragmentManager)
    }

    override fun onViewRecycled(holder: PagePreviewHolder) {
        super.onViewRecycled(holder)
        holder.disposable.clear()
    }

    override fun onFailedToRecycleView(holder: PagePreviewHolder): Boolean {
        lifecycleOwner.lifecycle.removeObserver(holder)
        return super.onFailedToRecycleView(holder)
    }

}

class PagePreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), DefaultLifecycleObserver {

    override fun onPause(owner: LifecycleOwner) {
        Log.d("NpPerviewFragment","onPause: active = false for page:${mPage.name}")
        active = false
    }
    override fun onStop(owner: LifecycleOwner) {
        Log.d("NpPerviewFragment","onStop: active = false for page:${mPage.name}")
        active = false
    }
    override fun onDestroy(owner: LifecycleOwner) {
        Log.d("NpPerviewFragment","onDestroy: active = false for page:${mPage.name}")
        active = false
    }
    override fun onResume(owner: LifecycleOwner) {
        active = true
    }

    companion object {
        val TAG = "NpPerviewFragment"
    }

    val disposable = CompositeDisposable()
    private var active = true

    lateinit var mPage:Page

    init {
        itemView.id = View.generateViewId()
//        Log.d(TAG,"itemView.id: ${itemView.id}")
    }

    @SuppressLint("CheckResult")
    fun bind(page: Page, fragmentManager: FragmentManager) {

        mPage = page

        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(itemView.context)

        //Log.d(TAG,"itemView.id: ${itemView.id}, itemId: ${itemId}")

        disposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            //Log.d(TAG, "Start for page ${page.name} Np: ${page.newsPaperId}")
                            appSettingsRepository
                                    .getChildPagesForTopLevelPage(page)
                                    .asSequence()
                                    .filter { it.getHasData() }
                                    .sortedBy { it.id }
                                    .toCollection(mutableListOf<Page>())
                        }
                        .map {
                            if (it.size > 0) {
                                if (page.getHasData()) it.set(0, page)
                            } else {
                                if (page.getHasData()) it.add(page)
                            }
                            it
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<MutableList<Page>>() {
                            override fun onComplete() {
//                                Log.d(TAG, "onComplete for page ${page.name} Np: ${page.newsPaperId} L1")
                            }

                            override fun onNext(data: MutableList<Page>) {
//                                Log.d(TAG, "Display block for page ${page.name} Np: ${page.newsPaperId}")
                                try {
                                    val fragment = when {
                                        data.size == 1 -> {
                                            FragmentArticlePreviewForPages.getInstanceForScreenFillPreview(data[0])
                                        }
                                        data.size > 1 -> {
                                            FragmentArticlePreviewForPages.getInstanceForCustomWidthPreview((data).toList())
                                        }
                                        else -> {
                                            null
                                        }
                                    }
                                    //itemView.context.resources.getResourceName(itemView.id)
                                    if (fragment != null && active) {
                                        fragmentManager.beginTransaction().replace(itemView.id, fragment).commit()
//                                        fragmentManager.executePendingTransactions()
                                    }
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }

                            override fun onError(e: Throwable) {
//                                Log.d(TAG, "Error: " + e.message + " for page ${page.name} Np: ${page.newsPaperId} L1")
                            }
                        })
        )
    }
}
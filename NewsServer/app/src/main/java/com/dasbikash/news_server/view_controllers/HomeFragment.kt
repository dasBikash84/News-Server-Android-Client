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

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dasbikash.news_server.R
import com.dasbikash.news_server.custom_views.ViewPagerTitleScroller
import com.dasbikash.news_server.model.PagableNewsPaper
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.view_controllers.view_helpers.PageListAdapter
import com.dasbikash.news_server.view_controllers.view_helpers.PageViewHolder
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_init.*

class HomeFragment : Fragment() {

    private lateinit var mViewPagerTitleScroller: ViewPagerTitleScroller
    private lateinit var mHomeViewPager:ViewPager
    private lateinit var mPageSearchTextBox:EditText
    private lateinit var mPageSearchResultHolder:RecyclerView
    private lateinit var mPageSearchResultContainer:ViewGroup

    private var mSearchResultListAdapter = SearchResultListAdapter()

    private lateinit var mHomeViewModel: HomeViewModel

    private val mNewsPapers = mutableListOf<PagableNewsPaper>()

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private val MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH = 3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewPagerTitleScroller = view.findViewById(R.id.newspaper_name_scroller)
        mHomeViewPager = view.findViewById(R.id.home_view_pager)
        mPageSearchTextBox = view.findViewById(R.id.page_search_box_edit_text)
        mPageSearchResultHolder = view.findViewById(R.id.page_search_result_holder)
        mPageSearchResultContainer = view.findViewById(R.id.page_search_result_container)

        mPageSearchResultHolder.adapter = mSearchResultListAdapter
        mHomeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)

        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(this.context!!)

        data_load_progress.visibility = View.GONE //not to be displayed on this page

        val mFragmentStatePagerAdapter =  object : FragmentStatePagerAdapter(activity!!.supportFragmentManager){
            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }

            override fun getItem(position: Int): Fragment {
                return NewspaperPerviewFragment.getInstance(mNewsPapers.get(position).newspaper)
            }
            override fun getCount(): Int {
                return mNewsPapers.size
            }
        }

        mHomeViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                mViewPagerTitleScroller.setCurrentItem(mNewsPapers.get(position))
            }
        })

        mHomeViewModel
            .getNewsPapers()
            .observe(this,object : Observer<List<Newspaper>>{
                override fun onChanged(newspapers: List<Newspaper>?) {
                    mDisposable.add(
                        Observable.just(true)
                            .subscribeOn(Schedulers.computation())
                            .map {
                                newspapers
                                        ?.sortedBy { it.getPosition() }
                                        ?.map { PagableNewsPaper(it) }
                                        ?.forEach { mNewsPapers.add(it) }
                                return@map mNewsPapers
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<MutableList<PagableNewsPaper>>(){
                                override fun onComplete() {
                                }
                                override fun onNext(t: MutableList<PagableNewsPaper>) {

                                    mViewPagerTitleScroller.initView(mNewsPapers.toList(), R.layout.view_page_label) {
                                        LoggerUtils.debugLog( "${it.keyString} clicked",this::class.java)
                                        mHomeViewPager.setCurrentItem(mNewsPapers.indexOf(it),true)
                                    }
                                    mHomeViewPager.adapter = mFragmentStatePagerAdapter
                                    mHomeViewPager.setCurrentItem(0)

                                    splash_screen.visibility = View.GONE
                                    mViewPagerTitleScroller.visibility = View.VISIBLE
                                    mHomeViewPager.visibility = View.VISIBLE
                                    page_search_text_box_layout.visibility = View.VISIBLE

                                    mPageSearchTextBox.addTextChangedListener(object : TextWatcher{
                                        override fun afterTextChanged(text: Editable?) {
                                            text?.let {
                                                if (it.length >= MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH){
                                                    mDisposable.add(
                                                        Observable.just(it.trim().toString())
                                                                .subscribeOn(Schedulers.io())
                                                                .map {
                                                                    val pageList = appSettingsRepository.findMatchingPages(it)
                                                                    pageList.filter {
                                                                        @Suppress("SENSELESS_COMPARISON")
                                                                        it !=null
                                                                    }.toList()
                                                                }
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribeWith(object : DisposableObserver<List<Page>>(){
                                                                    override fun onComplete() {}
                                                                    override fun onNext(pageList: List<Page>) {
                                                                        mPageSearchResultContainer.visibility = View.VISIBLE
                                                                        mPageSearchResultContainer.bringToFront()
                                                                        mPageSearchResultContainer.setOnClickListener({
                                                                            mPageSearchResultContainer.visibility = View.GONE
                                                                        })
                                                                        mSearchResultListAdapter.submitList(pageList)
                                                                    }
                                                                    override fun onError(e: Throwable) {}

                                                                })
                                                    )
                                                }else{
                                                    mPageSearchResultContainer.visibility = View.GONE
                                                }
                                            }
                                        }
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                    })
                                }
                                override fun onError(e: Throwable) {
                                }
                            })
                    )
                }
            })
    }
}


class SearchResultListAdapter(): PageListAdapter<SearchResultEntryViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultEntryViewHolder {
        return SearchResultEntryViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.view_page_label,parent,false)
        )
    }
}
class SearchResultEntryViewHolder(itemView: View): PageViewHolder(itemView){
    override fun bind(page: Page, parentPage: Page?, newspaper: Newspaper) {
        val pageLabelBuilder = StringBuilder(page.name!!).append(" | ")
        parentPage?.let { pageLabelBuilder.append(it.name).append(" | ") }
        pageLabelBuilder.append(newspaper.name)
        (itemView as TextView).setText(pageLabelBuilder.toString())

        itemView.setOnClickListener {
            itemView.context
                .startActivity(PageViewActivity.getIntentForPageDisplay(itemView.context,page))
        }
    }
}


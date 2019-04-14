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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dasbikash.news_server.R
import com.dasbikash.news_server.custom_views.ViewPagerTitleScroller
import com.dasbikash.news_server.model.PagableNewsPaper
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.display_models.entity.Newspaper
import io.reactivex.disposables.CompositeDisposable

class HomeFragment : Fragment() {

    private lateinit var mViewPagerTitleScroller: ViewPagerTitleScroller

    private lateinit var mHomeViewModel: HomeViewModel

    private lateinit var mHomeViewPager:ViewPager

    private val mNewsPapers = mutableListOf<PagableNewsPaper>()

    private val mDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewPagerTitleScroller = view.findViewById(R.id.newspaper_name_scroller)
        mHomeViewPager = view.findViewById(R.id.home_view_pager)
        mHomeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)

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
                        newspapers
                                ?.map { PagableNewsPaper(it) }
                                ?.forEach { mNewsPapers.add(it) }

                        mViewPagerTitleScroller.initView(mNewsPapers.toList(), R.layout.view_page_label) {
                            Log.d(TAG, "${it.keyString} clicked")
                            mHomeViewPager.setCurrentItem(mNewsPapers.indexOf(it),true)
                        }
                        mHomeViewPager.adapter = mFragmentStatePagerAdapter
                        mHomeViewPager.setCurrentItem(0)

                    }

                })

    }

    override fun onPause() {
        super.onPause()
        mDisposable.clear()
    }


    companion object {
        val TAG = "HomeFragment"
    }
}

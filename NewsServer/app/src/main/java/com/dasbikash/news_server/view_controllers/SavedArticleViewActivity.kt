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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.view_models.SavedArticlesViewViewModel
import com.dasbikash.news_server_data.models.room_entity.SavedArticle
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.google.android.material.appbar.AppBarLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class SavedArticleViewActivity : AppCompatActivity() {

    private lateinit var mSavedArticlePager: ViewPager
    private lateinit var mFragmentStatePagerAdapter: FragmentStatePagerAdapter
    private lateinit var mToolbar: Toolbar
    private lateinit var mAppBar: AppBarLayout
    private var mTargetSavedArticleId: String? = null

    private val mSavedArticleList = mutableListOf<SavedArticle>()

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_article_view)

        mTargetSavedArticleId = intent.getStringExtra(EXTRA_TARGET_SAVED_ARTICLE_ID)!!

        LoggerUtils.debugLog(mTargetSavedArticleId!!, this::class.java)

        mSavedArticlePager = findViewById(R.id.saved_article_view_pager)
        mAppBar = findViewById(R.id.app_bar_layout)
        mToolbar = findViewById(R.id.app_bar)

        setSupportActionBar(mToolbar)

        mFragmentStatePagerAdapter = object : FragmentStatePagerAdapter(supportFragmentManager,
                FragmentStatePagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT) {

            override fun getItemPosition(fragment: Any) = PagerAdapter.POSITION_NONE
            override fun getItem(position: Int): Fragment {
                return SavedArticleViewFragment.getInstance(mSavedArticleList.get(position).id)
            }

            override fun getCount(): Int {
                return mSavedArticleList.size
            }
        }
        mSavedArticlePager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                invalidateOptionsMenu()
            }
        })

        mSavedArticlePager.adapter = mFragmentStatePagerAdapter

        ViewModelProviders.of(this).get(SavedArticlesViewViewModel::class.java).getSavedArticlesLiveData()
                .observe(this, object : Observer<List<SavedArticle>> {
                    override fun onChanged(savedArticles: List<SavedArticle>?) {
                        LoggerUtils.debugLog("onChanged", this@SavedArticleViewActivity::class.java)
                        savedArticles?.let {
                            mSavedArticleList.clear()
                            it.asSequence().sortedBy { it.newspaperName+it.pageName }.forEach {
                                LoggerUtils.debugLog(it.toString(), this@SavedArticleViewActivity::class.java)
                                mSavedArticleList.add(it)
                            }
                            mFragmentStatePagerAdapter.notifyDataSetChanged()
//                            mSavedArticlePager.setCurrentItem(0)
                            mTargetSavedArticleId?.let {
                                LoggerUtils.debugLog(it, this@SavedArticleViewActivity::class.java)
                                val index = mSavedArticleList.indexOf(mSavedArticleList.filter { it.id.equals(mTargetSavedArticleId) }.first())
                                LoggerUtils.debugLog("index:${index}", this@SavedArticleViewActivity::class.java)
                                mSavedArticlePager.setCurrentItem(index)
                                mTargetSavedArticleId = null
                            }
                        }
                    }
                })
    }

    fun deleteArticle(savedArticle: SavedArticle) {
//        LoggerUtils.debugLog("Need to delete: ${it}",this::class.java)
        if (mSavedArticleList.contains(savedArticle)) {
            mDisposable.add(
                    Observable.just(savedArticle)
                            .subscribeOn(Schedulers.io())
                            .map {
                                val newsDataRepository = RepositoryFactory.getNewsDataRepository(this)
                                LoggerUtils.debugLog("Need to delete: ${it}",this::class.java)
                                newsDataRepository.deleteSavedArticle(savedArticle)
                                it
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<SavedArticle>() {
                                override fun onComplete() {}
                                override fun onNext(item: SavedArticle) {
                                    if (mSavedArticleList.remove(item)){
                                        LoggerUtils.debugLog("${item} deleted",this@SavedArticleViewActivity::class.java)
                                    }
                                    mFragmentStatePagerAdapter.notifyDataSetChanged()
                                }
                                override fun onError(e: Throwable) {}
                            })
            )
        }
    }

    companion object {

        const val EXTRA_TARGET_SAVED_ARTICLE_ID = "com.dasbikash.news_server.view_controllers." +
                "SavedArticleViewActivity.EXTRA_TARGET_SAVED_ARTICLE_ID"

        fun getIntent(savedArticle: SavedArticle, context: Context): Intent {
            val intent = Intent(context, SavedArticleViewActivity::class.java)
            intent.putExtra(EXTRA_TARGET_SAVED_ARTICLE_ID, savedArticle.id)
            return intent
        }
    }
}




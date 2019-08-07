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
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.OnceSettableBoolean
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server.view_controllers.view_helpers.ArticlePreviewListAdapter
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.NewsCategory
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.appbar.AppBarLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class NewsCategoryArticleViewActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var mAppBar: AppBarLayout

    private lateinit var mWaitScreen: LinearLayoutCompat
    private lateinit var mArticlePreviewHolder: RecyclerView
    private lateinit var mArticlePreviewHolderAdapter: ArticlePreviewListAdapter
    private lateinit var mNewsCategory: NewsCategory
    private lateinit var mNewsCategoryId: String

    private val mArticleList = mutableListOf<Article>()
    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    private var mNewArticleLoadInProgress = false
    private var mEndReachedForArticles = OnceSettableBoolean()

    var mArticleRequestChunkSize = ARTICLE_LOAD_CHUNK_SIZE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_category_article_view)

        mAppBar = findViewById(R.id.app_bar_layout)
        mToolbar = findViewById(R.id.app_bar)

        setSupportActionBar(mToolbar)

        mNewsCategoryId = intent!!.getStringExtra(EXTRA_NEWS_CATEGORY_ID)!!

        findViewItems()
        setListners()
        init()
    }

    private fun init() {

        mArticlePreviewHolderAdapter = ArticlePreviewListAdapter({ doOnArticleClick(it) }, { loadMoreArticles() }, { showLoadingIfRequired() }, ARTICLE_LOAD_CHUNK_SIZE)
        mArticlePreviewHolder.adapter = mArticlePreviewHolderAdapter
    }

    private fun showLoadingIfRequired() {
        if (mNewArticleLoadInProgress) {
            showWaitScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        hideWaitScreen()
        mNewArticleLoadInProgress = false
        if (mArticleList.isEmpty()) {
            loadMoreArticles()
        }
    }

    private fun loadMoreArticles() {
        if (!mNewArticleLoadInProgress && !mEndReachedForArticles.get()) {
            mNewArticleLoadInProgress = true
            if (mArticleList.isEmpty()) {
                showWaitScreen()
            }
            var amDisposed = false
            mDisposable.add(
                    Observable.just(true)
                            .subscribeOn(Schedulers.io())
                            .map {
                                if (!::mNewsCategory.isInitialized) {
                                    mNewsCategory = RepositoryFactory.getAppSettingsRepository(this).findNewsCategoryById(mNewsCategoryId)!!
                                    supportActionBar!!.title = mNewsCategory.name
                                }
                                mNewsCategory
                            }
                            .map {
                                val newsDataRepository = RepositoryFactory.getNewsDataRepository(this)
                                val articles = mutableListOf<Article>()
                                try {
                                    if (mArticleList.isEmpty()) {
                                        articles.addAll(newsDataRepository.getLatestArticlesByNewsCategory(it, this, mArticleRequestChunkSize))
                                    } else {
                                        val lastArticle = mArticleList.sortedBy { it.publicationTime!! }.first()
                                        articles.addAll(newsDataRepository
                                                .getArticlesByNewsCategoryBeforeLastArticle(
                                                        it, lastArticle, this, mArticleRequestChunkSize))
                                    }
                                } catch (ex: Exception) {
                                    if (!amDisposed) {
                                        throw ex
                                    }
                                }
                                articles.toList()
                            }
                            .doOnDispose {
                                mNewArticleLoadInProgress = false
                                amDisposed = true
//                                debugLog("amDisposed = true")
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<List<Article>>() {
                                override fun onComplete() {
                                    mNewArticleLoadInProgress = false
                                    hideWaitScreen()
                                }

                                override fun onNext(newArticleList: List<Article>) {
                                    var articleAdditionCount = 0
                                    newArticleList.asSequence().forEach {
                                        val article = it
                                        if (mArticleList.count { it.checkIfSameArticle(article) } == 0) {
                                            articleAdditionCount++
                                            mArticleList.add(article)
                                        }
                                    }
                                    if (articleAdditionCount > 0) {
                                        mArticleRequestChunkSize = ARTICLE_LOAD_CHUNK_SIZE
                                        mArticlePreviewHolderAdapter.submitList(mArticleList.toList())
                                    } else {
                                        mNewArticleLoadInProgress = false
                                        mArticleRequestChunkSize += ARTICLE_LOAD_CHUNK_SIZE
                                        Handler(Looper.getMainLooper()).postAtTime({ loadMoreArticles() }, 1000L)
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    mNewArticleLoadInProgress = false
                                    hideWaitScreen()
                                    LoggerUtils.printStackTrace(e)
                                    when (e) {
                                        is NoInternertConnectionException -> {
                                            NetConnectivityUtility.showNoInternetToast(this@NewsCategoryArticleViewActivity)
                                        }
                                        is DataServerException -> {
                                            mEndReachedForArticles.set()
                                        }
                                        else -> {

                                        }
                                    }
                                }
                            }))
        }
    }

    private fun doOnArticleClick(article: Article) {
        debugLog(article.toString())
        startActivity(ArticleViewActivity.getIntentForArticleView(this,article))
    }

    private fun setListners() {
    }

    private fun findViewItems() {
        mArticlePreviewHolder = findViewById(R.id.article_preview_holder)
        mWaitScreen = findViewById(R.id.wait_screen_for_settings_change)
    }

    private fun showWaitScreen() {
        mWaitScreen.visibility = View.VISIBLE
        mWaitScreen.bringToFront()
    }

    private fun hideWaitScreen() {
        mWaitScreen.visibility = View.GONE
    }

    companion object {
        private const val ARTICLE_LOAD_CHUNK_SIZE = 10
        private const val EXTRA_NEWS_CATEGORY_ID =
                "com.dasbikash.news_server.view_controllers.NewsCategoryArticleViewActivity.EXTRA_NEWS_CATEGORY_ID"

        fun getIntentForNewsCategory(context: Context, newsCategory: NewsCategory): Intent {
            val intent = Intent(context, NewsCategoryArticleViewActivity::class.java)
            intent.putExtra(EXTRA_NEWS_CATEGORY_ID, newsCategory.id)
            return intent
        }
    }
}

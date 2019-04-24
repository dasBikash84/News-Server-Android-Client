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
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.OnceSettableBoolean
import com.dasbikash.news_server.views.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server.views.view_helpers.ArticlePreviewListAdapter
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerNotAvailableExcepption
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class PageViewActivity : AppCompatActivity(),
        SignInHandler, WorkInProcessWindowOperator {


    companion object {
        const val TAG = "PageViewActivity"
        const val LOG_IN_REQ_CODE = 7777
        const val EXTRA_PAGE_TO_DISPLAY = "com.dasbikash.news_server.views.PageViewActivity.EXTRA_PAGE_TO_DISPLAY"

        fun getIntentForPageDisplay(context: Context, page: Page): Intent {
            val intent = Intent(context, PageViewActivity::class.java)
            intent.putExtra(EXTRA_PAGE_TO_DISPLAY, page)
            return intent
        }
    }

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mArticleLoadingProgressBarDrawerHolder: ConstraintLayout
    private lateinit var mArticlePreviewListHolder: RecyclerView
    private lateinit var mLoadMoreArticleButton: Button

    private lateinit var mPageViewContainer: CoordinatorLayout
    private lateinit var mArticleViewContainer: ViewPager
    private lateinit var mFragmentStatePagerAdapter: FragmentStatePagerAdapter
    private lateinit var mArticleLoadingProgressBarMiddle: ProgressBar

    private lateinit var mArticlePreviewListAdapter: ArticlePreviewListAdapter

    private lateinit var mAppSettingsRepository: AppSettingsRepository
    private lateinit var mUserSettingsRepository: UserSettingsRepository
    private lateinit var mNewsDataRepository: NewsDataRepository

    private lateinit var mPage: Page
    private lateinit var mNewspaper: Newspaper
    private lateinit var mLanguage: Language

    private var mIsPageOnFavList = false

    private val disposable = CompositeDisposable()
    private val mArticleList = mutableListOf<Article>()

    private val MINIMUM_INIT_ARTICLE_COUNT = 5
    private val ARTICLE_DOWNLOAD_CHUNCK_SIZE = 5

    private val mHaveMoreArticle = OnceSettableBoolean()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_view)

        @Suppress("CAST_NEVER_SUCCEEDS")
        mPage = (intent!!.getParcelableExtra(EXTRA_PAGE_TO_DISPLAY)) as Page

        initDrawerComponents()
        mPageViewContainer = findViewById(R.id.page_view_container)
        mArticleViewContainer = findViewById(R.id.article_view_container)
        mArticleLoadingProgressBarMiddle = findViewById(R.id.article_loading_progress_bar_middle)

        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(this)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(this)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(this)
        mArticleLoadingProgressBarMiddle.setOnClickListener { }

        initViewPager()

        loadMoreArticles()
        supportActionBar!!.setTitle(mPage.name)
    }

    private fun initViewPager() {

        mFragmentStatePagerAdapter = object : FragmentStatePagerAdapter(supportFragmentManager){
            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_UNCHANGED
            }
            override fun getItem(position: Int): Fragment {
                if (position == (mArticleList.size - 1)){
                    loadMoreArticles()
                }
                return ArticleViewFragment.getInstance(mArticleList.get(position).id,mLanguage,mArticleList.size,position+1)
            }
            override fun getCount(): Int {
                return mArticleList.size
            }
        }

        mArticleViewContainer.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
//                mArticlePreviewListHolder.scrollToPosition(position)
            }
        })
        mArticleViewContainer.adapter = mFragmentStatePagerAdapter
        mArticleViewContainer.setCurrentItem(0)
    }

    fun getArticleDownloaderObservable(): Observable<List<Article>> {
        return Observable.just(mPage)
                .subscribeOn(Schedulers.io())
                .map {
                    if (!::mNewspaper.isInitialized) {
                        mNewspaper = mAppSettingsRepository.getNewspaperByPage(mPage)
                    }
                    if (!::mLanguage.isInitialized) {
                        mLanguage = mAppSettingsRepository.getLanguageByPage(mPage)
                    }
                    var lastArticleId: String? = null
                    synchronized(mArticleList) {
                        if (!mArticleList.isEmpty()) {
                            lastArticleId = mArticleList.last().id
                            Log.d(TAG, "lastArticleId: ${lastArticleId}")
                        }
                    }
                    mNewsDataRepository.downloadArticlesByPage(mPage, lastArticleId)
                }
    }

    fun loadMoreArticles() {

        if (!mHaveMoreArticle.get()) {

            loadWorkInProcessWindow()//showProgressBars()

            disposable.add(
                    getArticleDownloaderObservable()
                            .map {
                                val newList = mNewsDataRepository.getArticlesByPage(mPage)
                                Log.d(TAG, "newList: ${newList.size}")
                                newList
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<List<Article>>() {
                                override fun onComplete() {
                                    removeWorkInProcessWindow()//hideProgressBars()
                                }

                                override fun onNext(articleList: List<Article>) {
                                    postNewArticlesForDisplay(articleList)
                                }

                                override fun onError(throwable: Throwable) {
                                    removeWorkInProcessWindow()//hideProgressBars()
                                    when {
                                        throwable is DataNotFoundException -> {
                                            mHaveMoreArticle.set()
                                            mLoadMoreArticleButton.visibility = View.GONE
                                            showShortSnack("No more articles to display.")
                                        }
                                        throwable is DataServerNotAvailableExcepption -> {
                                            showShortSnack("Remote server error! Please try again later.")
                                        }
                                        throwable is NoInternertConnectionException -> {
                                            showShortSnack("No internet connection!!!")
                                        }
                                        else -> {
                                            throw throwable
                                        }
                                    }
                                }
                            })
            )
        }
    }

    private fun hideProgressBars() {
        mArticleLoadingProgressBarDrawerHolder.visibility = View.GONE
        mArticleLoadingProgressBarMiddle.visibility = View.GONE
    }

    private fun showProgressBars() {
        mArticleLoadingProgressBarDrawerHolder.visibility = View.VISIBLE
        mArticleLoadingProgressBarMiddle.visibility = View.VISIBLE

        mArticleLoadingProgressBarDrawerHolder.bringToFront()
        mArticleLoadingProgressBarMiddle.bringToFront()
    }

    private fun postNewArticlesForDisplay(articleList: List<Article>) {
        mArticleList.clear()
        mArticleList.addAll(articleList)
        if (!::mArticlePreviewListAdapter.isInitialized) {
            mArticlePreviewListAdapter = ArticlePreviewListAdapter(mLanguage,{
                mArticleViewContainer.currentItem = it
            })
            mArticlePreviewListHolder.adapter = mArticlePreviewListAdapter
        }
        mArticlePreviewListAdapter.submitList(mArticleList.toList())
        if (!::mFragmentStatePagerAdapter.isInitialized){
            initViewPager()
        }else{
            mFragmentStatePagerAdapter.notifyDataSetChanged()
        }
    }

    fun showShortSnack(message: String) {
        Snackbar
                .make(mPageViewContainer, message, Snackbar.LENGTH_SHORT)
                .show()
    }

    private fun initDrawerComponents() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close)
        mDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        findViewById<NavigationView>(R.id.nav_view).setOnClickListener(View.OnClickListener { closeNavigationDrawer() })

        mArticleLoadingProgressBarDrawerHolder = mDrawerLayout.findViewById(R.id.article_loading_progress_bar_drawer_holder)
        mArticlePreviewListHolder = mDrawerLayout.findViewById(R.id.article_preview_list_holder)
        mLoadMoreArticleButton = mDrawerLayout.findViewById(R.id.load_more_articel_button)

        mArticleLoadingProgressBarDrawerHolder.setOnClickListener { }
        mLoadMoreArticleButton.setOnClickListener { loadMoreArticles() }
    }

    fun closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onResume() {
        super.onResume()
        refreshFavStatus()
    }

    private fun refreshFavStatus(doOnNext:()->Unit = {}) {
        disposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            mIsPageOnFavList = mUserSettingsRepository.checkIfOnFavList(mPage)
                            mIsPageOnFavList
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {}
                            override fun onNext(isFav: Boolean) {
                                invalidateOptionsMenu()
                                doOnNext()
                            }

                            override fun onError(e: Throwable) {}
                        })
        )
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (!mWaitWindowShown) {
                super.onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_page_view_activity, menu)

        menu.findItem(R.id.add_to_favourites_menu_item).setVisible(!mIsPageOnFavList)
        menu.findItem(R.id.remove_from_favourites_menu_item).setVisible(mIsPageOnFavList)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (!mWaitWindowShown) {
            return when (item.itemId) {
                R.id.add_to_favourites_menu_item -> {
                    addPageToFavListAction()
                    true
                }
                R.id.remove_from_favourites_menu_item -> {
                    removePageFromFavListAction()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return false
    }

    private enum class PAGE_FAV_STATUS_CHANGE_ACTION {
        ADD, REMOVE
    }

    private fun changePageFavStatus(action: PAGE_FAV_STATUS_CHANGE_ACTION) {

        val dialogMessage: String

        when (action) {
            PAGE_FAV_STATUS_CHANGE_ACTION.ADD -> dialogMessage = "Add \"${mPage.name}\" to favourites?"
            PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> dialogMessage = "Remove \"${mPage.name}\" from favourites?"
        }

        val positiveActionText = "Yes"
        val negetiveActionText = "Cancel"

        val positiveAction: () -> Unit = {
            loadWorkInProcessWindow()
            disposable.add(
                    Observable.just(mPage)
                            .subscribeOn(Schedulers.io())
                            .map {
                                when (action) {
                                    PAGE_FAV_STATUS_CHANGE_ACTION.ADD -> return@map mUserSettingsRepository.addPageToFavList(mPage, this)
                                    PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> return@map mUserSettingsRepository.removePageFromFavList(mPage, this)
                                }
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<Boolean>() {
                                override fun onComplete() {
                                    removeWorkInProcessWindow()
                                }
                                override fun onNext(result: Boolean) {
                                    if (result) {
                                        when (action) {
                                            PAGE_FAV_STATUS_CHANGE_ACTION.ADD -> {
                                                mIsPageOnFavList = true
                                                showShortSnack("${mPage.name} added to favourites")
                                            }
                                            PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> {
                                                mIsPageOnFavList = false
                                                showShortSnack("${mPage.name} removed from favourites")
                                            }
                                        }
                                        invalidateOptionsMenu()
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    showShortSnack("Error!! Please retry.")
                                    removeWorkInProcessWindow()
                                }
                            })
            )
        }

        val changeFavStatusDialog =
                DialogUtils.createAlertDialog(
                        this,
                        DialogUtils.AlertDialogDetails(
                                message = dialogMessage,
                                positiveButtonText = positiveActionText,
                                negetiveButtonText = negetiveActionText,
                                doOnPositivePress = positiveAction
                        )
                )

        if (mUserSettingsRepository.checkIfLoggedIn()) {
            changeFavStatusDialog.show()
        } else {
            DialogUtils.createAlertDialog(
                    this,
                    DialogUtils.AlertDialogDetails(
                            message = dialogMessage,
                            positiveButtonText = "Sign in and continue",
                            negetiveButtonText = "Cancel",
                            doOnPositivePress = {
                                launchSignInActivity({
                                    refreshFavStatus({changeFavStatusDialog.show()})
                                })
                            }
                    )
            ).show()
        }

    }

    private fun removePageFromFavListAction() =
            changePageFavStatus(PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE)

    private fun addPageToFavListAction() =
            changePageFavStatus(PAGE_FAV_STATUS_CHANGE_ACTION.ADD)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOG_IN_REQ_CODE) {
            loadWorkInProcessWindow()
            Observable.just(Pair(resultCode, data))
                    .subscribeOn(Schedulers.io())
                    .map { mUserSettingsRepository.processSignInRequestResult(it, this) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Pair<UserSettingsRepository.SignInResult, Throwable?>> {
                        override fun onComplete() {
                            actionAfterSuccessfulLogIn = null
                            removeWorkInProcessWindow()
                        }

                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(processingResult: Pair<UserSettingsRepository.SignInResult, Throwable?>) {
                            when (processingResult.first) {
                                UserSettingsRepository.SignInResult.SUCCESS -> {
                                    showShortSnack("Welcome ${mUserSettingsRepository.getCurrentUserName()
                                            ?: ""}")
                                    actionAfterSuccessfulLogIn?.let {
                                        it()
                                    }
                                }
                                UserSettingsRepository.SignInResult.USER_ABORT -> showShortSnack("Log in aborted")
                                UserSettingsRepository.SignInResult.SERVER_ERROR -> showShortSnack("Log in error. Details:${processingResult.second}")
                                UserSettingsRepository.SignInResult.SETTINGS_UPLOAD_ERROR -> showShortSnack("Error while User settings data saving. Details:${processingResult.second}")
                            }
                        }

                        override fun onError(e: Throwable) {
                            actionAfterSuccessfulLogIn = null
                            showShortSnack("Error while User settings data saving. Error: ${e}")
                            removeWorkInProcessWindow()
                        }
                    })
        }
    }

    var actionAfterSuccessfulLogIn: (() -> Unit)? = null

    override fun launchSignInActivity(doOnSignIn: () -> Unit) {
        val intent = mUserSettingsRepository.getLogInIntent()
        intent?.let {
            startActivityForResult(intent, LOG_IN_REQ_CODE)
        }
        actionAfterSuccessfulLogIn = doOnSignIn
    }

    var mWaitWindowShown = false
    override fun loadWorkInProcessWindow() {
        showProgressBars()
        mWaitWindowShown = true
    }

    override fun removeWorkInProcessWindow() {
        mWaitWindowShown = false
        hideProgressBars()
    }
}


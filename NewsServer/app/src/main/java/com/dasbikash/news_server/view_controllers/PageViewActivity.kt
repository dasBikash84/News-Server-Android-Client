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
import android.os.SystemClock
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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.OnceSettableBoolean
import com.dasbikash.news_server.view_controllers.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server.view_controllers.view_helpers.ArticlePreviewListAdapter
import com.dasbikash.news_server.view_models.PageViewViewModel
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerNotAvailableExcepption
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class PageViewActivity : AppCompatActivity(),
        SignInHandler, WorkInProcessWindowOperator {

    companion object {
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

    private lateinit var mTextSizeChangeFrame: ConstraintLayout//text_size_change_frame
    private lateinit var mTextSizeChangeCancelButton: MaterialButton//cancel_text_size_change
    private lateinit var mTextSizeChangeOkButton: MaterialButton//plus_text_size_change
    private lateinit var mTextSizeChangePlusButton: MaterialButton//minus_text_size_change
    private lateinit var mTextSizeChangeMinusButton: MaterialButton//ok_text_size_change

    private var transTextSize = 0
    private val mTextSizeChangeStep = 1

    private lateinit var mAppSettingsRepository: AppSettingsRepository
    private lateinit var mUserSettingsRepository: UserSettingsRepository
    private lateinit var mNewsDataRepository: NewsDataRepository

    private lateinit var mPage: Page
    private lateinit var mLanguage: Language

    private var mIsPageOnFavList = false
    private var mArticleLoadRunning = false
    private var mWaitWindowShown = false

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)
    private val mArticleList = mutableListOf<Article>()

    private val mInvHaveMoreArticle = OnceSettableBoolean()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_view)

        @Suppress("CAST_NEVER_SUCCEEDS")
        mPage = (intent!!.getParcelableExtra(EXTRA_PAGE_TO_DISPLAY)) as Page

        initDrawerComponents()
        initViewPager()
        initTextChangeViewComponents()

        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(this)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(this)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(this)

        supportActionBar!!.setTitle(mPage.name)

        ViewModelProviders.of(this).get(PageViewViewModel::class.java)
                .getArticleLiveDataForPage(mPage)
                .observe(this,object : androidx.lifecycle.Observer<List<Article>>{
                    override fun onChanged(articleList: List<Article>?) {
                        articleList?.let {
                            postArticlesForDisplay(articleList)
                        }
                    }
                })
        init()

    }

    private fun init(){
        Observable.just(mPage)
                .subscribeOn(Schedulers.io())
                .map {
                    if (!::mLanguage.isInitialized) {
                        mLanguage = mAppSettingsRepository.getLanguageByPage(mPage)
                    }
                }
                .subscribe()
    }

    override fun onResume() {
        super.onResume()
        refreshFavStatus()
        loadMoreArticles()
    }

    override fun onPause() {
        super.onPause()
        removeWorkInProcessWindow()
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_page_view_activity, menu)

        menu.findItem(R.id.add_to_favourites_menu_item).setVisible(!mIsPageOnFavList)
        menu.findItem(R.id.remove_from_favourites_menu_item).setVisible(mIsPageOnFavList)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
                R.id.change_text_font_menu_item -> {
                    changeTextFontAction()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return false
    }

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
                                    DisplayUtils.showShortSnack(mPageViewContainer,
                                            "Welcome ${mUserSettingsRepository.getCurrentUserName() ?: ""}")
                                    actionAfterSuccessfulLogIn?.let {it()}
                                }
                                UserSettingsRepository.SignInResult.USER_ABORT -> DisplayUtils.showShortSnack(mPageViewContainer,"Log in aborted")
                                UserSettingsRepository.SignInResult.SERVER_ERROR -> DisplayUtils.showShortSnack(mPageViewContainer,"Log in error.")//* Details:${processingResult.second}*/")
                                UserSettingsRepository.SignInResult.SETTINGS_UPLOAD_ERROR -> DisplayUtils.showShortSnack(mPageViewContainer,"Log in error.")//"Error while User settings data saving. Details:${processingResult.second}")
                            }
                        }

                        override fun onError(e: Throwable) {
                            actionAfterSuccessfulLogIn = null
                            DisplayUtils.showShortSnack(mPageViewContainer,"Log in error.")//"Error while User settings data saving. Error: ${e}")
                            removeWorkInProcessWindow()
                        }
                    })
        }
    }

    private fun initTextChangeViewComponents() {
        mTextSizeChangeFrame = findViewById(R.id.text_size_change_frame)
        mTextSizeChangeCancelButton = findViewById(R.id.cancel_text_size_change)
        mTextSizeChangePlusButton = findViewById(R.id.plus_text_size_change)
        mTextSizeChangeMinusButton = findViewById(R.id.minus_text_size_change)
        mTextSizeChangeOkButton = findViewById(R.id.ok_text_size_change)

        mTextSizeChangeCancelButton.setOnClickListener { changeTextFontAction() }
        mTextSizeChangePlusButton.setOnClickListener { incrementTextSize() }
        mTextSizeChangeMinusButton.setOnClickListener { decrementTextSize() }
        mTextSizeChangeOkButton.setOnClickListener { makeTextSizeEffective() }
    }

    private fun initViewPager() {

        mPageViewContainer = findViewById(R.id.page_view_container)
        mArticleViewContainer = findViewById(R.id.article_view_container)
        mArticleLoadingProgressBarMiddle = findViewById(R.id.article_loading_progress_bar_middle)
        mArticleLoadingProgressBarMiddle.setOnClickListener { }

        mFragmentStatePagerAdapter = object : FragmentStatePagerAdapter(supportFragmentManager,
                FragmentStatePagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT) {
            override fun getItemPosition(fragment: Any): Int {
                if (mTextSizeChangeFrame.visibility == View.GONE) {
                    return PagerAdapter.POSITION_UNCHANGED
                } else {
                    return PagerAdapter.POSITION_NONE
                }
            }

            override fun getItem(position: Int): Fragment {
                if (position == (mArticleList.size - 1)) {
                    loadMoreArticles()
                }
                val fragment = ArticleViewFragment.getInstance(
                        mArticleList.get(position).id, mLanguage, transTextSize)
                return fragment
            }

            override fun getCount(): Int {
                return mArticleList.size
            }
        }
        mArticleViewContainer.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                invalidateOptionsMenu()
            }
        })
        mArticleViewContainer.adapter = mFragmentStatePagerAdapter
        mArticleViewContainer.setCurrentItem(0)
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

    private fun makeTextSizeEffective() {
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            DisplayUtils.setArticleTextSize(this, transTextSize)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { changeTextFontAction() }
        )
    }

    private fun decrementTextSize() {
        if (transTextSize - mTextSizeChangeStep < DisplayUtils.MIN_ARTICLE_TEXT_SIZE) {
            transTextSize = DisplayUtils.MIN_ARTICLE_TEXT_SIZE
        } else {
            transTextSize -= mTextSizeChangeStep
        }
        mFragmentStatePagerAdapter.notifyDataSetChanged()
    }

    private fun incrementTextSize() {
        if (transTextSize + mTextSizeChangeStep > DisplayUtils.MAX_ARTICLE_TEXT_SIZE) {
            transTextSize = DisplayUtils.MAX_ARTICLE_TEXT_SIZE
        } else {
            transTextSize += mTextSizeChangeStep
        }
        mFragmentStatePagerAdapter.notifyDataSetChanged()
    }

    private fun changeTextFontAction() {
        if (mTextSizeChangeFrame.visibility == View.GONE) {
            mDisposable.add(
                    Observable.just(true)
                            .subscribeOn(Schedulers.computation())
                            .map {
                                DisplayUtils.getArticleTextSize(this)
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                transTextSize = it
                                mTextSizeChangeFrame.visibility = View.VISIBLE
                                mTextSizeChangeFrame.bringToFront()
                            }
            )
        } else {
            transTextSize = 0
            mFragmentStatePagerAdapter.notifyDataSetChanged()
            mDisposable.add(
                    Observable.just(true)
                            .subscribeOn(Schedulers.computation())
                            .map {
                                SystemClock.sleep(100)
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { mTextSizeChangeFrame.visibility = View.GONE }
            )
        }
    }

    private fun loadMoreArticles() {

        if (mArticleLoadRunning) return

        if (!mInvHaveMoreArticle.get()) {

            loadWorkInProcessWindow()//showProgressBars()
            mArticleLoadRunning = true
            mDisposable.add(
                    Observable.just(mPage)
                            .subscribeOn(Schedulers.io())
                            .map {
                                if(!::mLanguage.isInitialized) {
                                    mLanguage = mAppSettingsRepository.getLanguageByPage(mPage)
                                }
                                if (mArticleList.isEmpty()){
                                    mNewsDataRepository.getLatestArticleByPage(mPage)
                                }
                                mNewsDataRepository.downloadMoreArticlesByPage(mPage)
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<List<Article>>() {
                                override fun onComplete() {
                                    mArticleLoadRunning = false
                                    removeWorkInProcessWindow()//hideProgressBars()
                                }
                                override fun onNext(articleList: List<Article>) {}
                                override fun onError(throwable: Throwable) {
                                    mArticleLoadRunning = false
                                    removeWorkInProcessWindow()//hideProgressBars()
                                    when(throwable) {
                                         is DataNotFoundException -> {
                                            mInvHaveMoreArticle.set()
                                            mLoadMoreArticleButton.visibility = View.GONE
                                        }
                                        is DataServerNotAvailableExcepption -> {
                                            DisplayUtils.showShortSnack(mPageViewContainer,"Remote server error! Please try again later.")
                                        }
                                        is NoInternertConnectionException -> {
                                            NetConnectivityUtility.showNoInternetToast(this@PageViewActivity)
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

    private fun postArticlesForDisplay(articleList: List<Article>) {
        mArticleList.clear()
        mArticleList.addAll(articleList)
        if (!::mArticlePreviewListAdapter.isInitialized) {
            mArticlePreviewListAdapter = ArticlePreviewListAdapter(mLanguage, {
                mArticleViewContainer.currentItem = it
            })
            mArticlePreviewListHolder.adapter = mArticlePreviewListAdapter
        }
        mArticlePreviewListAdapter.submitList(mArticleList.toList())
        if (!::mFragmentStatePagerAdapter.isInitialized) {
            initViewPager()
        } else {
            mFragmentStatePagerAdapter.notifyDataSetChanged()
        }
    }

    private fun closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun refreshFavStatus(doOnNext: () -> Unit = {}) {
        mDisposable.add(
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
            mDisposable.add(
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
                                                DisplayUtils.showShortSnack(mPageViewContainer,"${mPage.name} added to favourites")
                                            }
                                            PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> {
                                                mIsPageOnFavList = false
                                                DisplayUtils.showShortSnack(mPageViewContainer,"${mPage.name} removed from favourites")
                                            }
                                        }
                                        invalidateOptionsMenu()
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    if (e is NoInternertConnectionException) {
                                        NetConnectivityUtility.showNoInternetToast(this@PageViewActivity)
                                    }else {
                                        DisplayUtils.showShortSnack(mPageViewContainer, "Error!! Please retry.")
                                    }
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
                                    refreshFavStatus({ changeFavStatusDialog.show() })
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

    var actionAfterSuccessfulLogIn: (() -> Unit)? = null

    override fun launchSignInActivity(doOnSignIn: () -> Unit) {
        val intent = mUserSettingsRepository.getLogInIntent()
        intent?.let {
            startActivityForResult(intent, LOG_IN_REQ_CODE)
        }
        actionAfterSuccessfulLogIn = doOnSignIn
    }
    override fun loadWorkInProcessWindow() {
        if (!mWaitWindowShown) {
            showProgressBars()
            mWaitWindowShown = true
        }
    }

    override fun removeWorkInProcessWindow() {
        if(mWaitWindowShown) {
            mWaitWindowShown = false
            hideProgressBars()
        }
    }

    private enum class PAGE_FAV_STATUS_CHANGE_ACTION {
        ADD, REMOVE
    }
}


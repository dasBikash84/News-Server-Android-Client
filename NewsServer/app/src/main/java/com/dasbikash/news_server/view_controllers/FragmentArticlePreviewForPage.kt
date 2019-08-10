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

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.*
import com.dasbikash.news_server.view_controllers.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server.view_controllers.view_helpers.ArticlePreviewListAdapter
import com.dasbikash.news_server.view_models.PageViewViewModel
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentArticlePreviewForPage : Fragment(), SignInHandler, WorkInProcessWindowOperator {

    companion object {
        private val BOTTOM_LOADING_SCREEN_ENABLER_COUNT = 3
        private const val ARTICLE_LOAD_CHUNK_SIZE = 10
        const val LOG_IN_REQ_CODE = 7777
        const val ARG_FOR_PAGE = "com.dasbikash.news_server.views.FragmentArticlePreviewForPage.ARG_FOR_PAGE"
        const val ARG_FOR_PURPOSE = "com.dasbikash.news_server.views.FragmentArticlePreviewForPage.ARG_FOR_PURPOSE"
        const val ARG_VALUE_FOR_LATEST_ARTICLE_DISPLAY = "com.dasbikash.news_server.views.FragmentArticlePreviewForPage.LATEST_ARTICLE_DISPLAY"
        const val ARG_VALUE_FOR_PAGE_BROWSING = "com.dasbikash.news_server.views.FragmentArticlePreviewForPage.PAGE_BROWSING"

        fun getInstanceForLatestArticleDisplay(page: Page): FragmentArticlePreviewForPage {
            val args = Bundle()
            args.putParcelable(ARG_FOR_PAGE, page)
            args.putString(ARG_FOR_PURPOSE, ARG_VALUE_FOR_LATEST_ARTICLE_DISPLAY)
            val fragment = FragmentArticlePreviewForPage()
            fragment.setArguments(args)
            return fragment
        }

        fun getInstancePageBrowsing(page: Page): FragmentArticlePreviewForPage {
            val args = Bundle()
            args.putParcelable(ARG_FOR_PAGE, page)
            args.putString(ARG_FOR_PURPOSE, ARG_VALUE_FOR_PAGE_BROWSING)
            val fragment = FragmentArticlePreviewForPage()
            fragment.setArguments(args)
            return fragment
        }
    }

    private lateinit var mPurposeString: String

    private lateinit var mPage: Page
    private lateinit var mLanguage: Language
    private lateinit var mNewspaper: Newspaper

    private lateinit var mCenterLoadingScreen: LinearLayoutCompat
    private lateinit var mBottomLoadingScreen: ProgressBar
    private lateinit var mArticlePreviewHolder: RecyclerView
    private lateinit var mArticlePreviewHolderAdapter: ArticlePreviewListAdapter

    private var mActionBarHeight = 0

    private lateinit var mPageViewContainer: CoordinatorLayout

    private var mHasJumpedToLatestArticle = OnceSettableBoolean()

    private var mIsPageOnFavList = false

    private lateinit var mAppSettingsRepository: AppSettingsRepository
    private lateinit var mUserSettingsRepository: UserSettingsRepository
    private lateinit var mNewsDataRepository: NewsDataRepository

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)
    private val mArticleList = mutableListOf<Article>()
    private val mArticleWithParentsList = mutableListOf<ArticleWithParents>()

    private val mInvHaveMoreArticle = OnceSettableBoolean()
    private var mArticleLoadRunning = false

    var mArticleRequestChunkSize = ARTICLE_LOAD_CHUNK_SIZE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoggerUtils.debugLog("onCreate", this::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LoggerUtils.debugLog("onCreateView", this::class.java)
        return inflater.inflate(R.layout.fragment_article_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoggerUtils.debugLog("onViewCreated", this::class.java)
        mPage = (arguments!!.getParcelable(ARG_FOR_PAGE)) as Page
        mPurposeString = arguments!!.getString(ARG_FOR_PURPOSE)!!

        mActionBarHeight = DisplayUtils.dpToPx(40,context!!).toInt()

        findViewItems(view)
        setListners()
        init()

        ViewModelProviders.of(activity!!).get(PageViewViewModel::class.java)
                .getUserPreferenceLiveData()
                .observe(this, object : androidx.lifecycle.Observer<UserPreferenceData?> {
                    override fun onChanged(userPreferenceData: UserPreferenceData?) {
                        userPreferenceData?.let {
                            it.favouritePageIds.asSequence().forEach {
                                if (it.equals(mPage.id)) {
                                    mIsPageOnFavList = true
                                    return@let
                                }
                            }
                            mIsPageOnFavList = false
                        }
                        activity!!.invalidateOptionsMenu()
                    }
                })
    }

    private fun findViewItems(view: View) {
        mArticlePreviewHolder = view.findViewById(R.id.article_preview_holder)
        mCenterLoadingScreen = view.findViewById(R.id.center_loading_screen)
        mPageViewContainer = view.findViewById(R.id.page_view_container)
        mBottomLoadingScreen = view.findViewById(R.id.bottom_loading_screen)
    }

    private fun init() {
        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)

        hideLoadingScreens()
        mArticlePreviewHolderAdapter = ArticlePreviewListAdapter({ doOnArticleClick(it) }, { loadMoreArticles() }, { showLoadingIfRequired() }, ARTICLE_LOAD_CHUNK_SIZE)
        mArticlePreviewHolder.adapter = mArticlePreviewHolderAdapter
    }
    private var mArticlePreviewHolderDySum = 0

    private fun setListners() {

        mCenterLoadingScreen.setOnClickListener {  }
        mArticlePreviewHolder.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                mArticlePreviewHolderDySum += dy

                if (recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                    if (dy < 0){
                        if ( mArticlePreviewHolderDySum  == 0) {
                            (activity!! as AppCompatActivity).supportActionBar!!.show()
                        }
                    }else{
                        if (mArticlePreviewHolderDySum > mActionBarHeight) {
                            (activity!! as AppCompatActivity).supportActionBar!!.hide()
                        }
                    }
                }
            }
        })
    }

    private fun showLoadingIfRequired() {
        if (mArticleLoadRunning) {
            showLoadingScreen()
        }
    }

    private fun showLoadingScreen() {
        if (mArticleList.size > BOTTOM_LOADING_SCREEN_ENABLER_COUNT){
            showBottomLoadingScreen()
        }else{
            showCenterLoadingScreen()
        }
    }

    private fun showCenterLoadingScreen() {
        mBottomLoadingScreen.visibility = View.GONE
        mCenterLoadingScreen.visibility = View.VISIBLE
        mCenterLoadingScreen.bringToFront()
    }

    private fun showBottomLoadingScreen() {
        mCenterLoadingScreen.visibility = View.GONE
        mBottomLoadingScreen.visibility = View.VISIBLE
        mBottomLoadingScreen.bringToFront()
    }

    private fun hideLoadingScreens() {
        mCenterLoadingScreen.visibility = View.GONE
        mBottomLoadingScreen.visibility = View.GONE
    }

    private fun doOnArticleClick(article: Article) {
        debugLog(article.toString())
        startActivity(ActivityArticleView.getIntentForArticleView(context!!, article))
    }

    private fun fragmentForPageBrowsing() =
            mPurposeString.equals(ARG_VALUE_FOR_PAGE_BROWSING)

    override fun onResume() {
        super.onResume()
        debugLog("onResume()")
        mDisposable.add(
                Observable.just(mPage)
                        .subscribeOn(Schedulers.io())
                        .map {
                            debugLog("inside map")
                            if (!::mNewspaper.isInitialized) {
                                mNewspaper = mAppSettingsRepository.getNewspaperByPage(it)
                            }
                            if (!::mLanguage.isInitialized) {
                                mLanguage = mAppSettingsRepository.getLanguageByNewspaper(mNewspaper)
                            }
//                                mNewsDataRepository.getLatestArticleByPageFromLocalDb(mPage)!!
                            getNewArticlesForDisplay()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Pair<List<Article>, List<ArticleWithParents>>>() {
                            override fun onComplete() {}
                            override fun onNext(data: Pair<List<Article>, List<ArticleWithParents>>) {
                                setAppBarTitle()
                                if (!fragmentForPageBrowsing() && !mHasJumpedToLatestArticle.get() && data.first.isNotEmpty()) {
                                    mHasJumpedToLatestArticle.set()
                                    Handler(Looper.getMainLooper()).postDelayed(
                                            { doOnArticleClick(data.first.get(0)) }, 50L
                                    )
                                } else {
                                    if (data.first.isNotEmpty()) {
                                        displayNewArticles(data)
                                    } else {
                                        loadMoreArticles()
                                    }
                                }
                            }

                            override fun onError(e: Throwable) {
                                activity!!.finish()
                            }
                        }))
    }

    private fun setAppBarTitle() {
        getAppBarTitle()?.let {
            (activity!! as AppCompatActivity).supportActionBar!!.title = it
        }
    }

    private fun getAppBarTitle(): String? {
        if (::mPage.isInitialized && ::mNewspaper.isInitialized) {
            return "${mPage.name} | ${mNewspaper.name}"
        }
        return null
    }

    override fun onPause() {
        super.onPause()
        hideLoadingScreens()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_article_previre_for_page, menu)

        menu.findItem(R.id.add_to_favourites_menu_item).setVisible(!mIsPageOnFavList)
        menu.findItem(R.id.remove_from_favourites_menu_item).setVisible(mIsPageOnFavList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mCenterLoadingScreen.visibility != View.VISIBLE) {
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
                    .map { mUserSettingsRepository.processSignInRequestResult(it, context!!) }
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
                                    DisplayUtils.showLogInWelcomeSnack(mPageViewContainer, context!!)
                                    actionAfterSuccessfulLogIn?.let { it() }
                                }
                                UserSettingsRepository.SignInResult.USER_ABORT -> DisplayUtils.showShortSnack(mPageViewContainer, "Log in aborted")
                                UserSettingsRepository.SignInResult.SERVER_ERROR -> DisplayUtils.showShortSnack(mPageViewContainer, "Log in error.")//* Details:${processingResult.second}*/")
                                UserSettingsRepository.SignInResult.SETTINGS_UPLOAD_ERROR -> DisplayUtils.showShortSnack(mPageViewContainer, "Log in error.")//"Error while User settings data saving. Details:${processingResult.second}")
                            }
                        }

                        override fun onError(e: Throwable) {
                            actionAfterSuccessfulLogIn = null
                            DisplayUtils.showShortSnack(mPageViewContainer, "Log in error.")
                            removeWorkInProcessWindow()
                        }
                    })
        }
    }

    private fun loadMoreArticles() {
        debugLog("loadMoreArticles()")
        if (!mArticleLoadRunning && !mInvHaveMoreArticle.get()) {
            mArticleLoadRunning = true

            if (mArticleList.size <= BOTTOM_LOADING_SCREEN_ENABLER_COUNT){
                showLoadingScreen()
            }

            var amDisposed = false

            mDisposable.add(
                    Observable.just(mPage)
                            .subscribeOn(Schedulers.io())
                            .map {

                                if (!::mNewspaper.isInitialized) {
                                    mNewspaper = mAppSettingsRepository.getNewspaperByPage(it)
                                }
                                if (!::mLanguage.isInitialized) {
                                    mLanguage = mAppSettingsRepository.getLanguageByNewspaper(mNewspaper)
                                }
                                mNewsDataRepository.getArticleCountForPage(mPage) == 0
                            }
                            .map {
                                val articles = mutableListOf<Article>()
                                try {
                                    if (it) {
                                        mNewsDataRepository.getLatestArticleByPage(mPage)
                                    }
                                    mNewsDataRepository.downloadPreviousArticlesByPage(mPage, ARTICLE_LOAD_CHUNK_SIZE)
                                } catch (ex: Exception) {
                                    if (!amDisposed) {
                                        throw ex
                                    }
                                }
                                getNewArticlesForDisplay()
                            }
                            .doOnDispose {
                                mArticleLoadRunning = false
                                amDisposed = true
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<Pair<List<Article>, List<ArticleWithParents>>>() {
                                override fun onComplete() {
                                    mArticleLoadRunning = false
                                    hideLoadingScreens()
                                }

                                override fun onNext(data: Pair<List<Article>, List<ArticleWithParents>>) {
                                    setAppBarTitle()
                                    displayNewArticles(data)
                                }

                                override fun onError(throwable: Throwable) {
                                    mArticleLoadRunning = false
                                    hideLoadingScreens()
                                    when (throwable) {
                                        is DataNotFoundException -> {
                                            mInvHaveMoreArticle.set()
                                        }
                                        is NoInternertConnectionException -> {
                                            NetConnectivityUtility.showNoInternetToast(context!!)
                                        }
                                        else -> {
                                            LoggerUtils.printStackTrace(throwable)
                                        }
                                    }
                                }
                            })
            )
        }
    }

    private fun getNewArticlesForDisplay(): Pair<List<Article>, List<ArticleWithParents>> {
        ExceptionUtils.checkRequestValidityBeforeDatabaseAccess()
        val newArticles = Article.removeDuplicates(mNewsDataRepository.getArticlesForPage(mPage).filter {
            val article = it
            mArticleList.count { it.checkIfSameArticle(article) } == 0
        })
        return Pair(newArticles, ArticleWithParents.getFromArticles(newArticles.sortedByDescending { it.publicationTime!! }, context!!))
    }

    private fun displayNewArticles(data: Pair<List<Article>, List<ArticleWithParents>>) {
        val newArticleList = data.first

        if (newArticleList.isNotEmpty()) {
//            mArticleRequestChunkSize = ARTICLE_LOAD_CHUNK_SIZE
            mArticleList.addAll(newArticleList)
            mArticleWithParentsList.addAll(data.second)
            mArticlePreviewHolderAdapter.submitList(mArticleWithParentsList.toList())
        } else {
            mArticleLoadRunning = false
//            mArticleRequestChunkSize += ARTICLE_LOAD_CHUNK_SIZE
            Handler(Looper.getMainLooper()).postAtTime({
                showLoadingScreen()
                loadMoreArticles()
            }, 1000L)
        }
    }

    private val SIGN_IN_PROMPT = "Sign in and continue."

    private fun changePageFavStatus(action: PAGE_FAV_STATUS_CHANGE_ACTION) {

        val dialogMessage: String

        when (action) {
            PAGE_FAV_STATUS_CHANGE_ACTION.ADD -> dialogMessage = "Add \"${mPage.name}\" to favourites?"
            PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> dialogMessage = "Remove \"${mPage.name}\" from favourites?"
        }

        val positiveAction: () -> Unit = {
            //            loadWorkInProcessWindow()
            showLoadingScreen()
            mDisposable.add(
                    Observable.just(mPage)
                            .subscribeOn(Schedulers.io())
                            .map {
                                when (action) {
                                    PAGE_FAV_STATUS_CHANGE_ACTION.ADD -> return@map mUserSettingsRepository.addPageToFavList(mPage, context!!)
                                    PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> return@map mUserSettingsRepository.removePageFromFavList(mPage, context!!)
                                }
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<Boolean>() {
                                override fun onComplete() {
                                    hideLoadingScreens()
                                }

                                override fun onNext(result: Boolean) {
                                    if (result) {
                                        when (action) {
                                            PAGE_FAV_STATUS_CHANGE_ACTION.ADD -> {
                                                DisplayUtils.showShortSnack(mPageViewContainer, "${mPage.name} added to favourites")
                                            }
                                            PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> {
                                                DisplayUtils.showShortSnack(mPageViewContainer, "${mPage.name} removed from favourites")
                                            }
                                        }
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    if (e is NoInternertConnectionException) {
                                        NetConnectivityUtility.showNoInternetToastAnyWay(context!!)
                                    } else {
                                        DisplayUtils.showShortSnack(mPageViewContainer, "Error!! Please retry.")
                                    }
                                    hideLoadingScreens()
                                }
                            })
            )
        }

        val changeFavStatusDialog =
                DialogUtils.createAlertDialog(
                        context!!,
                        DialogUtils.AlertDialogDetails(
                                message = dialogMessage,
                                doOnPositivePress = positiveAction
                        )
                )

        if (mUserSettingsRepository.checkIfLoggedIn()) {
            changeFavStatusDialog.show()
        } else {
            DialogUtils.createAlertDialog(
                    context!!,
                    DialogUtils.AlertDialogDetails(
                            message = dialogMessage,
                            positiveButtonText = SIGN_IN_PROMPT,
                            doOnPositivePress = {
                                launchSignInActivity()
                            }
                    )
            ).show()
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

    override fun loadWorkInProcessWindow() {
        showLoadingScreen()
    }

    override fun removeWorkInProcessWindow() {
        hideLoadingScreens()
    }

    private enum class PAGE_FAV_STATUS_CHANGE_ACTION {
        ADD, REMOVE
    }
}
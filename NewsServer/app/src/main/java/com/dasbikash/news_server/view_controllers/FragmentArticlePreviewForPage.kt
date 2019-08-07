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
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.*
import com.dasbikash.news_server.view_controllers.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server.view_models.PageViewViewModel
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.appbar.AppBarLayout
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentArticlePreviewForPage : Fragment(),SignInHandler, WorkInProcessWindowOperator {

    companion object {
        private const val ARTICLE_LOAD_CHUNK_SIZE = 10
        const val LOG_IN_REQ_CODE = 7777
        const val ARG_FOR_PAGE = "com.dasbikash.news_server.views.PageViewActivity.ARG_FOR_PAGE"
        const val ARG_FOR_PURPOSE = "com.dasbikash.news_server.views.PageViewActivity.ARG_FOR_PURPOSE"
        const val ARG_VALUE_FOR_LATEST_ARTICLE_DISPLAY = "com.dasbikash.news_server.views.PageViewActivity.LATEST_ARTICLE_DISPLAY"
        const val ARG_VALUE_FOR_PAGE_BROWSING = "com.dasbikash.news_server.views.PageViewActivity.PAGE_BROWSING"

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

    private lateinit var mWaitScreen: LinearLayoutCompat
    private lateinit var mArticlePreviewHolder: RecyclerView
    private lateinit var mArticlePreviewHolderAdapter: ArticlePreviewListAdapterForPage

    private lateinit var mPageViewContainer: CoordinatorLayout

    private var mHasJumpedToLatestArticle = OnceSettableBoolean()

    private var mIsPageOnFavList = false

    private lateinit var mAppSettingsRepository: AppSettingsRepository
    private lateinit var mUserSettingsRepository: UserSettingsRepository
    private lateinit var mNewsDataRepository: NewsDataRepository

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)
    private val mArticleList = mutableListOf<Article>()

    private val mInvHaveMoreArticle = OnceSettableBoolean()
    private var mArticleLoadRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoggerUtils.debugLog("onCreate", this::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LoggerUtils.debugLog("onCreateView", this::class.java)
        return inflater.inflate(R.layout.fragment_article_preview_for_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoggerUtils.debugLog("onViewCreated", this::class.java)
        mPage = (arguments!!.getParcelable(ARG_FOR_PAGE)) as Page
        mPurposeString = arguments!!.getString(ARG_FOR_PURPOSE)!!

        findViewItems(view)
        init()

        ViewModelProviders.of(activity!!).get(PageViewViewModel::class.java)
                .getArticleLiveDataForPage(mPage)
                .observe(this, object : androidx.lifecycle.Observer<List<Article>> {
                    override fun onChanged(articleList: List<Article>?) {
                        articleList?.let {
                            mArticleList.clear()
                            if (it.isNotEmpty()) {
                                it.asSequence().forEach {
                                    val article = it
                                    if (mArticleList.count { it.checkIfSameArticle(article) } == 0){
                                        mArticleList.add(article)
                                    }
                                }
                            }
                            mArticlePreviewHolderAdapter.submitList(mArticleList.toList())
                        }
                    }
                })

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
        mWaitScreen = view.findViewById(R.id.wait_screen)
        mPageViewContainer = view.findViewById(R.id.page_view_container)
    }

    private fun init() {

        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)

        hideWaitScreen()
        mArticlePreviewHolderAdapter = ArticlePreviewListAdapterForPage({ doOnArticleClick(it) }, { loadMoreArticles() }, { showLoadingIfRequired() }, ARTICLE_LOAD_CHUNK_SIZE)
        mArticlePreviewHolder.adapter = mArticlePreviewHolderAdapter
    }

    private fun showLoadingIfRequired() {
        if (mArticleLoadRunning){
            showWaitScreen()
        }
    }

    private fun showWaitScreen() {
        mWaitScreen.visibility = View.VISIBLE
        mWaitScreen.bringToFront()
    }

    private fun hideWaitScreen() {
        mWaitScreen.visibility = View.GONE
    }

    private fun doOnArticleClick(article: Article) {
        debugLog(article.toString())
        startActivity(ArticleViewActivity.getIntentForArticleView(context!!,article))
    }

    private fun activityForPageBrowsing() =
            mPurposeString.equals(ARG_VALUE_FOR_PAGE_BROWSING)

    override fun onResume() {
        super.onResume()
        mDisposable.add(
                Observable.just(mPage)
                        .observeOn(Schedulers.io())
                        .map {
                            if (!::mNewspaper.isInitialized){
                                mNewspaper = mAppSettingsRepository.getNewspaperByPage(it)
                            }
                            if (!::mLanguage.isInitialized){
                                mLanguage = mAppSettingsRepository.getLanguageByNewspaper(mNewspaper)
                            }
                            mNewsDataRepository.getLatestArticleByPageFromLocalDb(mPage)?.let {
                                return@map it
                            }
                            mPage
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Any>() {
                            override fun onComplete() {}
                            override fun onNext(data:Any) {
                                (activity!! as AppCompatActivity).supportActionBar!!.setTitle("${mPage.name} | ${mNewspaper.name}")
                                if (!mHasJumpedToLatestArticle.get() && (data is Article) && !activityForPageBrowsing()){
                                    mHasJumpedToLatestArticle.set()
                                    doOnArticleClick(data)
                                }else{
                                    loadMoreArticles()
                                }
                            }

                            override fun onError(e: Throwable) {}
                        })
        )
    }

    override fun onPause() {
        super.onPause()
        hideWaitScreen()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_article_previre_for_page, menu)

        menu.findItem(R.id.add_to_favourites_menu_item).setVisible(!mIsPageOnFavList)
        menu.findItem(R.id.remove_from_favourites_menu_item).setVisible(mIsPageOnFavList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mWaitScreen.visibility != View.VISIBLE) {
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

        if (!mArticleLoadRunning && !mInvHaveMoreArticle.get()) {

            if (mArticleList.size<2) {
                showWaitScreen()
            }
            mArticleLoadRunning = true
            var amDisposed = false
            mDisposable.add(
                    Observable.just(mPage)
                            .observeOn(Schedulers.io())
                            .map {
                                if (!::mLanguage.isInitialized) {
                                    mLanguage = mAppSettingsRepository.getLanguageByPage(mPage)
                                }
                                mNewsDataRepository.getArticleCountForPage(mPage) == 0
                            }
                            .map {
                                val articles = mutableListOf<Article>()
                                try {
                                    if (it) {
                                        articles.add(mNewsDataRepository.getLatestArticleByPage(mPage))
                                    }
                                    articles.addAll(mNewsDataRepository.downloadPreviousArticlesByPage(mPage,ARTICLE_LOAD_CHUNK_SIZE))
                                } catch (ex: Exception) {
                                    if (!amDisposed) {
                                        throw ex
                                    }
                                }
                                articles.toList()
                            }
                            .doOnDispose {
                                mArticleLoadRunning = false
                                amDisposed = true
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<List<Article>>() {
                                override fun onComplete() {
                                    mArticleLoadRunning = false
//                                    removeWorkInProcessWindow()
                                    hideWaitScreen()
                                }

                                override fun onNext(articleList: List<Article>) {}
                                override fun onError(throwable: Throwable) {
                                    mArticleLoadRunning = false
//                                    removeWorkInProcessWindow()
                                    hideWaitScreen()
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

    private val SIGN_IN_PROMPT = "Sign in and continue."

    private fun changePageFavStatus(action: PAGE_FAV_STATUS_CHANGE_ACTION) {

        val dialogMessage: String

        when (action) {
            PAGE_FAV_STATUS_CHANGE_ACTION.ADD -> dialogMessage = "Add \"${mPage.name}\" to favourites?"
            PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> dialogMessage = "Remove \"${mPage.name}\" from favourites?"
        }

        val positiveAction: () -> Unit = {
            //            loadWorkInProcessWindow()
            showWaitScreen()
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
                                    hideWaitScreen()
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
                                    hideWaitScreen()
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
        showWaitScreen()
    }

    override fun removeWorkInProcessWindow() {
        hideWaitScreen()
    }

    private enum class PAGE_FAV_STATUS_CHANGE_ACTION {
        ADD, REMOVE
    }
}
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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.*
import com.dasbikash.news_server.view_controllers.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server.view_controllers.view_helpers.ArticleDiffCallback
import com.dasbikash.news_server.view_models.PageViewViewModel
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.dasbikash.news_server_data.utills.ImageUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.appbar.AppBarLayout
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class PageViewActivity : ActivityWithBackPressQueueManager(),
        SignInHandler, WorkInProcessWindowOperator {

    companion object {
        private const val ARTICLE_LOAD_CHUNK_SIZE = 10
        const val LOG_IN_REQ_CODE = 7777
        const val EXTRA_FOR_PAGE = "com.dasbikash.news_server.views.PageViewActivity.EXTRA_FOR_PAGE"
        const val EXTRA_FOR_PURPOSE = "com.dasbikash.news_server.views.PageViewActivity.EXTRA_FOR_PURPOSE"
        const val EXTRA_VALUE_FOR_LATEST_ARTICLE_DISPLAY = "com.dasbikash.news_server.views.PageViewActivity.LATEST_ARTICLE_DISPLAY"
        const val EXTRA_VALUE_FOR_PAGE_BROWSING = "com.dasbikash.news_server.views.PageViewActivity.PAGE_BROWSING"

        fun getIntentForLatestArticleDisplay(context: Context, page: Page): Intent {
            val intent = Intent(context, PageViewActivity::class.java)
            intent.putExtra(EXTRA_FOR_PAGE, page)
            intent.putExtra(EXTRA_FOR_PURPOSE, EXTRA_VALUE_FOR_LATEST_ARTICLE_DISPLAY)
            return intent
        }

        fun getIntentForLatestPageBrowsing(context: Context, page: Page): Intent {
            val intent = Intent(context, PageViewActivity::class.java)
            intent.putExtra(EXTRA_FOR_PAGE, page)
            intent.putExtra(EXTRA_FOR_PURPOSE, EXTRA_VALUE_FOR_PAGE_BROWSING)
            return intent
        }
    }

    private lateinit var mToolbar: Toolbar
    private lateinit var mAppBar: AppBarLayout

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
        setContentView(R.layout.activity_page_view2)

        mAppBar = findViewById(R.id.app_bar_layout)
        mToolbar = findViewById(R.id.app_bar)

        setSupportActionBar(mToolbar)

        @Suppress("CAST_NEVER_SUCCEEDS")
        mPage = (intent!!.getParcelableExtra(EXTRA_FOR_PAGE)) as Page
        mPurposeString = intent!!.getStringExtra(EXTRA_FOR_PURPOSE)

        findViewItems()
        init()

        ViewModelProviders.of(this).get(PageViewViewModel::class.java)
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

        ViewModelProviders.of(this).get(PageViewViewModel::class.java)
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
                        invalidateOptionsMenu()
                    }
                })
    }

    private fun findViewItems() {
        mArticlePreviewHolder = findViewById(R.id.article_preview_holder)
        mWaitScreen = findViewById(R.id.wait_screen)
        mPageViewContainer = findViewById(R.id.page_view_container)
    }

    private fun init() {

        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(this)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(this)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(this)

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
    }

    private fun activityForPageBrowsing() =
            mPurposeString.equals(EXTRA_VALUE_FOR_PAGE_BROWSING)

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
                                supportActionBar!!.setTitle("${mPage.name} | ${mNewspaper.name}")
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_page_view_activity, menu)

        menu.findItem(R.id.add_to_favourites_menu_item).setVisible(!mIsPageOnFavList)
        menu.findItem(R.id.remove_from_favourites_menu_item).setVisible(mIsPageOnFavList)

        return true
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
                                    DisplayUtils.showLogInWelcomeSnack(mPageViewContainer, this@PageViewActivity)
                                    actionAfterSuccessfulLogIn?.let { it() }
                                }
                                UserSettingsRepository.SignInResult.USER_ABORT -> DisplayUtils.showShortSnack(mPageViewContainer, "Log in aborted")
                                UserSettingsRepository.SignInResult.SERVER_ERROR -> DisplayUtils.showShortSnack(mPageViewContainer, "Log in error.")//* Details:${processingResult.second}*/")
                                UserSettingsRepository.SignInResult.SETTINGS_UPLOAD_ERROR -> DisplayUtils.showShortSnack(mPageViewContainer, "Log in error.")//"Error while User settings data saving. Details:${processingResult.second}")
                            }
                        }

                        override fun onError(e: Throwable) {
                            actionAfterSuccessfulLogIn = null
                            DisplayUtils.showShortSnack(mPageViewContainer, "Log in error.")//"Error while User settings data saving. Error: ${e}")
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
                                            NetConnectivityUtility.showNoInternetToast(this@PageViewActivity)
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
                                    PAGE_FAV_STATUS_CHANGE_ACTION.ADD -> return@map mUserSettingsRepository.addPageToFavList(mPage, this)
                                    PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> return@map mUserSettingsRepository.removePageFromFavList(mPage, this)
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
                                        NetConnectivityUtility.showNoInternetToastAnyWay(this@PageViewActivity)
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
                        this,
                        DialogUtils.AlertDialogDetails(
                                message = dialogMessage,
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

class ArticlePreviewListAdapterForPage(val articleClickAction: (Article) -> Unit, val requestMoreArticle: () -> Unit,
                                       val showLoadingIfRequired: () -> Unit, val articleLoadChunkSize: Int) :
        ListAdapter<Article, ArticlePreviewHolderForPage>(ArticleDiffCallback) {

    override fun onBindViewHolder(holder: ArticlePreviewHolderForPage, position: Int) {
        holder.itemView.setOnClickListener { articleClickAction(getItem(position)) }
        if (position >= (itemCount - articleLoadChunkSize / 2)) {
            requestMoreArticle()
        }
        if (position == (itemCount - 1)) {
            showLoadingIfRequired()
        }
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlePreviewHolderForPage {
        return ArticlePreviewHolderForPage(LayoutInflater.from(parent.context)
                .inflate(R.layout.view_article_preview, parent, false))
    }
}


class ArticlePreviewHolderForPage(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val pageTitle: TextView
    val articlePreviewImage: ImageView
    val articleTitle: TextView
    val articlePublicationTime: TextView
    val articleTextPreview: TextView

    val articleTitlePlaceHolder: TextView
    val articlePublicationTimePlaceHolder: TextView
    val articleTextPreviewPlaceHolder: TextView

    lateinit var mdisposable: Disposable


    init {

        pageTitle = itemView.findViewById(R.id.page_title)
        articlePreviewImage = itemView.findViewById(R.id.article_preview_image)

        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)
        articleTextPreview = itemView.findViewById(R.id.article_text_preview)

        articleTitlePlaceHolder = itemView.findViewById(R.id.article_title_ph)
        articlePublicationTimePlaceHolder = itemView.findViewById(R.id.article_time_ph)
        articleTextPreviewPlaceHolder = itemView.findViewById(R.id.article_text_preview_ph)

        disableView()
    }

    private fun disableView() {
        pageTitle.visibility = View.GONE
        articleTitle.visibility = View.GONE
        articlePublicationTime.visibility = View.GONE
        articleTextPreview.visibility = View.GONE

        articleTitlePlaceHolder.visibility = View.VISIBLE
        articlePublicationTimePlaceHolder.visibility = View.VISIBLE
        articleTextPreviewPlaceHolder.visibility = View.VISIBLE
        ImageUtils.customLoader(imageView = articlePreviewImage,
                defaultImageResourceId = R.drawable.pc_bg,
                placeHolderImageResourceId = R.drawable.pc_bg)
    }

    private fun enableView() {

        articleTitlePlaceHolder.visibility = View.GONE
        articlePublicationTimePlaceHolder.visibility = View.GONE
        articleTextPreviewPlaceHolder.visibility = View.GONE

//        pageTitle.visibility = View.VISIBLE
        articleTitle.visibility = View.VISIBLE
        articlePublicationTime.visibility = View.VISIBLE
        articleTextPreview.visibility = View.VISIBLE
    }

    @SuppressLint("CheckResult")
    fun bind(article: Article) {
        disableView()
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(itemView.context)
        Observable.just(article)
                .subscribeOn(Schedulers.io())
                .map {
                    val page = appSettingsRepository.findPageById(it.pageId!!)!!
                    val newspaper = appSettingsRepository.getNewspaperByPage(page)
                    val language = appSettingsRepository.getLanguageByNewspaper(newspaper)
                    Triple(language, newspaper, page)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
//                    pageTitle.text = it.third.name!!
                    articleTitle.text = article.title
                    articlePublicationTime.text = DisplayUtils.getArticlePublicationDateString(article, it.first, itemView.context)
                    DisplayUtils.displayHtmlText(articleTextPreview, article.articleText ?: "")
//                    debugLog(article.toString())
                    enableView()

                    ImageUtils.customLoader(articlePreviewImage, article.previewImageLink,
                            R.drawable.pc_bg, R.drawable.app_big_logo)
                }
    }
}
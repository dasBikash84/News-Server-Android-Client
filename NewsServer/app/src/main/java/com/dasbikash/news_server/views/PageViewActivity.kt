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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.views.view_helpers.ArticlePreviewListAdapter
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
        SignInHandler/*, WorkInProcessWindowOperator*/ {


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
    private lateinit var mArticleLoadingProgressBarHolder:ConstraintLayout
    private lateinit var mArticlePreviewListHolder:RecyclerView
    private lateinit var mLoadMoreArticleButton:Button

    private lateinit var mPageViewContainer: CoordinatorLayout
    private lateinit var mArticleViewContainer: ViewPager

    private lateinit var mArticlePreviewListAdapter: ArticlePreviewListAdapter

    private lateinit var mAppSettingsRepository: AppSettingsRepository
    private lateinit var mUserSettingsRepository: UserSettingsRepository
    private lateinit var mNewsDataRepository: NewsDataRepository

    private lateinit var mPage: Page
    private lateinit var mNewspaper: Newspaper
    private lateinit var mLanguage: Language

    private var mIsPageOnFavList = false

    private val disposable = CompositeDisposable()

    private val MINIMUM_INIT_ARTICLE_COUNT = 5


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_view)

        @Suppress("CAST_NEVER_SUCCEEDS")
        mPage = (intent!!.getParcelableExtra(EXTRA_PAGE_TO_DISPLAY)) as Page

        initDrawerComponents()
        mPageViewContainer = findViewById(R.id.page_view_container)
        mArticleViewContainer = findViewById(R.id.article_view_container)

        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(this)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(this)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(this)

        disposable.add(
                Observable.just(mPage)
                        .subscribeOn(Schedulers.io())
                        .map {
                            mNewspaper = mAppSettingsRepository.getNewspaperByPage(mPage)
                            mLanguage = mAppSettingsRepository.getLanguageByPage(mPage)
                            val articleList = mNewsDataRepository.getArticlesByPage(mPage)
                            if (articleList.isEmpty() || articleList.size < MINIMUM_INIT_ARTICLE_COUNT){
                                mNewsDataRepository.downloadArticlesByPage(mPage)
                                return@map mNewsDataRepository.getArticlesByPage(mPage)
                            }
                            articleList
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<Article>>(){
                            override fun onComplete() {}
                            override fun onNext(articleList: List<Article>) {
                                mArticlePreviewListAdapter = ArticlePreviewListAdapter(mLanguage)
                                mArticlePreviewListHolder.adapter = mArticlePreviewListAdapter
                                mArticlePreviewListAdapter.submitList(articleList)

                            }
                            override fun onError(e: Throwable) {
                                throw e
                            }
                        })
        )

        supportActionBar!!.setTitle(mPage.name)
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

        mArticleLoadingProgressBarHolder = mDrawerLayout.findViewById(R.id.article_loading_progress_bar_holder)
        mArticlePreviewListHolder = mDrawerLayout.findViewById(R.id.article_preview_list_holder)
        mLoadMoreArticleButton = mDrawerLayout.findViewById(R.id.load_more_articel_button)
    }


    fun closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }


    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
                                override fun onComplete() {}
                                override fun onNext(result: Boolean) {
                                    if (result) {
                                        when (action) {
                                            PAGE_FAV_STATUS_CHANGE_ACTION.ADD -> {
                                                mIsPageOnFavList = true
                                                Snackbar.make(mPageViewContainer, "${mPage.name} added to favourites", Snackbar.LENGTH_SHORT)
                                                        .show()
                                            }
                                            PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE -> {
                                                mIsPageOnFavList = false
                                                Snackbar.make(mPageViewContainer, "${mPage.name} removed from favourites", Snackbar.LENGTH_SHORT)
                                                        .show()
                                            }
                                        }
                                        invalidateOptionsMenu()
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    Snackbar.make(mPageViewContainer, "Error!! Please retry.", Snackbar.LENGTH_SHORT).show()
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
                                    changeFavStatusDialog.show()
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

//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//         Handle navigation view item clicks here.
//        when (item.itemId) {
//            R.id.nav_home -> {
//                 Handle the camera action
//            }
//            R.id.nav_gallery -> {
//
//            }
//            R.id.nav_slideshow -> {
//
//            }
//            R.id.nav_tools -> {
//
//            }
//            R.id.nav_share -> {
//
//            }
//            R.id.nav_send -> {
//
//            }
//        }
//        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
//        drawerLayout.closeDrawer(GravityCompat.START)
//        return true
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOG_IN_REQ_CODE) {
            Observable.just(Pair(resultCode, data))
                    .subscribeOn(Schedulers.io())
                    .map { mUserSettingsRepository.processSignInRequestResult(it, this) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Pair<UserSettingsRepository.SignInResult, Throwable?>> {
                        override fun onComplete() {
                            actionAfterSuccessfulLogIn = null
                        }

                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(processingResult: Pair<UserSettingsRepository.SignInResult, Throwable?>) {
                            when (processingResult.first) {
                                UserSettingsRepository.SignInResult.SUCCESS -> {
                                    Log.d(HomeActivity.TAG, "User settings data saved.")
                                    actionAfterSuccessfulLogIn?.let {
                                        it()
                                    }
                                }
                                UserSettingsRepository.SignInResult.USER_ABORT -> Log.d(TAG, "Log in canceled by user")
                                UserSettingsRepository.SignInResult.SERVER_ERROR -> Log.d(TAG, "Log in error. Details:${processingResult.second}")
                                UserSettingsRepository.SignInResult.SETTINGS_UPLOAD_ERROR -> Log.d(TAG, "Error while User settings data saving. Details:${processingResult.second}")
                            }
                        }

                        override fun onError(e: Throwable) {
                            actionAfterSuccessfulLogIn = null
                            Log.d(TAG, "Error while User settings data saving. Error: ${e}")
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

    /*var mWaitWindowShown = false
    var mWaitWindow : Fragment? = null
    override fun loadWorkInProcessWindow() {
        mWaitWindow = FragmentWorkInProcess()
        showBottomNavigationView(false)
        addFragment(mWaitWindow!!)
        mWaitWindowShown = true
    }

    override fun removeWorkInProcessWindow() {
        mWaitWindowShown = false
        removeFragment(mWaitWindow!!)
        mWaitWindow = null
        showBottomNavigationView(true)
    }

    override fun onBackPressed() {
        if (!mWaitWindowShown) {
            super.onBackPressed()
        }
    }*/
}


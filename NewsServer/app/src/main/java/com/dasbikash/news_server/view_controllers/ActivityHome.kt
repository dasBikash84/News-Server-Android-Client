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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.BuildConfig
import com.dasbikash.news_server.R
import com.dasbikash.news_server.fcm.NewsServerFirebaseMessagingService
import com.dasbikash.news_server.utils.*
import com.dasbikash.news_server.view_controllers.interfaces.HomeNavigator
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_controllers.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*

class ActivityHome : ActivityWithBackPressQueueManager(),
        NavigationHost, HomeNavigator, SignInHandler, WorkInProcessWindowOperator {
    companion object {
        private const val EXIT_TOAST_MESSAGE = "Press back again to exit."
        private const val SIGN_OUT_PROMPT = "Sign Out?"
        private const val SIGNED_OUT_MESSAGE = "Signed out."
        private const val SIGN_OUT_ERROR_MSG = "Sign out Error!! Please retry."
        private const val EXIT_WAIT_WINDOW = 2000L

        private const val EXTRA_FCM_PAGE_ID = "FCM_PAGE_ID"
        private const val EXTRA_FCM_ARTICLE_ID = "FCM_ARTICLE_ID"
    }

    private lateinit var mToolbar: Toolbar
    private lateinit var mAppBar: AppBarLayout
    private lateinit var mCoordinatorLayout: CoordinatorLayout

    private lateinit var mUserSettingsRepository: UserSettingsRepository

    private lateinit var mLogInMenuHolder: ConstraintLayout

    private lateinit var mLogInButton: MaterialButton
    private lateinit var mUserDetailsTextView: AppCompatTextView
    private lateinit var mSignOutButton: MaterialButton
    private lateinit var mLogInBottomBar: View
    private lateinit var mAdminPanelHolder: LinearLayout
    private lateinit var mAdminPanelButton: MaterialButton

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    private val LOG_IN_REQ_CODE = 7777

    override fun showBottomNavigationView(show: Boolean) {
        when (show) {
            true -> mBottomNavigationView.visibility = View.VISIBLE
            false -> mBottomNavigationView.visibility = View.GONE
        }
    }

    val mBottomNavigationView: BottomNavigationView by lazy {
        findViewById(R.id.bottomNavigationView) as BottomNavigationView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoggerUtils.debugLog("Start", this::class.java, this)
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        setContentView(R.layout.activity_home)

        findViewItems()
        setSupportActionBar(mToolbar)

        setViewItemOnClickListners()
        setUpBottomNavigationView()
        initApp()

        addBackPressAction()

        if (intent.hasExtra(EXTRA_FCM_PAGE_ID) &&
                intent.hasExtra(EXTRA_FCM_ARTICLE_ID) &&
                (intent.getStringExtra(EXTRA_FCM_PAGE_ID) !=null ) &&
                (intent.getStringExtra(EXTRA_FCM_ARTICLE_ID) !=null )
        ){
            loadInitFragment(intent.getStringExtra(EXTRA_FCM_PAGE_ID)!!,intent.getStringExtra(EXTRA_FCM_ARTICLE_ID)!!)
        }else {
            loadInitFragment()
        }
    }

    private fun addBackPressAction() {
        addToBackPressTaskQueue {
            DisplayUtils.showShortToast(this, EXIT_TOAST_MESSAGE)
            mDisposable.add(
                    Observable.just(true)
                            .subscribeOn(Schedulers.io())
                            .map {
                                SystemClock.sleep(EXIT_WAIT_WINDOW)
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ addBackPressAction() })
            )
        }
    }

    private fun setViewItemOnClickListners() {
        mLogInButton.setOnClickListener {
            launchSignInActivity()
            hideLogInMenuHolder()
        }
        mSignOutButton.setOnClickListener {
            launchSignOutDialog()
            hideLogInMenuHolder()
        }
        mUserDetailsTextView.setOnClickListener({})

        mLogInMenuHolder.setOnClickListener {
            hideLogInMenuHolder()
        }
        mAdminPanelButton.setOnClickListener {
            hideLogInMenuHolder()
            launchAdminActivity()
        }
    }

    private fun launchAdminActivity() {
        startActivity(Intent(this,ActivityAdmin::class.java))
    }

    private fun findViewItems() {
        mAppBar = findViewById(R.id.app_bar_layout)
        mToolbar = findViewById(R.id.app_bar)
        mLogInMenuHolder = findViewById(R.id.log_in_menu_holder)
        mCoordinatorLayout = findViewById(R.id.activity_home_coordinator_container)

        mLogInButton = findViewById(R.id.log_in_sign_up_button)
        mUserDetailsTextView = findViewById(R.id.user_name_text)
        mSignOutButton = findViewById(R.id.sign_out_button)
        mLogInBottomBar = findViewById(R.id.bottom_bar)

        mAdminPanelHolder = findViewById(R.id.admin_panel_holder)
        mAdminPanelButton = findViewById(R.id.admin_panel_button)
    }

    private fun initApp() {
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(this)
        NetConnectivityUtility.initialize(applicationContext)
    }


    private fun setUpBottomNavigationView() {

        mBottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.main_frame)
            when (menuItem.itemId) {
                R.id.bottom_menu_item_home -> {
                    if (!(currentFragment is FragmentHome)) {
                        loadHomeNpFragment()
                    }
                    true
                }

                R.id.bottom_menu_item_news_categories -> {
                    if (!(currentFragment is FragmentNewsCategories)) {
                        loadNewsCategoriesViewFragment()
                    }
                    true
                }

                R.id.bottom_menu_item_article_search -> {
                    if (!(currentFragment is FragmentArticleSearch)) {
                        loadArticleSearchFragment()
                    }
                    true
                }

                R.id.bottom_menu_item_favourites -> {
                    if (!(currentFragment is FragmentFavourites)) {
                        loadFavouritesFragment()
                    }
                    true
                }
                R.id.bottom_menu_item_saved_articles -> {
                    if (!(currentFragment is FragmentSavedArticles)) {
                        loadSavedArticlesFragment()
                    }
                    true
                }

                else -> false
            }
        }
    }

    override fun addFragment(fragment: Fragment) {
        val transaction = supportFragmentManager
                .beginTransaction()
                .add(R.id.main_frame, fragment)
        transaction.commit()
    }

    override fun removeFragment(fragment: Fragment) {
        val transaction = supportFragmentManager
                .beginTransaction()
                .remove(fragment)

        transaction.commit()

    }

    override fun showAppBar(show: Boolean) {
        if (show) {
            supportActionBar!!.show()
        } else {
            supportActionBar!!.hide()
        }
    }

    var mWaitWindow: Fragment? = null
    private fun isWaitWindowShown() = mWaitWindow != null

    override fun loadWorkInProcessWindow() {
        mWaitWindow = FragmentWorkInProcess()
        showBottomNavigationView(false)
        addFragment(mWaitWindow!!)
    }

    override fun removeWorkInProcessWindow() {
        mWaitWindow?.let {
            removeFragment(it)
        }
        mWaitWindow = null
        showBottomNavigationView(true)
    }

    /**
     * Trigger a navigation to the specified fragment, optionally adding a transaction to the back
     * stack to make this navigation reversible.
     *
     * @param fragment
     * @param addToBackstack
     */
    override fun navigateTo(fragment: Fragment, addToBackstack: Boolean) {
        if (!isWaitWindowShown()) {
            val transaction = supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_frame, fragment)

            if (addToBackstack) {
                transaction.addToBackStack(null)
            }

            transaction.commit()
        }
    }

    fun loadInitFragment(pageId:String,articleId:String) {
        showAppBar(false)
        navigateTo(FragmentInit.getInstance(pageId,articleId))
        showBottomNavigationView(false)
    }

    fun loadInitFragment() {
        showAppBar(false)
        navigateTo(FragmentInit())
        showBottomNavigationView(false)
    }

    override fun loadHomeNpFragment(article:Article?) {
        showAppBar(false)
        if (article!=null){
            navigateTo(FragmentHome.getInstance(article))
        }else {
            navigateTo(FragmentHome())
        }
        showBottomNavigationView(true)
    }

    override fun loadNewsCategoriesViewFragment() {
        showAppBar(true)
        navigateTo(FragmentNewsCategories())
        showBottomNavigationView(true)
    }

    override fun loadFavouritesFragment() {
        showAppBar(true)
        navigateTo(FragmentFavourites())
        showBottomNavigationView(true)
    }

    override fun loadSavedArticlesFragment() {
        showAppBar(true)
        navigateTo(FragmentSavedArticles())
        showBottomNavigationView(true)
    }

    override fun loadMoreFragment() {
        navigateTo(FragmentMore())
        showBottomNavigationView(true)
    }

    override fun loadArticleSearchFragment() {
        showAppBar(false)
        navigateTo(FragmentArticleSearch())
        showBottomNavigationView(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!isWaitWindowShown()) {
            when (item.itemId) {
                R.id.share_app_menu_item -> {
                    shareAppMenuItemClickAction()
                    return true
                }
                R.id.settings_menu_item -> {
                    return true
                }
                R.id.log_in_app_menu_item -> {
                    logInAppMenuItemClickAction()
                    return true
                }
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_layout_basic, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun launchSignOutDialog() {
        DialogUtils.createAlertDialog(this, DialogUtils.AlertDialogDetails(
                title = SIGN_OUT_PROMPT, doOnPositivePress = { signOutAction() }
        )).show()
    }

    private var backPressActionTagForLogInMenuHolder: String? = null

    private fun logInAppMenuItemClickAction() {

        if (mLogInMenuHolder.visibility == View.GONE) {
            mLogInMenuHolder.visibility = View.VISIBLE
            mLogInMenuHolder.bringToFront()
            if (mUserSettingsRepository.checkIfLoggedIn()) {
                mSignOutButton.visibility = View.VISIBLE
                mLogInBottomBar.visibility = View.VISIBLE
                mLogInButton.visibility = View.GONE
                mUserDetailsTextView.visibility = View.GONE
                mUserSettingsRepository.getCurrentUserName()?.let {
                    DisplayUtils.displayHtmlText(mUserDetailsTextView, StringBuilder("").append(it).toString())
                    mUserDetailsTextView.visibility = View.VISIBLE
                }
                mDisposable.add(
                        Observable.just(true)
                                .subscribeOn(Schedulers.io())
                                .map {
                                    mUserSettingsRepository.checkIfLoogedAsAdmin()
                                }
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(object : DisposableObserver<Boolean>() {
                                    override fun onComplete() {}
                                    override fun onNext(result: Boolean) {
                                        if (result){mAdminPanelHolder.visibility = View.VISIBLE}
                                    }
                                    override fun onError(e: Throwable) {}
                                }))
            } else {
                mSignOutButton.visibility = View.GONE
                mLogInBottomBar.visibility = View.GONE
                mLogInButton.visibility = View.VISIBLE
                mUserDetailsTextView.visibility = View.GONE
                mAdminPanelHolder.visibility = View.GONE
            }
            if (backPressActionTagForLogInMenuHolder != null) {
                removeTaskFromQueue(backPressActionTagForLogInMenuHolder!!)
            }
            backPressActionTagForLogInMenuHolder =
                    addToBackPressTaskQueue {
                        mLogInMenuHolder.visibility = View.GONE
                        backPressActionTagForLogInMenuHolder = null
                    }
        } else {
            hideLogInMenuHolder()
        }
    }

    private fun hideLogInMenuHolder() {
        mLogInMenuHolder.visibility = View.GONE
        if (backPressActionTagForLogInMenuHolder != null) {
            removeTaskFromQueue(backPressActionTagForLogInMenuHolder!!)
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


    private fun signOutAction() {
        loadWorkInProcessWindow()
        var amDisposed = false
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            mUserSettingsRepository.signOutUser(this,{NewsServerFirebaseMessagingService.unSubscribeFromUserTopics(this)})
                        }
                        .onErrorReturn {
                            if (!amDisposed) {
                                throw it
                            }
                        }
                        .doOnDispose {
                            amDisposed = true
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Unit>() {
                            override fun onComplete() {}

                            override fun onNext(t: Unit) {
                                DisplayUtils.showShortSnack(mCoordinatorLayout, SIGNED_OUT_MESSAGE)
                                removeWorkInProcessWindow()
                            }

                            override fun onError(e: Throwable) {
                                if (e is CompositeException) {
                                    e.exceptions.asSequence().forEach {
                                        it.printStackTrace()
                                        LoggerUtils.debugLog("Error class: ${it::class.java.canonicalName}", this@ActivityHome::class.java)
                                        LoggerUtils.debugLog("Trace: ${it.stackTrace.asList()}", this@ActivityHome::class.java)
                                    }
                                    if (e.exceptions.filter { it is NoInternertConnectionException }.count() > 0) {
                                        NetConnectivityUtility.showNoInternetToastAnyWay(this@ActivityHome)
                                    } else {
                                        DisplayUtils.showShortSnack(mCoordinatorLayout, SIGN_OUT_ERROR_MSG)
                                    }
                                }
                                removeWorkInProcessWindow()
                            }
                        })
        )
    }

    private fun shareAppMenuItemClickAction() {
        startActivity(OptionsIntentBuilderUtility.getShareAppIntent(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOG_IN_REQ_CODE) {
            LogInPostProcessUtils.doLogInPostProcess(
                    mDisposable,this,resultCode,data,{loadWorkInProcessWindow()},
                    {removeWorkInProcessWindow()},
                    {
                        actionAfterSuccessfulLogIn?.let { it() }
                        actionAfterSuccessfulLogIn = null
                    })
        }
    }

}

interface SignInHandler {
    fun launchSignInActivity(doOnSignIn: () -> Unit = {})
}

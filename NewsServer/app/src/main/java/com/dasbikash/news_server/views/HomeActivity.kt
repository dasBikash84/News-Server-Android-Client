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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.OptionsIntentBuilderUtility
import com.dasbikash.news_server.views.interfaces.BottomNavigationViewOwner
import com.dasbikash.news_server.views.interfaces.HomeNavigator
import com.dasbikash.news_server.views.interfaces.NavigationHost
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class HomeActivity : AppCompatActivity(),
        NavigationHost, HomeNavigator, BottomNavigationViewOwner {

    private lateinit var mToolbar: Toolbar
    private lateinit var mAppBar: AppBarLayout

    private lateinit var mUserSettingsRepository:UserSettingsRepository

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
        setContentView(R.layout.activity_home)
        mAppBar = findViewById(R.id.app_bar_layout)
        mToolbar = findViewById(R.id.app_bar)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(this)

        setSupportActionBar(mToolbar)

        setUpBottomNavigationView()
        initApp()

        if (supportFragmentManager.findFragmentById(R.id.main_frame) == null) {
            mBottomNavigationView.visibility = View.INVISIBLE
            mAppBar.visibility = View.INVISIBLE
            loadInitFragment()
        }
    }

    private fun initApp() {
        NetConnectivityUtility.initialize(applicationContext)
    }


    private fun setUpBottomNavigationView() {
        //mBottomNavigationView = findViewById(R.id.bottom_navigation)

        mBottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            var handled: Boolean
            handled = when (menuItem.itemId) {
                R.id.bottom_menu_item_home -> {
                    if (!(supportFragmentManager.findFragmentById(R.id.main_frame) is HomeFragment)) {
                        mAppBar.visibility = View.GONE
                        loadHomeFragment()
                    }
                    true
                }
                R.id.bottom_menu_item_page_group -> {
                    if (!(supportFragmentManager.findFragmentById(R.id.main_frame) is PageGroupFragment)) {
                        mAppBar.visibility = View.VISIBLE
                        loadPageGroupFragment()
                    }
                    true
                }
                R.id.bottom_menu_item_favourites -> {
                    if (!(supportFragmentManager.findFragmentById(R.id.main_frame) is FavouritesFragment)) {
                        mAppBar.visibility = View.VISIBLE
                        loadFavouritesFragment()
                    }
                    true
                }
                R.id.bottom_menu_item_settings -> {
                    if (!(supportFragmentManager.findFragmentById(R.id.main_frame) is SettingsFragment)) {
                        mAppBar.visibility = View.VISIBLE
                        loadSettingsFragment()
                    }
                    true
                }
                R.id.bottom_menu_item_more -> {
                    if (!(supportFragmentManager.findFragmentById(R.id.main_frame) is MoreFragment)) {
                        mAppBar.visibility = View.VISIBLE
                        loadMoreFragment()
                    }
                    true
                }

                else -> false
            }
            handled
        }
    }

    /**
     * Trigger a navigation to the specified fragment, optionally adding a transaction to the back
     * stack to make this navigation reversible.
     *
     * @param fragment
     * @param addToBackstack
     */
    override fun navigateTo(fragment: Fragment, addToBackstack: Boolean) {

        val transaction = supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, fragment)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    fun loadInitFragment() {
        navigateTo(InitFragment())
    }

    override fun loadHomeFragment() {
        navigateTo(HomeFragment())
        mBottomNavigationView.visibility = View.VISIBLE
    }

    override fun loadPageGroupFragment() {
        navigateTo(PageGroupFragment())
    }

    override fun loadFavouritesFragment() {
        navigateTo(FavouritesFragment())
    }

    override fun loadSettingsFragment() {
        navigateTo(SettingsFragment())
    }

    override fun loadMoreFragment() {
        navigateTo(MoreFragment())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.share_app_menu_item -> {
                shareAppMenuItemAction()
                return true
            }
            R.id.settings_menu_item -> {
                loadSettingsFragment()
                return true
            }
            R.id.log_in_app_menu_item -> {
                logInAppMenuItemAction()
                return true
            }
        }
        return false
    }

    private fun logInAppMenuItemAction() {
        if(mUserSettingsRepository.getLogInStatus()){
            mUserSettingsRepository.signOutUser()
        }else {
            val intent = mUserSettingsRepository.getLogInIntent()
            intent?.let {
                startActivityForResult(intent, LOG_IN_REQ_CODE)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_layout_basic, menu)
        return super.onCreateOptionsMenu(menu)
    }


    private fun shareAppMenuItemAction() {
        startActivity(OptionsIntentBuilderUtility.getShareAppIntent(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOG_IN_REQ_CODE) {

            Observable.just(Pair(resultCode,data))
                    .subscribeOn(Schedulers.io())
                    .map { mUserSettingsRepository.processSignInRequestResult(it) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Pair<UserSettingsRepository.SignInResult, Throwable?>> {
                        override fun onComplete() {
                        }
                        override fun onSubscribe(d: Disposable) {
                        }
                        override fun onNext(processingResult: Pair<UserSettingsRepository.SignInResult,Throwable?>) {
                            when(processingResult.first){
                                UserSettingsRepository.SignInResult.SUCCESS -> Log.d(TAG,"User settings data saved.")
                                UserSettingsRepository.SignInResult.USER_ABORT -> Log.d(TAG,"Log in canceled by user")
                                UserSettingsRepository.SignInResult.SERVER_ERROR -> Log.d(TAG,"Log in error. Details:${processingResult.second}")
                                UserSettingsRepository.SignInResult.SETTINGS_UPLOAD_ERROR -> Log.d(TAG,"Error while User settings data saving. Details:${processingResult.second}")
                            }
                        }
                        override fun onError(e: Throwable) {
                            Log.d(TAG,"Error while User settings data saving. Error: ${e}")
                        }
                    })
        }
    }
    companion object{
        val TAG = "HomeActivity"
    }

}

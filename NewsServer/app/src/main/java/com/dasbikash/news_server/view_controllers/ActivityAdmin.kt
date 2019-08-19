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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_controllers.interfaces.TokenGenerationRequestAdder
import com.google.android.material.appbar.AppBarLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class ActivityAdmin : ActivityWithBackPressQueueManager(), NavigationHost {

    private lateinit var mToolbar: Toolbar
    private lateinit var mAppBar: AppBarLayout

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        setContentView(R.layout.activity_admin)

        mAppBar = findViewById(R.id.app_bar_layout)
        mToolbar = findViewById(R.id.app_bar)

        setSupportActionBar(mToolbar)

        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)


        if (getLoadedFragment() == null) {
            loadAdminTasksFragment()
        }
    }

    private fun getLoadedFragment() = supportFragmentManager.findFragmentById(R.id.admin_frame)

    private fun loadAdminTasksFragment() {
        navigateTo(FragmentAdminTasks(), false)
    }

    fun loadNPParserModeChangeRequestFragment() {
        navigateTo(FragmentNPParserModeChangeRequest(), true)
    }

    fun loadArticleUploaderModeChangeRequestFragment() {
        navigateTo(FragmentArticleUploaderModeChangeRequest(), true)
    }

    @Override
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Trigger a navigation to the specified fragment, optionally adding a transaction to the back
     * stack to make this navigation reversible.
     */
    override fun navigateTo(fragment: Fragment, addToBackstack: Boolean) {
        val transaction = supportFragmentManager
                .beginTransaction()
                .replace(R.id.admin_frame, fragment)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    override fun addFragment(fragment: Fragment) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeFragment(fragment: Fragment) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showBottomNavigationView(show: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showAppBar(show: Boolean) {
        if (show) {
            mAppBar.visibility = View.VISIBLE
        } else {
            mAppBar.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_admin_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_token_generation_request_menu_item -> {
                addTokenGenerationRequestAction()
                return true
            }
        }
        return false
    }

    private fun addTokenGenerationRequestAction() {

        val tokenGenerationRequestAdder = getLoadedFragment()

        if (tokenGenerationRequestAdder == null || !(tokenGenerationRequestAdder is TokenGenerationRequestAdder)) {
            DisplayUtils.showShortToast(this, TOKEN_GENERATION_NOT_SUPPORTED_MESSAGE)
            return
        }

        DialogUtils.createAlertDialog(this, DialogUtils.AlertDialogDetails(
                TOKEN_GENERATION_REQ_PROMPT,
                doOnPositivePress = { addTokenGenerationRequest(tokenGenerationRequestAdder as TokenGenerationRequestAdder) }
        )).show()
    }

    private fun addTokenGenerationRequest(tokenGenerationRequestAdder: TokenGenerationRequestAdder) {
        tokenGenerationRequestAdder.showWaitScreen()
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map { tokenGenerationRequestAdder.addTokenGenerationRequest() }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {
                                tokenGenerationRequestAdder.hideWaitScreen()
                            }

                            override fun onNext(result: Boolean) {
                                if (result) {
                                    DisplayUtils.showShortToast(this@ActivityAdmin, TOKEN_GENERATION_SUCCESS_MESSAGE)
                                } else {
                                    DisplayUtils.showShortToast(this@ActivityAdmin, TOKEN_GENERATION_FAILURE_MESSAGE)
                                }
                            }

                            override fun onError(e: Throwable) {
                                DisplayUtils.showShortToast(this@ActivityAdmin, TOKEN_GENERATION_ERROR_MESSAGE)
                                tokenGenerationRequestAdder.hideWaitScreen()
                            }
                        })
        )
    }

    companion object {
        private const val TOKEN_GENERATION_REQ_PROMPT = "Add token generation request?"
        private const val TOKEN_GENERATION_SUCCESS_MESSAGE = "Token generation request added."
        private const val TOKEN_GENERATION_NOT_SUPPORTED_MESSAGE = "Token generation not supported."
        private const val TOKEN_GENERATION_FAILURE_MESSAGE = "Token generation request addition failure."
        private const val TOKEN_GENERATION_ERROR_MESSAGE = "Error occured during token generation request addition."
    }
}


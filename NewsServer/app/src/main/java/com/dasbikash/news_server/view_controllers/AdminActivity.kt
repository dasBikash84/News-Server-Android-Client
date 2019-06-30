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
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.google.android.material.appbar.AppBarLayout

class AdminActivity : ActivityWithBackPressQueueManager(),NavigationHost {

    private lateinit var mToolbar: Toolbar
    private lateinit var mAppBar: AppBarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        mAppBar = findViewById(R.id.app_bar_layout)
        mToolbar = findViewById(R.id.app_bar)

        setSupportActionBar(mToolbar)

        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)


        if (supportFragmentManager.findFragmentById(R.id.admin_frame) == null) {
            loadAdminTasksFragment()
        }
    }

    private fun loadAdminTasksFragment(){
        navigateTo(FragmentAdminTasks(),false)
    }

    fun loadNPStatusChangeRequestFragment(){
        navigateTo(FragmentNPStatusChangeRequest(),true)
    }

    fun loadNPParserModeChangeRequestFragment(){
        navigateTo(FragmentNPParserModeChangeRequest(),true)
    }

    fun loadArticleUploaderModeChangeRequestFragment(){
        navigateTo(FragmentArticleUploaderModeChangeRequest(),true)
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
}

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

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.dasbikash.news_server.views.interfaces.HomeNavigator
import com.dasbikash.news_server.views.interfaces.NavigationHost
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(),
        NavigationHost, HomeNavigator {

    val mBottomNavigationView: BottomNavigationView by lazy {
        findViewById(R.id.bottomNavigationView) as BottomNavigationView
    }

    //lateinit var mViewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setUpBottomNavigationView()
        initApp()
        //mViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        if (supportFragmentManager.findFragmentById(R.id.main_frame) == null) {
            mBottomNavigationView.visibility = View.INVISIBLE
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
                    loadHomeFragment()
                    true
                }
                R.id.bottom_menu_item_page_group -> {
                    loadPageGroupFragment()
                    true
                }
                R.id.bottom_menu_item_favourites -> {
                    loadFavouritesFragment()
                    true
                }
                R.id.bottom_menu_item_settings -> {
                    loadSettingsFragment()
                    true
                }
                R.id.bottom_menu_item_more -> {
                    loadMoreFragment()
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
}

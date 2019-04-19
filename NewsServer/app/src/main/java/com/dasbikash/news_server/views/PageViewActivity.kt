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
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.dasbikash.news_server.R
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.display_models.entity.Article
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.repositories.AppSettingsRepository
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class PageViewActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {

        const val EXTRA_PAGE_TO_DISPLAY = "com.dasbikash.news_server.views.PageViewActivity.EXTRA_PAGE_TO_DISPLAY"
        const val EXTRA_FIRST_ARTICLE = "com.dasbikash.news_server.views.PageViewActivity.EXTRA_FIRST_ARTICLE"

        fun getIntentForPageDisplay(context: Context, page: Page, firstArticleId: String = ""): Intent {
            val intent = Intent(context, PageViewActivity::class.java)
            intent.putExtra(EXTRA_PAGE_TO_DISPLAY, page)
            intent.putExtra(EXTRA_FIRST_ARTICLE, firstArticleId)
            return intent
        }
    }

    private lateinit var mPage: Page
    private var mFirstArticle: Article? = null

    private lateinit var mFirstArticleId: String
    private lateinit var mArticleContent: AppCompatTextView
    private lateinit var mAppSettingsRepository: AppSettingsRepository
    private lateinit var mUserSettingsRepository: UserSettingsRepository
    private lateinit var mPageViewContainer:CoordinatorLayout

    private var mIsPageOnFavList = false

    private val disposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        mArticleContent = findViewById(R.id.articleContent)
        mPageViewContainer = findViewById(R.id.page_view_container)

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)


        mAppSettingsRepository = RepositoryFactory.getAppSettingsRepository(this)
        mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(this)

        @Suppress("CAST_NEVER_SUCCEEDS")
        mPage = (intent!!.getParcelableExtra(EXTRA_PAGE_TO_DISPLAY)) as Page

        @Suppress("CAST_NEVER_SUCCEEDS")
        mFirstArticleId = (intent!!.getStringExtra(EXTRA_FIRST_ARTICLE))

        supportActionBar!!.setTitle(mPage.name)
    }

    override fun onResume() {
        super.onResume()
        disposable.add(
                Observable.just(mFirstArticleId)
                        .subscribeOn(Schedulers.io())
                        .map {
                            mIsPageOnFavList = mUserSettingsRepository.checkIfOnFavList(mPage)
                            mFirstArticle = mAppSettingsRepository.findArticleById(mFirstArticleId)
                            mFirstArticle
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Article>() {
                            override fun onComplete() {
                            }

                            override fun onNext(article: Article) {
                                invalidateOptionsMenu()
                                mArticleContent.text = article.title
                            }

                            override fun onError(e: Throwable) {
                            }
                        })
        )
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

    private enum class PAGE_FAV_STATUS_CHANGE_ACTION{
        ADD,REMOVE
    }

    private fun changePageFavStatus(action:PAGE_FAV_STATUS_CHANGE_ACTION){
        disposable.add(
                Observable.just(mPage)
                        .subscribeOn(Schedulers.io())
                        .map {
                            when(action){
                                PAGE_FAV_STATUS_CHANGE_ACTION.ADD-> return@map mUserSettingsRepository.addPageToFavList(mPage)
                                PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE-> return@map mUserSettingsRepository.removePageFromFavList(mPage)
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {

                            }

                            override fun onNext(result: Boolean) {
                                if(result){
                                    when(action){
                                        PAGE_FAV_STATUS_CHANGE_ACTION.ADD->   {
                                            mIsPageOnFavList = true
                                            Snackbar.make(mPageViewContainer,"${mPage.name} added to favourites",Snackbar.LENGTH_SHORT)
                                                    .show()
                                        }
                                        PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE-> {
                                            mIsPageOnFavList = false
                                            Snackbar.make(mPageViewContainer,"${mPage.name} removed from favourites",Snackbar.LENGTH_SHORT)
                                                    .show()
                                        }
                                    }
                                    invalidateOptionsMenu()
                                }
                            }

                            override fun onError(e: Throwable) {

                            }
                        })
        )

    }

    private fun removePageFromFavListAction() =
            changePageFavStatus(PAGE_FAV_STATUS_CHANGE_ACTION.REMOVE)

    private fun addPageToFavListAction() =
            changePageFavStatus(PAGE_FAV_STATUS_CHANGE_ACTION.ADD)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}

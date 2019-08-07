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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.dasbikash.news_server_data.models.room_entity.NewsCategory
import com.dasbikash.news_server_data.models.room_entity.Page
import com.google.android.material.appbar.AppBarLayout

class ActivityArticlePreview : ActivityWithBackPressQueueManager() {

    companion object {
        const val EXTRA_FOR_PAGE = "com.dasbikash.news_server.views.ActivityArticlePreview.EXTRA_FOR_PAGE"
        const val EXTRA_FOR_PURPOSE = "com.dasbikash.news_server.views.ActivityArticlePreview.EXTRA_FOR_PURPOSE"
        const val EXTRA_VALUE_FOR_LATEST_ARTICLE_DISPLAY = "com.dasbikash.news_server.views.ActivityArticlePreview.LATEST_ARTICLE_DISPLAY"
        const val EXTRA_VALUE_FOR_PAGE_BROWSING = "com.dasbikash.news_server.views.ActivityArticlePreview.PAGE_BROWSING"
        private const val EXTRA_NEWS_CATEGORY =
                "com.dasbikash.news_server.view_controllers.ActivityArticlePreview.EXTRA_NEWS_CATEGORY"

        fun getIntentForLatestArticleDisplay(context: Context, page: Page): Intent {
            val intent = Intent(context, ActivityArticlePreview::class.java)
            intent.putExtra(EXTRA_FOR_PAGE, page)
            intent.putExtra(EXTRA_FOR_PURPOSE, EXTRA_VALUE_FOR_LATEST_ARTICLE_DISPLAY)
            return intent
        }

        fun getIntentForPageBrowsing(context: Context, page: Page): Intent {
            val intent = Intent(context, ActivityArticlePreview::class.java)
            intent.putExtra(EXTRA_FOR_PAGE, page)
            intent.putExtra(EXTRA_FOR_PURPOSE, EXTRA_VALUE_FOR_PAGE_BROWSING)
            return intent
        }

        fun getIntentForNewsCategory(context: Context, newsCategory: NewsCategory): Intent {
            val intent = Intent(context, ActivityArticlePreview::class.java)
            intent.putExtra(EXTRA_NEWS_CATEGORY, newsCategory)
            return intent
        }
    }

    private lateinit var mToolbar: Toolbar
    private lateinit var mAppBar: AppBarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_preview)

        mAppBar = findViewById(R.id.app_bar_layout)
        mToolbar = findViewById(R.id.app_bar)

        setSupportActionBar(mToolbar)

        if (intent!!.hasExtra(EXTRA_NEWS_CATEGORY)){
            val newsCategory = intent!!.getSerializableExtra(EXTRA_NEWS_CATEGORY) as NewsCategory
            navigateTo(FragmentArticlePreviewForNewsCategory.getInstance(newsCategory))
        }else if (intent!!.hasExtra(EXTRA_FOR_PAGE) && intent.hasExtra(EXTRA_FOR_PURPOSE)){

            val page = intent!!.getParcelableExtra(EXTRA_FOR_PAGE) as Page
            val purposeString = intent!!.getStringExtra(EXTRA_FOR_PURPOSE)

            if (purposeString.equals(EXTRA_VALUE_FOR_LATEST_ARTICLE_DISPLAY)){
                navigateTo(FragmentArticlePreviewForPage.getInstanceForLatestArticleDisplay(page))
            }else if (purposeString.equals(EXTRA_VALUE_FOR_PAGE_BROWSING)){
                navigateTo(FragmentArticlePreviewForPage.getInstancePageBrowsing(page))
            }else{
                throw IllegalArgumentException()
            }
        }else{
            throw IllegalArgumentException()
        }

    }

    fun navigateTo(fragment: Fragment, addToBackstack: Boolean=false) {
        val transaction = supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, fragment)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }
}
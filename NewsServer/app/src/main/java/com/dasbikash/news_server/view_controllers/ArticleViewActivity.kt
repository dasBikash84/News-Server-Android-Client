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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.SavedArticle
import com.google.android.material.appbar.AppBarLayout
import java.io.Serializable

class ArticleViewActivity : AppCompatActivity() {

    companion object {

        const val LOG_IN_REQ_CODE = 7777
        const val EXTRA_FOR_ARTICLE = "com.dasbikash.news_server.views.ArticleViewActivity.EXTRA_FOR_ARTICLE"
        const val EXTRA_FOR_SAVED_ARTICLE = "com.dasbikash.news_server.views.ArticleViewActivity.EXTRA_FOR_SAVED_ARTICLE"

        fun getIntentForArticleView(context: Context, article: Article): Intent {
            val intent = Intent(context, ArticleViewActivity::class.java)
            intent.putExtra(EXTRA_FOR_ARTICLE, article as Serializable)
            return intent
        }
    }

    private lateinit var mToolbar: Toolbar
    private lateinit var mAppBar: AppBarLayout

    private lateinit var mArticle: Article
    private lateinit var mSavedArticle: SavedArticle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_view)

        mAppBar = findViewById(R.id.app_bar_layout)
        mToolbar = findViewById(R.id.app_bar)

        setSupportActionBar(mToolbar)

        if (intent.hasExtra(EXTRA_FOR_ARTICLE)) {
            mArticle = (intent!!.getSerializableExtra(EXTRA_FOR_ARTICLE)) as Article
            navigateTo(ArticleViewFragment2.getInstance(mArticle))
        } else if (intent.hasExtra(EXTRA_FOR_SAVED_ARTICLE)) {
            mSavedArticle = (intent!!.getSerializableExtra(EXTRA_FOR_SAVED_ARTICLE)) as SavedArticle
            TODO()
        } else {
            finish()
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
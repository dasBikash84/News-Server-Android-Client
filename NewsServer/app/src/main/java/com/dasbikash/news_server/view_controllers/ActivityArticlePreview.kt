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
import androidx.fragment.app.Fragment
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
import java.lang.IllegalArgumentException

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
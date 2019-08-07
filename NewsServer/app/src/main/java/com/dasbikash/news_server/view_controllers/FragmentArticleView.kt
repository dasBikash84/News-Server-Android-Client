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
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentArticleView : Fragment() {

    private lateinit var mLanguage: Language
    private lateinit var mNewspaper: Newspaper
    private lateinit var mPage: Page
    private lateinit var mArticle: Article

    private lateinit var mDateString: String
    private var mArticleTextSize: Int? = null

    private lateinit var mArticleTitle: AppCompatTextView
    private lateinit var mArticlePublicationText: AppCompatTextView
    private lateinit var mArticleText: AppCompatTextView
    private lateinit var mArticleImageHolder: RecyclerView
    private lateinit var mArticleImageListAdapter: ArticleImageListAdapter
    private lateinit var mWaitScreen: LinearLayoutCompat

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoggerUtils.debugLog("onCreate", this::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LoggerUtils.debugLog("onCreateView", this::class.java)
        return inflater.inflate(R.layout.fragment_article_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoggerUtils.debugLog("onViewCreated", this::class.java)
        mArticle = arguments!!.getSerializable(ARG_ARTICLE) as Article
        debugLog(mArticle.toString())
        findViewItems(view)
    }

    private fun findViewItems(view: View) {
        mArticleTitle = view.findViewById(R.id.article_title)
        mArticlePublicationText = view.findViewById(R.id.article_publication_date_text)
        mArticleImageHolder = view.findViewById(R.id.article_image_holder)
        mArticleText = view.findViewById(R.id.article_text)
        mWaitScreen = view.findViewById(R.id.wait_screen_for_data_loading)

        mWaitScreen.setOnClickListener {  }
    }

    override fun onResume() {
        super.onResume()
        hideWaitScreen()
        displayArticleData()
    }

    private fun showWaitScreen() {
        mWaitScreen.visibility = View.VISIBLE
        mWaitScreen.bringToFront()
    }

    private fun hideWaitScreen() {
        mWaitScreen.visibility = View.GONE
    }

    private fun displayArticleData() {
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.computation())
                        .map {
                            val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
                            mPage = appSettingsRepository.findPageById(mArticle.pageId!!)!!
                            debugLog(mPage.toString())
                            mNewspaper = appSettingsRepository.getNewspaperByPage(mPage)
                            debugLog(mNewspaper.toString())
                            mLanguage = appSettingsRepository.getLanguageByNewspaper(mNewspaper)
                            mArticle.imageLinkList =
                                    mArticle.imageLinkList
                                            ?.asSequence()
                                            ?.filter {
                                                it.link?.isNotBlank() ?: false
                                            }?.toList()
                            mArticleTextSize = DisplayUtils.getArticleTextSize(context!!)
                            mDateString = DisplayUtils
                                    .getArticlePublicationDateString(mArticle, mLanguage, context!!, true)!!
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Unit>() {
                            override fun onComplete() {}
                            override fun onNext(t: Unit) {
                                (activity!! as AppCompatActivity).supportActionBar!!.setTitle("${mPage.name} | ${mNewspaper.name}")
                                displayArticle()
                            }

                            override fun onError(e: Throwable) {
                                LoggerUtils.printStackTrace(e)
                            }
                        }))
    }

    private fun displayArticle() {

        mArticleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mArticleTextSize!!.toFloat())

        mArticleTitle.text = mArticle.title
        mArticlePublicationText.text = mDateString
        DisplayUtils.displayHtmlText(mArticleText, mArticle.articleText!!)

        if (mArticle.imageLinkList != null && mArticle.imageLinkList!!.size > 0) {
            mArticleImageListAdapter = ArticleImageListAdapter(this, mArticleTextSize!!.toFloat(), true)
            mArticleImageHolder.adapter = mArticleImageListAdapter
            mArticleImageListAdapter.submitList(mArticle.imageLinkList)
            mArticleImageHolder.visibility = View.VISIBLE
        } else {
            mArticleImageHolder.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_fragment_article_view, menu)
        mDisposable.add(
                Observable.just(mArticle)
                        .subscribeOn(Schedulers.computation())
                        .map {
                            val newsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)
                            newsDataRepository.checkIfAlreadySaved(it)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {}
                            override fun onNext(alreadySaved: Boolean) {
                                menu.findItem(R.id.save_article_locally_menu_item).setVisible(!alreadySaved)
                            }

                            override fun onError(e: Throwable) {}
                        })
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.save_article_locally_menu_item) {
            DialogUtils.createAlertDialog(context!!,
                    DialogUtils.AlertDialogDetails(message = "Save Article?", doOnPositivePress = { saveArticleLocallyAction() }))
                    .show()
            return true
        }
        return false
    }

    private fun saveArticleLocallyAction() {
        showWaitScreen()
        mDisposable.add(
                Observable.just(mArticle)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val newsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)

                            if (!newsDataRepository.checkIfAlreadySaved(it)) {
                                LoggerUtils.debugLog("Going to save article", this@FragmentArticleView::class.java)
                                val savedArticleImage = newsDataRepository.saveArticleToLocalDisk(it, context!!)
                                LoggerUtils.debugLog(savedArticleImage.toString(), this@FragmentArticleView::class.java)
                                return@map true
                            }
                            false
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {
                                hideWaitScreen()
                            }
                            override fun onNext(result: Boolean) {
                                if (result) {
                                    DisplayUtils.showShortSnack(view as CoordinatorLayout, "Article Saved.")
                                    activity!!.invalidateOptionsMenu()
                                }
                            }

                            override fun onError(e: Throwable) {
                                hideWaitScreen()
                            }
                        })
        )
    }

    companion object {

        val ARG_ARTICLE = "com.dasbikash.news_server.views.FragmentArticleView.ARG_ARTICLE"

        fun getInstance(article: Article): FragmentArticleView {
            val args = Bundle()
            args.putSerializable(ARG_ARTICLE, article)
            val fragment = FragmentArticleView()
            fragment.setArguments(args)
            return fragment
        }
    }
}
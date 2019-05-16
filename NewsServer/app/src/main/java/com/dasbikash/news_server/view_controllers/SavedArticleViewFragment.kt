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
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server_data.models.room_entity.SavedArticle
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class SavedArticleViewFragment : Fragment() {

    private lateinit var mArticlePageDetails: AppCompatTextView
    private lateinit var mArticleTitle: AppCompatTextView
    private lateinit var mArticlePublicationText: AppCompatTextView
    private lateinit var mArticleText: AppCompatTextView
    private lateinit var mArticleImageHolder: RecyclerView
    private val mArticleImageListAdapter = ArticleImageListAdapter()

    private lateinit var mArticleDateString: String
    private lateinit var mSavedArticle: SavedArticle
    private lateinit var mArticleId: String

    private val mDisposable = CompositeDisposable()

    private lateinit var mSavedArticleViewActivity: SavedArticleViewActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mSavedArticleViewActivity = context as SavedArticleViewActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoggerUtils.debugLog("onCreate", this::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LoggerUtils.debugLog("onCreateView", this::class.java)
        return inflater.inflate(R.layout.fragment_saved_article_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoggerUtils.debugLog("onViewCreated", this::class.java)
        mArticleId = arguments!!.getString(ARG_ARTICLE_ID)!!
        LoggerUtils.debugLog(mArticleId, this::class.java)

        findViewItems(view)
        mArticleImageHolder.adapter = mArticleImageListAdapter

    }

    private fun findViewItems(view: View) {
        mArticlePageDetails = view.findViewById(R.id.article_page_details)
        mArticleTitle = view.findViewById(R.id.article_title)
        mArticlePublicationText = view.findViewById(R.id.article_publication_date_text)
        mArticleImageHolder = view.findViewById(R.id.article_image_holder)
        mArticleText = view.findViewById(R.id.article_text)
    }

    override fun onStart() {
        super.onStart()
        LoggerUtils.debugLog("onStart", this::class.java)
    }

    override fun onResume() {
        super.onResume()
        LoggerUtils.debugLog("onResume", this::class.java)

        mDisposable.add(
                Observable.just(mArticleId)
                        .subscribeOn(Schedulers.io())
                        .map {
                            if (!::mArticleDateString.isInitialized) {
                                val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
                                val newsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)
                                mSavedArticle = newsDataRepository.findSavedArticleById(it)!!
                                val language = appSettingsRepository.getLanguageByPage(appSettingsRepository.findPageById(mSavedArticle.pageId)!!)
                                mArticleDateString = DisplayUtils.getSavedArticlePublicationDateString(mSavedArticle, language, context!!)!!
                            }
                        }
                        .doOnError {
                            it.printStackTrace()
                            throw it
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Unit>() {
                            override fun onComplete() {}
                            override fun onNext(t: Unit) {
                                mArticlePageDetails.text = StringBuilder(mSavedArticle.pageName).append(" | ")
                                                                .append(mSavedArticle.newspaperName).toString()
                                mArticleTitle.text = mSavedArticle.title
                                mArticlePublicationText.text = mArticleDateString
                                DisplayUtils.displayHtmlText(mArticleText, mSavedArticle.articleText!!)
                                mSavedArticle.imageLinkList?.let {
                                    mArticleImageListAdapter.submitList(it.asSequence().filter { !it.link.isNullOrEmpty() }.map {
                                        LoggerUtils.debugLog(it.link.toString(),this@SavedArticleViewFragment::class.java)
                                        it
                                    }.toList())
                                }
                            }

                            override fun onError(e: Throwable) {}
                        })
        )
    }

    override fun onPause() {
        super.onPause()
        mDisposable.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_fragment_saved_article_view, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_article_from_local_storage_menu_item) {
            DialogUtils.createAlertDialog(context!!,
                    DialogUtils.AlertDialogDetails(message = "Delete Article?", doOnPositivePress = { deleteArticleAction() }))
                    .show()
            return true
        }
        return false
    }

    private fun deleteArticleAction() {
        mSavedArticleViewActivity.deleteArticle(mSavedArticle)
    }

    companion object {

        val ARG_ARTICLE_ID = "com.dasbikash.news_server.view_controllers.SavedArticleViewFragment.ARG_ARTICLE_ID"

        fun getInstance(articleId: String): SavedArticleViewFragment {
            val args = Bundle()
            args.putString(ARG_ARTICLE_ID, articleId)
            val fragment = SavedArticleViewFragment()
            fragment.setArguments(args)
            LoggerUtils.debugLog(articleId, SavedArticleViewActivity::class.java)
            return fragment
        }
    }
}
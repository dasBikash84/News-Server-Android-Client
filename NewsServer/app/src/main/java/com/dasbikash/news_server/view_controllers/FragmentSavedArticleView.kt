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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server_data.models.room_entity.SavedArticle
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentSavedArticleView : Fragment(),TextSizeChangeableArticleViewFragment {

//    private lateinit var mArticlePageDetails: AppCompatTextView
    private lateinit var mArticleTitle: AppCompatTextView
    private lateinit var mArticlePublicationText: AppCompatTextView
    private lateinit var mArticleText: AppCompatTextView
    private lateinit var mArticleImageHolder: RecyclerView
    private lateinit var mArticleImageListAdapter : ArticleImageListAdapter

    private lateinit var mArticleDateString: String
    private lateinit var mSavedArticle: SavedArticle
    private lateinit var mArticleId: String
    private var mArticleTextSize: Int? = null

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LoggerUtils.debugLog("onCreateView", this::class.java)
        return inflater.inflate(R.layout.fragment_article_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoggerUtils.debugLog("onViewCreated", this::class.java)
        mArticleId = arguments!!.getString(ARG_ARTICLE_ID)!!
        LoggerUtils.debugLog(mArticleId, this::class.java)

        findViewItems(view)

    }

    private fun findViewItems(view: View) {
//        mArticlePageDetails = view.findViewById(R.id.article_page_details)
        mArticleTitle = view.findViewById(R.id.article_title)
        mArticlePublicationText = view.findViewById(R.id.article_publication_date_text)
        mArticleImageHolder = view.findViewById(R.id.article_image_holder)
        mArticleText = view.findViewById(R.id.article_text)
    }

    override fun onResume() {
        super.onResume()
        displayArticle()
    }

    private fun displayArticle() {
        mDisposable.add(
                Observable.just(mArticleId)
                        .subscribeOn(Schedulers.computation())
                        .map {
                            if (!::mArticleDateString.isInitialized) {
                                val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
                                val newsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)
                                mSavedArticle = newsDataRepository.findSavedArticleById(it)!!
                                val language = appSettingsRepository.getLanguageByPage(appSettingsRepository.findPageById(mSavedArticle.pageId)!!)
                                mArticleDateString = DisplayUtils.getSavedArticlePublicationDateString(mSavedArticle, language, context!!)!!
                                mArticleTextSize = DisplayUtils.getArticleTextSize(context!!)
                            }
                        }
                        .doOnError {
                            LoggerUtils.printStackTrace(it)
                            throw it
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Unit>() {
                            override fun onComplete() {}
                            override fun onNext(t: Unit) {
                                (activity!! as AppCompatActivity).supportActionBar!!.title =
                                        StringBuilder(mSavedArticle.pageName).append(" | ").append(mSavedArticle.newspaperName).toString()
                                mArticleTitle.text = mSavedArticle.title
                                mArticleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mArticleTextSize!!.toFloat())
                                mArticlePublicationText.text = mArticleDateString
                                DisplayUtils.displayHtmlText(mArticleText, mSavedArticle.articleText!!)
                                mArticleImageListAdapter = ArticleImageListAdapter(this@FragmentSavedArticleView,DisplayUtils.DEFAULT_ARTICLE_TEXT_SIZE.toFloat())
                                mArticleImageHolder.adapter = mArticleImageListAdapter
                                mSavedArticle.imageLinkList?.let {
                                    mArticleImageListAdapter.submitList(it.asSequence().filter { !it.link.isNullOrEmpty() }.map {
                                        LoggerUtils.debugLog(it.link.toString(), this@FragmentSavedArticleView::class.java)
                                        it
                                    }.toList())
                                }
                            }

                            override fun onError(e: Throwable) {}
                        })
        )
    }

    override fun setArticleTextSpSizeTo(fontSize: Int) {
        mArticleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
    }

    companion object {

        val ARG_ARTICLE_ID = "com.dasbikash.news_server.view_controllers.FragmentSavedArticleView.ARG_ARTICLE_ID"

        fun getInstance(articleId: String): FragmentSavedArticleView {
            val args = Bundle()
            args.putString(ARG_ARTICLE_ID, articleId)
            val fragment = FragmentSavedArticleView()
            fragment.setArguments(args)
            return fragment
        }
    }
}
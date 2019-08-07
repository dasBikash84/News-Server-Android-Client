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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.RxJavaUtils
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server.utils.showShortSnack
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_controllers.view_helpers.TextListAdapter
import com.dasbikash.news_server_data.models.ArticleSearchReasultEntry
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.repositories.ArticleSearchRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentArticleSearch : Fragment() {

    private lateinit var mWaitWindow: LinearLayout
    private lateinit var mSearchKeywordEditText: EditText
    private lateinit var mSearchKeywordHintsHolder: RecyclerView
    private lateinit var mSearchButton: ImageButton
    //    private lateinit var mClearButton: Button
    private lateinit var mSearchResultsHolder: RecyclerView

    private var backPressTaskTag: String? = null

    private val mKeyWordHintListAdapter = TextListAdapter({ doOnKeywordHintItemClick(it) })

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_article_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewItems()
        initViewListners()

        init()
    }

    private fun init() {
        hideWaitScreen()
        mSearchKeywordHintsHolder.visibility = View.GONE

        mSearchKeywordHintsHolder.adapter = mKeyWordHintListAdapter

        mDisposable.add(RxJavaUtils.launchBackGroundTask {
            ArticleSearchRepository.updateSerachKeyWordsIfRequired(context!!)
        })
    }

    private fun findViewItems() {
        mWaitWindow = view!!.findViewById(R.id.wait_window)
        mSearchKeywordEditText = view!!.findViewById(R.id.search_key_input_box_edit_text)
        mSearchKeywordHintsHolder = view!!.findViewById(R.id.search_keyword_hints_holder)
        mSearchButton = view!!.findViewById(R.id.search_button)
//        mClearButton = view!!.findViewById(R.id.clear_button)
        mSearchResultsHolder = view!!.findViewById(R.id.search_results_holder)
    }

    private fun initViewListners() {
        mWaitWindow.setOnClickListener { }
        mSearchButton.setOnClickListener { searchButtonClickAction() }
//        mClearButton.setOnClickListener { clearButtonClickAction() }

        mSearchKeywordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editableText: Editable?) {
                editableText?.let {
                    it.trim().split(Regex(PATTERN_FOR_KEYWORD_SPLIT)).apply {
                        if (this.isNotEmpty()) {
                            mDisposable.add(
                                    Observable.just(this.last().trim())
                                            .subscribeOn(Schedulers.io())
                                            .map {
                                                if (it.length >= ArticleSearchRepository.MINIMUM_KEYWORD_LENGTH) {
                                                    ArticleSearchRepository.getMatchingSerachKeyWords(it, context!!)
                                                } else {
                                                    emptyList()
                                                }
                                            }
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeWith(object : DisposableObserver<List<String>>() {
                                                override fun onComplete() {}

                                                override fun onNext(keywordHintList: List<String>) {
                                                    debugLog(keywordHintList.toString())
                                                    if (keywordHintList.isNotEmpty()) {
                                                        mKeyWordHintListAdapter.submitList(keywordHintList)
                                                        mSearchKeywordHintsHolder.visibility = View.VISIBLE
                                                    } else {
                                                        mSearchKeywordHintsHolder.visibility = View.GONE
                                                    }
                                                }

                                                override fun onError(e: Throwable) {}
                                            })
                            )
                        } else {
                            mKeyWordHintListAdapter.submitList(emptyList())
                            mSearchKeywordHintsHolder.visibility = View.GONE
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun doOnKeywordHintItemClick(keyWordHint: CharSequence) {
        val lastString = mSearchKeywordEditText.text.split(Regex(PATTERN_FOR_KEYWORD_SPLIT)).last()
        val currentText = mSearchKeywordEditText.text
        mSearchKeywordEditText.setText(currentText.replaceRange(currentText.length - lastString.length, currentText.length, keyWordHint.toString() + " "))
        mSearchKeywordEditText.setSelection(mSearchKeywordEditText.text.length)
    }


    private fun clearSearchEditText() {
        mSearchKeywordEditText.setText("")
        mSearchKeywordHintsHolder.visibility = View.GONE
    }

    private fun searchButtonClickAction() {
        if (mSearchKeywordEditText.text.trim().length > 0) {
            startSearch()
        } else {
            showShortSnack(SEARCH_BOX_EMPTY_MESSAGE)
            clearSearchEditText()
        }
    }

    private var mArticleSearchTaskDisposable: Disposable? = null

    private fun startSearch() {
        debugLog("Starting article search")
        mCurrentSearchResultList.clear()
        mCurrentArticleSearchReasultEntries.clear()
        showWaitScreen()
        mArticleSearchTaskDisposable =
                Observable.just(mSearchKeywordEditText.text.trim().split(Regex(PATTERN_FOR_KEYWORD_SPLIT)))
                        .subscribeOn(Schedulers.io())
                        .map {
                            ArticleSearchRepository.getArticleSearchResultForKeyWords(context!!, it)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<ArticleSearchReasultEntry>>() {
                            override fun onComplete() {
                                hideWaitScreen()
                                removeBackPressTask()
                            }

                            override fun onNext(articleSearchResultEntries: List<ArticleSearchReasultEntry>) {
                                articleSearchResultEntries.asSequence().forEach {
                                    debugLog(it.toString())
                                }
                                mCurrentArticleSearchReasultEntries.clear()
                                mCurrentArticleSearchReasultEntries.addAll(articleSearchResultEntries)
                                displayArticleSearchResult()
                            }

                            override fun onError(e: Throwable) {
                                debugLog(e::class.java.canonicalName)
                                if (e is CompositeException) {
                                    e.exceptions.asSequence().forEach {
                                        LoggerUtils.printStackTrace(it)
                                    }
                                } else {
                                    LoggerUtils.printStackTrace(e)
                                }
                                hideWaitScreen()
                                removeBackPressTask()
                            }
                        })
        mArticleSearchTaskDisposable?.let {
            mDisposable.add(it)
        }

        addBackPressTask()
    }

    private val mCurrentArticleSearchReasultEntries = mutableListOf<ArticleSearchReasultEntry>()
    private val mCurrentSearchResultList = mutableListOf<ArticleSearchResult>()

    private fun displayArticleSearchResult() {
        debugLog("displayArticleSearchResult")
        loadMoreSearchResult()
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val SEARCH_RESULT_DOWNLOAD_CHUNK_SIZE = 5
    private var mSearchResultProcessingOnGoing = false

    fun showWaitScreenIfApplicable(){
        if (mSearchResultProcessingOnGoing){
            showWaitScreen()
        }
    }

    private fun loadMoreSearchResult(): Boolean {
        if (mSearchResultProcessingOnGoing){
            return true
        }
        if (mCurrentArticleSearchReasultEntries.isEmpty()) {
            return false
        }

        mSearchResultProcessingOnGoing =  true

        val availableEntryCount = when {
            mCurrentArticleSearchReasultEntries.size >= SEARCH_RESULT_DOWNLOAD_CHUNK_SIZE -> SEARCH_RESULT_DOWNLOAD_CHUNK_SIZE
            else -> mCurrentArticleSearchReasultEntries.size
        }

        val articleSearchReasultEntriesForProcessing =
                mCurrentArticleSearchReasultEntries.take(availableEntryCount)

        val newsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
        var amDisposed = false
        mDisposable.add(
                Observable.just(articleSearchReasultEntriesForProcessing)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val resultList = mutableListOf<ArticleSearchResult>()
                            try {
                                var currentResult:ArticleSearchReasultEntry?=null
                                it.asSequence()
                                        .filter { appSettingsRepository.findPageById(it.pageId) != null }
                                        .map {
                                            currentResult = it
                                            newsDataRepository.findArticleByIdFromRemoteDb(it.articleId, it.pageId)
                                        }
                                        .filter { it!=null }
                                        .map {
                                            it!!.newspaperId = appSettingsRepository.getNewspaperByPage(
                                                    appSettingsRepository.findPageById(it.pageId!!)!!).id
                                            ArticleSearchResult(it,currentResult!!)
                                        }
                                        .toCollection(resultList)
                            }catch (ex:Throwable){
                                if (!amDisposed){
                                    throw ex
                                }
                            }
                            resultList.toList()
                        }
                        .doOnDispose {
                            amDisposed = true
                            mSearchResultProcessingOnGoing =  false
                            hideWaitScreen()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<ArticleSearchResult>>() {
                            override fun onComplete() {
                                articleSearchReasultEntriesForProcessing
                                        .forEach { mCurrentArticleSearchReasultEntries.remove(it) }
                                mSearchResultProcessingOnGoing =  false
                                hideWaitScreen()
                            }
                            override fun onNext(articles: List<ArticleSearchResult>) {
                                var newsArticleCount = 0
                                articles.filter {
                                            val articleSearchResult = it
                                            mCurrentSearchResultList
                                                    .count { it.article.checkIfSameArticle(articleSearchResult.article) } == 0
                                        }
                                        .map {
                                            newsArticleCount++
                                            it
                                        }
                                        .forEach { mCurrentSearchResultList.add(it) }
                                if (newsArticleCount > 0) {
                                    mCurrentSearchResultList.map { it.article }.forEach { debugLog(it.toString()) }
//                                    TODO()
                                } else {
                                    loadMoreSearchResult()
                                }
                            }

                            override fun onError(e: Throwable) {
                                mSearchResultProcessingOnGoing =  false
                                hideWaitScreen()
                            }
                        })
        )
        return true
    }

    override fun onPause() {
        super.onPause()
        hideWaitScreen()
        removeBackPressTask()
    }

    private fun removeBackPressTask() {
        backPressTaskTag?.let {
            (activity as BackPressQueueManager).removeTaskFromQueue(it)
        }
        backPressTaskTag = null
    }

    private fun addBackPressTask() {
        backPressTaskTag =
                (activity as BackPressQueueManager).addToBackPressTaskQueue {
                    mArticleSearchTaskDisposable?.dispose()
                    hideWaitScreen()
                    removeBackPressTask()
                }
    }

    private fun showWaitScreen() {
        mWaitWindow.visibility = View.VISIBLE
        mWaitWindow.bringToFront()
    }

    private fun hideWaitScreen() {
        mWaitWindow.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as NavigationHost).showAppBar(false)
    }

    companion object {
        private const val CLEAR_SEARCH_BOX_MESSAGE = "Clear Search box?"
        private const val SEARCH_BOX_CLEARED_MESSAGE = "Search box cleared."
        private const val SEARCH_BOX_EMPTY_MESSAGE = "Please input keyword(s) & then press search."
        private const val PATTERN_FOR_KEYWORD_SPLIT = "[\\s+,;-]+"
    }
}

@Keep
data class ArticleSearchResult(
        val article: Article,
        val articleSearchReasultEntry: ArticleSearchReasultEntry
)
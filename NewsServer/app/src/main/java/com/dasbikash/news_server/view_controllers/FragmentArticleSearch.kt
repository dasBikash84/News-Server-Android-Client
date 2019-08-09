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
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.*
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_controllers.view_helpers.TextListAdapter
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.ArticleSearchReasultEntry
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.repositories.ArticleSearchRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.ImageUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentArticleSearch : Fragment() {

    private lateinit var mCenterLoadingScreen: LinearLayout
    private lateinit var mBottomLoadingScreen: ProgressBar
    private lateinit var mSearchKeywordEditText: EditText
    private lateinit var mSearchKeywordHintsHolder: RecyclerView
    private lateinit var mSearchButton: ImageButton
    //    private lateinit var mClearButton: Button
    private lateinit var mSearchResultsHolder: RecyclerView
    private lateinit var mSearchResultsHolderAdapter: ArticleSearchResultListAdapter

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
        mSearchResultsHolderAdapter = ArticleSearchResultListAdapter({ doOnSearchResultClick(it) }, { loadMoreSearchResult() })
        mSearchKeywordHintsHolder.adapter = mKeyWordHintListAdapter
        mSearchResultsHolder.adapter = mSearchResultsHolderAdapter

        mDisposable.add(RxJavaUtils.launchBackGroundTask (task = {
            ArticleSearchRepository.updateSerachKeyWordsIfRequired(context!!)
        }))
    }

    private fun doOnSearchResultClick(article: Article) {
        debugLog(article.toString())
        startActivity(ActivityArticleView.getIntentForArticleView(context!!,article))
    }

    private fun findViewItems() {
        mCenterLoadingScreen = view!!.findViewById(R.id.center_wait_window)
        mBottomLoadingScreen = view!!.findViewById(R.id.bottom_wait_window)
        mSearchKeywordEditText = view!!.findViewById(R.id.search_key_input_box_edit_text)
        mSearchKeywordHintsHolder = view!!.findViewById(R.id.search_keyword_hints_holder)
        mSearchButton = view!!.findViewById(R.id.search_button)
//        mClearButton = view!!.findViewById(R.id.clear_button)
        mSearchResultsHolder = view!!.findViewById(R.id.search_results_holder)
    }

    private fun initViewListners() {
        mSearchButton.setOnClickListener { searchButtonClickAction() }

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
                                                    mKeyWordHintListAdapter.submitList(keywordHintList)
                                                    if (keywordHintList.isNotEmpty()) {
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
        mSearchResultsHolderAdapter.submitList(emptyList())
        mSearchKeywordHintsHolder.visibility = View.GONE
        showWaitScreen()
        mArticleSearchTaskDisposable =
                Observable.just(mSearchKeywordEditText.text.trim().split(Regex(PATTERN_FOR_KEYWORD_SPLIT)))
                        .subscribeOn(Schedulers.io())
                        .map {
                            ArticleSearchRepository.getArticleSearchResultForKeyWords(context!!, it)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<ArticleSearchReasultEntry>>() {
                            override fun onComplete() {}

                            override fun onNext(articleSearchResultEntries: List<ArticleSearchReasultEntry>) {
                                if (articleSearchResultEntries.isNotEmpty()) {
                                    articleSearchResultEntries.asSequence().forEach {
                                        debugLog(it.toString())
                                    }
                                    mCurrentArticleSearchReasultEntries.addAll(articleSearchResultEntries)
                                    displayArticleSearchResult()
                                } else {
                                    showShortSnack(EMPTY_SEARCH_RESULT_MESSAGE)
                                    hideWaitScreen()
                                }
                                removeBackPressTask()
                            }

                            override fun onError(e: Throwable) {
                                if (e is CompositeException) {
                                    e.exceptions.asSequence().forEach {
                                        LoggerUtils.printStackTrace(it)
                                        LoggerUtils.debugLog("Error class: ${it::class.java.canonicalName}", this@FragmentArticleSearch::class.java)
                                        LoggerUtils.debugLog("Trace: ${it.stackTrace.asList()}", this@FragmentArticleSearch::class.java)
                                    }
                                    if (e.exceptions.filter { it is NoInternertConnectionException }.count() > 0) {
                                        NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleSearch.context!!)
                                    }
                                }else if (e is NoInternertConnectionException) {
                                    NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleSearch.context!!)
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

    fun showWaitScreenIfApplicable() {
        if (mSearchResultProcessingOnGoing) {
            showWaitScreen()
        }
    }

    private fun loadMoreSearchResult(): Boolean {
        if (mSearchResultProcessingOnGoing) {
            return true
        }
        if (mCurrentArticleSearchReasultEntries.isEmpty()) {
            return false
        }

        mSearchResultProcessingOnGoing = true

        val availableEntryCount = when {
            mCurrentArticleSearchReasultEntries.size >= SEARCH_RESULT_DOWNLOAD_CHUNK_SIZE -> SEARCH_RESULT_DOWNLOAD_CHUNK_SIZE
            else -> mCurrentArticleSearchReasultEntries.size
        }

        val articleSearchReasultEntriesForProcessing =
                mCurrentArticleSearchReasultEntries.take(availableEntryCount)
        showWaitScreen()
        val newsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
        var amDisposed = false
//        mDisposable.add(
        mArticleSearchTaskDisposable =
                Observable.just(articleSearchReasultEntriesForProcessing)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val resultList = mutableListOf<ArticleSearchResult>()
                            try {
                                var currentResult: ArticleSearchReasultEntry? = null
                                it.asSequence()
                                        .filter { appSettingsRepository.findPageById(it.pageId) != null }
                                        .map {
                                            currentResult = it
                                            newsDataRepository.findArticleByIdFromRemoteDb(it.articleId, it.pageId)
                                        }
                                        .filter { it != null }
                                        .map {
                                            it!!.newspaperId = appSettingsRepository.getNewspaperByPage(
                                                    appSettingsRepository.findPageById(it.pageId!!)!!).id
                                            ArticleSearchResult(it, currentResult!!)
                                        }
                                        .toCollection(resultList)
                            } catch (ex: Throwable) {
                                if (!amDisposed) {
                                    throw ex
                                }
                            }
                            resultList.toList()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnDispose {
                            amDisposed = true
                            mSearchResultProcessingOnGoing = false
                            hideWaitScreen()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<ArticleSearchResult>>() {
                            override fun onComplete() {
                                articleSearchReasultEntriesForProcessing
                                        .forEach { mCurrentArticleSearchReasultEntries.remove(it) }
                                mSearchResultProcessingOnGoing = false
                                hideWaitScreen()
                                removeBackPressTask()
                            }

                            override fun onNext(articleSearchResults: List<ArticleSearchResult>) {
                                var newsArticleCount = 0
                                ArticleSearchResult.removeDuplicates(articleSearchResults)
                                        .filter {
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
                                    mSearchResultsHolderAdapter.submitList(mCurrentSearchResultList.toList())
                                } else {
                                    loadMoreSearchResult()
                                }
                            }

                            override fun onError(e: Throwable) {
                                if (e is CompositeException) {
                                    e.exceptions.asSequence().forEach {
                                        LoggerUtils.printStackTrace(it)
                                        LoggerUtils.debugLog("Error class: ${it::class.java.canonicalName}", this@FragmentArticleSearch::class.java)
                                        LoggerUtils.debugLog("Trace: ${it.stackTrace.asList()}", this@FragmentArticleSearch::class.java)
                                    }
                                    if (e.exceptions.filter { it is NoInternertConnectionException }.count() > 0) {
                                        NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleSearch.context!!)
                                    }
                                }else if (e is NoInternertConnectionException) {
                                    NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleSearch.context!!)
                                }
                                mSearchResultProcessingOnGoing = false
                                hideWaitScreen()
                                removeBackPressTask()
                            }
                        })
//        )
        addBackPressTask()
        mDisposable.add(mArticleSearchTaskDisposable)
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
                    backPressTaskTag = null
                }
    }

    private fun showWaitScreen() {
        if (mCurrentSearchResultList.isEmpty()) {
            mCenterLoadingScreen.visibility = View.VISIBLE
            mCenterLoadingScreen.bringToFront()
            mBottomLoadingScreen.visibility = View.GONE
        }else{
            mBottomLoadingScreen.visibility = View.VISIBLE
            mBottomLoadingScreen.bringToFront()
            mCenterLoadingScreen.visibility = View.GONE
        }
    }

    private fun hideWaitScreen() {
        mCenterLoadingScreen.visibility = View.GONE
        mBottomLoadingScreen.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as NavigationHost).showAppBar(false)
    }

    companion object {
        private const val SEARCH_BOX_EMPTY_MESSAGE = "Please input keyword(s) & then press search."
        private const val EMPTY_SEARCH_RESULT_MESSAGE = "No matching article found."
        private const val PATTERN_FOR_KEYWORD_SPLIT = "[\\s+,;-]+"
    }
}

@Keep
data class ArticleSearchResult(
        val article: Article,
        val articleSearchReasultEntry: ArticleSearchReasultEntry
) {
    companion object {
        fun removeDuplicates(articles: List<ArticleSearchResult>):
                List<ArticleSearchResult> {
            val output = mutableListOf<ArticleSearchResult>()
            articles.asSequence()
                    .filter {
                        val articleSearchResult = it
                        output.count { it.article.checkIfSameArticle(articleSearchResult.article) } == 0
                    }.forEach { output.add(it) }
            return output
        }
    }
}

object ArticleSearchResultDiffCallback : DiffUtil.ItemCallback<ArticleSearchResult>() {
    override fun areItemsTheSame(oldItem: ArticleSearchResult, newItem: ArticleSearchResult): Boolean {
        return oldItem.article.checkIfSameArticle(newItem.article)
    }

    override fun areContentsTheSame(oldItem: ArticleSearchResult, newItem: ArticleSearchResult): Boolean {
        return oldItem.article.checkIfSameArticle(newItem.article)
    }
}

class ArticleSearchResultListAdapter(val clickAction: (Article) -> Unit, val loadMoreResult: () -> Unit) :
        ListAdapter<ArticleSearchResult, ArticleSearchResultHolder>(ArticleSearchResultDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleSearchResultHolder {
        return ArticleSearchResultHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_article_search_result_preview, parent, false))
    }

    override fun onBindViewHolder(holder: ArticleSearchResultHolder, position: Int) {
        holder.itemView.setOnClickListener { clickAction(getItem(position).article) }
        holder.bind(getItem(position))
        if (position >= itemCount - 2) {
            loadMoreResult()
        }
    }
}

class ArticleSearchResultHolder(itemView: View)
    : RecyclerView.ViewHolder(itemView) {

    val pageTitle: TextView
    val articlePreviewImage: ImageView
    val articleTitle: TextView
    val articlePublicationTime: TextView
    val articleTextPreview: TextView
    val matchingKeywordsView: TextView

    init {

        pageTitle = itemView.findViewById(R.id.page_title)
        articlePreviewImage = itemView.findViewById(R.id.article_preview_image)

        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)
        articleTextPreview = itemView.findViewById(R.id.article_text_preview)
        matchingKeywordsView = itemView.findViewById(R.id.matching_keywords)

        disableView()
    }

    private fun disableView() {
        pageTitle.visibility = View.GONE
        articleTitle.visibility = View.GONE
        articlePublicationTime.visibility = View.GONE
        articleTextPreview.visibility = View.GONE

        ImageUtils.customLoader(imageView = articlePreviewImage,
                defaultImageResourceId = R.drawable.pc_bg,
                placeHolderImageResourceId = R.drawable.pc_bg)
    }

    private fun enableView() {
        pageTitle.visibility = View.VISIBLE
        articleTitle.visibility = View.VISIBLE
        articlePublicationTime.visibility = View.VISIBLE
        articleTextPreview.visibility = View.VISIBLE
    }

    @SuppressLint("CheckResult")
    fun bind(articleSearchResult: ArticleSearchResult) {
        disableView()
        val article = articleSearchResult.article
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
                    pageTitle.text = it.third.name!! + " | " + it.second.name
                    articleTitle.text = article.title
                    articlePublicationTime.text = DisplayUtils.getArticlePublicationDateString(article, it.first, itemView.context)
                    DisplayUtils.displayHtmlText(articleTextPreview, article.articleText ?: "")
                    matchingKeywordsView.text = StringBuilder("Match found: ")
                            .append(articleSearchResult.articleSearchReasultEntry.getMatchingKeyWords()
                                    .joinToString(separator = ", ", prefix = "", postfix = "")).toString()
                    enableView()

                    ImageUtils.customLoader(articlePreviewImage, article.previewImageLink,
                            R.drawable.pc_bg, R.drawable.app_big_logo)
                }
    }
}
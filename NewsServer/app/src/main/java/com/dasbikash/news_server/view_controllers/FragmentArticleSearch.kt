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
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.*
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_controllers.view_helpers.TextListAdapter
import com.dasbikash.news_server_data.repositories.ArticleSearchRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentArticleSearch : Fragment() {

    private lateinit var mWaitWindow: LinearLayout
    private lateinit var mSearchKeywordEditText: EditText
    private lateinit var mSearchKeywordHintsHolder: RecyclerView
    private lateinit var mSearchButton: Button
    private lateinit var mClearButton: Button
    private lateinit var mSearchResultsHolder: RecyclerView

    private val mKeyWordHintListAdapter = TextListAdapter({doOnKeywordHintItemClick(it)})

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
        mClearButton = view!!.findViewById(R.id.clear_button)
        mSearchResultsHolder = view!!.findViewById(R.id.search_results_holder)
    }

    private fun initViewListners() {
        mWaitWindow.setOnClickListener { }
        mSearchButton.setOnClickListener { searchButtonClickAction() }
        mClearButton.setOnClickListener { clearButtonClickAction() }

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

    private fun doOnKeywordHintItemClick(keyWordHint:CharSequence){
        val lastString = mSearchKeywordEditText.text.split(Regex(PATTERN_FOR_KEYWORD_SPLIT)).last()
        val currentText = mSearchKeywordEditText.text
        mSearchKeywordEditText.setText(currentText.replaceRange(currentText.length-lastString.length,currentText.length,keyWordHint.toString()+" "))
        mSearchKeywordEditText.setSelection(mSearchKeywordEditText.text.length)
    }

    private fun clearButtonClickAction() {
        if (mSearchKeywordEditText.text.trim().length > 0) {
            DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                    title = CLEAR_SEARCH_BOX_MESSAGE,
                    doOnPositivePress = {
                        clearSearchEditText()
                        showShortSnack(SEARCH_BOX_CLEARED_MESSAGE)
                    }
            )).show()
        }
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

    private fun startSearch() {
        debugLog("Starting article search")
        showWaitScreen()
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
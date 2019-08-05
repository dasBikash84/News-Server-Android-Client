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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentHomeNp: Fragment() {

    private val MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH = 3

    private lateinit var mPageSearchTextBoxContainer: TextInputLayout
    private lateinit var mPageSearchTextBox: EditText
    private lateinit var mPageSearchResultHolder: RecyclerView
    private lateinit var mPageSearchResultContainer:ViewGroup
    private lateinit var mPageSearchBoxShowButton:ImageView

    private lateinit var mSelectBanglaPapers:AppCompatTextView
    private lateinit var mSelectEnglishPapers:AppCompatTextView

    private lateinit var mNewsPaperMenuHolder: RecyclerView
    private lateinit var mNewsPaperNameListContainer:ViewGroup
    private lateinit var mNewsPaperMenuShowButton:ImageView
    private lateinit var mNewsPaperMenuHideButton:ImageView
    private lateinit var mNewsPaperListAdapter: NewsPaperListAdapter
    private lateinit var mNewsPaperScrollerContainer: ViewGroup


    private lateinit var mPageArticlePreviewHolder: RecyclerView

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)
    private var mSearchResultListAdapter = SearchResultListAdapter()
    private var backPressTaskTag:String?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_np, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewComponents(view)
        setListnersForViewComponents()
        initViewComponents()
    }

    override fun onResume() {
        super.onResume()
        ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
                .getNewsPapersLiveData().observe(this,object : Observer<List<Newspaper>>{
            override fun onChanged(list: List<Newspaper>?) {
                if (list!=null && list.isNotEmpty()){
                    mNewsPaperListAdapter.submitList(list.sortedBy { it.getPosition() }.reversed().sortedBy { it.languageId }.reversed())
                }else{
                    mNewsPaperListAdapter.submitList(emptyList())
                }
            }
        })
    }

    private fun findViewComponents(view: View) {
        mPageSearchTextBoxContainer = view.findViewById(R.id.page_search_text_box_layout)
        mPageSearchTextBox = view.findViewById(R.id.page_search_box_edit_text)
        mPageSearchResultHolder = view.findViewById(R.id.page_search_result_holder)
        mPageSearchResultContainer = view.findViewById(R.id.page_search_result_container)
        mPageSearchBoxShowButton = view.findViewById(R.id.show_page_search_box)
        mSelectBanglaPapers = view.findViewById(R.id.bangla_text_view)
        mSelectEnglishPapers = view.findViewById(R.id.english_text_view)
        mNewsPaperMenuHolder = view.findViewById(R.id.np_name_holder)
        mNewsPaperNameListContainer = view.findViewById(R.id.np_name_scroller)
        mNewsPaperMenuShowButton = view.findViewById(R.id.show_np_name_menu)
        mNewsPaperMenuHideButton = view.findViewById(R.id.hide_np_name_menu)
        mNewsPaperScrollerContainer = view.findViewById(R.id.np_name_scroller_container)
        mPageArticlePreviewHolder = view.findViewById(R.id.page_article_preview_holder)
    }

    private fun setListnersForViewComponents() {
        mPageSearchBoxShowButton.setOnClickListener { showPageSearchBox() }

        mPageSearchTextBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                text?.let {
                    if (it.trim().length >= MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH){
                        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(this@FragmentHomeNp.context!!)
                        mDisposable.add(
                                Observable.just(it.trim().toString())
                                        .subscribeOn(Schedulers.io())
                                        .map {
                                            debugLog("Page Search string: $it")
                                            appSettingsRepository.findMatchingPages(it).filter {
                                                @Suppress("SENSELESS_COMPARISON")
                                                debugLog("Page: "+it.toString())
                                                it !=null
                                            }.toList()
                                        }
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeWith(object : DisposableObserver<List<Page>>(){
                                            override fun onComplete() {}
                                            override fun onNext(pageList: List<Page>) {
                                                debugLog(pageList.toString())
                                                mPageSearchResultContainer.visibility = View.VISIBLE
                                                mPageSearchResultContainer.bringToFront()
                                                mPageSearchResultContainer.setOnClickListener({
                                                    mPageSearchResultContainer.visibility = View.GONE
                                                })
                                                mSearchResultListAdapter.submitList(pageList)
                                                if (pageList.size>0){
                                                    if (backPressTaskTag!=null){
                                                        (activity as BackPressQueueManager).removeTaskFromQueue(backPressTaskTag!!)
                                                    }
                                                    backPressTaskTag =
                                                            (activity as BackPressQueueManager).addToBackPressTaskQueue {
                                                                mSearchResultListAdapter.submitList(emptyList())
                                                                mPageSearchResultContainer.visibility = View.GONE
                                                            }
                                                }
                                            }
                                            override fun onError(e: Throwable) {}
                                        })
                        )
                    }else{
                        mPageSearchResultContainer.visibility = View.GONE
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
//        mNewsPaperScrollerContainer.setOnClickListener { hideNewsPaperMenu() }
        mNewsPaperMenuShowButton.setOnClickListener { showNewsPaperMenu() }
        mNewsPaperMenuHideButton.setOnClickListener { hideNewsPaperMenu() }
    }

    private fun hideNewsPaperMenu() {
        hideNewsPaperMenuHolder()
        mNewsPaperMenuHideButton.visibility = View.GONE
        mNewsPaperMenuShowButton.visibility = View.VISIBLE
    }

    private fun hideNewsPaperMenuHolder() {
        mNewsPaperMenuHolder.visibility = View.GONE
    }

    private fun showNewsPaperMenu() {
        showNewsPaperMenuHolder()
        mNewsPaperMenuHideButton.visibility = View.VISIBLE
        mNewsPaperMenuShowButton.visibility = View.GONE
    }

    private fun showNewsPaperMenuHolder() {
        mNewsPaperMenuHolder.visibility = View.VISIBLE
    }

    private fun initViewComponents() {
        (activity as NavigationHost)
                .showBottomNavigationView(true)
        showPageSearchBox()
        showNewsPaperMenu()
        mNewsPaperScrollerContainer.bringToFront()
        mPageSearchResultHolder.adapter = mSearchResultListAdapter
        mNewsPaperListAdapter = NewsPaperListAdapter { doOnNewsPaperNameClick(it) }
        mNewsPaperMenuHolder.adapter = mNewsPaperListAdapter
    }

    fun doOnNewsPaperNameClick(newspaper: Newspaper){
        debugLog(newspaper.name!!)
    }

    private fun showPageSearchBox() {
        mPageSearchTextBoxContainer.visibility = View.VISIBLE
        hidePageSearchBoxShowButton()
    }

    private fun hidePageSearchBox() {
        mPageSearchTextBoxContainer.visibility = View.GONE
        mPageSearchTextBox.setText("")
        showPageSearchBoxShowButton()
    }

    private fun showPageSearchBoxShowButton() {
        mPageSearchBoxShowButton.visibility = View.VISIBLE
    }

    private fun hidePageSearchBoxShowButton() {
        mPageSearchBoxShowButton.visibility = View.GONE
    }
}

object NewsPaperDiffCallback: DiffUtil.ItemCallback<Newspaper>(){
    override fun areItemsTheSame(oldItem: Newspaper, newItem: Newspaper): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: Newspaper, newItem: Newspaper): Boolean {
        return oldItem == newItem
    }
}
class NewsPaperListAdapter(val doOnItemClick:(Newspaper)->Unit) : ListAdapter<Newspaper, NewsPaperNameHolder>(NewsPaperDiffCallback) {

    private val viewHolderList = mutableListOf<NewsPaperNameHolder>()
    private var currentPosition:Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsPaperNameHolder {
        val newsPaperNameHolder = NewsPaperNameHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.view_news_paper_label, parent, false))
        newsPaperNameHolder.itemView.setOnClickListener {
            doOnItemClick(newsPaperNameHolder.getNewsPaper())
            currentPosition = newsPaperNameHolder.position
            viewHolderList.filter { it!=newsPaperNameHolder }.forEach { it.highlightText(false) }
            newsPaperNameHolder.highlightText(true)
        }
        viewHolderList.add(newsPaperNameHolder)
        return newsPaperNameHolder
    }

    override fun onBindViewHolder(holder: NewsPaperNameHolder, position: Int) {
        holder.bind(getItem(position),position,currentPosition)
    }
}

class NewsPaperNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private lateinit var mNewspaper: Newspaper
    private val mNormalTextView:TextView
    private val mBoldTextView:TextView
    var position: Int?=null

    init {
        mNormalTextView = itemView.findViewById(R.id.normal_title_text_view)
        mBoldTextView = itemView.findViewById(R.id.bold_title_text_view)
    }

    fun bind(newspaper: Newspaper,position: Int,currentPosition:Int?){
        mNewspaper = newspaper
        this.position = position

        mNormalTextView.setText(mNewspaper.name)
        mBoldTextView.setText(mNewspaper.name)
        if (position == 0 && currentPosition==null){
            highlightText(true)
        }else{
            highlightText(false)
        }
    }

    fun highlightText(highlight:Boolean){
        if (highlight){
            mBoldTextView.visibility = View.VISIBLE
            mNormalTextView.visibility = View.GONE
        }else{
            mBoldTextView.visibility = View.GONE
            mNormalTextView.visibility = View.VISIBLE
        }
    }
    fun getNewsPaper() = mNewspaper
}
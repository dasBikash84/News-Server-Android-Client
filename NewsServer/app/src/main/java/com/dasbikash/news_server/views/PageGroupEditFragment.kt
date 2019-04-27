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

package com.dasbikash.news_server.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.views.interfaces.HomeNavigator
import com.dasbikash.news_server.views.interfaces.NavigationHost
import com.dasbikash.news_server.views.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server.views.view_helpers.PageListAdapter
import com.dasbikash.news_server.views.view_helpers.PageViewHolder
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class PageGroupEditFragment : Fragment() {

    private lateinit var mPageGroupNameEditText: EditText
    private lateinit var mPageSearchBoxEditText: EditText
    private lateinit var mCurrentPageListHolder: RecyclerView
    private lateinit var mSearchResultPageListHolder: RecyclerView
    private lateinit var mDoneButton: Button
    private lateinit var mResetButton: Button
    private lateinit var mCancelButton: Button

    private lateinit var mCurrentPageListAdapter: CurrentPageListAdapter
    private lateinit var mSearchResultPageListAdapter: SearchResultPageListAdapter

    private lateinit var mPageGroupName: String
    private lateinit var mMode: OPERATING_MODE

    private val mCurrentPageList = mutableListOf<Page>()
    private val mSearchResultPageList = mutableListOf<Page>()
    private lateinit var mPageGroup: PageGroup

    private val mDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page_group_edit, container, false)
    }

    private val MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as NavigationHost).showAppBar(false) //To disable sign out
        (activity as NavigationHost).disableBackPress(true) //To disable sign out
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as NavigationHost).showAppBar(true)
        (activity as NavigationHost).disableBackPress(false) //To disable sign out
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPageGroupName = arguments?.getString(ARG_PAGE_GROUP_NAME)!!
        if (mPageGroupName.isBlank()) {
            mMode = OPERATING_MODE.CREATE
        } else {
            mMode = OPERATING_MODE.EDIT
        }

        mPageGroupNameEditText = view.findViewById(R.id.pagegroup_name_input)
        mPageSearchBoxEditText = view.findViewById(R.id.page_search_box_edit_text)
        mCurrentPageListHolder = view.findViewById(R.id.current_page_list_holder)
        mSearchResultPageListHolder = view.findViewById(R.id.page_search_result_holder)
        mDoneButton = view.findViewById(R.id.done_button)
        mResetButton = view.findViewById(R.id.reset_button)
        mCancelButton = view.findViewById(R.id.cancel_button)

        //init RVs
        mCurrentPageListAdapter = CurrentPageListAdapter()
        mCurrentPageListHolder.adapter = mCurrentPageListAdapter
        ItemTouchHelper(CurrentPageSwipeToDeleteCallback()).attachToRecyclerView(mCurrentPageListHolder)

        mSearchResultPageListAdapter = SearchResultPageListAdapter()
        mSearchResultPageListHolder.adapter = mSearchResultPageListAdapter

        mDoneButton.setOnClickListener {
            doneButtonClickAction()
        }

        mResetButton.setOnClickListener {
            initView()
        }

        mCancelButton.setOnClickListener {
            cancelButtonClickAction()
        }

        mPageSearchBoxEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editableText: Editable?) {
                editableText?.let {
                    if (it.length < MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH) {
                        mSearchResultPageList.clear()
                        mSearchResultPageListAdapter.submitList(mSearchResultPageList.toList())
                    } else {

                        mDisposable.add(
                                Observable.just(it.trim().toString())
                                        .subscribeOn(Schedulers.io())
                                        .map {
                                            val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
                                            appSettingsRepository.findMatchingPages(it)
                                                    .filter {
                                                        @Suppress("SENSELESS_COMPARISON")
                                                        it != null && !mCurrentPageList.contains(it)
                                                    }.toList()
                                        }
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeWith(object : DisposableObserver<List<Page>>() {
                                            override fun onComplete() {}
                                            override fun onNext(pageList: List<Page>) {
                                                mSearchResultPageList.clear()
                                                mSearchResultPageList.addAll(pageList)
                                                mSearchResultPageListAdapter.submitList(mSearchResultPageList.toList())
                                            }

                                            override fun onError(e: Throwable) {}

                                        })
                        )
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()

        mDisposable.add(
                Observable.just(mMode)
                        .subscribeOn(Schedulers.io())
                        .map {
                            if (it == OPERATING_MODE.EDIT && mPageGroupName.isNotBlank()) {
                                val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)
                                val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
                                mPageGroup = userSettingsRepository.findPageGroupByName(mPageGroupName)
                                mPageGroup.pageList?.asSequence()
                                        ?.map { appSettingsRepository.findPageById(it) }
                                        ?.forEach {
                                            it?.let {
                                                Log.d(TAG, "Page found: ${it.name}")
                                                mPageGroup.pageEntityList.add(it)
                                            }
                                        }
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Unit>() {
                            override fun onComplete() {}
                            override fun onNext(t: Unit) {
                                initView()
                            }

                            override fun onError(e: Throwable) {
                                cancelButtonClickAction()
                            }
                        })
        )
    }

    private fun initView() {
        mCurrentPageList.clear()
        mSearchResultPageList.clear()
        Log.d(TAG, "initView")

        when (mMode) {
            OPERATING_MODE.EDIT -> {
                mPageGroupNameEditText.setText(mPageGroupName)
                mCurrentPageList.addAll(mPageGroup.pageEntityList)
                Log.d(TAG, "mCurrentPageList: ${mCurrentPageList.map { it.name }.toList()}")
            }
            OPERATING_MODE.CREATE -> {
                mPageGroupNameEditText.setText("")
            }
        }
        mCurrentPageListAdapter.submitList(mCurrentPageList.toList())
        mPageSearchBoxEditText.setText("")
        mSearchResultPageListAdapter.submitList(mSearchResultPageList.toList())
    }

    private inner class SearchResultPageListAdapter : PageListAdapter<PageSearchResultListHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageSearchResultListHolder {
            return PageSearchResultListHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.view_dragable_text_view, parent, false)
            )
        }
    }

    private inner class PageSearchResultListHolder(itemView: View) : PageViewHolder(itemView) {

        val mPageLabelTextView: AppCompatTextView
        val mHandlerIcon: AppCompatImageView
        lateinit var mPage: Page

        init {
            mPageLabelTextView = itemView.findViewById(R.id.dragable_text_view)
            mHandlerIcon = itemView.findViewById(R.id.handler_icon)
            mHandlerIcon.visibility = View.INVISIBLE
        }

        override fun bind(page: Page, parentPage: Page?, newspaper: Newspaper) {
            mPage = page
            val labelStringBuilder = StringBuilder("${page.name} | ")
            parentPage?.let { labelStringBuilder.append("${parentPage.name} | ") }
            labelStringBuilder.append("${newspaper.name}")
            mPageLabelTextView.text = labelStringBuilder.toString()
            itemView.setOnClickListener {
                Log.d(TAG, "I am clicked")
                if (!mCurrentPageList.contains(mPage)) {
                    mCurrentPageList.add(0, mPage)
                    mCurrentPageListAdapter.submitList(mCurrentPageList.toList())
                }
                mSearchResultPageList.remove(mPage)
                mSearchResultPageListAdapter.submitList(mSearchResultPageList.toList())
            }
        }
    }

    private inner class CurrentPageSwipeToDeleteCallback() :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.d(TAG, "Page to remove: ${(viewHolder as CurrentPageViewHolder).mPage.name}")
            if (mCurrentPageList.remove((viewHolder).mPage)) {
                Log.d(TAG, "Page to removed:")
                Log.d(TAG, "mCurrentPageList: ${mCurrentPageList.map { it.name }.toList()}")
                mCurrentPageListAdapter.submitList(mCurrentPageList.toList())
            }
        }
    }

    private fun checkIfModified(): Boolean {
        if (mMode == OPERATING_MODE.CREATE &&
                mPageGroupNameEditText.text.toString().isBlank() &&
                mCurrentPageList.size == 0) {
            return false
        } else return !(mMode == OPERATING_MODE.EDIT &&
                mPageGroupNameEditText.text.trim().toString().equals(mPageGroup.name) &&
                mCurrentPageList.size == mPageGroup.pageEntityList.size &&
                mCurrentPageList.filter { !mPageGroup.pageEntityList.contains(it) }.count() == 0)
    }

    private fun cancelButtonClickAction() {

        if (checkIfModified()) {
            DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                    title = "Exit!", message = "Discard changes and exit?", positiveButtonText = "Yes",
                    negetiveButtonText = "No", doOnPositivePress = {
                exit()
            })).show()
        } else {
            exit()
        }
    }

    private fun exit() {
        (activity as HomeNavigator).loadPageGroupFragment()
        (activity as NavigationHost).showAppBar(true)
        (activity as NavigationHost).disableBackPress(false) //To disable sign out
    }

    private fun doneButtonClickAction() {

        if (!checkIfModified()) {
            DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                    message = "No modifications. Exit?", doOnPositivePress = { exit() }
            )).show()
            return
        }

        val newPageGroupName = mPageGroupNameEditText.text.toString()

        var oldName = ""
        if (mMode == OPERATING_MODE.CREATE) {
            mPageGroup = PageGroup()
        } else {
            oldName = mPageGroup.name
        }
        mPageGroup.name = newPageGroupName.trim()
        mPageGroup.pageList = mCurrentPageList.map { it.id }.toList()

        val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)

        (activity as WorkInProcessWindowOperator).loadWorkInProcessWindow()
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            when (mMode) {
                                OPERATING_MODE.CREATE -> {
                                    return@map userSettingsRepository.addPageGroup(mPageGroup, context!!)
                                }
                                OPERATING_MODE.EDIT -> {
                                    return@map userSettingsRepository.savePageGroup(oldName, mPageGroup, context!!)
                                }
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {}
                            override fun onNext(t: Boolean) {

                                (activity as WorkInProcessWindowOperator).removeWorkInProcessWindow()
                                if (t) {
                                    Toast.makeText(context, "Data Saved!!", Toast.LENGTH_SHORT).show()
                                    exit()
                                } else {
                                    Toast.makeText(context, "Error!! Please retry!!", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onError(e: Throwable) {

                                (activity as WorkInProcessWindowOperator).removeWorkInProcessWindow()
                                Toast.makeText(context, "Error!! Please retry!!", Toast.LENGTH_SHORT).show()
                            }
                        })
        )
    }

    override fun onPause() {
        super.onPause()
        mDisposable.clear()
    }

    companion object {
        val TAG = "PageGroupEditFragment"
        const val ARG_PAGE_GROUP_NAME = "com.dasbikash.news_server.views.PageGroupEditFragment.ARG_PAGE_GROUP_NAME"

        fun getInstance(pageGroupName: String = ""): PageGroupEditFragment {
            val args = Bundle()
            args.putString(ARG_PAGE_GROUP_NAME, pageGroupName)
            val fragment = PageGroupEditFragment()
            fragment.setArguments(args)
            return fragment
        }
    }

    private enum class OPERATING_MODE {
        EDIT, CREATE
    }
}

class CurrentPageListAdapter : PageListAdapter<CurrentPageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentPageViewHolder {
        return CurrentPageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.view_dragable_text_view, parent, false)
        )
    }
}

class CurrentPageViewHolder(itemView: View) : PageViewHolder(itemView) {
    val mPageLabelTextView: AppCompatTextView
    lateinit var mPage: Page

    init {
        mPageLabelTextView = itemView.findViewById(R.id.dragable_text_view)
    }

    override fun bind(page: Page, parentPage: Page?, newspaper: Newspaper) {
        mPage = page
        val labelStringBuilder = StringBuilder("${page.name} | ")
        parentPage?.let { labelStringBuilder.append("${parentPage.name} | ") }
        labelStringBuilder.append("${newspaper.name}")
        mPageLabelTextView.text = labelStringBuilder.toString()
    }
}
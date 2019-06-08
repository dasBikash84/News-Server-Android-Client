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
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.OnceSettableBoolean
import com.dasbikash.news_server.view_controllers.interfaces.HomeNavigator
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_controllers.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server.view_controllers.view_helpers.PageListAdapter
import com.dasbikash.news_server.view_controllers.view_helpers.PageViewHolder
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
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
    private lateinit var mBackPressTaskTag: String

    private val mCurrentPageList = mutableListOf<Page>()
    private val mSearchResultPageList = mutableListOf<Page>()
    private lateinit var mPageGroup: PageGroup

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)
    private var mInitDone = OnceSettableBoolean()
    private var mInitInitiated = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page_group_edit, container, false)
    }

    private val MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doOnCreate()
        addBackPressAction()
    }

    private fun addBackPressAction() {
        mBackPressTaskTag = (activity as BackPressQueueManager).addToBackPressTaskQueue { cancelButtonClickAction({ addBackPressAction() }) }
    }

    private fun doOnCreate() {
        (activity as NavigationHost).showAppBar(false) //To disable sign out
        (activity as NavigationHost).showBottomNavigationView(false) //To disable navigation
    }

    private fun exit() {
        (activity as BackPressQueueManager).removeTaskFromQueue(mBackPressTaskTag)
        doOnExit()
    }

    private fun doOnExit() {
        (activity as HomeNavigator).loadPageGroupFragment()
        (activity as NavigationHost).showAppBar(true)
        (activity as NavigationHost).showBottomNavigationView(true)
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
            mPageSearchBoxEditText.setText("")
            val modificationStatus = getDataModificationStatus()
            if (modificationStatus == DataModificationStatus.MODIFIED) {
                DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                        message = DISCARD_CHANGES_MESSAGE, doOnPositivePress = { initView() }
                )).show()
            } else {
//                DisplayUtils.showShortSnack(view as CoordinatorLayout, NO_MODIFICATIONS_MESSAGE)
            }
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

        init()
    }

    private fun init() {
        if (!mInitDone.get() && !mInitInitiated) {
            mInitInitiated = true
            mDisposable.add(
                    Observable.just(mMode)
                            .subscribeOn(Schedulers.io())
                            .map {
                                if (it == OPERATING_MODE.EDIT && mPageGroupName.isNotBlank()) {
                                    val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)
                                    val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
                                    mPageGroup = userSettingsRepository.findPageGroupByName(mPageGroupName)!!
                                    mPageGroup.pageList?.asSequence()
                                            ?.map { appSettingsRepository.findPageById(it) }
                                            ?.forEach {
                                                it?.let {
                                                    LoggerUtils.debugLog("Page found: ${it.name}", this::class.java)
                                                    mPageGroup.pageEntityList.add(it)
                                                }
                                            }
                                }
                            }
                            .doOnDispose { mInitInitiated = false }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<Unit>() {
                                override fun onComplete() {}
                                override fun onNext(t: Unit) {
                                    initView()
                                    mInitDone.set()
                                    mInitInitiated = false
                                }

                                override fun onError(e: Throwable) {
                                    exit()
                                }
                            })
            )
        }
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun initView() {
        mCurrentPageList.clear()
        mSearchResultPageList.clear()
        LoggerUtils.debugLog("initView", this::class.java)

        when (mMode) {
            OPERATING_MODE.EDIT -> {
                mPageGroupNameEditText.setText(mPageGroupName)
                mCurrentPageList.addAll(mPageGroup.pageEntityList)
                LoggerUtils.debugLog("pageEntityList: ${mPageGroup.pageEntityList.map { it.name }.toList()}", this::class.java)
                LoggerUtils.debugLog("mCurrentPageList: ${mCurrentPageList.map { it.name }.toList()}", this::class.java)
            }
            OPERATING_MODE.CREATE -> {
                mPageGroupNameEditText.setText("")
            }
        }
        submitCurrentListToAdapter()
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
                LoggerUtils.debugLog("I am clicked", this::class.java)
                if (!mCurrentPageList.contains(mPage)) {
                    mCurrentPageList.add(0, mPage)
                    submitCurrentListToAdapter()
                }
                mSearchResultPageList.remove(mPage)
                mSearchResultPageListAdapter.submitList(mSearchResultPageList.toList())
            }
        }
    }

    private fun swapPagePositionOnList(page1: Page, page2: Page) {
        if (!mCurrentPageList.contains(page1) || !mCurrentPageList.contains(page2)) {
            return
        }
        val position1 = mCurrentPageList.indexOf(page1)
        val position2 = mCurrentPageList.indexOf(page2)
        mCurrentPageList.set(position1, page2)
        mCurrentPageList.set(position2, page1)
        submitCurrentListToAdapter()
    }

    private inner class CurrentPageSwipeToDeleteCallback() :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean {
            val mobilePage = (viewHolder as CurrentPageViewHolder).mPage
            val targetPage = (target as CurrentPageViewHolder).mPage
            LoggerUtils.debugLog("Mobile page: ${mobilePage.name}|${mobilePage.newspaperId}", this@PageGroupEditFragment::class.java)
            LoggerUtils.debugLog("Target page: ${targetPage.name}|${targetPage.newspaperId}", this@PageGroupEditFragment::class.java)
            swapPagePositionOnList(mobilePage, targetPage)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            LoggerUtils.debugLog("Page to remove: ${(viewHolder as CurrentPageViewHolder).mPage.name}", this::class.java)
            if (mCurrentPageList.remove((viewHolder).mPage)) {
                LoggerUtils.debugLog("Page removed", this::class.java)
                LoggerUtils.debugLog("mCurrentPageList: ${mCurrentPageList.map { it.name }.toList()}", this::class.java)
                submitCurrentListToAdapter()
            }
        }
    }

    private fun submitCurrentListToAdapter() {
        if (::mCurrentPageListAdapter.isInitialized) {
            mCurrentPageListAdapter.submitList(mCurrentPageList.toList())
        }
    }

    private enum class DataModificationStatus {MODIFIED, NO_MODIFICATION}
    private enum class DataStatus {OK, EMPTY_GROUP_TITLE, NO_PAGE_ADDED}

    private fun getDataStatus(): DataStatus{
        if (mPageGroupNameEditText.text.toString().isBlank()){
            return DataStatus.EMPTY_GROUP_TITLE
        }else if (mCurrentPageList.isEmpty()){
            return DataStatus.NO_PAGE_ADDED
        }else{
            return DataStatus.OK
        }
    }

    private fun getDataModificationStatus(): DataModificationStatus {
        if (mMode == OPERATING_MODE.CREATE &&
                mPageGroupNameEditText.text.toString().isBlank() &&
                        mCurrentPageList.isEmpty()) {
            return DataModificationStatus.NO_MODIFICATION
        } else if (mMode == OPERATING_MODE.EDIT &&
                mPageGroupNameEditText.text.trim().toString().equals(mPageGroup.name) &&
                mCurrentPageList.size == mPageGroup.pageEntityList.size &&
                mCurrentPageList.asSequence().map { Pair(mCurrentPageList.indexOf(it), it) }
                        .filter { mCurrentPageList.get(it.first).id != mPageGroup.pageEntityList.get(it.first).id }.count() == 0) {
            return DataModificationStatus.NO_MODIFICATION
        }
        return DataModificationStatus.MODIFIED
    }

    private fun cancelButtonClickAction(negitiveButtonAction: (() -> Unit)? = null) {
        if (getDataModificationStatus() == DataModificationStatus.MODIFIED) {
            DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                    message = DISCARD_CHANGES_AND_EXIT_MESSAGE, positiveButtonText = "Yes",
                    negetiveButtonText = "No", doOnPositivePress = { exit() }, doOnNegetivePress = negitiveButtonAction
                    ?: {}
                    , isCancelable = negitiveButtonAction == null))
                    .show()
        } else {
            DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                    message = EXIT_PROMPT_MESSAGE, positiveButtonText = "Yes",
                    negetiveButtonText = "No", doOnPositivePress = { exit() }, doOnNegetivePress = negitiveButtonAction
                    ?: {}
                    , isCancelable = negitiveButtonAction == null))
                    .show()
        }
    }

    private fun doneButtonClickAction() {

        if (getDataModificationStatus() == DataModificationStatus.NO_MODIFICATION) {
            DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                    message = NO_MODIFICATION_AND_EXIT_MESSAGE, doOnPositivePress = { exit() }
            )).show()
            return
        }

        val dataStatus = getDataStatus()
        if (dataStatus == DataStatus.NO_PAGE_ADDED){
            DisplayUtils.showShortSnack(view as CoordinatorLayout, NO_PAGE_ADDED_MESSAGE)
        }else if (dataStatus==DataStatus.EMPTY_GROUP_TITLE){
            DisplayUtils.showShortSnack(view as CoordinatorLayout, EMPTY_GROUP_TITLE_MESSAGE)
        }else {
            DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                    message = SAVE_CHANGES_AND_EXIT_MESSAGE, doOnPositivePress = { saveChangesAndExit() }
            )).show()
        }
    }

    private fun saveChangesAndExit() {
        val newPageGroupName = mPageGroupNameEditText.text.toString()

        val newPageGroup = PageGroup()
        newPageGroup.name = newPageGroupName.trim()
        newPageGroup.pageList = mCurrentPageList.map { it.id }.toList()

        val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)

        (activity as WorkInProcessWindowOperator).loadWorkInProcessWindow()

        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            when (mMode) {
                                OPERATING_MODE.CREATE -> {
                                    return@map userSettingsRepository.addPageGroup(newPageGroup, context!!)
                                }
                                OPERATING_MODE.EDIT -> {
                                    return@map userSettingsRepository.savePageGroup(mPageGroup.copy(), newPageGroup, context!!)
                                }
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {}
                            override fun onNext(t: Boolean) {

                                (activity as WorkInProcessWindowOperator).removeWorkInProcessWindow()
                                if (t) {
//                                    Toast.makeText(context, "Data Saved!!", Toast.LENGTH_SHORT).show()
                                    exit()
                                } else {
                                    DisplayUtils.showErrorRetryToast(context!!)
                                }
                            }

                            override fun onError(e: Throwable) {
                                if (e is NoInternertConnectionException) {
                                    NetConnectivityUtility.showNoInternetToastAnyWay(this@PageGroupEditFragment.context!!)
                                } else {
                                    LoggerUtils.debugLog(e.message ?: e::class.java.simpleName
                                    + " Error", this::class.java)
                                    DisplayUtils.showErrorRetryToast(this@PageGroupEditFragment.context!!)
                                }

                                (activity as WorkInProcessWindowOperator).removeWorkInProcessWindow()
                            }
                        })
        )
    }

    companion object {
        private const val DISCARD_CHANGES_AND_EXIT_MESSAGE = "Discard changes and exit?"
        private const val EXIT_PROMPT_MESSAGE = "Exit?"
        private const val NO_MODIFICATION_AND_EXIT_MESSAGE = "No modifications. Exit?"
        private const val NO_PAGE_ADDED_MESSAGE = "No page added!!!"
        private const val EMPTY_GROUP_TITLE_MESSAGE = "Empty page group name !!!"
        private const val NO_MODIFICATIONS_MESSAGE = "No modifications!!!"
        private const val SAVE_CHANGES_AND_EXIT_MESSAGE = "Save changes and exit?"
        private const val DISCARD_CHANGES_MESSAGE = "Discard changes and reset?"
        private const val ARG_PAGE_GROUP_NAME = "com.dasbikash.news_server.views.PageGroupEditFragment.ARG_PAGE_GROUP_NAME"

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
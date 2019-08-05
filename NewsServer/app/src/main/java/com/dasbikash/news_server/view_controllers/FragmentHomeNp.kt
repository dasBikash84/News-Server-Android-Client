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

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_controllers.view_helpers.PageDiffCallback
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.ImageUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

class FragmentHomeNp : Fragment() {

    private val MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH = 3

    private lateinit var mPageSearchTextBoxContainer: TextInputLayout
    private lateinit var mPageSearchTextBox: EditText
    private lateinit var mPageSearchResultHolder: RecyclerView
    private lateinit var mPageSearchResultContainer: ViewGroup
    private lateinit var mPageSearchBoxShowButton: ImageView

    private lateinit var mSelectBanglaPapers: AppCompatTextView
    private lateinit var mSelectEnglishPapers: AppCompatTextView

    private lateinit var mNewsPaperMenuHolder: RecyclerView
    private lateinit var mNewsPaperNameListContainer: ViewGroup
    private lateinit var mNewsPaperMenuShowButton: ImageView
    private lateinit var mNewsPaperMenuHideButton: ImageView
    private lateinit var mNewsPaperListAdapter: NewsPaperListAdapter
    private lateinit var mNewsPaperScrollerContainer: ViewGroup


    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    private lateinit var mPageArticlePreviewHolder: RecyclerView
    private lateinit var mPageArticlePreviewScroller: NestedScrollView
    private lateinit var mPageArticlePreviewHolderAdapter:PageListAdapter//

    private var mSearchResultListAdapter = SearchResultListAdapter()
    private var backPressTaskTag: String? = null

    private val MENU_BUTTON_VISIBILITY_CHECK_INTERVAL = 100L
    private val MENU_BUTTON_HIDE_TIME_MS = 1000L
    private var mLastArticlePreviewScrollTime: Long = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_np, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewComponents(view)
        setListnersForViewComponents()
        init()
    }

    override fun onResume() {
        super.onResume()
        initMenuButtonOperator()
    }

    override fun onPause() {
        super.onPause()
        if (backPressTaskTag != null) {
            (activity as BackPressQueueManager).removeTaskFromQueue(backPressTaskTag!!)
        }
        if (backPressTaskTagForNpMenu != null) {
            (activity as BackPressQueueManager).removeTaskFromQueue(backPressTaskTagForNpMenu!!)
        }
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
        mPageArticlePreviewScroller = view.findViewById(R.id.article_preview_scroller)
    }

    private fun setListnersForViewComponents() {
        mPageSearchBoxShowButton.setOnClickListener { showPageSearchBox() }

        mPageSearchTextBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                text?.let {
                    if (it.trim().length >= MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH) {
                        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(this@FragmentHomeNp.context!!)
                        mDisposable.add(
                                Observable.just(it.trim().toString())
                                        .subscribeOn(Schedulers.io())
                                        .map {
                                            debugLog("Page Search string: $it")
                                            appSettingsRepository.findMatchingPages(it).filter {
                                                @Suppress("SENSELESS_COMPARISON")
                                                debugLog("Page: " + it.toString())
                                                it != null
                                            }.toList()
                                        }
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeWith(object : DisposableObserver<List<Page>>() {
                                            override fun onComplete() {}
                                            override fun onNext(pageList: List<Page>) {
                                                debugLog(pageList.toString())
                                                mPageSearchResultContainer.visibility = View.VISIBLE
                                                mPageSearchResultContainer.bringToFront()
                                                mPageSearchResultContainer.setOnClickListener({
                                                    mPageSearchResultContainer.visibility = View.GONE
                                                })
                                                mSearchResultListAdapter.submitList(pageList)
                                                if (pageList.size > 0) {
                                                    if (backPressTaskTag != null) {
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
                    } else {
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
        mPageArticlePreviewScroller.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                if (mPageSearchTextBox.visibility == View.VISIBLE) {
                    if (scrollY >= mPageSearchTextBox.height) {
                        mPageSearchTextBox.visibility = View.GONE
                    }
                } else {
                    if (scrollY == 0) {
                        mPageSearchTextBox.visibility = View.VISIBLE
                    }
                }
                if (mNewsPaperMenuHideButton.visibility == View.VISIBLE) {
                    hideNewsPaperMenu()
                }
                mNewsPaperMenuShowButton.visibility = View.GONE
                mLastArticlePreviewScrollTime = System.currentTimeMillis()
            }
        })
    }

    private fun init() {
        (activity as NavigationHost)
                .showBottomNavigationView(true)
        showPageSearchBox()
        showNewsPaperMenu()
        mNewsPaperScrollerContainer.bringToFront()
        mPageSearchResultHolder.adapter = mSearchResultListAdapter
        mNewsPaperListAdapter = NewsPaperListAdapter { doOnNewsPaperNameClick(it) }
        mNewsPaperMenuHolder.adapter = mNewsPaperListAdapter
        mPageArticlePreviewHolderAdapter = PageListAdapter(mDisposable,ViewModelProviders.of(activity!!).get(HomeViewModel::class.java))
        mPageArticlePreviewHolder.adapter = mPageArticlePreviewHolderAdapter

        ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
                .getNewsPapersLiveData().observe(this, object : Observer<List<Newspaper>> {
                    override fun onChanged(list: List<Newspaper>?) {
                        if (list != null && list.isNotEmpty()) {
                            mNewsPaperListAdapter.submitList(list.sortedBy { it.getPosition() }.reversed().sortedBy { it.languageId }.reversed())
                        } else {
                            mNewsPaperListAdapter.submitList(emptyList())
                        }
                    }
                })
    }


    private fun initMenuButtonOperator() {
        mDisposable.add(
        Observable.create(fun(emitter: ObservableEmitter<NP_MENU_BUTTON_OPERATION_ACTION>) {
            do {
                SystemClock.sleep(MENU_BUTTON_VISIBILITY_CHECK_INTERVAL)
                emitter.onNext(determineMenuButtonOperationAction())
            } while (true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<NP_MENU_BUTTON_OPERATION_ACTION>() {
                    override fun onComplete() {}
                    override fun onNext(action: NP_MENU_BUTTON_OPERATION_ACTION) {
                        if (action == NP_MENU_BUTTON_OPERATION_ACTION.SHOW){
                            mNewsPaperMenuShowButton.visibility = View.VISIBLE
                        }
                    }
                    override fun onError(e: Throwable) {}
                }))
    }

    private fun hideNewsPaperMenu() {
        hideNewsPaperMenuHolder()
        mNewsPaperMenuHideButton.visibility = View.GONE
        mNewsPaperMenuShowButton.visibility = View.VISIBLE
        if (backPressTaskTagForNpMenu != null) {
            (activity as BackPressQueueManager).removeTaskFromQueue(backPressTaskTagForNpMenu!!)
        }
    }

    private fun hideNewsPaperMenuHolder() {
        mNewsPaperMenuHolder.visibility = View.GONE
//        ObjectAnimator.ofFloat(mNewsPaperMenuHolder,"scaleX",1f,0f).setDuration(100L).start()
//        ObjectAnimator.ofFloat(mNewsPaperMenuHolder,"scaleY",1f,0f).setDuration(100L).start()
    }

    var backPressTaskTagForNpMenu:String? = null

    private fun showNewsPaperMenu() {
        showNewsPaperMenuHolder()
        mNewsPaperMenuHideButton.visibility = View.VISIBLE
        mNewsPaperMenuShowButton.visibility = View.GONE

        if (backPressTaskTagForNpMenu != null) {
            (activity as BackPressQueueManager).removeTaskFromQueue(backPressTaskTagForNpMenu!!)
        }
        backPressTaskTagForNpMenu =
                (activity as BackPressQueueManager).addToBackPressTaskQueue {
                    hideNewsPaperMenu()
                }
    }

    private fun showNewsPaperMenuHolder() {
        mNewsPaperMenuHolder.visibility = View.VISIBLE
//        ObjectAnimator.ofFloat(mNewsPaperMenuHolder,"scaleX",0f,1f).setDuration(100L).start()
//        ObjectAnimator.ofFloat(mNewsPaperMenuHolder,"scaleY",0f,1f).setDuration(100L).start()
    }

    private fun determineMenuButtonOperationAction(): NP_MENU_BUTTON_OPERATION_ACTION {
        if (mNewsPaperMenuHideButton.visibility == View.GONE &&
                mNewsPaperMenuShowButton.visibility == View.GONE) {
            if (System.currentTimeMillis() - mLastArticlePreviewScrollTime > MENU_BUTTON_HIDE_TIME_MS) {
                return NP_MENU_BUTTON_OPERATION_ACTION.SHOW
            }
        }
        return NP_MENU_BUTTON_OPERATION_ACTION.NONE
    }

    fun doOnNewsPaperNameClick(newspaper: Newspaper) {
        mDisposable.add(
                Observable.just(newspaper)
                        .subscribeOn(Schedulers.io())
                        .map {
                            RepositoryFactory.getAppSettingsRepository(context!!).getTopPagesForNewspaper(it)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<Page>>() {
                            override fun onComplete() {}
                            override fun onNext(pageList: List<Page>) {
                                mPageArticlePreviewHolderAdapter.submitList(pageList.sortedBy { it.id })
                            }

                            override fun onError(e: Throwable) {
                                mPageArticlePreviewHolderAdapter.submitList(emptyList())
                            }
                        }))
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

    private enum class NP_MENU_BUTTON_OPERATION_ACTION {
        NONE, SHOW
    }
}

object NewsPaperDiffCallback : DiffUtil.ItemCallback<Newspaper>() {
    override fun areItemsTheSame(oldItem: Newspaper, newItem: Newspaper): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Newspaper, newItem: Newspaper): Boolean {
        return oldItem == newItem
    }
}

class NewsPaperListAdapter(val doOnItemClick: (Newspaper) -> Unit) : ListAdapter<Newspaper, NewsPaperNameHolder>(NewsPaperDiffCallback) {

    private val viewHolderList = mutableListOf<NewsPaperNameHolder>()
    private var currentPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsPaperNameHolder {
        val newsPaperNameHolder = NewsPaperNameHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.view_news_paper_label, parent, false))
        newsPaperNameHolder.itemView.setOnClickListener {
            currentPosition = newsPaperNameHolder.position
            doOnItemClick(newsPaperNameHolder.getNewsPaper())
            viewHolderList.filter { it != newsPaperNameHolder }.forEach { it.highlightText(false) }
            newsPaperNameHolder.highlightText(true)
        }
        viewHolderList.add(newsPaperNameHolder)
        return newsPaperNameHolder
    }

    override fun onBindViewHolder(holder: NewsPaperNameHolder, position: Int) {
        if (position == 0 && currentPosition == null) {
            currentPosition = position
            doOnItemClick(getItem(position))
        }
        holder.bind(getItem(position), position, currentPosition)
    }
}

class NewsPaperNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private lateinit var mNewspaper: Newspaper
    private val mNormalTextView: TextView
    private val mBoldTextView: TextView
    var position: Int? = null

    init {
        mNormalTextView = itemView.findViewById(R.id.normal_title_text_view)
        mBoldTextView = itemView.findViewById(R.id.bold_title_text_view)
    }

    fun bind(newspaper: Newspaper, position: Int, currentPosition: Int?) {
        mNewspaper = newspaper
        this.position = position

        mNormalTextView.setText(mNewspaper.name)
        mBoldTextView.setText(mNewspaper.name)
        if (position == currentPosition) {
            highlightText(true)
        } else {
            highlightText(false)
        }
    }

    fun highlightText(highlight: Boolean) {
        if (highlight) {
            mBoldTextView.visibility = View.VISIBLE
            mNormalTextView.visibility = View.GONE
        } else {
            mBoldTextView.visibility = View.GONE
            mNormalTextView.visibility = View.VISIBLE
        }
    }

    fun getNewsPaper() = mNewspaper
}

class PageListAdapter(val lifeCycleAwareCompositeDisposable: LifeCycleAwareCompositeDisposable,
                      val homeViewModel: HomeViewModel) :
        ListAdapter<Page, LatestArticlePreviewHolder>(PageDiffCallback), DefaultLifecycleObserver {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestArticlePreviewHolder {
        return LatestArticlePreviewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_article_preview, parent, false), lifeCycleAwareCompositeDisposable,homeViewModel)
    }

    override fun onBindViewHolder(holder: LatestArticlePreviewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class LatestArticlePreviewHolder(itemView: View, val lifeCycleAwareCompositeDisposable: LifeCycleAwareCompositeDisposable,
                                val homeViewModel: HomeViewModel)
    : RecyclerView.ViewHolder(itemView) {

    val pageTitle: TextView
    val articlePreviewImage: ImageView
    val articleTitle: TextView
    val articlePublicationTime: TextView
    val articleTextPreview: TextView

    val articleTitlePlaceHolder: TextView
    val articlePublicationTimePlaceHolder: TextView
    val articleTextPreviewPlaceHolder: TextView

    lateinit var mdisposable: Disposable
    lateinit var mPage: Page


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

        articleTitle.visibility = View.VISIBLE
        articlePublicationTime.visibility = View.VISIBLE
        articleTextPreview.visibility = View.VISIBLE
    }

    private fun resetView() {
        disableView()
        pageTitle.text = mPage.name
        pageTitle.visibility = View.VISIBLE
    }

    fun bind(page: Page) {
        mPage = page
        resetView()
        if (::mdisposable.isInitialized) {
            mdisposable.dispose()
        }
        val uuid = UUID.randomUUID()
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(itemView.context)
        mdisposable = homeViewModel.getLatestArticleProvider(Pair(uuid, mPage))
                .filter { it.first == uuid }
                .map {
                    it.second?.let {
                        val dateString = DisplayUtils.getArticlePublicationDateString(
                                        it, appSettingsRepository.getLanguageByPage(mPage), itemView.context)
                        return@map Pair(dateString, it)
                    }
                    return@map Any()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Any>() {
                    override fun onComplete() {
                        LoggerUtils.debugLog("onComplete for page ${mPage.name} Np: ${mPage.newspaperId} L2", this::class.java)
                    }

                    @Suppress("UNCHECKED_CAST")
                    override fun onNext(articleData: Any) {
                        if (articleData is Pair<*, *>) {
                            LoggerUtils.debugLog("art displayed for page: ${mPage.name} Np: ${mPage.newspaperId}", this::class.java)
                            val articlePubTimeText = (articleData as Pair<String?, Article>).first
                            val article = (articleData as Pair<String?, Article>).second

                            articleTitle.text = article.title
                            articlePublicationTime.text = articlePubTimeText
                            DisplayUtils.displayHtmlText(articleTextPreview, article.articleText?: "")
                            enableView()

                            ImageUtils.customLoader(articlePreviewImage, article.previewImageLink,
                                    R.drawable.pc_bg, R.drawable.app_big_logo)

                            //Add click listner
                            itemView.setOnClickListener(View.OnClickListener {
                                itemView.context.startActivity(
                                        PageViewActivity.getIntentForPageDisplay(
                                                itemView.context, page
                                        )
                                )
                            })
                        }
                    }

                    override fun onError(e: Throwable) {
                        if (e is CompositeException) {
                            if (e.exceptions.filter { it is NoInternertConnectionException }.count() > 0) {
                                NetConnectivityUtility.showNoInternetToast(itemView.context)
                            } else if (e.exceptions.filter { it is DataNotFoundException }.count() > 0) {
                                LoggerUtils.debugLog("DataNotFoundException", this::class.java)
                            } else if (e.exceptions.filter { it is DataServerException }.count() > 0) {
                                LoggerUtils.debugLog("DataServerException", this::class.java)
                            }
                        }
                        LoggerUtils.debugLog(e.message + "${e::class.java.simpleName} for page Np: ${mPage.newspaperId} ${mPage.name} L2", this::class.java)
                    }
                })
        lifeCycleAwareCompositeDisposable.add(mdisposable)
    }
}
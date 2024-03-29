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
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.*
import com.dasbikash.news_server.view_controllers.view_helpers.PageDiffCallback
import com.dasbikash.news_server.view_models.NSViewModel
import com.dasbikash.news_server_data.data_sources.data_services.AppVersionDetails
import com.dasbikash.news_server_data.exceptions.DataNotFoundException
import com.dasbikash.news_server_data.exceptions.DataServerException
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.AppVersionDetailsRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.ImageUtils
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*


class FragmentHome : Fragment() {

    companion object {
        private val ONE_DAY_IN_MS = 24 * 60 * 60 * 1000
        private val ONE_HOUR_IN_MS = 60 * 60 * 1000
        private const val SP_KEY_NEXT_UPDATE_PROMPT_TIME = "com.dasbikash.news_server.view_controllers.FragmentHome.SP_KEY_NEXT_UPDATE_PROMPT_TIME"
        private const val UPDATE_TO_NEW_VERSION_PROMPT = "Update available on app store."
        private const val ARG_ARTICLE = "com.dasbikash.news_server.view_controllers.FragmentHome.ARG_ARTICLE"

        fun getInstance(article: Article): FragmentHome {
            val args = Bundle()
            args.putParcelable(ARG_ARTICLE, article)
            val fragment = FragmentHome()
            fragment.setArguments(args)
            return fragment
        }
    }

    private val MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH = 3

    private lateinit var mPageSearchTextBoxContainer: TextInputLayout
    private lateinit var mPageSearchTextBox: EditText
    private lateinit var mPageSearchResultHolder: RecyclerView
    private lateinit var mPageSearchResultContainer: ViewGroup

    private lateinit var mNewsPaperMenuHolderScroller: NestedScrollView
    private lateinit var mNewsPaperMenuHolder: RecyclerView
    private lateinit var mNewsPaperListAdapter: NewsPaperListAdapter

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    private var mSearchResultListAdapter = SearchResultListAdapter()

    private var backPressTaskTagForPageSearchResults: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewComponents(view)
        setListnersForViewComponents()
        init()
    }

    private fun findViewComponents(view: View) {
        mPageSearchTextBoxContainer = view.findViewById(R.id.page_search_text_box_layout)
        mPageSearchTextBox = view.findViewById(R.id.page_search_box_edit_text)
        mPageSearchResultHolder = view.findViewById(R.id.page_search_result_holder)
        mPageSearchResultContainer = view.findViewById(R.id.page_search_result_container)
        mNewsPaperMenuHolder = view.findViewById(R.id.np_name_holder)
        mNewsPaperMenuHolderScroller = view.findViewById(R.id.np_name_holder_scroller)
    }

    private fun setListnersForViewComponents() {

        mPageSearchTextBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                text?.let {
                    if (it.trim().length >= MINIMUM_CHAR_LENGTH_FOR_PAGE_SEARCH) {
                        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(this@FragmentHome.context!!)
                        mDisposable.add(
                                Observable.just(it.trim().toString())
                                        .subscribeOn(Schedulers.io())
                                        .map {
                                            appSettingsRepository.findMatchingPages(it).filter {
                                                @Suppress("SENSELESS_COMPARISON")
                                                it != null
                                            }.toList()
                                        }
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeWith(object : DisposableObserver<List<Page>>() {
                                            override fun onComplete() {}
                                            override fun onNext(pageList: List<Page>) {
                                                removeBackPressTaskForPageSrearchResults()
                                                addBackPressTaskForPageSrearchResults()
                                                mSearchResultListAdapter.submitList(pageList)

                                                if (pageList.isNotEmpty()) {
                                                    mPageSearchResultContainer.visibility = View.VISIBLE
                                                    mPageSearchResultContainer.bringToFront()
                                                } else {
                                                    mPageSearchResultContainer.visibility = View.GONE
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

        mPageSearchResultContainer.setOnClickListener {
            mPageSearchResultContainer.visibility = View.GONE
            removeBackPressTaskForPageSrearchResults()
        }

        mNewsPaperMenuHolderScroller.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {

                debugLog("scrollY:$scrollY")

                if (mPageSearchTextBoxContainer.visibility == View.VISIBLE) {
                    if (scrollY >= mPageSearchTextBoxContainer.height) {
                        hidePageSearchBox()
                    }
                } else {
                    if (scrollY == 0) {
                        showPageSearchBox()
                    }
                }
            }
        })
    }

    fun addBackPressTaskForPageSrearchResults() {
        backPressTaskTagForPageSearchResults =
                (activity!! as BackPressQueueManager).addToBackPressTaskQueue {
                    mSearchResultListAdapter.submitList(emptyList())
                    mPageSearchResultContainer.visibility = View.GONE
                    backPressTaskTagForPageSearchResults = null
                }
    }

    fun removeBackPressTaskForPageSrearchResults() {
        if (backPressTaskTagForPageSearchResults != null) {
            (activity!! as BackPressQueueManager).removeTaskFromQueue(backPressTaskTagForPageSearchResults!!)
        }
    }

    var mFcmArticle: Article? = null
    var mFcmArticlehandled = false
    var mInitDone = false

    private fun init() {
        arguments?.let { mFcmArticle = it.getParcelable<Article>(ARG_ARTICLE) }
    }

    override fun onResume() {
        super.onResume()
        if (mFcmArticle != null && !mFcmArticlehandled) {
            mFcmArticlehandled = true
            startActivity(ActivityArticleView.getIntentForArticleView(context!!, mFcmArticle!!))
        } else {
            if (!mInitDone) {
                initView()
            } else {
                if (mPageSearchResultContainer.visibility == View.VISIBLE) {
                    addBackPressTaskForPageSrearchResults()
                }
            }
            checkIfNeedAppUpdate()
        }
    }

    private fun checkIfNeedAppUpdate() {
        val nextUpdatePromptTs = getNextUpdatePromptTs()
        if (nextUpdatePromptTs != NEXT_UPDATE_PROMPT_TIME_OPTIONS.NEVER.timeStamp &&
                System.currentTimeMillis() > nextUpdatePromptTs) {
            mDisposable.add(
                    Observable.just(true)
                            .subscribeOn(Schedulers.io())
                            .map { AppVersionDetailsRepository.getLatestVersionDetails() }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableObserver<AppVersionDetails>() {
                                override fun onComplete() {}

                                override fun onNext(lastVersionDetails: AppVersionDetails) {
                                    lastVersionDetails.versionCode?.let {
                                        if (it > AppInfoUtils.getAppVersionNumber(context!!)) {
                                            launchAppUpdateDialog()
                                        } else {
                                            saveNextUpdatePromptTs(NEXT_UPDATE_PROMPT_TIME_OPTIONS.REMIND_SIX_HOUR_LATER)
                                        }
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    saveNextUpdatePromptTs(NEXT_UPDATE_PROMPT_TIME_OPTIONS.REMIND_TOMORROW)
                                }
                            })
            )
        }
    }

    private fun launchAppUpdateDialog() {
        val view = LayoutInflater.from(context!!).inflate(R.layout.view_version_update_promt_dialog, null)
        val dialogBuilder =
                DialogUtils.getAlertDialogBuilder(context!!, DialogUtils.AlertDialogDetails(
                        title = UPDATE_TO_NEW_VERSION_PROMPT,
                        isCancelable = false,
                        doOnPositivePress = {
                            if (view.findViewById<RadioButton>(R.id.remind_me_later_button).isChecked) {
                                positivePressAction(VERSION_UPDATE_CANCEL_ACTION.REMIND_LATER)
                            } else if (view.findViewById<RadioButton>(R.id.never_ask_me_again_button).isChecked) {
                                positivePressAction(VERSION_UPDATE_CANCEL_ACTION.NEVER_ASK_AGAIN)
                            } else if (view.findViewById<RadioButton>(R.id.update_button).isChecked) {
                                positivePressAction(VERSION_UPDATE_CANCEL_ACTION.UPDATE)
                            }
                        },
                        doOnNegetivePress = {
                            positivePressAction(VERSION_UPDATE_CANCEL_ACTION.NONE)
                        }
                ))
        dialogBuilder.setView(view)
        dialogBuilder.create().show()
    }

    private enum class VERSION_UPDATE_CANCEL_ACTION {
        NONE, REMIND_LATER, NEVER_ASK_AGAIN, UPDATE
    }

    private enum class NEXT_UPDATE_PROMPT_TIME_OPTIONS(val timeStamp: Long) {
        NEVER(-1), REMIND_TOMORROW(System.currentTimeMillis() + ONE_DAY_IN_MS),
        REMIND_SIX_HOUR_LATER(System.currentTimeMillis() + 6 * ONE_HOUR_IN_MS)
    }

    private fun getNextUpdatePromptTs(): Long {
        val data = SharedPreferenceUtils.getData(context!!, SharedPreferenceUtils.DefaultValues.DEFAULT_LONG,
                SP_KEY_NEXT_UPDATE_PROMPT_TIME) as Long
        return data
    }

    private fun saveNextUpdatePromptTs(nextUpdatePromptTimeOptions: NEXT_UPDATE_PROMPT_TIME_OPTIONS) {
        SharedPreferenceUtils.saveData(context!!, nextUpdatePromptTimeOptions.timeStamp, SP_KEY_NEXT_UPDATE_PROMPT_TIME)
    }


    private fun positivePressAction(action: VERSION_UPDATE_CANCEL_ACTION) {
        when (action) {
            VERSION_UPDATE_CANCEL_ACTION.UPDATE -> updateToNewVersion()
            VERSION_UPDATE_CANCEL_ACTION.REMIND_LATER -> setRemindLater()
            VERSION_UPDATE_CANCEL_ACTION.NEVER_ASK_AGAIN -> setNeverAskForUpdate()
            else -> {
                saveNextUpdatePromptTs(NEXT_UPDATE_PROMPT_TIME_OPTIONS.REMIND_SIX_HOUR_LATER)
            }
        }
    }

    private fun updateToNewVersion() {
        saveNextUpdatePromptTs(NEXT_UPDATE_PROMPT_TIME_OPTIONS.REMIND_SIX_HOUR_LATER)
        try {
            val updateIntent = Intent(Intent.ACTION_VIEW, Uri.parse(OptionsIntentBuilderUtility.getRawAppLink(context!!)))
            activity!!.startActivity(updateIntent);
        } catch (e: Throwable) {
            DisplayUtils.showShortToast(context!!, "No application can handle this request."
                    + " Please install a webbrowser.")
        }
    }

    private fun setNeverAskForUpdate() {
        saveNextUpdatePromptTs(NEXT_UPDATE_PROMPT_TIME_OPTIONS.NEVER)
    }

    private fun setRemindLater() {
        saveNextUpdatePromptTs(NEXT_UPDATE_PROMPT_TIME_OPTIONS.REMIND_TOMORROW)
    }

    override fun onPause() {
        super.onPause()
        removeBackPressTaskForPageSrearchResults()
    }

    @SuppressLint("CheckResult")
    private fun initView() {
        mInitDone = true
        showPageSearchBox()
        mPageSearchResultContainer.visibility = View.GONE
        mPageSearchResultHolder.adapter = mSearchResultListAdapter
        mNewsPaperListAdapter = NewsPaperListAdapter(this, ViewModelProviders.of(activity!!).get(NSViewModel::class.java))
        mNewsPaperMenuHolder.adapter = mNewsPaperListAdapter

        Observable.just(true)
                .subscribeOn(Schedulers.io())
                .map { RepositoryFactory.getAppSettingsRepository(context!!).getNewsPapers() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val paperList = it
                    if (paperList.isNotEmpty()) {
                        mNewsPaperListAdapter.submitList(paperList.sortedBy { it.getNumberPartOfId() }.sortedBy { it.languageId })
                    } else {
                        activity!!.finish()
                    }
                }
    }

    private fun showPageSearchBox() {
        mPageSearchTextBoxContainer.visibility = View.VISIBLE
    }

    private fun hidePageSearchBox() {
        mPageSearchTextBoxContainer.visibility = View.GONE
        mPageSearchTextBox.setText("")
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

class NewsPaperListAdapter(val lifeCycleOwner: LifecycleOwner, val viewModel: NSViewModel)
    : ListAdapter<Newspaper, NewsPaperNameHolder>(NewsPaperDiffCallback) {

    init {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsPaperNameHolder {
        val newsPaperNameHolder = NewsPaperNameHolder(lifeCycleOwner, viewModel,
                LayoutInflater.from(parent.context).inflate(R.layout.view_article_preview_for_newspaper, parent, false))
        return newsPaperNameHolder
    }

    override fun onBindViewHolder(holder: NewsPaperNameHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class NewsPaperNameHolder(lifeCycleOwner: LifecycleOwner, viewModel: NSViewModel, itemView: View)
    : RecyclerView.ViewHolder(itemView) {

    private lateinit var mNewspaper: Newspaper
    private val mTitleTextView: TextView
    private val mPageArticlePreviewHolder: RecyclerView
    private val mPageListAdapter: PageListAdapter
    private var mPageListAdapterInitiated = false
    private var mDisposable: Disposable? = null

    init {
        mTitleTextView = itemView.findViewById(R.id.np_title_textview)
        mPageArticlePreviewHolder = itemView.findViewById(R.id.page_article_preview_holder)
        mPageListAdapter = PageListAdapter(lifeCycleOwner, viewModel)
        mPageArticlePreviewHolder.adapter = mPageListAdapter
    }

    private fun resetView() {
        mPageArticlePreviewHolder.visibility = View.GONE
        normalizeTitle()
        mPageListAdapterInitiated = false
        mDisposable?.dispose()
    }

    fun bind(newspaper: Newspaper) {
        resetView()
        mNewspaper = newspaper

        mTitleTextView.setText(mNewspaper.name)
        mTitleTextView.setOnClickListener { titleClickListner() }
    }

    private fun titleClickListner() {
        if (mPageArticlePreviewHolder.visibility == View.VISIBLE) {
            normalizeTitle()
            mPageArticlePreviewHolder.visibility = View.GONE
        } else {
            if (!mPageListAdapterInitiated) {
                initPreviewHolder()
            }
            highlightTitle()
            mPageArticlePreviewHolder.visibility = View.VISIBLE
        }
    }

    private fun highlightTitle() {
        mTitleTextView.setTypeface(null, Typeface.BOLD_ITALIC)
    }

    private fun normalizeTitle() {
        mTitleTextView.setTypeface(null, Typeface.NORMAL.or(Typeface.ITALIC))
    }

    private fun initPreviewHolder() {
        mDisposable?.dispose()
        mDisposable = Observable.just(mNewspaper)
                .subscribeOn(Schedulers.io())
                .map { RepositoryFactory.getAppSettingsRepository(itemView.context).getPagesForNewspaper(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<List<Page>>() {
                    override fun onComplete() {}
                    override fun onNext(pageList: List<Page>) {
                        mPageListAdapter.submitList(pageList.sortedBy { it.getNumberPartOfId() })
                        mPageListAdapterInitiated = true
                    }

                    override fun onError(e: Throwable) {}
                })
    }
}

class PageListAdapter(val lifeCycleOwner: LifecycleOwner,
                      val NSViewModel: NSViewModel) :
        ListAdapter<Page, LatestArticlePreviewHolder>(PageDiffCallback), DefaultLifecycleObserver {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestArticlePreviewHolder {
        return LatestArticlePreviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_article_preview_home, parent, false),
                lifeCycleOwner, NSViewModel)
    }

    override fun onBindViewHolder(holder: LatestArticlePreviewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class LatestArticlePreviewHolder(itemView: View, lifeCycleOwner: LifecycleOwner,
                                 val NSViewModel: NSViewModel)
    : RecyclerView.ViewHolder(itemView), DefaultLifecycleObserver {

    val pageTitle: TextView
    val articlePreviewImage: ImageView
    val articleTitle: TextView
    val articlePublicationTime: TextView

    val articleTitlePlaceHolder: TextView
    val articlePublicationTimePlaceHolder: TextView

    lateinit var mdisposable: Disposable
    lateinit var mPage: Page

    init {

        pageTitle = itemView.findViewById(R.id.page_title)
        articlePreviewImage = itemView.findViewById(R.id.article_preview_image)

        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)

        articleTitlePlaceHolder = itemView.findViewById(R.id.article_title_ph)
        articlePublicationTimePlaceHolder = itemView.findViewById(R.id.article_time_ph)

        articleTitlePlaceHolder.setOnClickListener { }
        articlePublicationTimePlaceHolder.setOnClickListener { }

        lifeCycleOwner.lifecycle.addObserver(this)
        disableView()
    }

    override fun onPause(owner: LifecycleOwner) {
        if (::mdisposable.isInitialized) {
            mdisposable.dispose()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        if (::mPage.isInitialized) {
            bind(mPage)
        }
    }

    private fun disableView() {
        pageTitle.visibility = View.GONE
        articleTitle.visibility = View.GONE
        articlePublicationTime.visibility = View.GONE

        articleTitlePlaceHolder.visibility = View.VISIBLE
        articlePublicationTimePlaceHolder.visibility = View.VISIBLE
        ImageUtils.customLoader(imageView = articlePreviewImage,
                defaultImageResourceId = R.drawable.pc_bg,
                placeHolderImageResourceId = R.drawable.pc_bg)
    }

    private fun enableView() {

        articleTitlePlaceHolder.visibility = View.GONE
        articlePublicationTimePlaceHolder.visibility = View.GONE

        articleTitle.visibility = View.VISIBLE
        articlePublicationTime.visibility = View.VISIBLE
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
        mdisposable = NSViewModel.getLatestArticleProvider(Pair(uuid, mPage))
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
                    override fun onComplete() {}

                    @Suppress("UNCHECKED_CAST")
                    override fun onNext(articleData: Any) {
                        if (articleData is Pair<*, *>) {
                            val articlePubTimeText = (articleData as Pair<String?, Article>).first
                            val article = articleData.second

                            articleTitle.text = article.title
                            articlePublicationTime.text = articlePubTimeText

                            enableView()

                            ImageUtils.customLoader(articlePreviewImage, article.previewImageLink,
                                    R.drawable.pc_bg, R.drawable.app_big_logo)

                            //Add click listner
                            itemView.setOnClickListener(View.OnClickListener {
                                itemView.context.startActivity(
                                        ActivityArticlePreview.getIntentForLatestArticleDisplay(
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
    }
}

class SearchResultListAdapter() : PageListAdapter2<SearchResultEntryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultEntryViewHolder {
        return SearchResultEntryViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.view_page_label, parent, false)
        )
    }
}

class SearchResultEntryViewHolder(itemView: View) : PageViewHolder(itemView) {

    private val textView: TextView
    private val bottomBar: View

    init {
        textView = itemView.findViewById(R.id.title_text_view)
        bottomBar = itemView.findViewById(R.id.bottom_bar)
        bottomBar.visibility = View.GONE
    }

    override fun bind(page: Page, newspaper: Newspaper) {
        val pageLabelBuilder = StringBuilder(page.name!!).append(" | ")
        pageLabelBuilder.append(newspaper.name)
        textView.setText(pageLabelBuilder.toString())

        itemView.setOnClickListener {
            itemView.context
                    .startActivity(ActivityArticlePreview.getIntentForLatestArticleDisplay(itemView.context, page))
        }
    }
}

abstract class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(page: Page, newspaper: Newspaper)
}

abstract class PageListAdapter2<VH : PageViewHolder>() : ListAdapter<Page, VH>(PageDiffCallback) {

    val mDisposable = CompositeDisposable()

    override fun onBindViewHolder(holder: VH, position: Int) {
        mDisposable.add(
                Observable.just(getItem(position))
                        .subscribeOn(Schedulers.io())
                        .map {
                            val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(holder.itemView.context)
                            val newspaper = appSettingsRepository.getNewspaperByPage(it)
                            Pair(it, newspaper)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Pair<Page, Newspaper>>() {
                            override fun onComplete() {}
                            override fun onNext(dataPair: Pair<Page, Newspaper>) {
                                holder.bind(dataPair.first, dataPair.second)
                            }

                            override fun onError(e: Throwable) {
                                holder.itemView.visibility = View.GONE
                            }

                        })
        )
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mDisposable.clear()
    }

}


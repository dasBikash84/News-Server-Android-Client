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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server.view_controllers.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server.view_controllers.view_helpers.PageDiffCallback
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FavouritesFragment : Fragment() {

    private lateinit var mCoordinatorLayout: CoordinatorLayout
    private lateinit var mScroller: NestedScrollView
    private lateinit var mFavItemsHolder: RecyclerView
    private lateinit var mNoFavPageMsgHolder: LinearLayout
    private lateinit var mNoFavPageLogInButton: MaterialButton

    lateinit var mFavouritePagesListAdapter: FavouritePagesListAdapter

    private val disposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    private lateinit var mHomeViewModel: HomeViewModel

    private val postLogInAction = { mNoFavPageLogInButton.visibility = View.GONE }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCoordinatorLayout = view.findViewById(R.id.fav_frag_coor_layout)
        mScroller = view.findViewById(R.id.fav_frag_item_scroller)
        mFavItemsHolder = view.findViewById(R.id.fav_frag_item_holder)
        mNoFavPageMsgHolder = view.findViewById(R.id.no_fav_page_found_message_holder)
        mNoFavPageLogInButton = view.findViewById(R.id.no_fav_page_found_log_in_button)

        mFavouritePagesListAdapter = FavouritePagesListAdapter()

        mHomeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)

        mNoFavPageLogInButton.setOnClickListener { (activity!! as SignInHandler).launchSignInActivity({ postLogInAction() }) }

        mFavItemsHolder.adapter = mFavouritePagesListAdapter
        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
        val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)

        ItemTouchHelper(FavPageSwipeToDeleteCallback(mFavouritePagesListAdapter, activity!! as SignInHandler, activity!! as WorkInProcessWindowOperator, postLogInAction))
                .attachToRecyclerView(mFavItemsHolder)
        if (userSettingsRepository.checkIfLoggedIn()) {
            postLogInAction()
        }

        mHomeViewModel.getUserPreferenceLiveData()
                .observe(activity!!, object : Observer<UserPreferenceData?> {
                    override fun onChanged(userPreferenceData: UserPreferenceData?) {
                        if (userPreferenceData == null) {
                            mNoFavPageMsgHolder.visibility = View.VISIBLE
                            mScroller.visibility = View.GONE
                        }
                        userPreferenceData.let {
                            val favouritePageIdList = it?.favouritePageIds?.toList() ?: emptyList()
                            disposable.add(Observable.just(favouritePageIdList)
                                    .subscribeOn(Schedulers.io())
                                    .map {
                                        it.asSequence()
                                                .map {
                                                    debugLog(it.toString())
                                                    it
                                                }.filter { appSettingsRepository.findPageById(it) !=null }
                                                .map {
                                                    val page = appSettingsRepository.findPageById(it)!!
                                                    debugLog("$it : $page")
                                                    page
                                                }
                                                .sortedBy { it.name }
                                                .toList()
                                    }
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeWith(object : DisposableObserver<List<Page>>() {
                                        override fun onComplete() {}
                                        override fun onNext(pageList: List<Page>) {
                                            if (pageList.isEmpty()) {
                                                mNoFavPageMsgHolder.visibility = View.VISIBLE
                                                mScroller.visibility = View.GONE
                                            } else {
                                                mNoFavPageMsgHolder.visibility = View.GONE
                                                mScroller.visibility = View.VISIBLE
                                            }
                                            pageList.asSequence().forEach { debugLog(it.toString()) }
                                            mFavouritePagesListAdapter.submitList(pageList)
                                        }

                                        override fun onError(e: Throwable) {}
                                    }))
                        }
                    }
                })

    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = TITLE_TEXT
    }

    companion object{
        private const val TITLE_TEXT = "Favourite Pages"
    }
}

class FavouritePagesListAdapter() :
        ListAdapter<Page, FavouritePagePreviewHolder>(PageDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritePagePreviewHolder {
        return FavouritePagePreviewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.view_article_perview_for_fav_page, parent, false))
    }

    override fun onBindViewHolder(holder: FavouritePagePreviewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            it.context.startActivity(
                    ActivityArticlePreview.getIntentForPageBrowsing(it.context, getItem(position)))
        }
        holder.bind(getItem(position))
    }
}

class FavouritePagePreviewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {

    private val pageTitle: AppCompatTextView
    private val pageTitleHolder: MaterialCardView

    lateinit var mPage: Page
    lateinit var mNewspaper: Newspaper

    lateinit var mDisposable: Disposable

    init {
        pageTitleHolder = itemview.findViewById(R.id.page_title_holder) as MaterialCardView
        pageTitle = itemview.findViewById(R.id.page_title)
    }

    fun bind(page: Page) {

        mPage = page

        if (::mDisposable.isInitialized){
            debugLog("mDisposable.dispose()")
            mDisposable.dispose()
        }

        mDisposable = Observable.just(mPage)
                            .subscribeOn(Schedulers.computation())
                            .map {
                                debugLog("On bind map: $it")
                                val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(itemView.context)
                                Pair(it, appSettingsRepository.getNewspaperByPage(it))
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer {
                                debugLog("On bind subscribe: ${it.first}")
                                debugLog("On bind subscribe: ${it.second}")
                                mNewspaper = it.second
                                pageTitle.text = StringBuilder().append(mPage.name).append(" | ").append(mNewspaper.name).toString()
                                pageTitleHolder.visibility = View.VISIBLE
                            })
    }

}

class FavPageSwipeToDeleteCallback(val favouritePagesListAdapter: FavouritePagesListAdapter,
                                   val signInHandler: SignInHandler,
                                   val workInProcessWindowOperator: WorkInProcessWindowOperator,
                                   val postLogInAction: () -> Unit) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        val page = (viewHolder as FavouritePagePreviewHolder).mPage
        val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(viewHolder.itemView.context)

        val message = "Remove \"${page.name}\" from favourites?"
        val positiveText = "Yes"
        val negetiveAction: () -> Unit = {
            favouritePagesListAdapter.notifyDataSetChanged()
        }
        val positiveAction: () -> Unit = {
            workInProcessWindowOperator.loadWorkInProcessWindow()
            Observable.just(page)
                    .subscribeOn(Schedulers.io())
                    .map { userSettingsRepository.removePageFromFavList(page, viewHolder.itemView.context) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : io.reactivex.Observer<Boolean> {
                        override fun onComplete() {
                            workInProcessWindowOperator.removeWorkInProcessWindow()
                        }

                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(result: Boolean) {
                            if (!result) {
                                favouritePagesListAdapter.notifyDataSetChanged()
                            }
                        }

                        override fun onError(e: Throwable) {
                            if (e is NoInternertConnectionException) {
                                NetConnectivityUtility.showNoInternetToastAnyWay(viewHolder.itemView.context)
                            } else {
                                LoggerUtils.debugLog(e.message ?: e::class.java.simpleName
                                + " Error", this@FavPageSwipeToDeleteCallback::class.java)
                                DisplayUtils.showErrorRetryToast(viewHolder.itemView.context)
                            }
                            workInProcessWindowOperator.removeWorkInProcessWindow()
                            favouritePagesListAdapter.notifyDataSetChanged()
                        }
                    })
        }

        val removeFavItemDialog =
                DialogUtils.createAlertDialog(
                        viewHolder.itemView.context,
                        DialogUtils.AlertDialogDetails(
                                message = message, positiveButtonText = positiveText,
                                doOnPositivePress = positiveAction, doOnNegetivePress = negetiveAction,
                                isCancelable = false
                        )
                )

        if (userSettingsRepository.checkIfLoggedIn()) {
            removeFavItemDialog.show()
        } else {
            DialogUtils.createAlertDialog(
                    viewHolder.itemView.context,
                    DialogUtils.AlertDialogDetails(
                            message = message, positiveButtonText = "Sign in and continue",
                            doOnPositivePress = {
                                signInHandler.launchSignInActivity({ postLogInAction() })
                            }, doOnNegetivePress = negetiveAction,
                            isCancelable = false
                    )
            ).show()
        }
    }
}
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
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.view_controllers.interfaces.NavigationHost
import com.dasbikash.news_server.view_controllers.interfaces.WorkInProcessWindowOperator
import com.dasbikash.news_server.view_controllers.view_helpers.PageGroupDiffCallback
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import com.google.android.material.card.MaterialCardView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class PageGroupFragment : Fragment() {

    private lateinit var mPageGroupListScroller: NestedScrollView
    private lateinit var mPageGroupListHolder: RecyclerView

    private lateinit var mPageGroupListAdapter: PageGroupListAdapter

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPageGroupListScroller = view.findViewById(R.id.page_group_list_scroller)
        mPageGroupListHolder = view.findViewById(R.id.page_group_list_holder)

        mPageGroupListAdapter =
                PageGroupListAdapter(activity!!.supportFragmentManager, this,
                        activity!! as SignInHandler, activity as NavigationHost, activity as WorkInProcessWindowOperator)
        mPageGroupListHolder.adapter = mPageGroupListAdapter

        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)

        ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
                .getPageGroupsLiveData().observe(this, object : Observer<List<PageGroup>> {
                    override fun onChanged(pageGroups: List<PageGroup>?) {
                        pageGroups?.let {
                            mDisposable.add(
                                    Observable.just(it)
                                            .subscribeOn(Schedulers.io())
                                            .map {
                                                it.asSequence()
                                                        .forEach {
                                                            val thisPageGroup = it
                                                            it.pageList?.let {
                                                                it.asSequence().forEach {
                                                                    appSettingsRepository.findPageById(it)?.let { thisPageGroup.pageEntityList.add(it) }
                                                                }
                                                            }
                                                        }
                                                it
                                            }
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeWith(object : DisposableObserver<List<PageGroup>>() {
                                                override fun onComplete() {}
                                                override fun onNext(items: List<PageGroup>) {
                                                    mPageGroupListAdapter.submitList(items)
                                                }

                                                override fun onError(e: Throwable) {}
                                            })
                            )
                        }
                    }
                })

        view.findViewById<AppCompatButton>(R.id.add_page_group_button)
                .setOnClickListener {

                    val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)

                    val notLoggedInDialog = DialogUtils.createAlertDialog(
                            context!!,
                            DialogUtils.AlertDialogDetails(
                                    message = "Add new page group?",
                                    positiveButtonText = "Sign in and continue",
                                    doOnPositivePress = {
                                        (activity as SignInHandler).launchSignInActivity({
                                            (activity as NavigationHost)
                                                    .addFragment(PageGroupEditFragment.getInstance())
                                        })
                                    }
                            )
                    )

                    if (userSettingsRepository.checkIfLoggedIn()) {
                        (activity as NavigationHost)
                                .addFragment(PageGroupEditFragment.getInstance())
                    } else {
                        notLoggedInDialog.show()
                    }
                }

    }
}


class PageGroupListAdapter(val fragmentManager: FragmentManager, val lifecycleOwner: LifecycleOwner,
                           val signInHandler: SignInHandler, val navigationHost: NavigationHost, val workInProcessWindowOperator: WorkInProcessWindowOperator)
    : ListAdapter<PageGroup, PageGroupHolder>(PageGroupDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageGroupHolder {
        val pagePreviewHolder = PageGroupHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.view_page_group_item, parent, false), fragmentManager,
                signInHandler, navigationHost, workInProcessWindowOperator)

        lifecycleOwner.lifecycle.addObserver(pagePreviewHolder)

        return pagePreviewHolder
    }

    override fun onBindViewHolder(holder: PageGroupHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onFailedToRecycleView(holder: PageGroupHolder): Boolean {
        lifecycleOwner.lifecycle.removeObserver(holder)
        return super.onFailedToRecycleView(holder)
    }
}

class PageGroupHolder(itemView: View, val fragmentManager: FragmentManager,
                      val signInHandler: SignInHandler, val navigationHost: NavigationHost,
                      val workInProcessWindowOperator: WorkInProcessWindowOperator)
    : RecyclerView.ViewHolder(itemView), DefaultLifecycleObserver {

    private val titleHolder: MaterialCardView
    private val titleText: TextView
    private val editIcon: ImageButton
    private val frameLayout: FrameLayout

    private val rightArrow: ImageView
    private val downArrow: ImageView

    private var active = true

    lateinit var mPageGroup: PageGroup

    init {
        titleHolder = itemView.findViewById(R.id.page_group_title_holder)
        titleText = itemView.findViewById(R.id.page_group_title)
        rightArrow = itemView.findViewById(R.id.right_arrow)
        downArrow = itemView.findViewById(R.id.down_arrow)
        editIcon = itemView.findViewById(R.id.edit_page_group_icon)
        frameLayout = itemView.findViewById(R.id.page_group_items_holder)
        frameLayout.id = DisplayUtils.getNextViewId(itemView.context)//View.generateViewId()

        editIcon.setOnClickListener {
            if (::mPageGroup.isInitialized) {

                val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(itemView.context)

                val positiveText: String = "Delete group"
                val neutralText: String = "Edit group"
                val pageGroupDeletionAction: () -> Unit = {
                    DialogUtils.createAlertDialog(
                            itemView.context,
                            DialogUtils.AlertDialogDetails(
                                    message = "Proceed with deletion of \"${mPageGroup.name}\" page group?",
                                    positiveButtonText = "Yes",
                                    doOnPositivePress = {
                                        workInProcessWindowOperator.loadWorkInProcessWindow()
                                        Observable.just(mPageGroup)
                                                .subscribeOn(Schedulers.io())
                                                .map {
                                                    userSettingsRepository.deletePageGroup(mPageGroup, itemView.context)
                                                }
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeWith(object : DisposableObserver<Boolean>() {
                                                    override fun onComplete() {
                                                        workInProcessWindowOperator.removeWorkInProcessWindow()
                                                    }

                                                    override fun onNext(result: Boolean) {

                                                    }

                                                    override fun onError(e: Throwable) {
                                                        LoggerUtils.printStackTrace(e)
                                                        if (e is NoInternertConnectionException) {
                                                            NetConnectivityUtility.showNoInternetToastAnyWay(itemView.context)
                                                        } else {
                                                            LoggerUtils.debugLog(e.message
                                                                    ?: e.cause.toString()
                                                                    ?: e::class.java.simpleName
                                                                    + " Error", this::class.java)
                                                            DisplayUtils.showShortToast(itemView.context!!, "Error!! Please retry.")
                                                        }
                                                        workInProcessWindowOperator.removeWorkInProcessWindow()
                                                    }
                                                })
                                    }

                            )
                    ).show()
                }
                val pageGroupEditFragmentLaunchAction: () -> Unit = {
                    Observable.just(mPageGroup)
                            .subscribeOn(Schedulers.io())
                            .map {
                                userSettingsRepository.findPageGroupByName(it.name)
                            }
                            .subscribeWith(object :DisposableObserver<PageGroup?>(){
                                override fun onComplete() {}
                                override fun onNext(pageGroup: PageGroup) {
                                    navigationHost.addFragment(PageGroupEditFragment.getInstance(pageGroup.name))
                                }
                                override fun onError(e: Throwable) {}
                            })


                }

                val modifyActionDialog = DialogUtils.createAlertDialog(
                        itemView.context,
                        DialogUtils.AlertDialogDetails(
                                message = "Edit \"${mPageGroup.name}\" group?",
                                positiveButtonText = positiveText, neutralButtonText = neutralText,
                                doOnPositivePress = pageGroupDeletionAction, doOnNeutralPress = pageGroupEditFragmentLaunchAction
                        )
                )

                val logInPromptPositiveText = "Sign in and continue"
                val logInPromptPositiveAction: () -> Unit = {
                    signInHandler.launchSignInActivity({
//                        modifyActionDialog.show()
                    })
                }

                val notLoggedInDialog = DialogUtils.createAlertDialog(
                        itemView.context,
                        DialogUtils.AlertDialogDetails(
                                message = "Edit \"${mPageGroup.name}\" group?",
                                positiveButtonText = logInPromptPositiveText,
                                doOnPositivePress = logInPromptPositiveAction
                        )
                )

                if (userSettingsRepository.checkIfLoggedIn()) {
                    modifyActionDialog.show()
                } else {
                    notLoggedInDialog.show()
                }
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        active = true
    }

    override fun onPause(owner: LifecycleOwner) {
        active = false
    }

    override fun onStop(owner: LifecycleOwner) {
        active = false
    }

    override fun onDestroy(owner: LifecycleOwner) {
        active = false
    }

    fun bind(item: PageGroup?) {

        mPageGroup = item!!

        @Suppress("SENSELESS_COMPARISON")
        if (item != null) {
            titleText.setText(item.name)
            rightArrow.visibility = View.VISIBLE
            downArrow.visibility = View.GONE
            frameLayout.visibility = View.GONE
            editIcon.visibility = View.GONE

            item.apply {

                try {
                    val fragment = when {
                        this.pageEntityList.size == 1 -> {
                            FragmentArticlePreviewForPages
                                    .getInstanceForScreenFillPreview(this.pageEntityList.first(), this.pageEntityList.size)
                        }
                        this.pageEntityList.size > 1 -> {
                            FragmentArticlePreviewForPages
                                    .getInstanceForCustomWidthPreview(this.pageEntityList.filter { it != null }.toList(), this.pageEntityList.size)
                        }
                        else -> {
                            null
                        }
                    }
                    if (fragment != null && active) {
                        fragmentManager.beginTransaction().replace(frameLayout.id, fragment).commit()
                    }
                } catch (ex: Exception) {
                    LoggerUtils.printStackTrace(ex)
                }
            }

            titleHolder.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    if (frameLayout.visibility == View.GONE) {
                        frameLayout.visibility = View.VISIBLE
                        downArrow.visibility = View.VISIBLE
                        rightArrow.visibility = View.GONE
                        editIcon.visibility = View.VISIBLE
                    } else {
                        frameLayout.visibility = View.GONE
                        editIcon.visibility = View.GONE
                        downArrow.visibility = View.GONE
                        rightArrow.visibility = View.VISIBLE
                    }
                }
            })

        } else {
            itemView.visibility = View.GONE
        }
    }

}


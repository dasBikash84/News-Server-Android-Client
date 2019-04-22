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
import android.util.Log
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
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server.views.interfaces.NavigationHost
import com.dasbikash.news_server.views.rv_helpers.PageGroupDiffCallback
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.google.android.material.card.MaterialCardView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class PageGroupFragment : Fragment() {

    private val TAG = "PageGroupFragment"

    private lateinit var mPageGroupListScroller: NestedScrollView
    private lateinit var mPageGroupListHolder: RecyclerView

    private lateinit var mPageGroupListAdapter: PageGroupListAdapter

    private val mDisposable = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPageGroupListScroller = view.findViewById(R.id.page_group_list_scroller)
        mPageGroupListHolder = view.findViewById(R.id.page_group_list_holder)

        mPageGroupListAdapter =
                PageGroupListAdapter(activity!!.supportFragmentManager, this,
                        activity!! as SignInHandler, activity as NavigationHost)
        mPageGroupListHolder.adapter = mPageGroupListAdapter

        val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)

        ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
                .getPageGroups().observe(this, object : Observer<List<PageGroup>> {
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
                                    message = "Add new page group?", negetiveButtonText = "Cancel",
                                    positiveButtonText = "Sign in and continue",
                                    doOnPositivePress = {
                                        (activity as SignInHandler).launchSignInActivity()
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

    override fun onPause() {
        super.onPause()
        mDisposable.clear()
    }
}


class PageGroupListAdapter(val fragmentManager: FragmentManager, val lifecycleOwner: LifecycleOwner,
                           val signInHandler: SignInHandler, val navigationHost: NavigationHost)
    : ListAdapter<PageGroup, PageGroupHolder>(PageGroupDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageGroupHolder {
        val pagePreviewHolder = PageGroupHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.view_page_group_item, parent, false), fragmentManager, signInHandler, navigationHost)

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
                      val signInHandler: SignInHandler, val navigationHost: NavigationHost)
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
        frameLayout.id = View.generateViewId()

        editIcon.setOnClickListener {
            if (::mPageGroup.isInitialized) {

                val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(itemView.context)

                val positiveText: String = "Delete group"
                val negetiveText: String = "Cancel"
                val neutralText: String = "Add/remove page(s)"
                val pageGroupDeletionAction: () -> Unit = {
                    DialogUtils.createAlertDialog(
                            itemView.context,
                            DialogUtils.AlertDialogDetails(
                                    message = "Proceed with deletion of \"${mPageGroup.name}\" page group?",
                                    positiveButtonText = "Yes", negetiveButtonText = "Cancel",
                                    doOnPositivePress = {
                                        Observable.just(mPageGroup)
                                                .subscribeOn(Schedulers.io())
                                                .map {
                                                    userSettingsRepository.deletePageGroup(mPageGroup, itemView.context)
                                                }
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeWith(object : DisposableObserver<Boolean>() {
                                                    override fun onComplete() {

                                                    }

                                                    override fun onNext(result: Boolean) {

                                                    }

                                                    override fun onError(e: Throwable) {

                                                    }
                                                })
                                    }

                            )
                    ).show()
                }
                val pageGroupEditFragmentLaunchAction: () -> Unit = {
                    navigationHost.addFragment(PageGroupEditFragment.getInstance(mPageGroup.name))
                }

                val modifyActionDialog = DialogUtils.createAlertDialog(
                        itemView.context,
                        DialogUtils.AlertDialogDetails(
                                message = "Edit \"${mPageGroup.name}\" group?",
                                positiveButtonText = positiveText, negetiveButtonText = negetiveText, neutralButtonText = neutralText,
                                doOnPositivePress = pageGroupDeletionAction, doOnNeutralPress = pageGroupEditFragmentLaunchAction
                        )
                )

                val logInPromptNegetiveText: String = "Cancel"
                val logInPromptPositiveText: String = "Sign in and continue"
                val logInPromptPositiveAction: () -> Unit = {
                    signInHandler.launchSignInActivity()
                }

                val notLoggedInDialog = DialogUtils.createAlertDialog(
                        itemView.context,
                        DialogUtils.AlertDialogDetails(
                                message = "Edit \"${mPageGroup.name}\" group?",
                                positiveButtonText = logInPromptPositiveText, negetiveButtonText = logInPromptNegetiveText,
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
        Log.d("NpPerviewFragment", "active = false for pageGroup:${mPageGroup.name}")
        active = false
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("NpPerviewFragment", "active = false for pageGroup:${mPageGroup.name}")
        active = false
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d("NpPerviewFragment", "active = false for pageGroup:${mPageGroup.name}")
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
                            FragmentArticlePreviewForPages.getInstanceForScreenFillPreview(this.pageEntityList.first())
                        }
                        this.pageEntityList.size > 1 -> {
                            FragmentArticlePreviewForPages.getInstanceForCustomWidthPreview(this.pageEntityList.filter { it != null }.toList())
                        }
                        else -> {
                            null
                        }
                    }
                    if (fragment != null && active) {
                        fragmentManager.beginTransaction().replace(frameLayout.id, fragment).commit()
//                        fragmentManager.executePendingTransactions()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
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


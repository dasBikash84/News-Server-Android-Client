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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.views.rv_helpers.PageGroupDiffCallback
import com.dasbikash.news_server_data.RepositoryFactory
import com.dasbikash.news_server_data.display_models.entity.PageGroup
import com.google.android.material.card.MaterialCardView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class PageGroupFragment : Fragment() {

    private val TAG = "PageGroupFragment"

    private lateinit var mPageGroupListScroller:NestedScrollView
    private lateinit var mPageGroupListHolder:RecyclerView

    private lateinit var mPageGroupListAdapter : PageGroupListAdapter

    private val mDisposable = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPageGroupListScroller = view.findViewById(R.id.page_group_list_scroller)
        mPageGroupListHolder = view.findViewById(R.id.page_group_list_holder)

        mPageGroupListAdapter = PageGroupListAdapter(activity!!.supportFragmentManager)

        mPageGroupListHolder.adapter = mPageGroupListAdapter

        val settingsRepository = RepositoryFactory.getSettingsRepository(context!!)

        mDisposable.add(
        Observable.just(true)
                .subscribeOn(Schedulers.io())
                .map {
                    settingsRepository.getPageGroupList()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<List<PageGroup>>(){
                    override fun onComplete() {
                    }

                    override fun onNext(items: List<PageGroup>) {
                        mPageGroupListAdapter.submitList(items)
                    }


                    override fun onError(e: Throwable) {
                    }
                })
        )

    }

    override fun onPause() {
        super.onPause()
        mDisposable.clear()
    }
}


class PageGroupListAdapter(val fragmentManager: FragmentManager):ListAdapter<PageGroup,PageGroupHolder>(PageGroupDiffCallback){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageGroupHolder {
        return PageGroupHolder( LayoutInflater.from(parent.context).inflate(
                                R.layout.view_page_group_item,parent,false),fragmentManager)
    }
    override fun onBindViewHolder(holder: PageGroupHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PageGroupHolder(itemView: View,val fragmentManager: FragmentManager):RecyclerView.ViewHolder(itemView){

    private val titleHolder:MaterialCardView
    private val titleText:TextView
    private val rightArrow:ImageView
    private val downArrow:ImageView
    private val editIcon:ImageButton
    private val frameLayout:FrameLayout

    init {
//        itemView.id = View.generateViewId()
        titleHolder = itemView.findViewById(R.id.page_group_title_holder)
        titleText = itemView.findViewById(R.id.page_group_title)
        rightArrow = itemView.findViewById(R.id.right_arrow)
        downArrow = itemView.findViewById(R.id.down_arrow)
        editIcon = itemView.findViewById(R.id.edit_page_group_icon)
        frameLayout = itemView.findViewById(R.id.page_group_items_holder)
        frameLayout.id = View.generateViewId()
    }
    fun bind(item: PageGroup?) {

        if (item!=null){
            titleText.setText(item.name)
            rightArrow.visibility = View.VISIBLE
            downArrow.visibility = View.GONE
            frameLayout.visibility = View.GONE

            item.apply {
                when {
                    this.pageEntityList.size == 1 -> {
                        fragmentManager
                                .beginTransaction()
                                .replace(
                                        frameLayout.id,
                                        FragmentArticlePreviewForPages.getInstanceForScreenFillPreview(this.pageEntityList.first())
                                )
                                .commit()
                    }
                    this.pageEntityList.size > 1 -> {
                        fragmentManager
                                .beginTransaction()
                                .replace(
                                        frameLayout.id,
                                        FragmentArticlePreviewForPages.getInstanceForCustomWidthPreview(this.pageEntityList.filter { it!=null }.toList())
                                )
                                .commit()
                    }
                    else -> {
                        itemView.visibility = View.GONE
                    }
                }
            }

            titleHolder.setOnClickListener(object : View.OnClickListener{
                override fun onClick(v: View?) {
                    if (frameLayout.visibility == View.GONE){
                        frameLayout.visibility = View.VISIBLE
                        downArrow.visibility = View.VISIBLE
                        rightArrow.visibility = View.GONE
                    }else{
                        frameLayout.visibility = View.GONE
                        downArrow.visibility = View.GONE
                        rightArrow.visibility = View.VISIBLE
                    }
                }
            })

        }else{
            itemView.visibility = View.GONE
        }
    }

}

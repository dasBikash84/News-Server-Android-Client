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

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.RepositoryFactory
import com.dasbikash.news_server_data.display_models.entity.Newspaper
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.repositories.SettingsRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class NewspaperPerviewFragment : Fragment() {

    private lateinit var mNewspaper: Newspaper
    private lateinit var mHomeViewModel: HomeViewModel

    private lateinit var mPagePreviewList:RecyclerView//newspaper_page_preview_list
    private lateinit var mListAdapter:PagePreviewListAdapter

    private val mTopPageList = mutableListOf<Page>()

    private val mDisposable = CompositeDisposable()
    lateinit var mArticleCountObservable:Observable<Int>
    lateinit var mSettingsRepository:SettingsRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_newspaper_page_list_preview_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSettingsRepository = RepositoryFactory.getSettingsRepository(activity!!)

        mNewspaper = arguments!!.getSerializable(ARG_NEWS_PAPAER) as Newspaper
        mPagePreviewList = view.findViewById(R.id.newspaper_page_preview_list)
        mHomeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)


        //Create observable from ArticleCountForNewspaper Livedata
        mArticleCountObservable =
                Observable.fromPublisher<Int> {
                    LiveDataReactiveStreams
                            .toPublisher(this,mHomeViewModel.mArticleCountForNewspaperMap.get(mNewspaper)!!)
                }

        mListAdapter = PagePreviewListAdapter.getPagePreviewListAdapter(activity!!.applicationContext,mArticleCountObservable)
        mPagePreviewList.adapter = mListAdapter

        mDisposable.add(
            Observable.just(true)
                .subscribeOn(Schedulers.io())
                .map {
                    mSettingsRepository
                            .getTopPagesForNewspaper(mNewspaper)
                            .sortedBy { it.id }
                            .toCollection(mutableListOf())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mListAdapter.submitList(it)
                })
        )

    }

    override fun onPause() {
        super.onPause()
        mDisposable.clear()
    }

    companion object {

        val ARG_NEWS_PAPAER = "com.dasbikash.news_server.views.NewspaperPerviewFragment.ARG_NEWS_PAPAER"
        val TAG = "NpPerviewFragment"

        fun getInstance(newspaper: Newspaper): NewspaperPerviewFragment {
            val args = Bundle()
            args.putSerializable(ARG_NEWS_PAPAER, newspaper)
            val fragment = NewspaperPerviewFragment()
            fragment.setArguments(args)
            return fragment
        }
    }


}

class PagePreviewHolder(itemView: View,articleCountObservable: Observable<Int>) : RecyclerView.ViewHolder(itemView) {

    val childListArticlePreviewRV : RecyclerView
    val selfArticlePreview:CardView
    val selfPageTitle:TextView
    val selfArticlePreviewImage:ImageView
    val selfArticleTitle:TextView
    val selfArticlePublicationTime:TextView

    //val disposable:Disposable

    init {

//        disposable = articleCountObservable.

        childListArticlePreviewRV = itemView.findViewById(R.id.top_page_child_list_preview)
        selfArticlePreview = itemView.findViewById(R.id.child_less_top_page_preview)
        selfPageTitle = selfArticlePreview.findViewById(R.id.child_less_top_page_title)
        selfArticlePreviewImage = selfArticlePreview.findViewById(R.id.child_less_top_page_article_preview_image)
        selfArticleTitle = selfArticlePreview.findViewById(R.id.child_less_top_page_article_title)
        selfArticlePublicationTime = selfArticlePreview.findViewById(R.id.child_less_top_page_article_time)
    }

    fun bind(page: Page){
        childListArticlePreviewRV.visibility = View.GONE
        selfArticlePreviewImage.visibility = View.GONE
        selfArticleTitle.visibility = View.GONE
        selfArticlePublicationTime.visibility = View.GONE

        if (!page.hasChild) {
            selfPageTitle.setText(page.name)
            if(page.hasData){
                //getDataOfLatestArticleAndDisplay

            }
        }else{
            selfPageTitle.setText("Child pages will come here for page: ${page.name}")
            //Make page list that has data

            //fetch latest article data for all pages

            //Init recyclerView with fetched article list

        }



    }


}

class PagePreviewListAdapter(diffCallback: DiffUtil.ItemCallback<Page>,
                             val context: Context,
                             var articleCountObservable: Observable<Int>?) :
        ListAdapter<Page, PagePreviewHolder>(diffCallback) {

    companion object{
        fun getPagePreviewListAdapter(context: Context,articleCountObservable: Observable<Int>):PagePreviewListAdapter{
            val diffCallback = object : DiffUtil.ItemCallback<Page>() {
                override fun areItemsTheSame(oldItem: Page, newItem: Page): Boolean {
                    return oldItem.id == newItem.id
                }
                override fun areContentsTheSame(oldItem: Page, newItem: Page): Boolean {
                    return oldItem == newItem
                }
            }
            return PagePreviewListAdapter(diffCallback,context,articleCountObservable)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagePreviewHolder {
//        val view =  LayoutInflater.from(context).inflate(R.layout.view_page_label,parent,false)
        val view =  LayoutInflater.from(context).inflate(R.layout.view_top_page_child_list_preview_holder,parent,false)
        return PagePreviewHolder(view,articleCountObservable!!)//PagePreviewHolder(TextView(context))
    }
    override fun onBindViewHolder(holder: PagePreviewHolder, position: Int) {
//        (holder.itemView as TextView).setText(getItem(position).name)
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: PagePreviewHolder) {
        super.onViewRecycled(holder)
    }

    override fun onViewAttachedToWindow(holder: PagePreviewHolder) {
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: PagePreviewHolder) {
        super.onViewDetachedFromWindow(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        articleCountObservable = null
    }

}

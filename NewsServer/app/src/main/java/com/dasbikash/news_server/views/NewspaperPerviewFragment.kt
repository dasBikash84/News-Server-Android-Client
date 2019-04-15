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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server.views.interfaces.BottomNavigationViewOwner
import com.dasbikash.news_server_data.RepositoryFactory
import com.dasbikash.news_server_data.display_models.entity.Article
import com.dasbikash.news_server_data.display_models.entity.Language
import com.dasbikash.news_server_data.display_models.entity.Newspaper
import com.dasbikash.news_server_data.display_models.entity.Page
import com.dasbikash.news_server_data.repositories.SettingsRepository
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_newspaper_page_list_preview_holder.*

class NewspaperPerviewFragment : Fragment() {

    private lateinit var mNewspaper: Newspaper
    private lateinit var mLanguage: Language
    private lateinit var mHomeViewModel: HomeViewModel

    private lateinit var mPagePreviewList:RecyclerView//newspaper_page_preview_list
    private lateinit var mListAdapter:PagePreviewListAdapter

    private val mDisposable = CompositeDisposable()
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

        (activity as BottomNavigationViewOwner)
                .showBottomNavigationView(true)

        page_preview_scroller.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener{
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val remainingYOfRV = mPagePreviewList.height-scrollY-resources.displayMetrics.heightPixels
                if (remainingYOfRV < resources.displayMetrics.heightPixels/5){
                    (activity as BottomNavigationViewOwner)
                            .showBottomNavigationView(false)
                }else{
                    (activity as BottomNavigationViewOwner)
                            .showBottomNavigationView(true)
                }
            }
        })

        mDisposable.add(
            Observable.just(true)
                .subscribeOn(Schedulers.io())
                .map {
                    mLanguage = mSettingsRepository.getLanguageByNewspaper(mNewspaper)
                    mSettingsRepository
                            .getTopPagesForNewspaper(mNewspaper)
                            .sortedBy { it.id }
                            .toCollection(mutableListOf())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mListAdapter = PagePreviewListAdapter(activity!!.applicationContext,mLanguage)
                    mPagePreviewList.adapter = mListAdapter
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

object PageDiffCallback:DiffUtil.ItemCallback<Page>(){
    override fun areItemsTheSame(oldItem: Page, newItem: Page): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: Page, newItem: Page): Boolean {
        return oldItem == newItem
    }
}

class PagePreviewListAdapter(val context: Context,val language: Language) :
        ListAdapter<Page, PagePreviewHolder>(PageDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagePreviewHolder {
        val view =  LayoutInflater.from(context).inflate(R.layout.view_top_page_child_list_preview_holder,parent,false)
        return PagePreviewHolder(view,language)
    }
    override fun onBindViewHolder(holder: PagePreviewHolder, position: Int) {
        holder.disposable.clear()
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: PagePreviewHolder) {
        super.onViewRecycled(holder)
        holder.disposable.clear()
    }

}

class PagePreviewHolder(itemView: View,val language: Language) : RecyclerView.ViewHolder(itemView) {

    companion object{
        val TAG = "NpPerviewFragment"
    }

    val childListArticlePreviewRV : RecyclerView
    val selfArticlePreview:CardView
    val selfPageTitle:TextView
    val selfArticlePreviewImage:ImageView
    val selfArticleTitle:TextView
    val selfArticlePublicationTime:TextView

    val disposable = CompositeDisposable()

    init {
        childListArticlePreviewRV = itemView.findViewById(R.id.top_page_child_list_preview)

        selfArticlePreview = itemView.findViewById(R.id.child_less_top_page_preview)
        selfPageTitle = selfArticlePreview.findViewById(R.id.child_less_top_page_title)
        selfArticlePreviewImage = selfArticlePreview.findViewById(R.id.child_less_top_page_article_preview_image)
        selfArticleTitle = selfArticlePreview.findViewById(R.id.child_less_top_page_article_title)
        selfArticlePublicationTime = selfArticlePreview.findViewById(R.id.child_less_top_page_article_time)
    }

    @SuppressLint("CheckResult")
    fun bind(page: Page){

        childListArticlePreviewRV.visibility = View.GONE

        selfPageTitle.visibility = View.GONE
        selfArticlePreviewImage.visibility = View.GONE
        selfArticleTitle.visibility = View.GONE
        selfArticlePublicationTime.visibility = View.GONE

        val settingsRepository = RepositoryFactory.getSettingsRepository(itemView.context)
        val newsDataRepository = RepositoryFactory.getNewsDataRepository(itemView.context)

        disposable.add(
            Observable.just(true)
                .subscribeOn(Schedulers.io())
                .map {
                    Log.d(TAG,"Start for page ${page.name} Np: ${page.newsPaperId}")
                    settingsRepository
                            .getChildPagesForTopLevelPage(page)
                            .asSequence()
                            .filter { it.getHasData() }
                            .sortedBy { it.id }
                            .toCollection(mutableListOf<Page>())
                    //if()
                }
                .map {
                    if (it.size>0){
                        Log.d(TAG,"page ${page.name} Np: ${page.newsPaperId} has ${it.size} child pages")
                        if (page.getHasData()) it.set(0,page)
                        it
                    }else{
                        Log.d(TAG,"page ${page.name} Np: ${page.newsPaperId} has no child pages")
                        page
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Any>(){
                    override fun onComplete() {
                        Log.d(TAG,"onComplete for page ${page.name} Np: ${page.newsPaperId} L1")
                    }
                    override fun onNext(data: Any) {
                        Log.d(TAG,"Display block for page ${page.name} Np: ${page.newsPaperId}")
                        when(data){
                            is Page ->{
                                Log.d(TAG,"page ${page.name} Np: ${page.newsPaperId} own article display")
                                data.apply {
                                    if (getHasData()){
                                        Log.d(TAG,"page ${page.name} Np: ${page.newsPaperId} has data")
                                        disposable.add(
                                                Observable.just(this)
                                                        .subscribeOn(Schedulers.io())
                                                        .map {
                                                            val article = newsDataRepository.getLatestArticleByPage(it)
                                                            article?.let {
                                                                val dateString = DisplayUtils.getArticlePublicationDateString(article,language,itemView.context)
                                                                return@map Pair(dateString,it)
                                                            }
                                                        }
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribeWith(object : DisposableObserver<Pair<String?,Article>>(){
                                                            override fun onComplete() {
                                                                Log.d(TAG,"onComplete for page ${page.name} Np: ${page.newsPaperId} L2")
                                                            }
                                                            override fun onNext(articleData: Pair<String?,Article>) {

                                                                Log.d(TAG,"page ${page.name} Np: ${page.newsPaperId} has article title: ${articleData.second.title}")

                                                                selfPageTitle.text = page.name
                                                                selfArticleTitle.text = articleData.second.title
                                                                selfArticlePublicationTime.text = articleData.first//publicationDate.toString()

                                                                selfPageTitle.visibility = View.VISIBLE
                                                                selfArticleTitle.visibility = View.VISIBLE
                                                                selfArticlePublicationTime.visibility = View.VISIBLE

                                                                articleData.second.previewImageLink?.let {
                                                                    Picasso.get().load(it).into(selfArticlePreviewImage)
                                                                    selfArticlePreviewImage.visibility = View.VISIBLE
                                                                } ?: let {
                                                                    Picasso.get().load(R.drawable.app_big_logo).into(selfArticlePreviewImage)
                                                                    selfArticlePreviewImage.visibility = View.VISIBLE
                                                                }
                                                            }
                                                            override fun onError(e: Throwable) {
                                                                Log.d(TAG,e.message+" for page Np: ${page.newsPaperId} ${page.name} L2")
                                                            }
                                                        })

                                        )
                                    } else{
                                        Log.d(TAG,"page ${page.name} Np: ${page.newsPaperId} has no data")
                                    }
                                }
                            }
                            is MutableList<*> ->{
                                Log.d(TAG,"page ${page.name} Np: ${page.newsPaperId} child article display")
                                val articlePreviewListAdapter = ArticlePreviewListAdapter(itemView.context,language)
                                childListArticlePreviewRV.adapter = articlePreviewListAdapter
                                @Suppress("UNCHECKED_CAST")
                                articlePreviewListAdapter.submitList(data as MutableList<Page>)
                                childListArticlePreviewRV.visibility = View.VISIBLE
                            }
                        }
                    }
                    override fun onError(e: Throwable) {
                        Log.d(TAG,e.message+" for page ${page.name} Np: ${page.newsPaperId} L1")
                    }
                })
        )}
}

//ListAdapter for child page article preview display
//ViewHolder for child page article preview display

class ArticlePreviewListAdapter(val context: Context,val language: Language)
    :ListAdapter<Page,ArticlePreviewHolder>(PageDiffCallback)
{
    val holders = mutableListOf<ArticlePreviewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlePreviewHolder {

        val holder = ArticlePreviewHolder(
                LayoutInflater.from(context).inflate(R.layout.view_article_preview_holder,parent,false),
                language
        )
        holders.add(holder)

        return holder
    }
    override fun onBindViewHolder(holder: ArticlePreviewHolder, position: Int) {
        holder.disposable.clear()
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ArticlePreviewHolder) {
        super.onViewRecycled(holder)
        holder.disposable.clear()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        Log.d("NpPerviewFragment","ArticlePreviewListAdapter onDetachedFromRecyclerView")
        holders.forEach { it.disposable.clear() }
    }

}

class ArticlePreviewHolder(itemView: View,val language: Language) : RecyclerView.ViewHolder(itemView){

    companion object{
        val TAG = "NpPerviewFragment"
    }

    val pageTitle:TextView
    val articlePreviewImage:ImageView
    val articleTitle:TextView
    val articlePublicationTime:TextView

    val disposable = CompositeDisposable()

    init {
        pageTitle = itemView.findViewById(R.id.page_title)
        articlePreviewImage = itemView.findViewById(R.id.article_preview_image)
        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)
    }

    fun bind(page: Page){

        pageTitle.visibility = View.GONE
        articlePreviewImage.visibility = View.GONE
        articleTitle.visibility = View.GONE
        articlePublicationTime.visibility = View.GONE


        val newsDataRepository = RepositoryFactory.getNewsDataRepository(itemView.context)

        disposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val article = newsDataRepository.getLatestArticleByPage(page)
                            article?.let {
                                val dateString = DisplayUtils.getArticlePublicationDateString(article,language,itemView.context)
                                return@map Pair(dateString,it)
                            }
//                            return@map UInt
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Pair<String?,Article>>(){
                            override fun onComplete() {
                                Log.d(TAG,"onComplete for page ${page.name} Np: ${page.newsPaperId} L2")
                            }
                            override fun onNext(articleData: Pair<String?,Article>) {

                                Log.d(TAG,"page ${page.name} Np: ${page.newsPaperId} has article title: ${articleData.second.title}")

                                pageTitle.text = page.name
                                articleTitle.text = articleData.second.title
                                articlePublicationTime.text = articleData.first

                                pageTitle.visibility = View.VISIBLE
                                articleTitle.visibility = View.VISIBLE
                                articlePublicationTime.visibility = View.VISIBLE

                                articleData.second.previewImageLink?.let {
                                    Picasso.get().load(it).into(articlePreviewImage)
                                    articlePreviewImage.visibility = View.VISIBLE
                                } ?: let {
                                    Picasso.get().load(R.drawable.app_big_logo).into(articlePreviewImage)
                                    articlePreviewImage.visibility = View.VISIBLE
                                }
                            }
                            override fun onError(e: Throwable) {
                                Log.d(TAG,e.message+" for page Np: ${page.newsPaperId} ${page.name} L2")
                            }
                        })
        )

    }
}

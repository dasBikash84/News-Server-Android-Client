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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.ImageUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.view_models.HomeViewModel
import com.dasbikash.news_server_data.models.room_entity.SavedArticle
import com.dasbikash.news_server_data.repositories.NewsDataRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class SavedArticlesFragment : Fragment() {

    private lateinit var mSavedArticlePreviewHolder: RecyclerView
    private lateinit var mListAdapter: SavedArticlePreviewListAdapter

    private lateinit var mNewsDataRepository: NewsDataRepository

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LoggerUtils.debugLog("onCreateView", this::class.java)
        return inflater.inflate(R.layout.fragment_saved_articles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoggerUtils.debugLog("onViewCreated", this::class.java)
        mSavedArticlePreviewHolder = view.findViewById(R.id.saved_article_preview_holder)
        mNewsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)

        mListAdapter = SavedArticlePreviewListAdapter { item -> doOnArticleClick(item) }
        mSavedArticlePreviewHolder.adapter = mListAdapter
        ItemTouchHelper(SavedArticleSwipeToDeleteCallback({item->savedArticleDeleteAction(item)},mListAdapter))
                                            .attachToRecyclerView(mSavedArticlePreviewHolder)

        ViewModelProviders.of(activity!!).get(HomeViewModel::class.java).getSavedArticlesLiveData()
                .observe(this,object : androidx.lifecycle.Observer<List<SavedArticle>>{
                    override fun onChanged(t: List<SavedArticle>?) {
                        t?.let {
                            mListAdapter.submitList(it.sortedBy { it.newspaperName+it.pageName }.toList())
                        }

                    }
                })

    }

    fun doOnArticleClick(savedArticle: SavedArticle) {
        LoggerUtils.debugLog("${savedArticle.title} clicked", this::class.java)
        activity!!.startActivity(SavedArticleViewActivity.getIntent(savedArticle,context!!))
    }

    fun savedArticleDeleteAction(savedArticle: SavedArticle){
        mDisposable.add(
        Observable.just(savedArticle)
                .subscribeOn(Schedulers.io())
                .map {
                    mNewsDataRepository.deleteSavedArticle(it)
                }
                .subscribeWith(object : DisposableObserver<Unit>(){
                    override fun onComplete() {}
                    override fun onNext(t: Unit) {
                        DisplayUtils.showShortSnack(getView() as CoordinatorLayout,"Article deleted.")
                    }
                    override fun onError(e: Throwable) {
                        mListAdapter.notifyDataSetChanged()
                    }
                }))
    }
}

object SavedArticleDiffCallback : DiffUtil.ItemCallback<SavedArticle>() {
    override fun areItemsTheSame(oldItem: SavedArticle, newItem: SavedArticle): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SavedArticle, newItem: SavedArticle): Boolean {
        return oldItem == newItem
    }
}

class SavedArticlePreviewListAdapter(val clickAction: (savedArticle: SavedArticle) -> Unit) :
        ListAdapter<SavedArticle, SavedArticlePreviewHolder>(SavedArticleDiffCallback) {

    override fun onBindViewHolder(holder: SavedArticlePreviewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            clickAction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedArticlePreviewHolder {
        return SavedArticlePreviewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_saved_article_preview_holder, parent, false))
    }
}

class SavedArticlePreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val pageTitle: AppCompatTextView
    private val articleTitle: AppCompatTextView
    private val articlePublicationTime: AppCompatTextView
    private val articleImage: AppCompatImageView

    lateinit var savedArticle: SavedArticle

    init {
        pageTitle = itemView.findViewById(R.id.page_title)
        articleTitle = itemView.findViewById(R.id.article_title)
        articlePublicationTime = itemView.findViewById(R.id.article_time)
        articleImage = itemView.findViewById(R.id.article_preview_image)
    }

    @SuppressLint("CheckResult")
    fun bind(currentSavedArticle: SavedArticle) {
        LoggerUtils.debugLog(currentSavedArticle.toString(), this::class.java)
        savedArticle = currentSavedArticle
        pageTitle.text = StringBuilder(savedArticle.pageName).append(" | ").append(savedArticle.newspaperName).toString()
        articleTitle.text = savedArticle.title
        ImageUtils.customLoader(articleImage, savedArticle.previewImageLink)
        Observable.just(savedArticle.publicationTime)
                .observeOn(Schedulers.io())
                .map {
                    val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(itemView.context)
                    val language = appSettingsRepository.getLanguageByPage(appSettingsRepository.findPageById(savedArticle.pageId)!!)
                    DisplayUtils.getSavedArticlePublicationDateString(savedArticle, language, itemView.context)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : Observer<String?> {
                    override fun onComplete() {}
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(dateString: String) {
                        articlePublicationTime.text = dateString
                    }

                    override fun onError(e: Throwable) {}
                })
    }

}

class SavedArticleSwipeToDeleteCallback(val deleteSavedArticle:(savedArticle:SavedArticle)->Unit,
                                        val listAdapter: SavedArticlePreviewListAdapter) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return false
    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val savedArticle = (viewHolder as SavedArticlePreviewHolder).savedArticle

        DialogUtils.createAlertDialog(viewHolder.itemView.context, DialogUtils.AlertDialogDetails(
                title = "Delete saved article?",
                doOnPositivePress = {deleteSavedArticle(savedArticle)},
                doOnNegetivePress = {listAdapter.notifyDataSetChanged()}
        )).show()
    }
}

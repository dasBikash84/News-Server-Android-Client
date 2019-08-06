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
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server_data.models.room_entity.NewsCategory
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentNewsCategories : Fragment() {

    lateinit var mNewscategoryListHolder: RecyclerView
    lateinit var mNewscategoryListAdapter: NewsCategoryListAdapter

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewComponents(view)
        setListnersForViewComponents()
        initViewComponents()
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = TITLE_TEXT
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
                            appSettingsRepository.getNewsCategories()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<NewsCategory>>() {
                            override fun onComplete() {}

                            override fun onNext(newsCategories: List<NewsCategory>) {
                                newsCategories.forEach {
                                    debugLog(it.toString())
                                }
                                mNewscategoryListAdapter.submitList(newsCategories.sortedBy { it.id })
                            }

                            override fun onError(e: Throwable) {}
                        })
        )
    }

    private fun findViewComponents(view: View) {
        mNewscategoryListHolder = view.findViewById(R.id.news_category_list_holder)
    }

    private fun setListnersForViewComponents() {}

    private fun initViewComponents() {
        mNewscategoryListAdapter = NewsCategoryListAdapter { doOnNewsCategoryNameClick(it) }
        mNewscategoryListHolder.adapter = mNewscategoryListAdapter
    }

    private fun doOnNewsCategoryNameClick(newsCategory: NewsCategory) {
        debugLog(newsCategory.toString())
        activity!!.startActivity(NewsCategoryArticleViewActivity.getIntentForNewsCategory(context!!,newsCategory))
    }

    companion object{
        private const val TITLE_TEXT = "Topics"
    }
}

object NewsCategoryCallback : DiffUtil.ItemCallback<NewsCategory>() {
    override fun areItemsTheSame(oldItem: NewsCategory, newItem: NewsCategory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NewsCategory, newItem: NewsCategory): Boolean {
        return oldItem == newItem
    }
}

class NewsCategoryListAdapter(val doOnItemClick: (NewsCategory) -> Unit)
    : ListAdapter<NewsCategory, NewsCategoryrNameHolder>(NewsCategoryCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsCategoryrNameHolder {
        return NewsCategoryrNameHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.view_news_cat_label, parent, false))
    }

    override fun onBindViewHolder(holder: NewsCategoryrNameHolder, position: Int) {
        holder.bind(getItem(position),doOnItemClick)
    }
}

class NewsCategoryrNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val titleButton: Button

    init {
        titleButton = itemView.findViewById(R.id.news_category_title)
    }

    fun bind(newsCategory: NewsCategory,doOnItemClick: (NewsCategory) -> Unit) {
        titleButton.setText(newsCategory.name)
        titleButton.setOnClickListener { doOnItemClick(newsCategory) }
    }
}
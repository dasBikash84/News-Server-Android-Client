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

package com.dasbikash.news_server.view_controllers.view_helpers

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

object PageDiffCallback: DiffUtil.ItemCallback<Page>(){
    override fun areItemsTheSame(oldItem: Page, newItem: Page): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: Page, newItem: Page): Boolean {
        return oldItem == newItem
    }
}
abstract class PageListAdapter<VH:PageViewHolder>() : ListAdapter<Page, VH>(PageDiffCallback) {

    val mDisposable = CompositeDisposable()

    override fun onBindViewHolder(holder: VH, position: Int) {
        mDisposable.add(
                Observable.just(getItem(position))
                        .subscribeOn(Schedulers.io())
                        .map {
                            val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(holder.itemView.context)
                            val parentPage = appSettingsRepository.findPageById(it.parentPageId!!)
                            val newspaper = appSettingsRepository.getNewspaperByPage(it)
                            Triple(it, parentPage, newspaper)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Triple<Page, Page?, Newspaper>>() {
                            override fun onComplete() {}
                            override fun onNext(triple: Triple<Page, Page?, Newspaper>) {
                                holder.bind(triple.first, triple.second, triple.third)
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

abstract class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(page: Page, parentPage: Page?, newspaper: Newspaper)
}
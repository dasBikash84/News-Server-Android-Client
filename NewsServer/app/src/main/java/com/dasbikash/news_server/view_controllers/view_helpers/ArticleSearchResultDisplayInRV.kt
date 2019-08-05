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
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server_data.models.ArticleSearchReasultEntry

object ArticleSearchReasultEntryDiffCallback: DiffUtil.ItemCallback<ArticleSearchReasultEntry>(){
    override fun areItemsTheSame(oldItem: ArticleSearchReasultEntry, newItem: ArticleSearchReasultEntry): Boolean {
        return oldItem.articleId == newItem.articleId
    }
    override fun areContentsTheSame(oldItem: ArticleSearchReasultEntry, newItem: ArticleSearchReasultEntry): Boolean {
        return oldItem.equals(newItem)
    }
}

class ArticleSearchReasultListAdapter() :
        ListAdapter<ArticleSearchReasultEntry, ArticleSearchReasultEntryHolder>(ArticleSearchReasultEntryDiffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleSearchReasultEntryHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: ArticleSearchReasultEntryHolder, position: Int) {
        holder.bind(getItem(position))
    }
}


class ArticleSearchReasultEntryHolder(itemView: View):RecyclerView.ViewHolder(itemView) {

    fun bind(articleSearchReasultEntry: ArticleSearchReasultEntry){

    }
}
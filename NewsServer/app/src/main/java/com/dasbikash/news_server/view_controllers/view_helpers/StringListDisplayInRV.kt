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

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.utils.DisplayUtils

object StringDiffCallback: DiffUtil.ItemCallback<String>(){
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem.trim() == newItem.trim()
    }
    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem.trim() == newItem.trim()
    }
}

class TextItemHolder(itemView: View, size:Float = 16.00f, unit:Int = TypedValue.COMPLEX_UNIT_SP,horPaddingDp:Int = 4) :
        RecyclerView.ViewHolder(itemView) {
    init {
        if (itemView is TextView){
            val paddingSize = DisplayUtils.dpToPx(horPaddingDp,itemView.context).toInt()
            itemView.setTextSize(unit,size)
            itemView.setPaddingRelative(paddingSize*2,paddingSize,paddingSize*2,paddingSize)
        }
    }
    fun bind(text:CharSequence){
        if (itemView is TextView){
            itemView.setText(text)
        }
    }
}

class TextListAdapter(val doOnTextClick:(text:CharSequence)->Unit = {}) :ListAdapter<String, TextItemHolder>(StringDiffCallback){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemHolder {
        return TextItemHolder(TextView(parent.context))
    }
    override fun onBindViewHolder(holder: TextItemHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            if (it is TextView){
                doOnTextClick(it.text)
            }
        }
    }
}
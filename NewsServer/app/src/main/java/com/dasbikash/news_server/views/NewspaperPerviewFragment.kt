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
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.dasbikash.news_server_data.display_models.entity.Newspaper

class NewspaperPerviewFragment :Fragment() {

    private lateinit var mNewspaper:Newspaper


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_newspaper_page_list_preview_holder,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mNewspaper = arguments!!.getSerializable(ARG_NEWS_PAPAER) as Newspaper
        view.findViewById<TextView>(R.id.test_text).setText(mNewspaper.name)

    }

    companion object{

        val ARG_NEWS_PAPAER = "com.dasbikash.news_server.views.NewspaperPerviewFragment.ARG_NEWS_PAPAER"

        fun getInstance(newspaper: Newspaper):NewspaperPerviewFragment{
            val args = Bundle()
            args.putSerializable(ARG_NEWS_PAPAER, newspaper)
            val fragment = NewspaperPerviewFragment()
            fragment.setArguments(args)
            return fragment
        }
    }


}
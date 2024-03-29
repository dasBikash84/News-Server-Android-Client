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
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.google.android.material.button.MaterialButton

class   FragmentAdminTasks : Fragment() {

    private lateinit var mNewsPaperParserModeChangeRequestButton:MaterialButton
    private lateinit var mArticleUploaderStatusChangeRequestButton:MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_tasks,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mNewsPaperParserModeChangeRequestButton = view.findViewById(R.id.np_parser_mode_change_request_button)
        mArticleUploaderStatusChangeRequestButton = view.findViewById(R.id.article_uploader_status_change_request_button)

        mNewsPaperParserModeChangeRequestButton.setOnClickListener {
            (activity!! as ActivityAdmin).loadNPParserModeChangeRequestFragment()
        }

        mArticleUploaderStatusChangeRequestButton.setOnClickListener {
            (activity!! as ActivityAdmin).loadArticleUploaderModeChangeRequestFragment()
        }
    }
}
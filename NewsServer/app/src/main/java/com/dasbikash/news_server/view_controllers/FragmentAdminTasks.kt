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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server_data.repositories.AdminTaskRepository
import com.google.android.material.button.MaterialButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class   FragmentAdminTasks : Fragment() {

    private lateinit var mNewsPaperStatusChangeRequestButton:MaterialButton
    private lateinit var mNewsPaperParserModeChangeRequestButton:MaterialButton
    private lateinit var mArticleUploaderStatusChangeRequestButton:MaterialButton
    private lateinit var mTokenGenerationRequestButton:MaterialButton

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_tasks,container,false)
    }

    private val TOKEN_GENERATION_REQ_PROMPT = "Add token generation request?"
    private val TOKEN_GENERATION_SUCCESS_MESSAGE = "Token generation request added."
    private val TOKEN_GENERATION_FAILURE_MESSAGE = "Token generation request addition failure."
    private val TOKEN_GENERATION_ERROR_MESSAGE = "Error occured during token generation request addition."

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mNewsPaperStatusChangeRequestButton = view.findViewById(R.id.np_status_change_request_button)
        mNewsPaperParserModeChangeRequestButton = view.findViewById(R.id.np_parser_mode_change_request_button)
        mArticleUploaderStatusChangeRequestButton = view.findViewById(R.id.article_uploader_status_change_request_button)
        mTokenGenerationRequestButton = view.findViewById(R.id.token_generation_request_button)

        mNewsPaperStatusChangeRequestButton.setOnClickListener {
            (activity!! as AdminActivity).loadNPStatusChangeRequestFragment()
        }

        mNewsPaperParserModeChangeRequestButton.setOnClickListener {
            (activity!! as AdminActivity).loadNPParserModeChangeRequestFragment()
        }

        mArticleUploaderStatusChangeRequestButton.setOnClickListener {
            (activity!! as AdminActivity).loadArticleUploaderModeChangeRequestFragment()
        }

        mTokenGenerationRequestButton.setOnClickListener {
            DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                    message = TOKEN_GENERATION_REQ_PROMPT,doOnPositivePress = {addTokenGenerationRequest()}
            )).show()
        }
    }

    private fun addTokenGenerationRequest() {
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map { AdminTaskRepository.addTokenGenerationRequest() }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object :DisposableObserver<Boolean>(){
                            override fun onComplete() {}

                            override fun onNext(result: Boolean) {
                                if (result){
                                    DisplayUtils.showShortSnack(this@FragmentAdminTasks.view!! as  CoordinatorLayout,TOKEN_GENERATION_SUCCESS_MESSAGE)
                                }else{
                                    DisplayUtils.showShortSnack(this@FragmentAdminTasks.view!! as  CoordinatorLayout,TOKEN_GENERATION_FAILURE_MESSAGE)
                                }
                            }

                            override fun onError(e: Throwable) {
                                DisplayUtils.showShortSnack(this@FragmentAdminTasks.view!! as  CoordinatorLayout,TOKEN_GENERATION_ERROR_MESSAGE)                            }
                        })
        )
    }
}
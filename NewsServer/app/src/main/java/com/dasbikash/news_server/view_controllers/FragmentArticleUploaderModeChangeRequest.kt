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
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DialogUtils
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.view_controllers.interfaces.TokenGenerationRequestAdder
import com.dasbikash.news_server_data.models.ArticleUploadTarget
import com.dasbikash.news_server_data.models.ArticleUploaderStatusChangeRequest
import com.dasbikash.news_server_data.models.TwoStateStatus
import com.dasbikash.news_server_data.repositories.AdminTaskRepository
import com.google.android.material.button.MaterialButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class   FragmentArticleUploaderModeChangeRequest : Fragment(),TokenGenerationRequestAdder {

    companion object {
        private const val DISCARD_CHANGES_AND_EXIT_MESSAGE = "Discard changes and exit?"
        private const val SUBMIT_REQUEST_MESSAGE = "Submit request with current data?"
        private const val INVALID_INPUT_MESSAGE = "Invalid input. Please check!!"
        private val REQUEST_SUBMISSION_SUCCESS_MESSAGE = "Article uploader mode change request submitted."
        private val REQUEST_SUBMISSION_FAILURE_MESSAGE = "Article uploader  mode change request submission failure."
        private val REQUEST_SUBMISSION_ERROR_MESSAGE = "Error!!!"
    }

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    private lateinit var mArticleUploaderSelectorSpinner: Spinner
    private lateinit var mArticleUploaderModeSelectorSpinner: Spinner
    private lateinit var mAuthTokenEditText: EditText
    private lateinit var mOkButton: Button
    private lateinit var mCancelButton: Button
    private lateinit var mWaitScreen: LinearLayoutCompat

    private lateinit var mCurrentSelectedArticleUploader: String
    private lateinit var mCurrentSelectedMode: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_article_uploader_status_change_request,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewItems(view)

        mOkButton.setOnClickListener {
            if (mAuthTokenEditText.text.isNotBlank()) {
                DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                        message = SUBMIT_REQUEST_MESSAGE,
                        doOnPositivePress = { okButtonClickAction() })).show()
            } else {
                DisplayUtils.showShortSnack(this.view!! as CoordinatorLayout, INVALID_INPUT_MESSAGE)
            }
        }

        mCancelButton.setOnClickListener {
            DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                    message = DISCARD_CHANGES_AND_EXIT_MESSAGE,
                    doOnPositivePress = { (activity as AdminActivity).onBackPressed() })).show()
        }

        mArticleUploaderSelectorSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mCurrentSelectedArticleUploader = parent?.getItemAtPosition(position).toString()
            }
        })

        mArticleUploaderModeSelectorSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mCurrentSelectedMode = parent?.getItemAtPosition(position).toString()
            }
        })

        val articleUploaderListAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, ArticleUploadTarget.values().toList().map { it.name })
        articleUploaderListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mArticleUploaderSelectorSpinner.setAdapter(articleUploaderListAdapter)

        val articleUploaderModeListAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, TwoStateStatus.values().toList().map { it.name })
        articleUploaderModeListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mArticleUploaderModeSelectorSpinner.setAdapter(articleUploaderModeListAdapter)
    }

    private fun findViewItems(view: View) {
        mArticleUploaderSelectorSpinner = view.findViewById(R.id.article_uploader_selector_spinner)
        mArticleUploaderModeSelectorSpinner = view.findViewById(R.id.article_uploader_mode_selector_spinner)
        mAuthTokenEditText = view.findViewById(R.id.auth_token_box_edit_text)
        mOkButton = view.findViewById(R.id.ok_button)
        mCancelButton = view.findViewById(R.id.cancel_button)
        mWaitScreen = view.findViewById(R.id.wait_screen)
    }

    override fun showWaitScreen(){
        mWaitScreen.visibility = View.VISIBLE
        mWaitScreen.bringToFront()
        mWaitScreen.setOnClickListener {  }
    }

    override fun hideWaitScreen(){
        mWaitScreen.visibility = View.GONE
    }

    private fun okButtonClickAction() {
        showWaitScreen()
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val articleUploaderStatusChangeRequest: ArticleUploaderStatusChangeRequest
                            val articleUploadTarget = ArticleUploadTarget.values().find { it.name.equals(mCurrentSelectedArticleUploader) }!!
                            if (mCurrentSelectedMode.equals(TwoStateStatus.ON.name)) {
                                articleUploaderStatusChangeRequest =
                                        ArticleUploaderStatusChangeRequest.getInstanceForOnMode(mAuthTokenEditText.text.trim().toString(), articleUploadTarget)
                            }else{
                                articleUploaderStatusChangeRequest =
                                        ArticleUploaderStatusChangeRequest.getInstanceForOffMode(mAuthTokenEditText.text.trim().toString(), articleUploadTarget)
                            }
                            AdminTaskRepository.addArticleUploaderStatusChangeRequest(articleUploaderStatusChangeRequest)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {}
                            override fun onNext(result: Boolean) {
                                if (result) {
                                    DisplayUtils.showShortSnack(this@FragmentArticleUploaderModeChangeRequest.view!! as CoordinatorLayout, REQUEST_SUBMISSION_SUCCESS_MESSAGE)
                                    (activity as AdminActivity).onBackPressed()
                                } else {
                                    DisplayUtils.showShortSnack(this@FragmentArticleUploaderModeChangeRequest.view!! as CoordinatorLayout, REQUEST_SUBMISSION_FAILURE_MESSAGE)
                                }
                                hideWaitScreen()
                            }

                            override fun onError(e: Throwable) {
                                hideWaitScreen()
                                DisplayUtils.showShortSnack(this@FragmentArticleUploaderModeChangeRequest.view!! as CoordinatorLayout, "$REQUEST_SUBMISSION_ERROR_MESSAGE ${e.message}")
                            }
                        })

        )
    }

    override fun addTokenGenerationRequest(): Boolean {
        return AdminTaskRepository.addDataCoordinatorTokenGenerationRequest()
    }
}
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
import com.dasbikash.news_server_data.models.NewsPaperStatusChangeRequest
import com.dasbikash.news_server_data.models.OffOnStatus
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.repositories.AdminTaskRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.google.android.material.button.MaterialButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers


class FragmentNPStatusChangeRequest : Fragment(),TokenGenerationRequestAdder {
    companion object {
        private const val DISCARD_CHANGES_AND_EXIT_MESSAGE = "Discard changes and exit?"
        private const val SUBMIT_REQUEST_MESSAGE = "Submit request with current data?"
        private const val INVALID_INPUT_MESSAGE = "Invalid input. Please check!!"
        private const val REQUEST_SUBMISSION_SUCCESS_MESSAGE = "News paper status change request submitted."
        private const val REQUEST_SUBMISSION_FAILURE_MESSAGE = "News paper status change request submission failure."
        private const val REQUEST_SUBMISSION_ERROR_MESSAGE = "Error!!!"
    }

    private val mNewsPapers = mutableListOf<Newspaper>()
    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)


    private lateinit var mNewsPaperSelectorSpinner: Spinner
    private lateinit var mNewsPaperStatusSelectorSpinner: Spinner
    private lateinit var mNewsPaperIdEditText: EditText
    private lateinit var mAuthTokenEditText: EditText
    private lateinit var mOkButton: Button
    private lateinit var mCancelButton: Button
    private lateinit var mWaitScreen: LinearLayoutCompat


    private lateinit var mCurrentSelectedStatus: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_np_status_change_request, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewItems(view)

        mOkButton.setOnClickListener {
            if (mAuthTokenEditText.text.isNotBlank() &&
                    mNewsPaperIdEditText.text.isNotBlank()) {
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

        mNewsPaperSelectorSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mNewsPaperIdEditText.setText(mNewsPapers.get(position).id)
            }
        })

        mNewsPaperStatusSelectorSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mCurrentSelectedStatus = parent?.getItemAtPosition(position).toString()
            }
        })

//        RepositoryFactory.getAppSettingsRepository(context!!).getNewsPapersLiveData().observe(this,Observer<List<Newspaper>>{
//
//            mNewsPapers.clear()
//            mNewsPapers.addAll(it)
//            val newspaperNameListAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, mNewsPapers.map { it.name }.toList())
//
//            newspaperNameListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            mNewsPaperSelectorSpinner.setAdapter(newspaperNameListAdapter)
//        })

        val newspaperStatusListAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, OffOnStatus.values().toList().map { it.name })

        newspaperStatusListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mNewsPaperStatusSelectorSpinner.setAdapter(newspaperStatusListAdapter)


        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
                            appSettingsRepository.getRawAppsettings(context!!).newspapers?.values?.toList()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<Newspaper>>() {
                            override fun onComplete() {}
                            override fun onNext(newsPapers: List<Newspaper>) {
                                if (newsPapers.isNotEmpty()) {

                                    mNewsPapers.clear()
                                    mNewsPapers.addAll(newsPapers)
                                    val dataAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, mNewsPapers.map { it.name }.toList())

                                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    mNewsPaperSelectorSpinner.setAdapter(dataAdapter)
                                }
                            }

                            override fun onError(e: Throwable) {}
                        })

        )
    }

    private fun findViewItems(view: View) {
        mNewsPaperSelectorSpinner = view.findViewById(R.id.newspaper_selector_spinner)
        mNewsPaperStatusSelectorSpinner = view.findViewById(R.id.newspaper_status_spinner)
        mNewsPaperIdEditText = view.findViewById(R.id.newspaper_id_edit_text)
        mAuthTokenEditText = view.findViewById(R.id.auth_token_box_edit_text)
        mOkButton = view.findViewById(R.id.ok_button)
        mCancelButton = view.findViewById(R.id.cancel_button)
        mWaitScreen = view.findViewById(R.id.wait_screen)
    }

    private fun showWaitScreen(){
        mWaitScreen.visibility = View.VISIBLE
        mWaitScreen.bringToFront()
        mWaitScreen.setOnClickListener {  }
    }

    private fun hideWaitScreen(){
        mWaitScreen.visibility = View.GONE
    }

    private fun okButtonClickAction() {
        showWaitScreen()
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val newsPaperStatusChangeRequest: NewsPaperStatusChangeRequest
                            if (mCurrentSelectedStatus.equals(OffOnStatus.ON.name)) {
                                newsPaperStatusChangeRequest =
                                        NewsPaperStatusChangeRequest.getInstanceForOnMode(mAuthTokenEditText.text.trim().toString(), mNewsPaperIdEditText.text.trim().toString())
                            } else {
                                newsPaperStatusChangeRequest =
                                        NewsPaperStatusChangeRequest.getInstanceForOffMode(mAuthTokenEditText.text.trim().toString(), mNewsPaperIdEditText.text.trim().toString())
                            }
                            AdminTaskRepository.addNewsPaperStatusChangeRequest(newsPaperStatusChangeRequest)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {}
                            override fun onNext(result: Boolean) {
                                if (result) {
                                    DisplayUtils.showShortSnack(this@FragmentNPStatusChangeRequest.view!! as CoordinatorLayout, REQUEST_SUBMISSION_SUCCESS_MESSAGE)
                                    (activity as AdminActivity).onBackPressed()
                                } else {
                                    DisplayUtils.showShortSnack(this@FragmentNPStatusChangeRequest.view!! as CoordinatorLayout, REQUEST_SUBMISSION_FAILURE_MESSAGE)
                                }
                                hideWaitScreen()
                            }

                            override fun onError(e: Throwable) {
                                hideWaitScreen()
                                DisplayUtils.showShortSnack(this@FragmentNPStatusChangeRequest.view!! as CoordinatorLayout, "$REQUEST_SUBMISSION_ERROR_MESSAGE ${e.message}")
                            }
                        })

        )
    }

    override fun addTokenGenerationRequest(): Boolean {
        return AdminTaskRepository.addParserTokenGenerationRequest()
    }
}
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dasbikash.news_server.BuildConfig
import com.dasbikash.news_server.R
import com.dasbikash.news_server.fcm.NewsServerFirebaseMessagingService
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server.utils.debugLog
import com.dasbikash.news_server.view_controllers.interfaces.HomeNavigator
import com.dasbikash.news_server_data.exceptions.AuthServerException
import com.dasbikash.news_server_data.exceptions.DataSourceNotFoundException
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.exceptions.SettingsServerException
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.dasbikash.news_server_data.utills.NetConnectivityUtility
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentInit : Fragment() {

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mNoInternetMessage: TextView

    private var mRetryDelayForRemoteDBError = 0L
    private var mRetryCountForError = 0L

    private var mFcmPageId: String? = null
    private var mFcmArticleId: String? = null

    private var mDisposableDisposed = false


    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    private val mNetConAvailableBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            unregisterBrodcastReceivers()
            initSettingsDataLoading(0L)
        }
    }

    internal enum class DataLoadingStatus private constructor(val isSetProgressbarDeterminate: Boolean, val progressBarValue: Int) {

        WAITING_FOR_NETWORK_INIT(false, 0),
        STARTING_INITIALIZATION(true, 10),
        APP_SETTINGS_DATA_LOADED(true, 40),
        USER_SETTINGS_DATA_LOADED(true, 70),
        NEWS_DATA_REPO_INITIATED(true, 100),
        EXIT(true, 100);

        override fun toString(): String {
            return "DataLoadingStatus{" +
                    "setProgressbarDeterminate=" + isSetProgressbarDeterminate +
                    ", progressBarValue=" + progressBarValue +
                    '}'.toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_init, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mProgressBar = view.findViewById(R.id.data_load_progress)
        mNoInternetMessage = view.findViewById(R.id.no_internet_message)

        arguments?.let {
            mFcmPageId = it.getString(ARG_PAGE_ID)
            mFcmArticleId = it.getString(ARG_ARTICLE_ID)
        }
        debugLog("mFcmPageId: $mFcmPageId, mFcmArticleId: $mFcmArticleId")
    }

    override fun onResume() {
        super.onResume()
        initSettingsDataLoading(0L)
    }


    private fun initSettingsDataLoading(initDelay: Long) {

        mNoInternetMessage.visibility = View.INVISIBLE

        mProgressBar.visibility = View.VISIBLE
        mProgressBar.isIndeterminate = true
        mDisposable.add(
                getDataLoadingStatusObservable(initDelay)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<DataLoadingStatus>() {
                            override fun onNext(loadingStatus: DataLoadingStatus) {
                                LoggerUtils.debugLog("onNext: $loadingStatus", this::class.java)
                                if (loadingStatus.isSetProgressbarDeterminate) {
                                    mProgressBar.isIndeterminate = false
                                    mProgressBar.progress = loadingStatus.progressBarValue
                                } else {
                                    mProgressBar.isIndeterminate = true
                                }
                            }

                            override fun onError(e: Throwable) {
                                LoggerUtils.debugLog("onError: ${e.javaClass.canonicalName!!} cause: ${e.message}", this::class.java)
                                LoggerUtils.printStackTrace(e)
                                doOnError(e)
                            }

                            override fun onComplete() {
                                if (mFcmPageId != null && mFcmArticleId != null) {
                                    mDisposable.add(
                                            Observable.just(true)
                                                    .subscribeOn(Schedulers.io())
                                                    .map {
                                                        RepositoryFactory.getNewsDataRepository(context!!)
                                                                .findArticleByIdFromRemoteDb(mFcmArticleId!!, mFcmPageId!!)?.let {
                                                                    debugLog("$it")
                                                                    return@map it
                                                                }
                                                        debugLog("false")
                                                        false
                                                    }
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeWith(object : DisposableObserver<Any>() {
                                                        override fun onComplete() {}

                                                        override fun onNext(data: Any) {
                                                            debugLog("type of data is ${data::class.java.canonicalName}")
                                                            debugLog("value of data is ${data.toString()}")
                                                            when (data) {
                                                                is Article -> (activity as HomeNavigator).loadHomeNpFragment(data)
                                                                else -> {
                                                                    (activity as HomeNavigator).loadHomeNpFragment()
                                                                }
                                                            }
                                                        }

                                                        override fun onError(e: Throwable) {
                                                            (activity as HomeNavigator).loadHomeNpFragment()
                                                        }
                                                    }))
                                } else {
                                    (activity as HomeNavigator).loadHomeNpFragment()
                                }
                            }
                        })
        )
    }

    private fun getDataLoadingStatusObservable(initDelay: Long): Observable<DataLoadingStatus> {
        mDisposableDisposed=false
        return Observable.create { emitter ->
            try {
                SystemClock.sleep(initDelay)

                //wait for network connection initialization
                emitter.onNext(DataLoadingStatus.WAITING_FOR_NETWORK_INIT)
                while (!NetConnectivityUtility.isInitialize);

                //Initialization started
                emitter.onNext(DataLoadingStatus.STARTING_INITIALIZATION)

                RepositoryFactory.initDataSourceImplementation()

                RepositoryFactory
                        .getFreshAppSettingsRepository(context!!)
                        .initAppSettings(context!!)
                emitter.onNext(DataLoadingStatus.APP_SETTINGS_DATA_LOADED)

                RepositoryFactory
                        .getUserSettingsRepository(context!!)
                        .initUserSettings(context!!)

                emitter.onNext(DataLoadingStatus.USER_SETTINGS_DATA_LOADED)

                RepositoryFactory
                        .getFreshNewsDataRepository(context!!)
                        .init(context!!)
                NewsServerFirebaseMessagingService.init(context!!)
                emitter.onNext(DataLoadingStatus.NEWS_DATA_REPO_INITIATED)

                if (BuildConfig.DEBUG) {
                    testRoutine()
                }

                emitter.onComplete()
            }catch (ex:Throwable){
                if (!mDisposableDisposed){
                    throw ex
                }
            }
        }
    }

    private fun testRoutine() {
    }

    private fun doOnError(throwable: Throwable) {

        when (throwable) {
            is NoInternertConnectionException -> {
                mProgressBar.isIndeterminate = true
                mNoInternetMessage.visibility = View.VISIBLE
                registerBrodcastReceivers()
            }
            is SettingsServerException -> {
                mRetryCountForError++
                mRetryDelayForRemoteDBError += INCREMENTAL_RETRY_DELAY_MS * mRetryCountForError
                initSettingsDataLoading(mRetryDelayForRemoteDBError)
            }
            is AuthServerException -> {
                val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)
                mDisposable.add(
                        Observable.just(true)
                                .subscribeOn(Schedulers.io())
                                .map {
                                    if (userSettingsRepository.checkIfLoggedIn()) {
                                        userSettingsRepository.signOutUser(context!!)
                                    }
                                }
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(object : DisposableObserver<Unit>() {
                                    override fun onComplete() {}

                                    override fun onNext(t: Unit) {
                                        (activity as SignInHandler).launchSignInActivity({
                                            initSettingsDataLoading(0L)
                                        })
                                    }

                                    override fun onError(e: Throwable) {
                                        when (e) {
                                            is NoInternertConnectionException -> {
                                                NetConnectivityUtility.showNoInternetToast(this@FragmentInit.context!!)
                                                mProgressBar.isIndeterminate = true
                                                mNoInternetMessage.visibility = View.VISIBLE
                                                registerBrodcastReceivers()
                                            }
                                            else -> {
                                                mRetryCountForError++
                                                mRetryDelayForRemoteDBError += INCREMENTAL_RETRY_DELAY_MS * mRetryCountForError
                                                initSettingsDataLoading(mRetryDelayForRemoteDBError)
//                                                Snackbar.make(mCoordinatorLayout, "Signed out. Please retry.", Snackbar.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                })
                )
            }
            is DataSourceNotFoundException -> {
                mRetryCountForError++
                mRetryDelayForRemoteDBError += INCREMENTAL_RETRY_DELAY_MS * mRetryCountForError
                initSettingsDataLoading(mRetryDelayForRemoteDBError)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mDisposableDisposed = true
        unregisterBrodcastReceivers()
    }


    private fun registerBrodcastReceivers() {
        LocalBroadcastManager.getInstance(context!!).registerReceiver(mNetConAvailableBroadcastReceiver,
                NetConnectivityUtility.intentFilterForNetworkAvailableBroadcastReceiver)
    }

    private fun unregisterBrodcastReceivers() {
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(mNetConAvailableBroadcastReceiver)
    }

    companion object {

        private const val ARG_PAGE_ID = "com.dasbikash.news_server.view_controllers.FragmentInit.ARG_PAGE_ID"
        private const val ARG_ARTICLE_ID = "com.dasbikash.news_server.view_controllers.FragmentInit.ARG_ARTICLE_ID"

        fun getInstance(pageId: String, articleId: String): FragmentInit {
            val args = Bundle()
            args.putString(ARG_PAGE_ID, pageId)
            args.putString(ARG_ARTICLE_ID, articleId)
            val fragment = FragmentInit()
            fragment.setArguments(args)
            return fragment
        }

        private const val INCREMENTAL_RETRY_DELAY_MS = 3000L
    }

}

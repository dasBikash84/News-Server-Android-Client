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

package com.dasbikash.news_server.utils

import android.content.Context
import android.content.Intent
import com.dasbikash.news_server.fcm.NewsServerFirebaseMessagingService
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

object LogInPostProcessUtils {

    fun doLogInPostProcess(disposable: LifeCycleAwareCompositeDisposable, context: Context, resultCode: Int, data: Intent?,
                           doOnInit: (() -> Unit)? = null, doOnFinish: (() -> Unit)? = null, actionAfterSuccessfulLogIn: (() -> Unit)? = null) {

        doOnInit?.let { it() }
        disposable.add(
                Observable.just(Pair(resultCode, data))
                        .subscribeOn(Schedulers.io())
                        .map {
                            val mUserSettingsRepository = RepositoryFactory.getUserSettingsRepository(context)
                            mUserSettingsRepository.processSignInRequestResult(it, context, {
                                mUserSettingsRepository.getFavouritePageEntries().filter { it.subscribed }.forEach { NewsServerFirebaseMessagingService.subscribeToTopic(it.pageId) }
                            })
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Pair<UserSettingsRepository.SignInResult, Throwable?>>() {
                            override fun onComplete() {
                                doOnFinish?.let { it() }
                            }

                            override fun onNext(processingResult: Pair<UserSettingsRepository.SignInResult, Throwable?>) {
                                when (processingResult.first) {
                                    UserSettingsRepository.SignInResult.SUCCESS -> {
                                        DisplayUtils.showShortToast(context, "Signed In!")
                                        actionAfterSuccessfulLogIn?.let { it() }
                                    }
                                    UserSettingsRepository.SignInResult.USER_ABORT -> DisplayUtils.showShortToast(context, "Log in aborted!")
                                    UserSettingsRepository.SignInResult.SERVER_ERROR -> DisplayUtils.showShortToast(context, "Log in error!")
                                    UserSettingsRepository.SignInResult.SETTINGS_UPLOAD_ERROR -> DisplayUtils.showShortToast(context, "Log in error!")
                                }
                            }

                            override fun onError(e: Throwable) {
                                DisplayUtils.showShortToast(context, "Log in error!")
                                doOnFinish?.let { it() }
                            }
                        }))
    }
}
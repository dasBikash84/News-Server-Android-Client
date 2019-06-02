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

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dasbikash.news_server_data.utills.LoggerUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer

class LifeCycleAwareCompositeDisposable private constructor():
        DefaultLifecycleObserver, Disposable, DisposableContainer {
    private val mDisposable = CompositeDisposable()

//    fun clear(){
//        mDisposable.clear()
//    }

    override fun onPause(owner: LifecycleOwner) {
        LoggerUtils.debugLog("Disposing",this::class.java)
        mDisposable.clear()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        LoggerUtils.debugLog("Disposing",this::class.java)
        mDisposable.clear()
    }

    override fun isDisposed(): Boolean {
        return mDisposable.isDisposed
    }

    override fun dispose() {
        mDisposable.clear()
    }

    override fun add(disposable: Disposable?): Boolean {
        disposable?.let {
            mDisposable.add(disposable)
            return true
        }
        return false
    }

    override fun remove(disposable: Disposable?): Boolean {
        disposable?.let {
            mDisposable.remove(disposable)
            return true
        }
        return false
    }

    override fun delete(disposable: Disposable?): Boolean {
        disposable?.let {
            mDisposable.delete(disposable)
            return true
        }
        return false
    }



    companion object{
        fun getInstance(lifecycleOwner: LifecycleOwner):LifeCycleAwareCompositeDisposable{
            val instance = LifeCycleAwareCompositeDisposable()
            lifecycleOwner.lifecycle.addObserver(instance)
            return instance
        }
    }
}
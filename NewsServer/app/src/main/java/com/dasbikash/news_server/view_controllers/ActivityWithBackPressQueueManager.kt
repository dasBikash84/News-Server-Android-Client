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

import androidx.appcompat.app.AppCompatActivity
import com.dasbikash.news_server_data.utills.ExceptionUtils
import java.util.*

abstract class ActivityWithBackPressQueueManager:AppCompatActivity(),BackPressQueueManager{
    private val backPressTaskMap = mutableMapOf<String,()->Unit>()
    private val backPressTaskTagList = mutableListOf<String>()

    override fun addToBackPressTaskQueue(task: () -> Unit): String {
        ExceptionUtils.thowExceptionIfNotOnMainThred()
        val uuid= UUID.randomUUID().toString()
        backPressTaskMap.put(uuid,task)
        backPressTaskTagList.add(uuid)
        return uuid
    }

    override fun removeTaskFromQueue(taskTag: String) {
        ExceptionUtils.thowExceptionIfNotOnMainThred()
        val task = backPressTaskMap.get(taskTag)
        task?.let {
            backPressTaskTagList.remove(taskTag)
            backPressTaskMap.remove(taskTag)
        }
    }

    override fun onBackPressed() {
        if (backPressTaskTagList.isNotEmpty()){
            val taskTag = backPressTaskTagList.last()
            val task = backPressTaskMap.get(taskTag)!!
            task()
            backPressTaskTagList.remove(taskTag)
            backPressTaskMap.remove(taskTag)
        }else {
            super.onBackPressed()
        }
    }
}

interface BackPressQueueManager{
    fun addToBackPressTaskQueue(task:()->Unit):String
    fun removeTaskFromQueue(taskTag:String)
}
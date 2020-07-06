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
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPreferenceUtilsTest {

    private lateinit var appContext: Context

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getTargetContext()
    }

    @After
    fun tearDown() {
    }

    /*@Test
    fun testForString(){
        SharedPreferenceUtils.saveData(context = appContext,data = "Test String",key = "test_string")
        SystemClock.sleep(1000)
        LoggerUtils.debugLog( SharedPreferenceUtils.getData(appContext,defaultValue = "",key="test_string").toString(),this::class.java)
    }

    @Test
    fun testForLong(){
        SharedPreferenceUtils.saveData(context = appContext,data = 1245L,key = "test_long")
        SystemClock.sleep(1000)
        LoggerUtils.debugLog(""+SharedPreferenceUtils.getData(appContext,defaultValue = 0L,key = "test_long") as Long,this::class.java)
    }

    @Test
    fun testForInt(){
        SharedPreferenceUtils.saveData(context = appContext,data = 12413125,key = "test_int")
        SystemClock.sleep(1000)
        LoggerUtils.debugLog(""+SharedPreferenceUtils.getData(appContext,defaultValue = 0,key = "test_int") as Int,this::class.java)
    }

    @Test
    fun testForFloat(){
        SharedPreferenceUtils.saveData(context = appContext,data = 123.23F,key = "test_Float")
        SystemClock.sleep(1000)
        LoggerUtils.debugLog("${SharedPreferenceUtils.getData(appContext,defaultValue = 0F,key = "test_Float") as Float}",this::class.java)
    }

    @Test
    fun testForBoolean(){
        SharedPreferenceUtils.saveData(context = appContext,data = true,key = "test_Boolean")
        SystemClock.sleep(1000)
        LoggerUtils.debugLog(SharedPreferenceUtils.getData(appContext,defaultValue = false,key = "test_Boolean").toString(),this::class.java)
    }*/
    @org.junit.Test
    fun getWebPageContent() {
        println("https://square.github.io/okhttp/")
    }
}
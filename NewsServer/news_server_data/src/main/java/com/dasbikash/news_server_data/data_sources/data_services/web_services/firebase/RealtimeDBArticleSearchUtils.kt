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

package com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase

import com.dasbikash.news_server_data.utills.ExceptionUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

internal object RealtimeDBArticleSearchUtils {

    private const val MAX_WAITING_MS_FOR_NET_RESPONSE = 30000L

    fun getSerachKeyWords():List<String>{

        val serachKeyWords = mutableListOf<String>()
        val lock = Object()

        RealtimeDBUtils.mSerachKeyWordsNode
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        synchronized(lock){lock.notify()}
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.asSequence().forEach {
                            serachKeyWords.add(it.key!!)
                        }
                        synchronized(lock){lock.notify()}
                    }
                })
        try {
            synchronized(lock){lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE)}
        }catch (ex:InterruptedException){}

        return serachKeyWords.toList()
    }

    fun getKeyWordSerachResult(keyWord:String):Map<String,String>{

        val keyWordSerachResult = mutableMapOf<String,String>()
        val lock = Object()

        RealtimeDBUtils.mKeyWordSerachResultNode
                .child(keyWord)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        synchronized(lock){lock.notify()}
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.asSequence().forEach {
                            keyWordSerachResult.put(it.key!!,it.value!! as String)
                        }
                        synchronized(lock){lock.notify()}
                    }
                })
        try {
            synchronized(lock){lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE)}
        }catch (ex:InterruptedException){}

        return keyWordSerachResult.toMap()
    }
}
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

import androidx.annotation.Keep
import com.dasbikash.news_server_data.exceptions.SettingsServerException
import com.dasbikash.news_server_data.exceptions.SettingsServerUnavailable
import com.dasbikash.news_server_data.exceptions.UserSettingsNotFoundException
import com.dasbikash.news_server_data.exceptions.WrongCredentialException
import com.dasbikash.news_server_data.models.UserSettingsUpdateDetails
import com.dasbikash.news_server_data.models.UserSettingsUpdateTime
import com.dasbikash.news_server_data.models.room_entity.FavouritePageEntry
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.utills.LoggerUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

internal object RealtimeDBUserSettingsUtils {

    private const val MAX_WAITING_MS_FOR_NET_RESPONSE = 30000L
    private const val FAV_PAGE_ID_MAP_NODE = "favPageIdMap"
    private const val PAGE_GROUPS_NODE = "pageGroups"
    private const val FAV_PAGE_ENTRY_MAP_NODE = "favPageEntryMap"
    private const val UPDATE_LOG_NODE = "updateLog"

    fun getCurrentUserName(): String? {
        FirebaseAuth.getInstance().currentUser?.let {
            LoggerUtils.debugLog("providerId: ${it.providerId}", this::class.java)
            val email = it.email
            val diplayName = it.displayName
            val phoneNumber = it.phoneNumber
            if (!email.isNullOrEmpty()) {
                if (!diplayName.isNullOrEmpty()) {
                    LoggerUtils.debugLog("returning diplayName with email", this::class.java)
                    return "${diplayName} <br>(${email})"
                }
                LoggerUtils.debugLog("returning email only", this::class.java)
                return email
            }
            if (!phoneNumber.isNullOrEmpty()) {
                if (!diplayName.isNullOrEmpty()) {
                    LoggerUtils.debugLog("returning phoneNumber with email", this::class.java)
                    return "${diplayName} <br>(${phoneNumber})"
                }
                LoggerUtils.debugLog("returning phoneNumber only", this::class.java)
                return phoneNumber
            }
        }
        return null
    }

    fun signOutUser() {
        LoggerUtils.debugLog("signOutUser()", this::class.java)
        completeSignOut()
        signInAnonymously()
    }

    fun completeSignOut() {
        LoggerUtils.debugLog("completeSignOut()", this::class.java)
        FirebaseAuth.getInstance().currentUser?.let {
            if (it.isAnonymous) {
                it.delete()
            }
        }
        FirebaseAuth.getInstance().signOut()
    }

    fun getUserPreferenceData(): List<FavouritePageEntry> {
        LoggerUtils.debugLog("getUserPreferenceData()", this@RealtimeDBUserSettingsUtils::class.java)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || user.isAnonymous) {
            throw WrongCredentialException()
        }

        var data: List<FavouritePageEntry> = emptyList()
        val lock = Object()

        var userSettingsException: SettingsServerException? = null

        getUserSettingsNodes(user)
                .rootUserSettingsNode
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        LoggerUtils.debugLog("onCancelled", this@RealtimeDBUserSettingsUtils::class.java)
                        userSettingsException = SettingsServerException(error.message)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        LoggerUtils.debugLog("onDataChange", this@RealtimeDBUserSettingsUtils::class.java)
                        if (dataSnapshot.exists()) {
                            try {
                                val userSettingsData = dataSnapshot.getValue(UserSettingsData::class.java)!!
                                if (userSettingsData.favPageIdMap.isNotEmpty()){
                                    val newFavPageEntryMap = mutableMapOf<String,FavouritePageEntry>()
                                    userSettingsData.favPageIdMap.filter { it.value }.forEach{
                                        newFavPageEntryMap.put(it.key, FavouritePageEntry(pageId = it.key,subscribed = false))
                                    }
                                    userSettingsData.favPageEntryMap = newFavPageEntryMap.toMap()
                                    getUserSettingsNodes(user).favPageIdMapRef.setValue(null)
                                    getUserSettingsNodes(user).pageGroupMapRef.setValue(null)
                                    getUserSettingsNodes(user).favPageEntryMap.setValue(userSettingsData.favPageEntryMap)
                                }
                                data = userSettingsData.favPageEntryMap.values.toList()
                            } catch (ex: Exception) {
                                userSettingsException = UserSettingsNotFoundException(ex)
                            }
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })
        LoggerUtils.debugLog("before synchronized(lock)", this@RealtimeDBUserSettingsUtils::class.java)
        try {
            synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}
        LoggerUtils.debugLog("after synchronized(lock)", this@RealtimeDBUserSettingsUtils::class.java)
        userSettingsException?.let { throw userSettingsException as SettingsServerException }
        return data
    }

    private fun getUserSettingsNodes(user: FirebaseUser) = object {
        val rootUserSettingsNode = RealtimeDBUtils.mUserSettingsRootReference.child(user.uid)
        val favPageIdMapRef = RealtimeDBUtils.mUserSettingsRootReference.child(user.uid).child(FAV_PAGE_ID_MAP_NODE)
        val pageGroupMapRef = RealtimeDBUtils.mUserSettingsRootReference.child(user.uid).child(PAGE_GROUPS_NODE)
        val favPageEntryMap = RealtimeDBUtils.mUserSettingsRootReference.child(user.uid).child(FAV_PAGE_ENTRY_MAP_NODE)
        val updateLogRef = RealtimeDBUtils.mUserSettingsRootReference.child(user.uid).child(UPDATE_LOG_NODE)
    }

    private fun getLoggedInFirebaseUser(): FirebaseUser {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null || user.isAnonymous) {
            throw WrongCredentialException()
        }
        return user
    }

    private fun updateFavPageEntryMap(page: Page, status: Boolean
                                      , doOnSuccess: (() -> Unit)? = null, doOnFailure: (() -> Unit)? = null) {
        val payload:Any?
        if (status){
            payload = FavouritePageEntry(pageId = page.id)
        }else{
            payload=null
        }
        getUserSettingsNodes(getLoggedInFirebaseUser()).favPageEntryMap
                .child(page.id)
                .setValue(payload)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        executeBackGroundTask {
                            addSettingsUpdateTime()
                            doOnSuccess?.let { it() }
                        }
                    } else {
                        doOnFailure?.let { it() }
                    }
                }


    }

    fun addPageToFavList(page: Page, doOnSuccess: (() -> Unit)? = null, doOnFailure: (() -> Unit)? = null) = updateFavPageEntryMap(page, true, doOnSuccess, doOnFailure)
    fun removePageFromFavList(page: Page, doOnSuccess: (() -> Unit)? = null, doOnFailure: (() -> Unit)? = null) = updateFavPageEntryMap(page, false, doOnSuccess, doOnFailure)

    fun updateFavouritePageEntry(favouritePageEntry: FavouritePageEntry, doOnSuccess: (() -> Unit)? = null, doOnFailure: (() -> Unit)? = null){
        getUserSettingsNodes(getLoggedInFirebaseUser()).favPageEntryMap
                .child(favouritePageEntry.pageId)
                .setValue(favouritePageEntry)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        executeBackGroundTask {
                            addSettingsUpdateTime()
                            doOnSuccess?.let { it() }
                        }
                    } else {
                        doOnFailure?.let { it() }
                    }
                }

    }

    private fun addSettingsUpdateTime() {
        val user = getLoggedInFirebaseUser()
        val userSettingsNodes = getUserSettingsNodes(user)

        userSettingsNodes.updateLogRef
                .push()
                .setValue(UserSettingsUpdateDetails())
    }

    private fun executeBackGroundTask(task: () -> Unit) {
        LoggerUtils.debugLog("executeBackGroundTask", this::class.java)
        Observable.just(task)
                .subscribeOn(Schedulers.io())
                .map {
                    task()
                }
                .subscribe()
    }


    fun getLastUserSettingsUpdateTime(): Long {

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || user.isAnonymous) {
            throw WrongCredentialException()
        }

        val userSettingsUpdateLogNode = getUserSettingsNodes(user).updateLogRef
        val lock = Object()

        var lastUpdateTime = 0L
        var settingsServerException: SettingsServerException? = null

        userSettingsUpdateLogNode
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        settingsServerException = SettingsServerException(databaseError.message)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try {
                            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                                lastUpdateTime = dataSnapshot.children.last().getValue(UserSettingsUpdateTime::class.java)?.timeStamp
                                        ?: 0L
                            }
                        } catch (ex: Exception) {
                            settingsServerException = UserSettingsNotFoundException(ex)
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })
        try {
            synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        settingsServerException?.let { throw it }

        return lastUpdateTime
    }

    fun checkIfLoogedAsAdmin(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || user.isAnonymous) {
            return false
        }

        val userSettingsUpdateLogNode = RealtimeDBUtils.mAdminListReference
        val lock = Object()

        var settingsServerException: SettingsServerException? = UserSettingsNotFoundException()

        userSettingsUpdateLogNode
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        settingsServerException = SettingsServerException(databaseError.message)
                        synchronized(lock) { lock.notify() }
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try {
                            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                                settingsServerException = null
                            }
                        } catch (ex: Exception) {
                            settingsServerException = UserSettingsNotFoundException(ex)
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })

        try {
            synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        settingsServerException?.let { return false }

        return true
    }

    fun signInAnonymously() {

        LoggerUtils.debugLog("completeSignOut()", this::class.java)

        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        val lock = Object()
        var settingsServerException: SettingsServerException? = SettingsServerException()

        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                    override fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful) {
                            settingsServerException = null
                        }else{
                            task.exception?.let {
                                settingsServerException = SettingsServerException(it)
                            }
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })
        try {
            synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        settingsServerException?.let { throw it }
    }

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
}

@Keep
data class UserSettingsData(
        var favPageIdMap:Map<String,Boolean> = emptyMap(),
        var favPageEntryMap:Map<String,FavouritePageEntry> = emptyMap()
)
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

import android.content.Context
import com.dasbikash.news_server_data.data_sources.data_services.user_details_data_service.UserIpDataService
import com.dasbikash.news_server_data.exceptions.SettingsServerException
import com.dasbikash.news_server_data.exceptions.SettingsServerUnavailable
import com.dasbikash.news_server_data.exceptions.UserSettingsNotFoundException
import com.dasbikash.news_server_data.exceptions.WrongCredentialException
import com.dasbikash.news_server_data.models.UserSettingsUpdateDetails
import com.dasbikash.news_server_data.models.UserSettingsUpdateTime
import com.dasbikash.news_server_data.models.room_entity.Page
import com.dasbikash.news_server_data.models.room_entity.PageGroup
import com.dasbikash.news_server_data.models.room_entity.UserPreferenceData
import com.dasbikash.news_server_data.repositories.UserSettingsRepository
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
    private const val PAGE_GROUP_LIST_NODE = "pageGroups"
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
        LoggerUtils.debugLog("signOutUser()",this::class.java)
        completeSignOut()
        signInAnonymously()
    }

    fun completeSignOut(){
        LoggerUtils.debugLog("completeSignOut()",this::class.java)
        FirebaseAuth.getInstance().currentUser?.let {
            if (it.isAnonymous){
                it.delete()
            }
        }
        FirebaseAuth.getInstance().signOut()
    }

    fun getUserPreferenceData(): UserPreferenceData {
        LoggerUtils.debugLog("getUserPreferenceData()", this@RealtimeDBUserSettingsUtils::class.java)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || user.isAnonymous) {
            throw WrongCredentialException()
        }

        var data: UserPreferenceData? = null
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
                                data = dataSnapshot.getValue(UserPreferenceData::class.java)!!
                                LoggerUtils.debugLog(data!!.toString(), this@RealtimeDBUserSettingsUtils::class.java)
                                data!!.favouritePageIds.clear()
                                data!!.favPageIdMap.filter {
                                    it.value
                                }.keys.asSequence().forEach { data!!.favouritePageIds.add(it) }
                                LoggerUtils.debugLog(data!!.toString(), this@RealtimeDBUserSettingsUtils::class.java)
//                                data!!.favouritePageIds.addAll(data!!.favPageIdMap.keys)
                                synchronized(lock) { lock.notify() }
                            } catch (ex: Exception) {
                                userSettingsException = UserSettingsNotFoundException(ex)
                            }
                        }
                    }
                })
        LoggerUtils.debugLog("before synchronized(lock)", this@RealtimeDBUserSettingsUtils::class.java)
        synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }
        LoggerUtils.debugLog("after synchronized(lock)", this@RealtimeDBUserSettingsUtils::class.java)
        userSettingsException?.let { throw userSettingsException as SettingsServerException }
        if (data == null) {
            throw SettingsServerUnavailable()
        }
        return data!!
    }

    private fun getUserSettingsNodes(user: FirebaseUser) = object {
        val rootUserSettingsNode = RealtimeDBUtils.mUserSettingsRootReference.child(user.uid)
        val favPageIdMapRef = RealtimeDBUtils.mUserSettingsRootReference.child(user.uid).child(FAV_PAGE_ID_MAP_NODE)
        val pageGroupListRef = RealtimeDBUtils.mUserSettingsRootReference.child(user.uid).child(PAGE_GROUP_LIST_NODE)
        val updateLogRef = RealtimeDBUtils.mUserSettingsRootReference.child(user.uid).child(UPDATE_LOG_NODE)
    }

    fun uploadUserPreferenceData(userPreferenceData: UserPreferenceData) {

        val user = getLoggedInFirebaseUser()
        val userSettingsNodes = getUserSettingsNodes(user)

        userSettingsNodes.favPageIdMapRef
                .setValue(getFavPageIdMapForRemoteDb(userPreferenceData.favouritePageIds))

        userSettingsNodes.pageGroupListRef
                .setValue(userPreferenceData.pageGroups)

        addSettingsUpdateTime()
    }

    private fun getIpAddress(): String {
        var ipAddress: String
        try {
            ipAddress = UserIpDataService.getIpAddress()
        } catch (ex: Throwable) {
            ipAddress = UserSettingsUpdateDetails.NULL_IP
        }
        return ipAddress
    }

    private fun getLoggedInFirebaseUser(): FirebaseUser {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null || user.isAnonymous) {
            throw WrongCredentialException()
        }
        return user
    }

    private fun getFavPageRef(page: Page) =
            RealtimeDBUtils.mUserSettingsRootReference.child(getLoggedInFirebaseUser().uid).child(FAV_PAGE_ID_MAP_NODE).child(page.id)

    private fun changePageStateOnFavList(page: Page, status: Boolean) {
        val favPageRef = getFavPageRef(page)
        favPageRef.setValue(status)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    } else {
                        when (status) {
                            true -> {
                                UserSettingsRepository.undoAddPageToFavList(page)
                            }
                            false -> {
                                UserSettingsRepository.undoRemovePageFromFavList(page)
                            }
                        }
                    }
                }

        addSettingsUpdateTime()
    }

    enum class PageGroupEditAction { ADD, DELETE }

    private fun uploadPageGroupData(pageGroup: PageGroup, pageGroupEditAction: PageGroupEditAction
                                    ,doOnSuccess:(()->Unit)?=null,doOnFailure:(()->Unit)?=null)
    {
        LoggerUtils.debugLog("uploadPageGroupData. Action: ${pageGroupEditAction.name} for ${pageGroup.name}", this::class.java)
        val pageGroupEntryRef = getPageGroupEntryRef(pageGroup.name)
        pageGroupEntryRef.setValue(when (pageGroupEditAction) {
            RealtimeDBUserSettingsUtils.PageGroupEditAction.ADD -> pageGroup
            RealtimeDBUserSettingsUtils.PageGroupEditAction.DELETE -> null
        })
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        doOnSuccess?.let {
                            Observable.just(it)
                                    .subscribeOn(Schedulers.io())
                                    .map { it() }
                                    .subscribe()
                        }
                    } else {
                        when (pageGroupEditAction) {
                            RealtimeDBUserSettingsUtils.PageGroupEditAction.ADD -> {
                                UserSettingsRepository.undoAddPageGroup(pageGroup)
                            }
                            RealtimeDBUserSettingsUtils.PageGroupEditAction.DELETE -> {
                                UserSettingsRepository.undoDeletePageGroup(pageGroup)
                            }
                        }
                        doOnFailure?.let {
                            Observable.just(it)
                                    .subscribeOn(Schedulers.io())
                                    .map { it() }
                                    .subscribe()
                        }
                    }
                }
        addSettingsUpdateTime()
    }

    private fun getPageGroupEntryRef(pageGroupEntryId: String) =
            RealtimeDBUtils.mUserSettingsRootReference.child(getLoggedInFirebaseUser().uid)
                    .child(PAGE_GROUP_LIST_NODE).child(pageGroupEntryId)

    private fun addSettingsUpdateTime() {
        val user = getLoggedInFirebaseUser()
        val userSettingsNodes = getUserSettingsNodes(user)

        val ipAddress: String = getIpAddress()
        userSettingsNodes.updateLogRef
                .push()
                .setValue(UserSettingsUpdateDetails(userIp = ipAddress))
    }

    private fun getFavPageIdMapForRemoteDb(favouritePageIds: List<String>): Map<String, Boolean> {
        val favPageIdMapForRemoteDb = mutableMapOf<String, Boolean>()
        favouritePageIds.asSequence().forEach { favPageIdMapForRemoteDb.put(it, true) }
        return favPageIdMapForRemoteDb.toMap()
    }

    fun addPageGroup(pageGroup: PageGroup
                     ,doOnSuccess:(()->Unit)?=null,doOnFailure:(()->Unit)?=null) =
            uploadPageGroupData(pageGroup, RealtimeDBUserSettingsUtils.PageGroupEditAction.ADD,doOnSuccess,doOnFailure)
    fun deletePageGroup(pageGroup: PageGroup
                        ,doOnSuccess:(()->Unit)?=null,doOnFailure:(()->Unit)?=null) =
            uploadPageGroupData(pageGroup, RealtimeDBUserSettingsUtils.PageGroupEditAction.DELETE,doOnSuccess,doOnFailure)

    fun savePageGroup(oldPageGroup: PageGroup, pageGroup: PageGroup) {
        deletePageGroup(oldPageGroup, doOnSuccess = {
            addPageGroup(pageGroup, doOnFailure = {
                addPageGroup(oldPageGroup)
                UserSettingsRepository.undoDeletePageGroup(oldPageGroup)
            })
        })

    }

    fun addPageToFavList(page: Page) = changePageStateOnFavList(page, true)
    fun removePageFromFavList(page: Page) = changePageStateOnFavList(page, false)


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
        synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }

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

        synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }

        settingsServerException?.let { return false }

        return true
    }

    fun signInAnonymously(){

        LoggerUtils.debugLog("completeSignOut()",this::class.java)

        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        val lock = Object()
        var settingsServerException: SettingsServerException? = SettingsServerException()

        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(object :OnCompleteListener<AuthResult>{
                    override fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful){
                            settingsServerException = null
                        }
                        synchronized(lock) { lock.notify() }
                    }
                })

        synchronized(lock) { lock.wait(MAX_WAITING_MS_FOR_NET_RESPONSE) }
        settingsServerException?.let { throw it }
    }
}
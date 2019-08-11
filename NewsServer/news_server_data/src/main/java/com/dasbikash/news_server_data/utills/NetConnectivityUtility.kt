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

package com.dasbikash.news_server_data.utills

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object NetConnectivityUtility : BroadcastReceiver() {

    private var mNoInternertToastShown = false
    private const val NO_INTERNET_TOAST_MESSAGE = "No internet connection!!!"
    private var mCurrentNetworkType = NetConnectivityUtility.NETWORK_TYPE.UN_INITIALIZED

    enum class NETWORK_TYPE {
        MOBILE, WIFI, WIMAX, ETHERNET, BLUETOOTH, DC, OTHER, UN_INITIALIZED
    }

    private fun generateNetworkAvailableBroadcast(context: Context) {
        val broadcastIntent = Intent(NETWORK_AVAILABLE_BROADCAST)
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && intent.action != null &&
                intent.action!!.equals(CONNECTIVITY_CHANGE_FILTER, ignoreCase = true)) {
            refreshNetworkType(context)
        }
    }

    @Suppress("DEPRECATION")
    private fun refreshNetworkType(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mCurrentNetworkType = when (activeNetworkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> NetConnectivityUtility.NETWORK_TYPE.WIFI
                    ConnectivityManager.TYPE_MOBILE,
                    ConnectivityManager.TYPE_MOBILE_DUN,
                    ConnectivityManager.TYPE_MOBILE_HIPRI,
                    ConnectivityManager.TYPE_MOBILE_MMS,
                    ConnectivityManager.TYPE_MOBILE_SUPL -> NetConnectivityUtility.NETWORK_TYPE.MOBILE
                    ConnectivityManager.TYPE_BLUETOOTH -> NetConnectivityUtility.NETWORK_TYPE.BLUETOOTH
                    ConnectivityManager.TYPE_ETHERNET -> NetConnectivityUtility.NETWORK_TYPE.ETHERNET
                    ConnectivityManager.TYPE_WIMAX -> NetConnectivityUtility.NETWORK_TYPE.WIMAX
                    else -> NetConnectivityUtility.NETWORK_TYPE.OTHER
                }
            } else {
                val network = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false ||
                        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) ?: false) {
                    mCurrentNetworkType = NetConnectivityUtility.NETWORK_TYPE.WIFI
                } else if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)  ?: false) {
                    mCurrentNetworkType = NetConnectivityUtility.NETWORK_TYPE.MOBILE
                } else if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)  ?: false) {
                    mCurrentNetworkType = NetConnectivityUtility.NETWORK_TYPE.BLUETOOTH
                } else if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)  ?: false) {
                    mCurrentNetworkType = NetConnectivityUtility.NETWORK_TYPE.ETHERNET
                } else {
                    mCurrentNetworkType = NetConnectivityUtility.NETWORK_TYPE.OTHER
                }
            }
            mNoInternertToastShown = false
            generateNetworkAvailableBroadcast(context)
        } else {
            mCurrentNetworkType = NetConnectivityUtility.NETWORK_TYPE.DC
        }
        LoggerUtils.fileLog("Current Network Type: ${mCurrentNetworkType.name}", this::class.java, context)
    }

    private val NETWORK_AVAILABLE_BROADCAST = "NetConnectivityUtility.net_available"

    val intentFilterForNetworkAvailableBroadcastReceiver: IntentFilter
        get() = IntentFilter(NETWORK_AVAILABLE_BROADCAST)

    private val CONNECTIVITY_CHANGE_FILTER = "android.net.conn.CONNECTIVITY_CHANGE"

    private val intentFilterForConnectivityChangeBroadcastReceiver: IntentFilter
        get() = IntentFilter(CONNECTIVITY_CHANGE_FILTER)

    val isOnMobileDataNetwork: Boolean
        get() {
            return mCurrentNetworkType == NetConnectivityUtility.NETWORK_TYPE.MOBILE
        }

    val isConnected: Boolean
        get() {
            return mCurrentNetworkType != NetConnectivityUtility.NETWORK_TYPE.DC
        }

    val isInitialize: Boolean
        get() {
            return mCurrentNetworkType != NetConnectivityUtility.NETWORK_TYPE.UN_INITIALIZED
        }

    fun refreshNetworkStatus(context: Context): NETWORK_TYPE{
        if (mCurrentNetworkType == NetConnectivityUtility.NETWORK_TYPE.UN_INITIALIZED) {
            refreshNetworkType(context)
        }
        return mCurrentNetworkType
    }

    fun initialize(context: Context) {
        context.registerReceiver(this, intentFilterForConnectivityChangeBroadcastReceiver)
    }

    fun showNoInternetToast(context: Context) {
        if (!isConnected && !mNoInternertToastShown) {
            mNoInternertToastShown = true
            Toast.makeText(context, NO_INTERNET_TOAST_MESSAGE, Toast.LENGTH_LONG).show()
        }
    }

    fun showNoInternetToastAnyWay(context: Context) {
        if (!isConnected) {
            mNoInternertToastShown = true
            Toast.makeText(context, NO_INTERNET_TOAST_MESSAGE, Toast.LENGTH_SHORT).show()
        }
    }
}

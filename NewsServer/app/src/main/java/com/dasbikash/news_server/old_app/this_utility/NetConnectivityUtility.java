/*
 * Copyright 2019 www.dasbikash.org. All rights reserved.
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

package com.dasbikash.news_server.old_app.this_utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

@SuppressWarnings({"deprecation", "unchecked"})
public class NetConnectivityUtility extends BroadcastReceiver {
    private static final String TAG = "StackTrace";

    private static final String NETWORK_AVAILABLE_BROADCAST =
            "NetConnectivityUtility.net_available";
    private static final String CONNECTIVITY_CHANGE_FILTER = "android.net.conn.CONNECTIVITY_CHANGE";

    private static boolean sNetConToastShown = false;
    private static NetConnectivityUtility sNetConnectivityUtility;

    public enum NETWORK_TYPE {
        MOBILE, WIFI, WIMAX, ETHERNET, BLUETOOTH,DC,OTHER
    }

    private NETWORK_TYPE mCurrentNetworkType = NETWORK_TYPE.DC;

    private static IntentFilter getIntentFilterForConnectivityChangeBroadcastReceiver(){
        return new IntentFilter(CONNECTIVITY_CHANGE_FILTER);
    }

    private void generateNetworkAvailableBroadcast(Context context) {
        Intent broadcastIntent = new Intent(NETWORK_AVAILABLE_BROADCAST);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent!=null && intent.getAction()!=null &&
        intent.getAction().equalsIgnoreCase(CONNECTIVITY_CHANGE_FILTER)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo sActiveNetworkInfo = null;
            if (connectivityManager!=null) {
                sActiveNetworkInfo = connectivityManager.getActiveNetworkInfo();
            }

            if (sActiveNetworkInfo !=null && sActiveNetworkInfo.isConnected()){
                //Log.d(TAG, "onReceive: Network found");
                CalenderUtility.updateTime();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    switch (sActiveNetworkInfo.getType()) {
                        case ConnectivityManager.TYPE_WIFI:
                            mCurrentNetworkType = NETWORK_TYPE.WIFI;
                            //Log.d(TAG, "onReceive Old: NETWORK_TYPE.WIFI");
                            break;
                        case ConnectivityManager.TYPE_MOBILE:
                        case ConnectivityManager.TYPE_MOBILE_DUN:
                        case ConnectivityManager.TYPE_MOBILE_HIPRI:
                        case ConnectivityManager.TYPE_MOBILE_MMS:
                        case ConnectivityManager.TYPE_MOBILE_SUPL:
                            mCurrentNetworkType = NETWORK_TYPE.MOBILE;
                            //Log.d(TAG, "onReceive Old: NETWORK_TYPE.MOBILE");
                            break;
                        case ConnectivityManager.TYPE_BLUETOOTH:
                            mCurrentNetworkType = NETWORK_TYPE.BLUETOOTH;
                            break;
                        case ConnectivityManager.TYPE_ETHERNET:
                            mCurrentNetworkType = NETWORK_TYPE.ETHERNET;
                            break;
                        case ConnectivityManager.TYPE_WIMAX:
                            mCurrentNetworkType = NETWORK_TYPE.WIMAX;
                            break;
                        default:
                            mCurrentNetworkType = NETWORK_TYPE.OTHER;
                            //Log.d(TAG, "onReceive: NETWORK_TYPE.OTHER");
                            break;
                    }
                }else {
                    Network network = connectivityManager.getActiveNetwork();
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)){
                        mCurrentNetworkType = NETWORK_TYPE.WIFI;
                        //Log.d(TAG, "onReceive New: NETWORK_TYPE.WIFI");
                    } else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                        mCurrentNetworkType = NETWORK_TYPE.MOBILE;
                        //Log.d(TAG, "onReceive New: NETWORK_TYPE.MOBILE");
                    } else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)){
                        mCurrentNetworkType = NETWORK_TYPE.BLUETOOTH;
                    } else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        mCurrentNetworkType = NETWORK_TYPE.ETHERNET;
                    } else {
                        mCurrentNetworkType = NETWORK_TYPE.OTHER;
                    }
                }
                generateNetworkAvailableBroadcast(context);
            } else {
                mCurrentNetworkType = NETWORK_TYPE.DC;
                //Log.d(TAG, "onReceive: NETWORK_TYPE.DC");
            }
        }
    }

    public static boolean isOnMobileDataNetwork(){
        if (sNetConnectivityUtility!=null){
            return sNetConnectivityUtility.mCurrentNetworkType == NETWORK_TYPE.MOBILE;
        }
        return false;
    }

    public static void init(){
        if (sNetConnectivityUtility == null) {
            sNetConnectivityUtility = new NetConnectivityUtility();
            NewsServerUtility.getContext().registerReceiver(sNetConnectivityUtility,
                    getIntentFilterForConnectivityChangeBroadcastReceiver());
        }
        sNetConToastShown = false;
    }

    //Only returns connection status
    public static boolean checkConnection(){
        return (sNetConnectivityUtility != null) &&
                (sNetConnectivityUtility.mCurrentNetworkType != NETWORK_TYPE.DC);
    }

    //Returns connection status, shows toast if not any any also does init if required
    public static boolean isConnected(){

        if (checkConnection()){
            sNetConToastShown = false;
            return true;
        }else {
            if (sNetConnectivityUtility == null){
                init();
            }
            if (!sNetConToastShown){
                sNetConToastShown = true;
                DisplayUtility.showLongToast(R.string.NO_NET_NOTIFICATION_MESSAGE);
            }
            return false;
        }
    }

    public static IntentFilter getIntentFilterForNetworkAvailableBroadcastReceiver(){
        return new IntentFilter(NETWORK_AVAILABLE_BROADCAST);
    }

}

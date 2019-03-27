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

package com.dasbikash.news_server.old_app.this_view;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderService;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroup;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group_entry.FeatureGroupEntry;
import com.dasbikash.news_server.old_app.this_data.feature_group_entry.FeatureGroupEntryHelper;
import com.dasbikash.news_server.old_app.this_utility.NetConnectivityUtility;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.util.ArrayList;

@SuppressWarnings({"ConstantConditions", "FieldCanBeLocal"})
public class WelcomeScreenFragment extends Fragment {

    private static final String TAG = "StackTrace";

    private static final long EDI_PLACEWMENT_DELAY_MS_FOR_NET_CON = 2000L;
    private static final long EDI_LOAD_DELAY_MS = 5000L;

    private PlaceHomeEditionDownloadRequest mPlaceHomeEditionDownloadRequest=null;
    private HomePageLoader mHomePageLoader = null;
    private int mEditionDownloadRequestCount=0;


    ArrayList<Feature> mHomeFeatures = new ArrayList<>();
    private CallBacks mCallBacks;


    interface CallBacks{
        void loadHomePageFromWelcomeScreen();
    }

    private final BroadcastReceiver mNetConAvailableBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleNetConAvailableBroadcast(intent);
        }
    };


    private final BroadcastReceiver mEditionLoadBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEditionLoadBrodcastMessage(intent);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBacks = (CallBacks) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_screen_ns,container,false);
    }

    @Override
    public void onResume() {
        super.onResume();

        //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR, WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR);

        registerBrodcastReceivers();
        mPlaceHomeEditionDownloadRequest = new PlaceHomeEditionDownloadRequest();
        NewsServerUtility.init(getActivity().getApplicationContext());
        mPlaceHomeEditionDownloadRequest.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBrodcastReceivers();
        if (mPlaceHomeEditionDownloadRequest !=null){
            mPlaceHomeEditionDownloadRequest.cancel(true);
            mPlaceHomeEditionDownloadRequest = null;
        }
        if (mHomePageLoader !=null){
            mHomePageLoader.cancel(true);
            mHomePageLoader = null;
        }
    }

    private class PlaceHomeEditionDownloadRequest extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            if (!NetConnectivityUtility.checkConnection()) {
                try {
                    //Log.d(TAG, "doInBackground: Going to sleep.");
                    Thread.sleep(EDI_PLACEWMENT_DELAY_MS_FOR_NET_CON);
                } catch (Exception ex) {
                    //Log.d(TAG, "doInBackground: interrupted by Netcon Braodcast");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mPlaceHomeEditionDownloadRequest = null;
            //mCallBacks.loadHomePageFromWelcomeScreen();
            placeHomeEditionDownloadAction();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mPlaceHomeEditionDownloadRequest = null;
            placeHomeEditionDownloadAction();
        }
    }

    private void placeHomeEditionDownloadAction() {
        //Log.d(TAG, "placeHomeEditionDownloadAction: ");
        //if (NetConnectivityUtility.isConnected()){
            FeatureGroup homeFeatureGroup = FeatureGroupHelper.getFeatureGroupForHomePage();
            ArrayList<FeatureGroupEntry> homeFeatureGroupEntries = FeatureGroupEntryHelper.getEntriesForFeatureGroup(homeFeatureGroup);
            for (FeatureGroupEntry featureGroupEntry :
                    homeFeatureGroupEntries) {
                mHomeFeatures.add(FeatureHelper.findFeatureById(featureGroupEntry.getFeatureId()));
            }
            for (Feature feature :
                    mHomeFeatures) {
                if (EditionLoaderService.placeFirstEditionDownloadRequest(feature)) {
                    //Log.d(TAG, "placeHomeEditionDownloadAction: mEditionDownloadRequestCount++");
                    mEditionDownloadRequestCount++;
                }
                if (mHomeFeatures.indexOf(feature) == 1){
                    break;
                }
            }
        //}
        mHomePageLoader = new HomePageLoader();
        mHomePageLoader.execute();
    }

    private class HomePageLoader extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            //Log.d(TAG, "HomePageLoader onPreExecute: ");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mEditionDownloadRequestCount>0){
                try {
                    Thread.sleep(EDI_LOAD_DELAY_MS);
                } catch (Exception ex){
                    //Log.d(TAG, "doInBackground: interrupted by Edition load Braodcast");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Log.d(TAG, "HomePageLoader onPostExecute: ");
            mHomePageLoader = null;
            mCallBacks.loadHomePageFromWelcomeScreen();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            //Log.d(TAG, "HomePageLoader onCancelled: ");
            mHomePageLoader = null;
            mCallBacks.loadHomePageFromWelcomeScreen();
        }
    }

    private void registerBrodcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver
                (mEditionLoadBroadcastReceiver, EditionLoaderBase.getIntentFilterForEditionDownloadBroadcastMessage());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver
                (mNetConAvailableBroadcastReceiver, NetConnectivityUtility.getIntentFilterForNetworkAvailableBroadcastReceiver());
    }

    private void unregisterBrodcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).
                unregisterReceiver(mEditionLoadBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).
                unregisterReceiver(mNetConAvailableBroadcastReceiver);
    }

    private void handleEditionLoadBrodcastMessage(Intent intent) {
        if (mHomePageLoader!=null){
            mEditionDownloadRequestCount--;
            if (mEditionDownloadRequestCount == 0) {
                mHomePageLoader.cancel(true);
            }
        }
    }

    private void handleNetConAvailableBroadcast(Intent intent) {
        //Log.d(TAG, "handleNetConAvailableBroadcast: ");
        if (mPlaceHomeEditionDownloadRequest!=null){
            mPlaceHomeEditionDownloadRequest.cancel(true);
        }
    }

}

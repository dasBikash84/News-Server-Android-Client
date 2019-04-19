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

package com.dasbikash.news_server.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.view_models.HomeViewModel;
import com.dasbikash.news_server.views.interfaces.HomeNavigator;
import com.dasbikash.news_server_data.RepositoryFactory;
import com.dasbikash.news_server_data.exceptions.DataNotFoundException;
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException;
import com.dasbikash.news_server_data.exceptions.OnMainThreadException;
import com.dasbikash.news_server_data.exceptions.RemoteDbException;
import com.dasbikash.news_server_data.repositories.SettingsRepository;
import com.dasbikash.news_server_data.utills.ExceptionUtils;
import com.dasbikash.news_server_data.utills.NetConnectivityUtility;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class InitFragment extends Fragment {

    private static final String TAG = "InitFragment";
    private static final long RETRY_DELAY_FOR_REMOTE_ERROR_INC_VALUE = 3000L;



    private HomeViewModel mHomeViewModel;

    private HomeNavigator mHomeNavigator;
    private ProgressBar mProgressBar;
    private TextView mNoInternetMessage;

    private long mRetryDelayForRemoteDBError = 0L;
    private long mRetryCountForRemoteDBError = 0L;

    private AtomicBoolean mLoadSettingsDataOnNetConnection = new AtomicBoolean(false);

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private final BroadcastReceiver mNetConAvailableBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mLoadSettingsDataOnNetConnection.get()) {
                mLoadSettingsDataOnNetConnection.set(false);
                initSettingsDataLoading(0L);
            }
        }
    };

    enum DataLoadingStatus {

        WAITING_FOR_NETWORK_INIT(false, 0),
        STARTING_SETTINGS_DATA_LOADING(true, 0),
        NEED_TO_READ_DATA_FROM_SERVER(true, 20),
        SETTINGS_DATA_LOADED(true, 75),
        USER_SETTINGS_GOING_TO_BE_LOADED(true, 80),
        EXIT(true, 100);

        private boolean setProgressbarDeterminate;
        private int progressBarValue;

        boolean isSetProgressbarDeterminate() {
            return setProgressbarDeterminate;
        }

        int getProgressBarValue() {
            return progressBarValue;
        }

        DataLoadingStatus(boolean setProgressbarDeterminate, int progressBarValue) {
            this.setProgressbarDeterminate = setProgressbarDeterminate;
            this.progressBarValue = progressBarValue;
        }

        @Override
        public String toString() {
            return "DataLoadingStatus{" +
                    "setProgressbarDeterminate=" + setProgressbarDeterminate +
                    ", progressBarValue=" + progressBarValue +
                    '}';
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_init, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.data_load_progress);
        mNoInternetMessage = view.findViewById(R.id.no_internet_message);
        mHomeViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(HomeViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBrodcastReceivers();

        initSettingsDataLoading(0L);
    }

    private void initSettingsDataLoading(long initDelay) {

        mNoInternetMessage.setVisibility(View.INVISIBLE);

        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);

        mDisposable.add(
                getDataLoadingStatusObservable(initDelay)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<DataLoadingStatus>() {
                            @Override
                            public void onNext(DataLoadingStatus loadingStatus) {
                                Log.d(TAG, "onNext: " + loadingStatus);
                                if (loadingStatus.isSetProgressbarDeterminate()) {
                                    mProgressBar.setIndeterminate(false);
                                    mProgressBar.setProgress(loadingStatus.progressBarValue);
                                } else {
                                    mProgressBar.setIndeterminate(true);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError: " + e.getClass().getCanonicalName());
                                Log.d(TAG, "onError: " + Arrays.asList(e.getStackTrace()));
                                e.printStackTrace();
                                Toast
                                        .makeText(
                                                getActivity(),
                                                "onError: " + e.getClass().getCanonicalName(),
                                                Toast.LENGTH_SHORT
                                        )
                                        .show();
                                doOnError(e);
                            }

                            @Override
                            public void onComplete() {
                                mHomeViewModel
                                        .getNewsPapers()
                                        .observe(Objects.requireNonNull(getActivity()), newspapers -> {
                                            mHomeNavigator.loadHomeFragment();
                                        });
                            }
                        })
        );
    }

    private Observable<DataLoadingStatus> getDataLoadingStatusObservable(long initDelay) {
        return Observable.create(new ObservableOnSubscribe<DataLoadingStatus>() {
            @Override
            public void subscribe(ObservableEmitter<DataLoadingStatus> emitter) throws Exception {

                SystemClock.sleep(initDelay);

                //wait for network connection
                emitter.onNext(DataLoadingStatus.WAITING_FOR_NETWORK_INIT);

                //noinspection StatementWithEmptyBody
                while (!NetConnectivityUtility.isInitialize());

                //Initialization started
                emitter.onNext(DataLoadingStatus.STARTING_SETTINGS_DATA_LOADING);

                SettingsRepository settingsRepo =
                        RepositoryFactory.getSettingsRepository(Objects.requireNonNull(getActivity()));

                if (!settingsRepo.isSettingsDataLoaded() ||
                        settingsRepo.isAppSettingsUpdated(getActivity())) {
                    // going to load app data
                    emitter.onNext(DataLoadingStatus.NEED_TO_READ_DATA_FROM_SERVER);
                    settingsRepo.loadAppSettings(getActivity());
                }

                //App data loaded
                emitter.onNext(DataLoadingStatus.SETTINGS_DATA_LOADED);

                //Check if user settings need to be checked
                //SystemClock.sleep(1000);
//                checkIfLoggedIn()
                //checkIfSettingsUpdated()
                emitter.onNext(DataLoadingStatus.USER_SETTINGS_GOING_TO_BE_LOADED);
                //loadUserSettings()
                //SystemClock.sleep(3000);
                //Settings data loading finished
                emitter.onNext(DataLoadingStatus.EXIT);
                Log.d(TAG, "subscribe: ");
                emitter.onComplete();
            }
        });
    }

    private void doOnError(Throwable throwable) {
        if (throwable instanceof NoInternertConnectionException) {
            mLoadSettingsDataOnNetConnection.set(true);
            mProgressBar.setIndeterminate(true);
            mNoInternetMessage.setVisibility(View.VISIBLE);
        } else if (throwable instanceof DataNotFoundException ||
                throwable instanceof RemoteDbException) {
            mRetryCountForRemoteDBError++;
            mRetryDelayForRemoteDBError +=
                    RETRY_DELAY_FOR_REMOTE_ERROR_INC_VALUE *
                    mRetryCountForRemoteDBError;
            initSettingsDataLoading(mRetryDelayForRemoteDBError);
        } else if (throwable instanceof OnMainThreadException) {
            ExceptionUtils.thowExceptionIfOnMainThred();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mDisposable.clear();
        unregisterBrodcastReceivers();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mHomeNavigator = (HomeNavigator) context;
    }


    private void registerBrodcastReceivers() {
        //noinspection ConstantConditions
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mNetConAvailableBroadcastReceiver,
                NetConnectivityUtility.INSTANCE.getIntentFilterForNetworkAvailableBroadcastReceiver());
    }

    private void unregisterBrodcastReceivers() {
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).unregisterReceiver(mNetConAvailableBroadcastReceiver);
    }

}

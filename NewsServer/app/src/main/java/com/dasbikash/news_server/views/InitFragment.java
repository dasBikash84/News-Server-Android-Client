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

package com.dasbikash.news_server.views;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.utils.ToDoUtils;
import com.dasbikash.news_server.view_models.HomeViewModel;
import com.dasbikash.news_server.views.interfaces.HomeNavigator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class InitFragment extends Fragment {

    private HomeNavigator mHomeNavigator;
    private ProgressBar mProgressBar;

    private HomeViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        mProgressBar = view.findViewById(R.id.data_load_progress);
        mViewModel = (HomeViewModel) ViewModelProviders.of((HomeActivity)getActivity()).get(HomeViewModel.class);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadSettingsData(mHomeNavigator, mProgressBar, mViewModel).execute();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mHomeNavigator = (HomeNavigator) context;
    }

    private static class LoadSettingsData extends AsyncTask<Void,Integer,Void>{

        private HomeNavigator mHomeNavigator;
        private ProgressBar mProgressBar;
        private int mProgressValue=0;
        private HomeViewModel mViewModel;

        public LoadSettingsData(HomeNavigator homeNavigator, ProgressBar progressBar, HomeViewModel viewModel) {
            mHomeNavigator = homeNavigator;
            mProgressBar = progressBar;
            mViewModel = viewModel;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setIndeterminate(true);
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mHomeNavigator.loadHomeFragment();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int value = values[0] + mProgressBar.getProgress();
            if (mProgressBar.isIndeterminate()) mProgressBar.setIndeterminate(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mProgressBar.setProgress(value,true);
            } else {
                mProgressBar.setProgress(value);
            }

        }

        @Override
        protected Void doInBackground(Void... voids) {

            //Tasks to be done during init app settings loading
            // 1. Check if settings data loaded or not. If not jump to 3
            // 2. Check if settings have been updated or not. If not jump to 4.
            if (!isSettingsDataLoaded() || isGlobalSettingsUpdated()){
                loadSettingsData();
            }
            //4. Check if signed in or not. If not exit.
            //5. Check if personal settings have been modified or not. If not exit.
            //6. Load personal settings.

            return null;

        }

        private void loadSettingsData() {

            //3. Load settings data
            //   => Load country and language data.
            //   => Load Newspaper info.
            //   => Load page info.
            //   => Load page group info.
            ToDoUtils.workToDo("Load settings data from remote DB");
        }

        private boolean isGlobalSettingsUpdated() {
            //ToDoUtils.workToDo("Check if NewsPaper structure data has been updated or not");
            return mViewModel.isGlobalSettingsUpdated();
        }

        private boolean isSettingsDataLoaded() {
            return mViewModel.isSettingsDataLoaded();
        }
    }

}

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

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.this_utility.SettingsUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

public class SettingsBasePageFragment extends Fragment {

    private static final String CLEAR_CACHE_PROMPT =
            "Data & images are cached to mininize data network usage during" +
                    " article revisit. Selecting \"Yes\" will erase that all. Do you" +
                    " want to proceed?";
    private static final String RESTORE_FACTORY_SETTINGS_PROMPT =
            "Selecting \"Yes\" will revert all settings to initial app settings.Saved articles " +
            "data will remain intact though.Do you want to proceed?";
    private static final String CLEAR_CACHE_COMPLETE_MESSAGE =
            "Cached data cleared.";
    public static final String FACTORY_SETTINGS_RESTORED_MESSAGE = "Settings Restored.";
    private static final CharSequence FAILURE_TOAST_MESSAGE = "Error occurred, please retry.";

    private Switch mImageDownloadOnDNCheckBox;
    private Switch mNavDrawerEnablerViewCheckBox;
    //private Button mClearCacheDataView;
    private Button mCustomizeHomePageView;
    private Button mCustomizeNewsCategoriesView;
    //private Button mCustomizePushNotificationView;
    private Button mResetToFactorySettingdView;
    private Button mDeleteSavedArticlesView;
    private Button mCustomizeNewspaperDisplayView;
    private LinearLayout mProgressBarHolder;
    private ProgressBar mProgressBar;

    private SettingsActivity mParentActivity;

    @Override
    public void onAttach(Context context) {
        if (context instanceof SettingsActivity) {
            super.onAttach(context);
            mParentActivity = (SettingsActivity) context;
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_base,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViewItems(view);

        setEventListners();

        refreshDisplay();
    }

    void refreshDisplay() {
        mImageDownloadOnDNCheckBox.setChecked(SettingsUtility.getDNImageDownloadSetting());
        mNavDrawerEnablerViewCheckBox.setChecked(SettingsUtility.getNavMenuDisplaySetting());
    }

    void setEventListners() {
        /*mClearCacheDataView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(CLEAR_CACHE_PROMPT)
                        .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                            new ClearCacheData().execute();
                        })
                        .setNegativeButton("No", null)
                        .create()
                        .show();
            }
        });*/

        mDeleteSavedArticlesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsUtility.deleteSavedArticlesAction(getActivity());
            }
        });

        mResetToFactorySettingdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(RESTORE_FACTORY_SETTINGS_PROMPT)
                        .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                            new RestoreFactorySettings().execute();
                        })
                        .setNegativeButton("No", null)
                        .create()
                        .show();
            }
        });

        mImageDownloadOnDNCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (!SettingsUtility.enableImageDownloadOnDN()){
                        mImageDownloadOnDNCheckBox.setChecked(false);
                    }
                } else {
                    if (!SettingsUtility.disableImageDownloadOnDN()){
                        mImageDownloadOnDNCheckBox.setChecked(true);
                    }
                }

            }
        });

        mNavDrawerEnablerViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (!SettingsUtility.enableNavigationMenuDisplay()){
                        mNavDrawerEnablerViewCheckBox.setChecked(false);
                    }
                } else {
                    if (!SettingsUtility.disableNavigationMenuDisplay()){
                        mNavDrawerEnablerViewCheckBox.setChecked(true);
                    }
                }
            }
        });

        mCustomizeNewspaperDisplayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mParentActivity.repllaceFragmentOnFrameAddingToBackStack(new CustomizeNewspaperMenuFragment());
            }
        });

        mCustomizeHomePageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mParentActivity.repllaceFragmentOnFrameAddingToBackStack(new CustomizeHomePagesFragment());
            }
        });

        mCustomizeNewsCategoriesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customFeatureGroupModifyAction();
            }
        });
    }

    void findViewItems(@NonNull View view) {
        mImageDownloadOnDNCheckBox = view.findViewById(R.id.settings_item_image_download_on_data_network);
        mNavDrawerEnablerViewCheckBox = view.findViewById(R.id.settings_item_show_drawer_menu_on_start);
        //mClearCacheDataView = view.findViewById(R.id.settings_item_clear_cached_data);
        mCustomizeNewspaperDisplayView = view.findViewById(R.id.settings_item_customize_newspaper_views);
        mCustomizeHomePageView = view.findViewById(R.id.settings_item_customize_home_pages);
        mCustomizeNewsCategoriesView = view.findViewById(R.id.settings_item_customize_feature_groups);
        //mCustomizePushNotificationView = view.findViewById(R.id.settings_item_customize_notifications);
        mResetToFactorySettingdView = view.findViewById(R.id.settings_item_reset_to_factory);
        mDeleteSavedArticlesView = view.findViewById(R.id.settings_item_deleted_saved_articles);
        mProgressBarHolder = view.findViewById(R.id.progress_bar_holder);
        mProgressBar = view.findViewById(R.id.progress_bar);
    }

    private void customFeatureGroupModifyAction() {
        mParentActivity.repllaceFragmentOnFrameAddingToBackStack(
                new CustomizeCustomFeatureGroupFragment()
        );
    }

    private void disableViewItems(){
        mImageDownloadOnDNCheckBox.setEnabled(false);
        mNavDrawerEnablerViewCheckBox.setEnabled(false);
        mResetToFactorySettingdView.setEnabled(false);
        mDeleteSavedArticlesView.setEnabled(false);
        //mClearCacheDataView.setEnabled(false);
        mCustomizeNewspaperDisplayView.setEnabled(false);
        mCustomizeHomePageView.setEnabled(false);
        mCustomizeNewsCategoriesView.setEnabled(false);
        //mCustomizePushNotificationView.setEnabled(false);
    }

    private void enableViewItems(){
        mImageDownloadOnDNCheckBox.setEnabled(true);
        mNavDrawerEnablerViewCheckBox.setEnabled(true);
        mResetToFactorySettingdView.setEnabled(true);
        mDeleteSavedArticlesView.setEnabled(true);
        //mClearCacheDataView.setEnabled(true);
        mCustomizeNewspaperDisplayView.setEnabled(true);
        mCustomizeHomePageView.setEnabled(true);
        mCustomizeNewsCategoriesView.setEnabled(true);
        //mCustomizePushNotificationView.setEnabled(true);
    }

    private void showProgressBar(){
        mProgressBarHolder.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBarHolder.bringToFront();
    }

    private void hideProgressBar(){
        mProgressBarHolder.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
    }

    /*private class ClearCacheData extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            jobsBeforeCriticalTaskInitiation();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SettingsUtility.stopDownLoaders();
            DiskCleaner.clearCachedDataAction();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            jobsAfterCriticalTaskCompletion();
            Toast.makeText(getActivity(),
                    CLEAR_CACHE_COMPLETE_MESSAGE,Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
        }
    }*/

    private class RestoreFactorySettings extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected void onPreExecute() {
            jobsBeforeCriticalTaskInitiation();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            SettingsUtility.stopDownLoaders();
            return SettingsUtility.restoreFactoySettings();
            //return null;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            jobsAfterCriticalTaskCompletion();
            if (aVoid) {
                DisplayUtility.showShortToast(FACTORY_SETTINGS_RESTORED_MESSAGE);
            } else {
                DisplayUtility.showShortToast(FAILURE_TOAST_MESSAGE);
            }
            refreshDisplay();
            super.onPostExecute(aVoid);
        }
    }

    private void jobsAfterCriticalTaskCompletion() {
        hideProgressBar();
        enableViewItems();
        mParentActivity.mWorkInProcess = false;
    }

    private void jobsBeforeCriticalTaskInitiation() {
        disableViewItems();
        showProgressBar();
        mParentActivity.mWorkInProcess = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((SettingsActivity)getActivity()).getSupportActionBar().setTitle(
                getResources().getString(R.string.settings_activity_lebel)
        );
    }
}

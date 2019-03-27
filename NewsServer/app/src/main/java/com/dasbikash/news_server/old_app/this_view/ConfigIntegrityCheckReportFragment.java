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


import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.this_utility.SettingsUtility;
import com.dasbikash.news_server.old_app.this_data.config_check_summary_entry.ConfigCheckSummaryEntry;
import com.dasbikash.news_server.old_app.this_data.config_check_summary_entry.ConfigCheckSummaryEntryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_data.parent_feature_check_entry.ParentFeatureCheckEntry;
import com.dasbikash.news_server.old_app.this_data.parent_feature_check_entry.ParentFeatureCheckEntryHelper;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class ConfigIntegrityCheckReportFragment extends Fragment {

    private static final String TAG = "StackTrace";
    private static final String CLEAR_PARENT_FEATURE_CHECK_DATA_PROMPT = "Clear parent feature check data?";
    private static final String DATA_CLEARED_MESSAGE = "Data cleared.";
    private static final String DATA_CLEAR_FAILURE_MESSAGE = "Data clearing Failed!";
    private static final String CLEAR_REPORT_SUMMARY_DATA_PROMPT = "Clear report summary data?";

    private TextView mSummaryReportSelector;
    private TextView mParentFeatureCheckReportSelector;
    private RecyclerView mSummaryReportView;
    private RecyclerView mParentFeatureCheckReportView;

    private ArrayList<ConfigCheckSummaryEntry>
            mConfigCheckSummaryEntries = new ArrayList<>();

    private ArrayList<ParentFeatureCheckEntry>
            mParentFeatureCheckEntries = new ArrayList<>();

    private SimpleDateFormat mSimpleDateFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config_integrity_report_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSummaryReportSelector = view.findViewById(R.id.summary_report_selector);
        mParentFeatureCheckReportSelector = view.findViewById(R.id.parent_feature_check_report_selector);
        mSummaryReportView = view.findViewById(R.id.summary_report_view);
        mParentFeatureCheckReportView = view.findViewById(R.id.parent_feature_check_report_view);

        mConfigCheckSummaryEntries = ConfigCheckSummaryEntryHelper.getConfigCheckSummaryEntries();
        mParentFeatureCheckEntries = ParentFeatureCheckEntryHelper.getParentFeatureCheckEntries();

        mSummaryReportSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                summaryReportSelectorAction();
            }
        });

        mParentFeatureCheckReportSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentFeatureCheckReportSelectorAction();
            }
        });

        mSimpleDateFormat = new SimpleDateFormat(getString(R.string.config_check_report_display_date_format));
        mSimpleDateFormat.setTimeZone(TimeZone.getDefault());

        mSummaryReportView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSummaryReportView.setAdapter(new SummaryReportListAdapter());

        mParentFeatureCheckReportView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mParentFeatureCheckReportView.setAdapter(new ParentFeatureCheckReportListAdapter());

        summaryReportSelectorAction();
    }

    private void parentFeatureCheckReportSelectorAction() {
        mParentFeatureCheckReportSelector.setBackgroundColor(Color.WHITE);
        mSummaryReportSelector.setBackgroundColor(Color.parseColor(
                getString(R.string.light_button_background)
        ));
        mSummaryReportSelector.setTypeface(null, Typeface.NORMAL);
        mParentFeatureCheckReportSelector.setTypeface(null, Typeface.BOLD);
        mSummaryReportView.setVisibility(View.GONE);
        mParentFeatureCheckReportView.setVisibility(View.VISIBLE);
    }

    private void summaryReportSelectorAction() {
        mSummaryReportSelector.setBackgroundColor(Color.WHITE);
        mParentFeatureCheckReportSelector.setBackgroundColor(Color.parseColor(
                getString(R.string.light_button_background)
        ));
        mSummaryReportSelector.setTypeface(null, Typeface.BOLD);
        mParentFeatureCheckReportSelector.setTypeface(null, Typeface.NORMAL);
        mSummaryReportView.setVisibility(View.VISIBLE);
        mParentFeatureCheckReportView.setVisibility(View.GONE);
    }


    private class SummaryReportListAdapter extends RecyclerView.Adapter<SummaryReportEntryHolder>{

        @NonNull
        @Override
        public SummaryReportEntryHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new SummaryReportEntryHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(@NonNull SummaryReportEntryHolder summaryReportEntryHolder, int i) {
            summaryReportEntryHolder.bind(mConfigCheckSummaryEntries.get(i));
        }

        @Override
        public int getItemCount() {
            return mConfigCheckSummaryEntries.size();
        }
    }

    private class SummaryReportEntryHolder extends RecyclerView.ViewHolder{

        private ConfigCheckSummaryEntry mConfigCheckSummaryEntry;
        private TextView mSummarySerialNum;
        private TextView mSummaryEntryTime;
        private TextView mSummaryReportEntry;


        public SummaryReportEntryHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_config_check_summary_entry_view,parent,false));
            mSummarySerialNum = itemView.findViewById(R.id.summary_serial_num);
            mSummaryEntryTime = itemView.findViewById(R.id.summary_entry_time);
            mSummaryReportEntry = itemView.findViewById(R.id.summary_report_entry);
        }

        public void bind(ConfigCheckSummaryEntry configCheckSummaryEntry){
            mConfigCheckSummaryEntry = configCheckSummaryEntry;

            Calendar entryDate = Calendar.getInstance();
            entryDate.setTimeZone(TimeZone.getDefault());
            entryDate.setTimeInMillis(mConfigCheckSummaryEntry.getEntryTimeStamp());

            mSummarySerialNum.setText(""+(mConfigCheckSummaryEntries.indexOf(mConfigCheckSummaryEntry)+1));
            mSummaryEntryTime.setText(mSimpleDateFormat.format(entryDate.getTime()));
            DisplayUtility.displayHtmlText(
                    mSummaryReportEntry,
                    mConfigCheckSummaryEntry.getReportText()
            );
            if (mConfigCheckSummaryEntries.indexOf(mConfigCheckSummaryEntry) == mConfigCheckSummaryEntries.size()-1){
                mSummaryReportView.setPadding(0,0,0,mSummaryReportSelector.getHeight());
            } else {
                mSummaryReportView.setPadding(0,0,0,0);
            }
        }
    }

    private class ParentFeatureCheckReportListAdapter extends RecyclerView.Adapter<ParentFeatureCheckReportEntryHolder>{

        @NonNull
        @Override
        public ParentFeatureCheckReportEntryHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ParentFeatureCheckReportEntryHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ParentFeatureCheckReportEntryHolder parentFeatureCheckReportEntryHolder, int i) {
            parentFeatureCheckReportEntryHolder.bind(mParentFeatureCheckEntries.get(i));
        }

        @Override
        public int getItemCount() {
            return mParentFeatureCheckEntries.size();
        }
    }

    private class ParentFeatureCheckReportEntryHolder extends RecyclerView.ViewHolder{

        private ParentFeatureCheckEntry mParentFeatureCheckEntry;
        private TextView mSerialNum;
        private TextView mEntryTime;
        private TextView mNewspaperName;
        private TextView mParentFeatureName;
        private TextView mCheckedFeatureName;
        private TextView mCheckStatus;


        public ParentFeatureCheckReportEntryHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_parent_check_report_entry_view,parent,false));
            mSerialNum = itemView.findViewById(R.id.serial_num);
            mEntryTime = itemView.findViewById(R.id.entry_time);
            mNewspaperName = itemView.findViewById(R.id.newspaper_name);
            mParentFeatureName = itemView.findViewById(R.id.parent_feature_name);
            mCheckedFeatureName = itemView.findViewById(R.id.checked_feature_name);
            mCheckStatus = itemView.findViewById(R.id.check_result);
        }

        public void bind(ParentFeatureCheckEntry parentFeatureCheckEntry){
            mParentFeatureCheckEntry = parentFeatureCheckEntry;

            Calendar entryDate = Calendar.getInstance();
            entryDate.setTimeZone(TimeZone.getDefault());
            entryDate.setTimeInMillis(mParentFeatureCheckEntry.getEntryTimeStamp());

            Feature parentFeature =
                    FeatureHelper.findFeatureById(mParentFeatureCheckEntry.getParentFeatureId());
            Newspaper newspaper =
                    NewspaperHelper.findNewspaperById(parentFeature.getNewsPaperId());
            Feature checkedFeature = null;
            if (mParentFeatureCheckEntry.getParentFeatureId() ==
                    mParentFeatureCheckEntry.getCheckedFeatureId()){
                checkedFeature = parentFeature;
            } else {
                checkedFeature = FeatureHelper.findFeatureById(
                        mParentFeatureCheckEntry.getCheckedFeatureId()
                );
            }

            mSerialNum.setText(""+(mParentFeatureCheckEntries.indexOf(mParentFeatureCheckEntry)+1));
            mEntryTime.setText(mSimpleDateFormat.format(entryDate.getTime()));
            mNewspaperName.setText(newspaper.getName());
            mParentFeatureName.setText(parentFeature.getTitle());
            mCheckedFeatureName.setText(checkedFeature.getTitle());
            if (mParentFeatureCheckEntry.isCheckStatus()){
                mCheckStatus.setText("S");
                mCheckStatus.setTypeface(null, Typeface.NORMAL);
            }else {
                mCheckStatus.setText("F");
                mCheckStatus.setTypeface(null, Typeface.BOLD);
            }
            if (mParentFeatureCheckEntries.indexOf(mParentFeatureCheckEntry) == mParentFeatureCheckEntries.size()-1){
                mParentFeatureCheckReportView.setPadding(0,0,0,mParentFeatureCheckReportSelector.getHeight());
            } else {
                mParentFeatureCheckReportView.setPadding(0,0,0,0);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_config_check_report_fragment,menu);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.clear_report_summary_data_menu_item:
                clearReportSummaryDataAction();
                return true;
            case R.id.clear_parent_feature_check_data_menu_item:
                clearParentFeatureCheckDataAction();
                return true;
        }
        return false;
    }

    private void clearParentFeatureCheckDataAction() {
        new AlertDialog.Builder(getActivity())
                .setMessage(CLEAR_PARENT_FEATURE_CHECK_DATA_PROMPT)
                .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                    if (SettingsUtility.clearParentFeatureCheckData()) {
                        mParentFeatureCheckEntries =
                                ParentFeatureCheckEntryHelper.getParentFeatureCheckEntries();
                        mParentFeatureCheckReportView.getAdapter().notifyDataSetChanged();
                        DisplayUtility.showShortToast(DATA_CLEARED_MESSAGE);
                    }else {
                        DisplayUtility.showShortToast(DATA_CLEAR_FAILURE_MESSAGE);
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    private void clearReportSummaryDataAction() {
        new AlertDialog.Builder(getActivity())
                .setMessage(CLEAR_REPORT_SUMMARY_DATA_PROMPT)
                .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                    if(SettingsUtility.clearConfigIntegrityCheckReportSummaryData()) {
                        mConfigCheckSummaryEntries =
                                ConfigCheckSummaryEntryHelper.getConfigCheckSummaryEntries();
                        mSummaryReportView.getAdapter().notifyDataSetChanged();
                        DisplayUtility.showShortToast(DATA_CLEARED_MESSAGE);
                    }else {
                        DisplayUtility.showShortToast(DATA_CLEAR_FAILURE_MESSAGE);
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }


}

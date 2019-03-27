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
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import java.util.ArrayList;

public class CustomizeNewspaperMenuFragment extends Fragment {


    //private static final String TAG = "StackTrace";
    private static final String TAG = "CustomizeNewspaperMenuFragment";
    private static final CharSequence ENABLE_ALL_FEATURES_PROMPT = "Enable all newspapers and pages?";
    public static final String ENABLE_SUCCESS_TOAST = "All newspapers and pages enabled.";
    private static final CharSequence ENABLE_FAILURE_TOAST = "Error occurred,please retry.";

    private Button mEnableAllFeaturesButton;
    private Button mCustomizeIndividuallyButton;
    private RecyclerView mNewsPaperListView;

    private ArrayList<Newspaper> mNewspaperList;

    private ArrayList<NewspaperItemHolder> mNewspaperItemHolders = new ArrayList<>();
    private ArrayList<ParentFeatureHolder> mParentFeatureHolders = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customize_newspaper_menu,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEnableAllFeaturesButton = view.findViewById(R.id.enable_all_features_button);
        mCustomizeIndividuallyButton = view.findViewById(R.id.customize_individually_button);
        mNewsPaperListView = view.findViewById(R.id.recycler_view_for_newspaper_menu);
        mNewspaperList = NewspaperHelper.getAllNewspapers();
        mNewsPaperListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mNewsPaperListView.setAdapter(new NewspaperListAdapter());
        mNewsPaperListView.setVisibility(View.VISIBLE);

        mEnableAllFeaturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(ENABLE_ALL_FEATURES_PROMPT)
                        .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                            if(NewspaperHelper.activateAllNewspapers() &&
                                FeatureHelper.activateAllFeatures()) {
                                mNewspaperList = NewspaperHelper.getAllNewspapers();
                                mNewsPaperListView.getAdapter().notifyDataSetChanged();
                                mNewsPaperListView.setVisibility(View.VISIBLE);
                                DisplayUtility.showShortToast(ENABLE_SUCCESS_TOAST);
                            } else {
                                DisplayUtility.showShortToast(ENABLE_FAILURE_TOAST);
                            }
                        })
                        .setNegativeButton("No", null)
                        .create()
                        .show();
            }
        });

        mCustomizeIndividuallyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNewsPaperListView.getVisibility() == View.GONE){
                    mNewsPaperListView.setVisibility(View.VISIBLE);
                } else {
                    mNewsPaperListView.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        ((SettingsActivity)getActivity()).getSupportActionBar().setTitle(R.string.settings_item_customize_newspaper_views_text);
    }

    private class NewspaperListAdapter extends RecyclerView.Adapter<NewspaperItemHolder>{

        @NonNull
        @Override
        public NewspaperItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new NewspaperItemHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull NewspaperItemHolder newspaperItemHolder, int position) {
            newspaperItemHolder.bind(mNewspaperList.get(position));
        }

        @Override
        public int getItemCount() {
            return mNewspaperList.size();
        }
    }

    private class NewspaperItemHolder extends RecyclerView.ViewHolder{

        private LinearLayout mNewsPaperNameBloackView;
        private TextView mNewspaperTitleView;
        private ImageButton mShowButton;
        private ImageButton mHideButton;
        private Switch mSwitchView;
        private Newspaper mNewspaper;
        private Country mCountry;
        private RecyclerView mParentFeatureListView;
        private ArrayList<Feature> mParentFeatures = new ArrayList<>();


        public NewspaperItemHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_item_drop_down_with_switch_for_newspaper, parent, false));

            mNewsPaperNameBloackView = itemView.findViewById(R.id.item_name_block);
            mNewspaperTitleView = mNewsPaperNameBloackView.findViewById(R.id.title_text);
            mShowButton = mNewsPaperNameBloackView.findViewById(R.id.show_decendents_button);
            mHideButton = mNewsPaperNameBloackView.findViewById(R.id.hide_decendents_button);
            mSwitchView = itemView.findViewById(R.id.switch_view);
            mParentFeatureListView = itemView.findViewById(R.id.parent_feature_list_view);

            mNewspaperItemHolders.add(NewspaperItemHolder.this);
            //Log.d(TAG, "NewspaperRVListItemHolder: mNewspaperItemHolders.size(): "+mNewspaperItemHolders.size());

            mSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b){
                        if (!NewspaperHelper.activateNewspaper(mNewspaper)){
                            mSwitchView.setChecked(false);
                        } else {
                            mNewspaper = NewspaperHelper.findNewspaperById(mNewspaper.getId());
                            showSubParentFeatures();
                        }
                    } else {
                        if (!NewspaperHelper.deactivateNewspaper(mNewspaper)){
                            mSwitchView.setChecked(true);
                        } else {
                            mNewspaper = NewspaperHelper.findNewspaperById(mNewspaper.getId());
                            hideSubParentFeatures();
                        }
                    }
                }
            });
            mNewsPaperNameBloackView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    titleClickListner();
                }
            });
            mShowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    titleClickListner();
                }
            });
            mHideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    titleClickListner();
                }
            });
        }

        private void titleClickListner() {
            if(mShowButton.getVisibility() == View.VISIBLE){
                if (mParentFeatures.size()>0) {
                    for (NewspaperItemHolder newspaperItemHolder :
                            mNewspaperItemHolders) {
                        if (newspaperItemHolder != NewspaperItemHolder.this) {
                            newspaperItemHolder.hideSubParentFeatures();
                            //Log.d(TAG, "onClick: Sub parent features of "+newspaperItemHolder.mNewspaper.getName()+" hidden.");
                        }
                    }
                    mShowButton.setVisibility(View.GONE);
                    mHideButton.setVisibility(View.VISIBLE);
                    mParentFeatureListView.setVisibility(View.VISIBLE);
                }
            } else {
                mShowButton.setVisibility(View.VISIBLE);
                mHideButton.setVisibility(View.GONE);
                mParentFeatureListView.setVisibility(View.GONE);
            }
        }

        private void showSubParentFeatures() {
            if (mParentFeatures.size()>0) {
                mShowButton.setVisibility(View.GONE);
                mHideButton.setVisibility(View.VISIBLE);
                mParentFeatureListView.setVisibility(View.VISIBLE);
            }
        }

        private void hideSubParentFeatures() {
            if (mParentFeatures.size()>0) {
                mShowButton.setVisibility(View.VISIBLE);
            } else {
                mShowButton.setVisibility(View.GONE);
            }
            mHideButton.setVisibility(View.GONE);
            mParentFeatureListView.setVisibility(View.GONE);
        }

        void bind(Newspaper newspaper){

            mNewspaper = newspaper;

            if (mNewspaper == null){
                itemView.setVisibility(View.GONE);
                return;
            }
            mCountry = CountryHelper.findCountryByName(mNewspaper.getCountryName());

            if (mCountry != null){
                mNewspaperTitleView.setText(
                        mNewspaper.getName()+
                                " ("+
                                mCountry.getCountryCode()+
                                ")"
                );
            } else {
                mNewspaperTitleView.setText(mNewspaper.getName());
            }

            mSwitchView.setChecked(mNewspaper.isActive());
            mShowButton.setVisibility(View.VISIBLE);
            mHideButton.setVisibility(View.GONE);
            mParentFeatureListView.setVisibility(View.GONE);

            mParentFeatures = FeatureHelper.getAllParentFeaturesForNewspaper(mNewspaper);
            if (mParentFeatures.size()>0){
                mParentFeatureListView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mParentFeatureListView.setAdapter(new ParentFeatureListAdapter(mParentFeatures));
            }
        }

    }

    private class ParentFeatureListAdapter extends RecyclerView.Adapter<ParentFeatureHolder>{

        ArrayList<Feature> mParentFeatureList;

        public ParentFeatureListAdapter(ArrayList<Feature> parentFeatureList) {
            mParentFeatureList = parentFeatureList;
        }

        @NonNull
        @Override
        public ParentFeatureHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ParentFeatureHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull ParentFeatureHolder parentFeatureHolder, int position) {
            parentFeatureHolder.bind(mParentFeatureList.get(position));
        }

        @Override
        public int getItemCount() {
            return mParentFeatureList.size();
        }
    }

    private class ParentFeatureHolder extends RecyclerView.ViewHolder{

        private LinearLayout mParentFeatureNameBloackView;
        private TextView mParentFeatureTitleView;
        private ImageButton mShowButton;
        private ImageButton mHideButton;
        private Feature mFeature;
        //private Country mCountry;
        private RecyclerView mChildFeatureListView;
        private ArrayList<Feature> mChildFeatures = new ArrayList<>();
        private Switch mSwitchView;


        public ParentFeatureHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_item_drop_down_with_switch_for_parent_feature, parent, false));

            mParentFeatureNameBloackView = itemView.findViewById(R.id.item_name_block);
            mParentFeatureTitleView = mParentFeatureNameBloackView.findViewById(R.id.title_text);
            mShowButton = mParentFeatureNameBloackView.findViewById(R.id.show_decendents_button);
            mHideButton = mParentFeatureNameBloackView.findViewById(R.id.hide_decendents_button);
            mChildFeatureListView = itemView.findViewById(R.id.child_feature_list_view);
            mSwitchView=itemView.findViewById(R.id.switch_view);
            mSwitchView.bringToFront();

            mParentFeatureHolders.add(ParentFeatureHolder.this);
            //Log.d(TAG, "ParentFeatureHolder: mParentFeatureHolders.size(): "+mParentFeatureHolders.size());

            mSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b){
                        if (!FeatureHelper.activateFeature(mFeature)){
                            mSwitchView.setChecked(false);
                        } else {
                            mFeature = FeatureHelper.findFeatureById(mFeature.getId());
                            showChildFeatures();
                        }
                    } else {
                        if (!FeatureHelper.deactivateFeature(mFeature)){
                            mSwitchView.setChecked(true);
                        } else {
                            mFeature = FeatureHelper.findFeatureById(mFeature.getId());
                            hideChildFeatures();
                        }
                    }
                }
            });

            mParentFeatureNameBloackView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    titleClickListner();
                }
            });

            mShowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    titleClickListner();
                }
            });

            mHideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    titleClickListner();
                }
            });

        }

        private void titleClickListner() {
            if (mChildFeatures.size()>0) {

                if (mShowButton.getVisibility() == View.VISIBLE) {
                        mShowButton.setVisibility(View.GONE);
                        mHideButton.setVisibility(View.VISIBLE);
                        mChildFeatureListView.setVisibility(View.VISIBLE);

                    for (ParentFeatureHolder parentFeatureHolder :
                            mParentFeatureHolders) {
                        if (parentFeatureHolder != ParentFeatureHolder.this) {
                            parentFeatureHolder.hideChildFeatures();
                            //Log.d(TAG, "onClick: Child features of "+parentFeatureHolder.mFeature.getTitle()+" hidden.");
                        }
                    }
                } else {
                        mShowButton.setVisibility(View.VISIBLE);
                        mHideButton.setVisibility(View.GONE);
                        mChildFeatureListView.setVisibility(View.GONE);
                }
            }
        }

        private void hideChildFeatures() {
            if (mChildFeatures.size()>0) {
                mShowButton.setVisibility(View.VISIBLE);
                mHideButton.setVisibility(View.GONE);
                mChildFeatureListView.setVisibility(View.GONE);
            }
        }

        private void showChildFeatures() {
            if (mChildFeatures.size()>0) {
                mShowButton.setVisibility(View.GONE);
                mHideButton.setVisibility(View.VISIBLE);
                mChildFeatureListView.setVisibility(View.VISIBLE);
            }
        }

        void bind(Feature feature){

            mFeature = feature;
            mSwitchView.setChecked(mFeature.isActive());
            mShowButton.setVisibility(View.VISIBLE);
            mHideButton.setVisibility(View.GONE);
            mChildFeatureListView.setVisibility(View.GONE);
            mParentFeatureTitleView.setText(mFeature.getTitle());

            mChildFeatures = FeatureHelper.getAllChildFeatures(mFeature.getId());

            if (mChildFeatures.size()>0){
                mChildFeatureListView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mChildFeatureListView.setAdapter(new ChildFeatureListAdapter(mChildFeatures));
            } else {
                mShowButton.setVisibility(View.GONE);
            }
        }

    }

    private class ChildFeatureListAdapter extends RecyclerView.Adapter<ChildFeatureHolder>{

        ArrayList<Feature> mChildFeatureList;

        public ChildFeatureListAdapter(ArrayList<Feature> childFeatureList) {
            mChildFeatureList = childFeatureList;
        }

        @NonNull
        @Override
        public ChildFeatureHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ChildFeatureHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull ChildFeatureHolder childFeatureHolder, int position) {
            childFeatureHolder.bind(mChildFeatureList.get(position));
        }

        @Override
        public int getItemCount() {
            return mChildFeatureList.size();
        }
    }

    private class ChildFeatureHolder extends RecyclerView.ViewHolder{

        private TextView mChildFeatureTitleView;
        private Feature mFeature;
        private Switch mSwitchView;


        public ChildFeatureHolder(@NonNull LayoutInflater inflater, ViewGroup child) {
            super(inflater.inflate(R.layout.layout_item_drop_down_with_switch_for_child_feature, child, false));

            mChildFeatureTitleView = itemView.findViewById(R.id.title_text);
            mSwitchView=itemView.findViewById(R.id.switch_view);
            mSwitchView.bringToFront();

            mSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b){
                        if (!FeatureHelper.activateFeature(mFeature)){
                            mSwitchView.setChecked(false);
                        } else {
                            mFeature = FeatureHelper.findFeatureById(mFeature.getId());
                        }
                    } else {
                        if (!FeatureHelper.deactivateFeature(mFeature)){
                            mSwitchView.setChecked(true);
                        } else {
                            mFeature = FeatureHelper.findFeatureById(mFeature.getId());
                        }
                    }
                }
            });

        }

        void bind(Feature feature){
            mFeature = feature;
            mSwitchView.setChecked(mFeature.isActive());
            mChildFeatureTitleView.setText(mFeature.getTitle());
        }

    }
}

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
import android.widget.ImageButton;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group_entry.FeatureGroupEntryHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.SerializableItemListDisplayCallbacks;

import java.util.ArrayList;

public class CustomizeNewspaperHomePageFragment extends Fragment {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "CustomizeNewspaperHomePageFragment";
    public static final int CELLING_FOR_CURRENT_FEATURE_LIST_RV = 4;

    private Newspaper mNewspaper;
    private TextView mTextView1;
    private TextView mTextView2;

    //private RecyclerView mCurrentFeatureListView;
    private View mCurrentFeatureListView;
    private RecyclerView mAvailableFeatureListView;

    private ArrayList<Integer> mCurrentFeatureIdList = new ArrayList<>();
    private ArrayList<Feature> mCurrentFeatureList = new ArrayList<>();
    private ArrayList<Feature> mAvailableFeatureList = new ArrayList<>();

    private View mThisFragmentView;

    private SerializableItemListDisplayCallbacks<Feature>
            mCurrentFeatureListDisplayCallbacks;

    private static final String ARG_CURRENT_NEWSPAPER =
            "CustomizeNewspaperHomePageFragment.ARG_CURRENT_NEWSPAPER";

    public static CustomizeNewspaperHomePageFragment newInstance(Newspaper newspaper){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CURRENT_NEWSPAPER, newspaper);
        CustomizeNewspaperHomePageFragment fragment = new CustomizeNewspaperHomePageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customize_newspaper_home_page,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mThisFragmentView = view;

        mNewspaper = (Newspaper) getArguments().getSerializable(ARG_CURRENT_NEWSPAPER);

        if (mNewspaper == null) getActivity().finish();


        ((SettingsActivity)getActivity()).getSupportActionBar().setTitle(
                "Customize \""+mNewspaper.getName()+"\" "+NewspaperHelper.getNewspaperHomePageTitle(mNewspaper)
        );

        mTextView1 = view.findViewById(R.id.customize_newspaper_home_page_text1);
        mTextView2 = view.findViewById(R.id.customize_newspaper_home_page_text2);
        //mCurrentFeatureListView = view.findViewById(R.id.current_Feature_list_rv_np_home);
        mAvailableFeatureListView = view.findViewById(R.id.available_feature_list);

        mTextView1.setText("Currently added pages on \""+
                            mNewspaper.getName()+"\" "+NewspaperHelper.getNewspaperHomePageTitle(mNewspaper)+":");
        mTextView2.setText("Select page from below list to add:");

        mCurrentFeatureListDisplayCallbacks =
                new SerializableItemListDisplayCallbacks<Feature>() {
                    @Override
                    public int getListViewId() {
                        return R.id.current_Feature_list_np_home;
                    }

                    @Override
                    public int getRecyclerViewId() {
                        return R.id.current_Feature_list_rv_np_home;
                    }

                    @Override
                    public int getIdForItemDisplay() {
                        return R.layout.layout_current_feature_holder;
                    }

                    @Override
                    public int getIdOfItemTextView() {
                        return R.id.feture_title_text;
                    }

                    @Override
                    public int getIdOfItemHorSeparator() {
                        return R.id.seperator;
                    }

                    @Override
                    public int getIdOfItemImageButton() {
                        return R.id.remove_feature_button;
                    }

                    @Override
                    public int getRVDisplayThresholdCount() {
                        return CELLING_FOR_CURRENT_FEATURE_LIST_RV;
                    }

                    @Override
                    public ArrayList<Feature> getSerializableItemListForDisplay() {
                        return mCurrentFeatureList;
                    }

                    @Override
                    public String getTextStringForTextView(Feature feature) {

                        Feature parentFeature = FeatureHelper.findFeatureById(feature.getParentFeatureId());

                        StringBuilder featureFullTitle = new StringBuilder(feature.getTitle());

                        if (parentFeature!=null){
                            featureFullTitle.append(" | "+parentFeature.getTitle());
                        }

                        return featureFullTitle.toString();
                    }

                    @Override
                    public void callBackForTextItemClickAction(Feature feature) {
                    }

                    @Override
                    public void callBackForImageButtonItemClickAction(Feature feature) {

                        new AlertDialog.Builder(getActivity())
                                .setMessage(getPromptMessageForFeatureRemoval(feature))
                                .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                                    removeFeatureFromHomePageAction(feature);
                                })
                                .setNegativeButton("No", null)
                                .create()
                                .show();

                    }
                };

        mTextView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentFeatureListView.getVisibility() == View.VISIBLE){
                    mCurrentFeatureListView.setVisibility(View.GONE);
                } else {
                    mCurrentFeatureListView.setVisibility(View.VISIBLE);
                }
            }
        });

        mAvailableFeatureListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        refreshDisplay();
    }

    void refreshDisplay() {
        refreshCurrentFeatureIdList();
        refreshAvailableFeatureList();
        mCurrentFeatureListView = DisplayUtility.inflateSerializableItemList
                                        (mThisFragmentView,mCurrentFeatureListDisplayCallbacks);
        mAvailableFeatureListView.setAdapter(new AvailableFeatureListAdapter());
    }

    private void refreshCurrentFeatureIdList() {
        mCurrentFeatureIdList = FeatureGroupHelper.getFeatureIdsForNewspaperHomeFeatureGroup(mNewspaper);
        mCurrentFeatureList.clear();
        for (Integer featureId :
                mCurrentFeatureIdList) {
            mCurrentFeatureList.add(FeatureHelper.findFeatureById(featureId));
        }
    }

    private void refreshAvailableFeatureList() {
        ArrayList<Feature> newspaperFeatureList = FeatureHelper.getAllActiveFeaturesForNewspaper(mNewspaper);
        mAvailableFeatureList.clear();
        for (Feature feature :
                newspaperFeatureList) {
            if (feature!=null &&
                    !mCurrentFeatureIdList.contains(feature.getId())
                    && feature.getLinkFormat()!=null){
                mAvailableFeatureList.add(feature);
            }
        }
    }

    private CharSequence getPromptMessageForFeatureRemoval(Feature feature) {
        return "Remove \""+feature.getTitle()+"\""+
                " from "+"\""+mNewspaper.getName()+"\" "+
                NewspaperHelper.getNewspaperHomePageTitle(mNewspaper)+"?";
    }

    private void removeFeatureFromHomePageAction(Feature feature) {
        if (FeatureGroupEntryHelper.
                removeFeatureFromNewspaperHomePage(feature,mNewspaper)){
            refreshDisplay();
            DisplayUtility.showShortToast(getMessageForSuccessfulToast(feature));
        } else {
            DisplayUtility.showShortToast(getMessageForFailureToast());
        }
    }

    private String  getMessageForFailureToast() {
        return "Error occured! Please retry.";
    }

    private String getMessageForSuccessfulToast(Feature feature) {
        return "\""+feature.getTitle()+"\" removed from \""+
                mNewspaper.getName()+"\" "+
                NewspaperHelper.getNewspaperHomePageTitle(mNewspaper);
    }

    private class AvailableFeatureListAdapter extends RecyclerView.Adapter<AvailableFeatureHolder>{
        @NonNull
        @Override
        public AvailableFeatureHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new AvailableFeatureHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull AvailableFeatureHolder availableFeatureHolder, int position) {
            availableFeatureHolder.bind(mAvailableFeatureList.get(position));
        }

        @Override
        public int getItemCount() {
            return mAvailableFeatureList.size();
        }
    }

    private class AvailableFeatureHolder extends RecyclerView.ViewHolder{

        private TextView mFeatureTitleView;
        private ImageButton mRemoveFeatureView;
        private Feature mFeature;

        public AvailableFeatureHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_current_feature_holder, parent, false));
            mFeatureTitleView = itemView.findViewById(R.id.feture_title_text);
            mRemoveFeatureView = itemView.findViewById(R.id.remove_feature_button);
            mRemoveFeatureView.setVisibility(View.GONE);
            mFeatureTitleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(getPromptMessageForFeatureAddition())
                            .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                                addFeatureToHomePageAction();
                            })
                            .setNegativeButton("No", null)
                            .create()
                            .show();
                }
            });
        }
        private CharSequence getPromptMessageForFeatureAddition() {
            return "Add \""+mFeature.getTitle()+"\""+
                    " on "+"\""+mNewspaper.getName()+"\" "+
                    NewspaperHelper.getNewspaperHomePageTitle(mNewspaper)+"?";
        }
        private void addFeatureToHomePageAction() {
            if (FeatureGroupEntryHelper.
                    addFeatureToNewspaperHome(mFeature,mNewspaper)){
                refreshDisplay();
                DisplayUtility.showShortToast(getMessageForSuccessfulToast());
            } else {
                DisplayUtility.showShortToast(getMessageForFailureToast());
            }
        }
        private String  getMessageForFailureToast() {
            return "Error occured! Please retry.";
        }
        private String getMessageForSuccessfulToast() {
            return "\""+mFeature.getTitle()+"\" added to \""+
                    mNewspaper.getName()+"\" "+
                    NewspaperHelper.getNewspaperHomePageTitle(mNewspaper);
        }
        void bind(Feature feature){
            mFeature = feature;
            Feature parentFeature = FeatureHelper.findFeatureById(mFeature.getParentFeatureId());
            if (parentFeature == null) {
                mFeatureTitleView.setText(mFeature.getTitle());
            } else {
                mFeatureTitleView.setText(
                        mFeature.getTitle()+" | "+parentFeature.getTitle()
                );
            }
        }
    }
}

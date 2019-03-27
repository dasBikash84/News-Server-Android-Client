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
import android.os.Handler;
import android.os.Looper;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroup;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group_entry.FeatureGroupEntryHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.SerializableItemListDisplayCallbacks;

import java.util.ArrayList;

public class CustomizeNonNewspaperFeatureGroupFragment extends Fragment {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "CustomizeNonNewspaper";
    public static final int CELLING_FOR_CURRENT_FEATURE_LIST_RV = 4;

    private final ArrayList<NewspaperHolder> mNewspaperHolderList = new ArrayList<>();

    private TextView mTextView1;
    private TextView mTextView2;

    private FeatureGroup mFeatureGroup;
    private ArrayList<Newspaper> mNewspaperList = new ArrayList<>();
    private ArrayList<Integer> mCurrentFeatureIdList = new ArrayList<>();
    private ArrayList<Feature> mCurrentFeatureList = new ArrayList<>();

    //private RecyclerView mCurrentFeatureListView;
    private View mCurrentFeatureListView;
    private RecyclerView mNewspaperListView;

    private View mThisFragmentView;

    private SerializableItemListDisplayCallbacks<Feature> mCurrentFeatureListDisplayCallbacks;

    private static final String ARG_CURRENT_FEATUREGROUP =
            "CustomizeNewspaperHomePageFragment.ARG_CURRENT_FEATUREGROUP";

    public static CustomizeNonNewspaperFeatureGroupFragment newInstance(FeatureGroup featureGroup){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CURRENT_FEATUREGROUP, featureGroup);
        CustomizeNonNewspaperFeatureGroupFragment fragment = new CustomizeNonNewspaperFeatureGroupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customize_non_newspaper_feature_group,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mThisFragmentView = view;

        mFeatureGroup = (FeatureGroup) getArguments().getSerializable(ARG_CURRENT_FEATUREGROUP);


        if (mFeatureGroup == null ||
                !(mFeatureGroup.getCategoryIdentifier() == NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP
                || mFeatureGroup.getCategoryIdentifier() == NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_HOMEPAGE)){
            getActivity().finish();
        }
        mNewspaperList = NewspaperHelper.getAllActiveNewspapers();
        if (mNewspaperList == null ||
                mNewspaperList.size() == 0){
            getActivity().finish();
        }
        //Log.d(TAG, "onViewCreated: mFeatureGroup.getCategoryIdentifier(): "+mFeatureGroup.getCategoryIdentifier());

        mTextView1 = view.findViewById(R.id.customize_non_newspaper_feature_group_text1);
        mTextView2 = view.findViewById(R.id.customize_non_newspaper_feature_group_text2);

        mNewspaperListView = view.findViewById(R.id.newspaper_list);

        mTextView1.setText("Currently added pages on \""+ mFeatureGroup.getTitle()+"\":");
        mTextView2.setText("Select page from below list to add:");

        mCurrentFeatureListDisplayCallbacks =
                new SerializableItemListDisplayCallbacks<Feature>() {
                    @Override
                    public int getListViewId() {
                        return R.id.current_Feature_list_nnp_home;
                    }

                    @Override
                    public int getRecyclerViewId() {
                        return R.id.current_Feature_list_rv_nnp_home;
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

                        Newspaper newspaper = NewspaperHelper.findNewspaperById(feature.getNewsPaperId());
                        Feature parentFeature = FeatureHelper.findFeatureById(feature.getParentFeatureId());

                        StringBuilder featureFullTitle = new StringBuilder(feature.getTitle());

                        if (parentFeature!=null){
                            featureFullTitle.append(" | "+parentFeature.getTitle());
                        }

                        featureFullTitle.append(" | "+newspaper.getName());

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
                                    removeFeatureFromFeatureGroupAction(feature);
                                })
                                .setNegativeButton("No", null)
                                .create()
                                .show();

                    }
                };


        mTextView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentFeatureListView!=null) {
                    if (mCurrentFeatureListView.getVisibility() == View.VISIBLE) {
                        mCurrentFeatureListView.setVisibility(View.GONE);
                    } else {
                        mCurrentFeatureListView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        refreshCurrentFeatureListDisplay();

        mNewspaperListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mNewspaperListView.setAdapter(new NewspaperListAdapter());

    }
    private CharSequence getPromptMessageForFeatureRemoval(Feature feature) {
        return "Remove \""+feature.getTitle()+"\""+
                " from "+"\""+mFeatureGroup.getTitle()+"\"?";
    }
    private void removeFeatureFromFeatureGroupAction(Feature feature) {
        if (FeatureGroupEntryHelper.
                removeFeatureFromFeatureGroup(feature,mFeatureGroup)){

            refreshCurrentFeatureListDisplay();

            Newspaper newspaper = NewspaperHelper.findNewspaperById(feature.getNewsPaperId());
            refreshFeatureListForNewspaper(newspaper);

            DisplayUtility.showShortToast(getMessageForSuccessToast(feature));
        } else {
            DisplayUtility.showShortToast(getMessageForFailureToast()/*getMessageForSuccessToast(feature)*/);
        }
    }

    private String  getMessageForFailureToast() {
        return "Error occured! Please retry.";
    }

    private String getMessageForSuccessToast(Feature feature) {

        Feature parentFeature = FeatureHelper.findFeatureById(feature.getParentFeatureId());
        Newspaper newspaper = NewspaperHelper.findNewspaperById(feature.getNewsPaperId());
        StringBuilder promptSB = new StringBuilder("\""+feature.getTitle());
        if (parentFeature!=null){
            promptSB.append(" | "+parentFeature.getTitle());
        }
        if (newspaper!=null){
            promptSB.append(" | "+newspaper.getName());
        }
        promptSB.append("\" removed from \""+mFeatureGroup.getTitle()+"\"");

        return promptSB.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((SettingsActivity)getActivity()).getSupportActionBar().setTitle(
                "Customize \""+mFeatureGroup.getTitle()+"\""
        );
    }

    private void refreshCurrentFeatureIdList() {
        mCurrentFeatureIdList = FeatureGroupHelper.getFeatureIdsForFeatureGroup(mFeatureGroup);
        mCurrentFeatureList.clear();
        for (Integer featureId :
                mCurrentFeatureIdList) {
            mCurrentFeatureList.add(FeatureHelper.findFeatureById(featureId));
        }
    }

    private void refreshCurrentFeatureListDisplay() {
        refreshCurrentFeatureIdList();
        mCurrentFeatureListView = DisplayUtility.inflateSerializableItemList(mThisFragmentView,mCurrentFeatureListDisplayCallbacks);
        mCurrentFeatureListView.setVisibility(View.VISIBLE);
    }

    private void refreshFeatureListForNewspaper(Newspaper newspaper) {
        for (NewspaperHolder newspaperHolder :
                mNewspaperHolderList) {
            if (newspaperHolder.mNewspaper.getId() ==
                    newspaper.getId()){
                newspaperHolder.refreshNewspaperList();
                break;
            }
        }
    }

    private class NewspaperListAdapter extends RecyclerView.Adapter<NewspaperHolder>{

        @NonNull
        @Override
        public NewspaperHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new NewspaperHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull NewspaperHolder newspaperHolder, int position) {
            newspaperHolder.bind(mNewspaperList.get(position));
        }

        @Override
        public int getItemCount() {
            return mNewspaperList.size();
        }
    }

    private class NewspaperHolder extends RecyclerView.ViewHolder{

        private TextView mNewsPaperTitleView;
        private Newspaper mNewspaper;
        private RecyclerView mFeatureListView;
        private ArrayList<Feature> mFeatureList = new ArrayList<>();
        private LinearLayout mTitleHolder;

        private ImageButton mShowPages;
        private ImageButton mHidePages;

        public NewspaperHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_current_newspaper_holder, parent, false));
            mNewsPaperTitleView = itemView.findViewById(R.id.newspaper_title_text);
            mTitleHolder = itemView.findViewById(R.id.title_holder);
            mFeatureListView = itemView.findViewById(R.id.feature_list_view);
            mShowPages = itemView.findViewById(R.id.show_pages_button);
            mHidePages = itemView.findViewById(R.id.hide_pages_button);
            mNewspaperHolderList.add(NewspaperHolder.this);

            mTitleHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickAction();
                }
            });

            mShowPages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickAction();
                }
            });

            mHidePages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickAction();
                }
            });
        }

        void clickAction() {
            for (NewspaperHolder newspaperHolder:
                    mNewspaperHolderList){
                if (newspaperHolder == NewspaperHolder.this){
                    if(mFeatureListView.getVisibility() == View.VISIBLE){
                        mFeatureListView.setVisibility(View.GONE);
                        mShowPages.setVisibility(View.VISIBLE);
                        mHidePages.setVisibility(View.GONE);
                    } else {
                        mFeatureListView.setVisibility(View.VISIBLE);
                        mShowPages.setVisibility(View.GONE);
                        mHidePages.setVisibility(View.VISIBLE);
                    }
                } else {
                    newspaperHolder.mFeatureListView.setVisibility(View.GONE);
                    newspaperHolder.mShowPages.setVisibility(View.VISIBLE);
                    newspaperHolder.mHidePages.setVisibility(View.GONE);
                }
            }
        }

        void bind(Newspaper newspaper){

            if (newspaper == null){
                getActivity().finish();
            }
            mNewspaper = newspaper;

            Country country = CountryHelper.findCountryByName(newspaper.getCountryName());

            if (country == null){
                getActivity().finish();
            }

            mNewsPaperTitleView.
                    setText(mNewspaper.getName()+" ("+country.getCountryCode()+")");

            refreshChildFeatureList();

            mFeatureListView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mFeatureListView.setAdapter(new NewspaperFeatureListAdapter(mFeatureList));

            mFeatureListView.setVisibility(View.GONE);
            mShowPages.setVisibility(View.VISIBLE);
            mHidePages.setVisibility(View.GONE);
        }

        void refreshChildFeatureList(){
            ArrayList<Feature> featureArrayList =
                    FeatureHelper.getAllActiveFeaturesForNewspaper(mNewspaper);
            mFeatureList.clear();
            if (featureArrayList == null){
                //itemView.setVisibility(View.GONE);
                return;
            }
            //ArrayList<Feature> tempFeatureArrayList = new ArrayList<>();
            for (Feature feature :
                    featureArrayList) {
                if (feature.getLinkFormat() != null
                        && !mCurrentFeatureIdList.contains(feature.getId())){
                    mFeatureList.add(feature);
                }
            }
        }

        public void refreshNewspaperList() {

            refreshChildFeatureList();
            mFeatureListView.getAdapter().notifyDataSetChanged();

            for (NewspaperHolder newspaperHolder:
                    mNewspaperHolderList){
                if (newspaperHolder == NewspaperHolder.this){
                    mFeatureListView.setVisibility(View.VISIBLE);
                } else {
                    newspaperHolder.mFeatureListView.setVisibility(View.GONE);
                }
            }
        }
    }

    private class NewspaperFeatureListAdapter extends RecyclerView.Adapter<NewspaperFeatureListHolder>{

        private ArrayList<Feature> mFeatureList = new ArrayList<>();

        public NewspaperFeatureListAdapter(ArrayList<Feature> featureList) {
            mFeatureList = featureList;
        }

        @NonNull
        @Override
        public NewspaperFeatureListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new NewspaperFeatureListHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull NewspaperFeatureListHolder newspaperFeatureListHolder, int i) {
            newspaperFeatureListHolder.bind(mFeatureList.get(i));
        }

        @Override
        public int getItemCount() {
            return mFeatureList.size();
        }
    }

    private class NewspaperFeatureListHolder extends RecyclerView.ViewHolder{

        private Feature mFeature;
        private Button mFeatureTitleView;

        public NewspaperFeatureListHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_menu_button, parent, false));
            mFeatureTitleView = itemView.findViewById(R.id.menu_button_item);
            mFeatureTitleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(getPromptMessageForFeatureAddition())
                            .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                                addFeatureToFeatureGroupAction();
                            })
                            .setNegativeButton("No", null)
                            .create()
                            .show();

                }
            });
        }

        private void addFeatureToFeatureGroupAction() {
            //Log.d(TAG, "addFeatureToFeatureGroupAction: for: "+mFeature.getTitle());
            if (FeatureGroupEntryHelper.addFeatureToFeatureGroup(mFeature,mFeatureGroup)){
                refreshCurrentFeatureListDisplay();
                Newspaper newspaper = NewspaperHelper.findNewspaperById(mFeature.getNewsPaperId());
                new Handler(Looper.getMainLooper()).
                        postAtTime(new Runnable() {
                            @Override
                            public void run() {
                                refreshFeatureListForNewspaper(newspaper);
                            }
                        },500);
                DisplayUtility.showShortToast(getMessageForSuccessfulToast());
        } else {

                DisplayUtility.showShortToast(getMessageForFailureToast());
            }
        }

        public void bind(Feature feature) {
            if (feature == null){
                itemView.setVisibility(View.GONE);
                return;
            }
            mFeature = feature;
            Feature parentFeature = FeatureHelper.findFeatureById(mFeature.getParentFeatureId());
            if (parentFeature == null) {
                mFeatureTitleView.setText(
                        mFeature.getTitle()
                );
            } else {
                mFeatureTitleView.setText(
                        mFeature.getTitle()+" | "+parentFeature.getTitle()
                );
            }
        }

        private String getPromptMessageForFeatureAddition() {
            Feature parentFeature = FeatureHelper.findFeatureById(mFeature.getParentFeatureId());
            Newspaper newspaper = NewspaperHelper.findNewspaperById(mFeature.getNewsPaperId());
            StringBuilder promptSB = new StringBuilder("Add \""+mFeature.getTitle());
            if (parentFeature!=null){
                promptSB.append(" | "+parentFeature.getTitle());
            }
            if (newspaper!=null){
                promptSB.append(" | "+newspaper.getName());
            }
            promptSB.append("\" on \""+mFeatureGroup.getTitle()+"\"?");
            return promptSB.toString();
        }

        private String getMessageForFailureToast() {
            return "Error occured, please retry.";
        }

        private String getMessageForSuccessfulToast() {

            Feature parentFeature = FeatureHelper.findFeatureById(mFeature.getParentFeatureId());
            Newspaper newspaper = NewspaperHelper.findNewspaperById(mFeature.getNewsPaperId());
            StringBuilder promptSB = new StringBuilder("\""+mFeature.getTitle());
            if (parentFeature!=null){
                promptSB.append(" | "+parentFeature.getTitle());
            }
            if (newspaper!=null){
                promptSB.append(" | "+newspaper.getName());
            }
            promptSB.append("\" added on \""+mFeatureGroup.getTitle()+"\"");
            return promptSB.toString();

        }
    }

}

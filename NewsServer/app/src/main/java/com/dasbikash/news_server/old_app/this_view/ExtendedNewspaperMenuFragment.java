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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroup;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import java.io.Serializable;
import java.util.ArrayList;

public class ExtendedNewspaperMenuFragment extends Fragment {

    //private static final String TAG = "ExtNPMenu";
    private static final String TAG = "StackTrace";

    private static final String ARG_PREVIOUS_ITEM =
            "ExtendedNewspaperMenuFragment.ARG_PREVIOUS_ITEM";

    public static final String PARENT_FEATURE_LIST_TITLE_PREAMBLE = "Active pages of ";
    public static final String NEWSPAPER_LIST_TITLE_TEXT = "Active <br>Newspapers";
    public static final String NEWSPAPER_MENU_PAGE_TITLE = "Newspaper Menu";

    private TextView mParentFeatureListTitle;
    private TextView mNewspaperHomePageTitle;
    private TextView mNewspaperFeatureListTitle;
    private RecyclerView mNewspaperListRV;
    private RecyclerView mParentFeatureListRV;

    private ArrayList<Newspaper> mNewspaperList = new ArrayList<>();
    private ArrayList<Feature> mParentFeatureList =
            new ArrayList<>();

    private Newspaper mCurrentNewspaper;
    private Feature mCurrentParentFeature;

    private NewspaperRVListItemHolder mCurrentNewspaperRVListItemHolder;

    private boolean mAmNewBorn = false;


    public static ExtendedNewspaperMenuFragment newInstance(Serializable previousLoadedItem){
        Bundle args = new Bundle();
        args.putSerializable(ARG_PREVIOUS_ITEM, previousLoadedItem);
        ExtendedNewspaperMenuFragment fragment = new ExtendedNewspaperMenuFragment();
        fragment.setArguments(args);
        return fragment;
    }

    interface CallBacksForPageLoad{
        void loadNewspaperHomePage(Newspaper newspaper);
        void loadFeature(Feature feature);
    }

    private CallBacksForPageLoad mCallBacksForPageLoad;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBacksForPageLoad = (CallBacksForPageLoad) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_extended_newspaper_menu,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


         if ( getArguments()!= null && getArguments().containsKey(ARG_PREVIOUS_ITEM)){

             Serializable previousItem = getArguments().getSerializable(ARG_PREVIOUS_ITEM);

             mNewspaperList = NewspaperHelper.getAllActiveNewspapers();
             mAmNewBorn =true;

             if (previousItem instanceof Feature){
                 Feature feature = (Feature)previousItem;
                 mCurrentNewspaper = NewspaperHelper.findNewspaperById(feature.getNewsPaperId());
                 if (feature.getParentFeatureId() !=
                         NewsServerDBSchema.NULL_PARENT_FEATURE_ID){
                     mCurrentParentFeature = FeatureHelper.findFeatureById(
                             feature.getParentFeatureId()
                     );
                 } else {
                     mCurrentParentFeature = feature;
                 }
             } else if(previousItem instanceof FeatureGroup){
                 FeatureGroup featureGroup = (FeatureGroup) previousItem;
                 mCurrentParentFeature = null;
                 mCurrentNewspaper = NewspaperHelper.findNewspaperById(featureGroup.getCategoryIdentifier());
                 if (mCurrentNewspaper ==null){
                     mCurrentNewspaper = mNewspaperList.get(0);
                 }
             }
         }

        mNewspaperFeatureListTitle = view.findViewById(R.id.newspaper_list_title_text);
        mNewspaperHomePageTitle = view.findViewById(R.id.np_home_page_title);
        mParentFeatureListTitle = view.findViewById(R.id.parent_feature_list_title_text);
        mNewspaperListRV = view.findViewById(R.id.extended_menu_newspaper_list);
        mParentFeatureListRV = view.findViewById(R.id.newspaper_parent_feature_list);

        DisplayUtility.displayHtmlText(mNewspaperFeatureListTitle,NEWSPAPER_LIST_TITLE_TEXT);

        mNewspaperHomePageTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentNewspaper !=null){
                    mCallBacksForPageLoad.loadNewspaperHomePage(mCurrentNewspaper);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mAmNewBorn) {
            ArrayList<Newspaper> tempNewspaperList =
                    NewspaperHelper.getAllActiveNewspapers();
            if (checkIfNewspaperListChanged(tempNewspaperList)) {
                mNewspaperList.clear();
                mNewspaperList.addAll(tempNewspaperList);
                if (mCurrentNewspaper == null ||
                        !newspaperListContain(mCurrentNewspaper)) {
                    if (mNewspaperList.size() > 0) {
                        mCurrentNewspaper = mNewspaperList.get(0);
                    }
                    mCurrentParentFeature = null;
                    mParentFeatureList.clear();
                }
                mParentFeatureListRV.setVisibility(View.GONE);
                mNewspaperHomePageTitle.setVisibility(View.GONE);
            }
        }else {
            getCurrentNewspaperFromList();
            mAmNewBorn = false;
        }
        mNewspaperListRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        mNewspaperListRV.setAdapter(new NewspaperListRVAdapter());
        if (mCurrentNewspaper !=null){
            mNewspaperListRV.scrollToPosition(mNewspaperList.indexOf(mCurrentNewspaper));
        }

        ((ArticleListActivity)getActivity()).getSupportActionBar().setTitle(
                NEWSPAPER_MENU_PAGE_TITLE
        );
    }

    private void getCurrentNewspaperFromList(){
        for (Newspaper newspaper :
                mNewspaperList) {
            if (newspaper.getId() == mCurrentNewspaper.getId()){
                mCurrentNewspaper = newspaper;
                return;
            }
        }
    }

    private boolean newspaperListContain(Newspaper currentNewspaper) {
        for (Newspaper newspaper :
                mNewspaperList) {
            if (newspaper.getId() == currentNewspaper.getId()){
                return true;
            }
        }
        return false;
    }

    private boolean checkIfNewspaperListChanged(ArrayList<Newspaper> tempNewspaperList) {

        if (mNewspaperList.size() != tempNewspaperList.size()) return true;

        label1:
        for (Newspaper newspaper :
                tempNewspaperList) {
            for (Newspaper oldNewspaper :
                    mNewspaperList) {
                if (oldNewspaper.getId() == newspaper.getId()){
                    continue label1;
                }
            }
            return true;
        }
        return false;
    }

    private class NewspaperListRVAdapter extends RecyclerView.Adapter<NewspaperRVListItemHolder>{

        @NonNull
        @Override
        public NewspaperRVListItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new NewspaperRVListItemHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull NewspaperRVListItemHolder newspaperRVListItemHolder, int position) {
            newspaperRVListItemHolder.bind(mNewspaperList.get(position));
        }

        @Override
        public int getItemCount() {
            return mNewspaperList.size();
        }
    }

    private class NewspaperRVListItemHolder extends RecyclerView.ViewHolder{

        private TextView mNewspaperTitleText;
        private Newspaper mNewspaper;
        private Country mCountry;

        @SuppressLint("ResourceType")
        public NewspaperRVListItemHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_extended_menu_newspaper_menu_item, parent, false));

            mNewspaperTitleText = itemView.findViewById(R.id.extended_menu_newspaper_title_text_view);
            mNewspaperTitleText.setTextColor(Color.parseColor(getActivity().getResources().getString(R.color.color_navy_blue)));
            mNewspaperTitleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newspaperTitleClickAction();
                }
            });
        }

        String getTitleText(){
            return (mNewspaperList.indexOf(mNewspaper)+1)+") "+mNewspaper.getName()
                    +" ("+mCountry.getCountryCode()+")";
        }

        void showAsActive(){
            DisplayUtility.displayHtmlText(mNewspaperTitleText,
                    "<strong><u>"+getTitleText()+"</u></strong>");
            setParentFeatureListTitletext(mNewspaper);
            mNewspaperHomePageTitle.setText(NewspaperHelper.getNewspaperHomePageTitle(mNewspaper));
            mNewspaperHomePageTitle.setVisibility(View.VISIBLE);
            mNewspaperTitleText.setBackgroundColor(Color.parseColor(
                    getString(R.string.light_button_background)
            ));
        }

        void showAsInActive(){
            mNewspaperTitleText.setBackgroundColor(Color.WHITE);
            mNewspaperTitleText.setText(getTitleText());
        }

        void newspaperTitleClickAction() {
            if (mCurrentNewspaper.getId()!=mNewspaper.getId()){
                if (mCurrentNewspaperRVListItemHolder!=null){
                    mCurrentNewspaperRVListItemHolder.
                            showAsInActive();
                }
                mCurrentNewspaper = mNewspaper;
                showAsActive();
                mParentFeatureList =
                        FeatureHelper.getActiveParentFeaturesForNewspaper(mNewspaper);
                setUpParentFeatureView();
            }
            mCurrentNewspaperRVListItemHolder =
                    NewspaperRVListItemHolder.this;
        }

        void bind(Newspaper newspaper){
            mNewspaper = newspaper;
            mCountry = CountryHelper.findCountryByName(mNewspaper.getCountryName());
            if (mCurrentNewspaper.getId() == mNewspaper.getId()){
                showAsActive();
                ArrayList<Feature> tempParentFeatureList =
                        FeatureHelper.getActiveParentFeaturesForNewspaper(mNewspaper);
                if (checkIfParentFeatureListChanged(tempParentFeatureList)){
                    mParentFeatureList.clear();
                    mParentFeatureList.addAll(tempParentFeatureList);
                }
                setUpParentFeatureView();
                mCurrentNewspaperRVListItemHolder =
                        NewspaperRVListItemHolder.this;
            }else {
                showAsInActive();
            }
        }

        private void setUpParentFeatureView() {
            mParentFeatureListRV.setLayoutManager(new LinearLayoutManager(getActivity()));
            mParentFeatureListRV.setAdapter(new ParentFeatureListAdapter());
            mParentFeatureListRV.setVisibility(View.VISIBLE);
            int parentFeatureIndex = -1;
            if (mCurrentParentFeature !=null){
                for (Feature feature :
                        mParentFeatureList) {
                    if (feature.getId() == mCurrentParentFeature.getId()){
                        parentFeatureIndex = mParentFeatureList.indexOf(feature);
                    }
                }
            }
            if (parentFeatureIndex!=-1){
                mParentFeatureListRV.scrollToPosition(parentFeatureIndex);
            }
        }

        private boolean checkIfParentFeatureListChanged(ArrayList<Feature> tempParentFeatureList) {
            if (mParentFeatureList.size() != tempParentFeatureList.size()) return true;
            label2:
            for (Feature feature :
                    tempParentFeatureList) {
                for (Feature oldFeature :
                        mParentFeatureList) {
                    if (oldFeature.getId() == feature.getId()){
                        continue label2;
                    }
                }
                return true;
            }
            return false;
        }
    }

    private void setParentFeatureListTitletext(Newspaper newspaper) {
        DisplayUtility.displayHtmlText(mParentFeatureListTitle,
                PARENT_FEATURE_LIST_TITLE_PREAMBLE +"<br>"+newspaper.getName());
    }

    private class ParentFeatureListAdapter extends RecyclerView.Adapter<ParentFeatureHolder>{
        private ParentFeatureHolder mCurrentParentFeatureHolders;

        public ParentFeatureListAdapter() {
        }

        @NonNull
        @Override
        public ParentFeatureHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ParentFeatureHolder(layoutInflater, parent,ParentFeatureListAdapter.this);
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

        private TextView mParentFeatureTitleTextView;
        private ImageButton mShowChildFeaturesView;
        private ImageButton mHideChildFeaturesView;
        private RecyclerView mChildrenListView;
        private Feature mParentFeature;
        ArrayList<Feature> mChildFeatureList= new ArrayList<>();
        private ParentFeatureListAdapter mParentFeatureListAdapter;

        public ParentFeatureHolder(@NonNull LayoutInflater inflater, ViewGroup parent,ParentFeatureListAdapter parentFeatureListAdapter) {
            super(inflater.inflate(R.layout.layout_parent_feature_nav_item, parent, false));
            mParentFeatureTitleTextView = itemView.findViewById(R.id.title_text);
            mShowChildFeaturesView = itemView.findViewById(R.id.show_decendents_button);
            mHideChildFeaturesView = itemView.findViewById(R.id. hide_decendents_button);
            mChildrenListView = itemView.findViewById(R.id.children_feature_RV);
            mParentFeatureTitleTextView.setTextColor(Color.BLACK);
            mParentFeatureListAdapter = parentFeatureListAdapter;

            mParentFeatureTitleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickAction();
                }
            });

            mShowChildFeaturesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickAction();
                }
            });

            mHideChildFeaturesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickAction();
                }
            });
        }

        void bind(Feature parentFeature){

            mShowChildFeaturesView.setVisibility(View.VISIBLE);
            mHideChildFeaturesView.setVisibility(View.GONE);
            mChildrenListView.setVisibility(View.GONE);
            //hideChildrenFeatures();
            //mChildFeatureList = new ArrayList<>();
            mParentFeature = parentFeature;
            mParentFeatureTitleTextView.setText(mParentFeature.getTitle());
            mChildFeatureList = FeatureHelper.getActiveChildFeatures(mParentFeature.getId());

            if (mChildFeatureList.size() > 0) {
                if (mParentFeature.getLinkFormat() != null) {
                    mChildFeatureList.add(0, mParentFeature);
                }
                mChildrenListView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mChildrenListView.setAdapter(new ChildFeatureListAdapter(mChildFeatureList));

                if (mCurrentParentFeature!=null &&
                        mCurrentParentFeature.getId() ==
                                mParentFeature.getId()){
                    showChildrenFeatures();
                    mParentFeatureListAdapter.mCurrentParentFeatureHolders =
                            ParentFeatureHolder.this;
                }
            } else {
                mHideChildFeaturesView.setVisibility(View.GONE);
                mShowChildFeaturesView.setVisibility(View.GONE);
                mChildrenListView.setVisibility(View.GONE);
            }
        }

        void onClickAction(){

            if (mParentFeatureListAdapter.mCurrentParentFeatureHolders !=null &&
                    mParentFeatureListAdapter.mCurrentParentFeatureHolders !=
                        ParentFeatureHolder.this){
                mParentFeatureListAdapter.mCurrentParentFeatureHolders.
                        hideChildrenFeatures();
            }
            mParentFeatureListAdapter.mCurrentParentFeatureHolders =
                    ParentFeatureHolder.this;

            if (mChildFeatureList.size() > 0) {
                switch (mChildrenListView.getVisibility()) {
                    case View.VISIBLE:
                        hideChildrenFeatures();
                        break;
                    case View.GONE:
                        showChildrenFeatures();
                        break;
                    default:
                        break;
                }
            } else if (mParentFeature.getLinkFormat() != null) {
                mCurrentParentFeature = mParentFeature;
                mCallBacksForPageLoad.loadFeature(mParentFeature);
            }
        }

        private void hideChildrenFeatures() {
            if (mChildFeatureList.size() > 0) {
                mChildrenListView.setVisibility(View.GONE);
                mHideChildFeaturesView.setVisibility(View.GONE);
                mShowChildFeaturesView.setVisibility(View.VISIBLE);
            }
        }

        private void showChildrenFeatures() {
            if (mChildFeatureList.size() > 0) {
                mChildrenListView.setVisibility(View.VISIBLE);
                mHideChildFeaturesView.setVisibility(View.VISIBLE);
                mShowChildFeaturesView.setVisibility(View.GONE);
                mCurrentParentFeature = mParentFeature;
            }
        }
    }

    private class ChildFeatureListAdapter extends RecyclerView.Adapter<ChildFeatureHolder>{

        private ArrayList<Feature> mChildFeatureList;

        public ChildFeatureListAdapter(ArrayList<Feature> childFeatureList) {
            mChildFeatureList = childFeatureList;
        }

        @NonNull
        @Override
        public ChildFeatureHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ChildFeatureHolder(layoutInflater, parent);
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

    private class ChildFeatureHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        private TextView mChildNavListItemText;
        private Feature mChildFeature;

        @SuppressLint("ResourceType")
        public ChildFeatureHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_child_feature_nav_item, parent, false));
            mChildNavListItemText = itemView.findViewById(R.id.child_nav_item_view);
            mChildNavListItemText.setTextColor(Color.parseColor(
                    getString(R.color.color_navy_blue)
            ));
            itemView.setOnClickListener(this);
        }

        void bind(Feature childFeature){
            if (childFeature !=null) {
                mChildFeature =childFeature;
                String featureTitle = "<em>"+mChildFeature.getTitle()+"</em>";
                DisplayUtility.displayHtmlText(mChildNavListItemText,featureTitle);
            } else {
                itemView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            mCallBacksForPageLoad.loadFeature(mChildFeature);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().invalidateOptionsMenu();
        inflater.inflate(R.menu.menu_layout_extended_newspaper_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit_newspaper_menu_menu_item:
                editNewspaperMenuAction();
                return true;
        }
        return false;
    }

    private void editNewspaperMenuAction() {
        Intent intent = SettingsActivity.
                            newIntentForNewspaperMenuCustomization(getActivity());
        if (intent!=null) {
            startActivityForResult(intent,ArticleListActivity.DEFAULT_REQUEST_CODE);
        }
    }
}

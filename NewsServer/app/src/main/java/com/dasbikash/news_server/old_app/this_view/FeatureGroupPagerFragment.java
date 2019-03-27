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
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroup;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group_entry.FeatureGroupEntry;
import com.dasbikash.news_server.old_app.this_data.feature_group_entry.FeatureGroupEntryHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_view.transformer_anims.ZoomPageTransformer;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import java.util.ArrayList;

@SuppressLint("ParcelCreator")
public class FeatureGroupPagerFragment extends Fragment
        implements ArticleListActivity.CallBackForChildFragment,Parcelable {

    private static final String TAG = "StackTrace";

    private static final String ARG_FEATURE_GROUP =
            "FeatureGroupPagerFragment.ARG_FEATURE_GROUP";

    //private static FeatureGroupPagerFragment sFeatureGroupPagerFragment;
    //public static transient DNode dNode = null;

    RecyclerView mFeatureGroupMenu;
    private ViewPager mFeatureGroupPager;
    private LinearLayout mFeatureMenuView;
    private LinearLayout mViewForEmptyGroup;
    private Button mAddPageButton;

    private FeatureGroup mFeatureGroup;
    private ArrayList<Feature>  mFeatureList = new ArrayList<>();

    private Feature mCurrentMenuItem;
    private ProgressBar mEditionDataLoadingProgressBar;

    private FragmentStatePagerAdapter mFragmentStatePagerAdapter;
    private ArrayList<FeatureMenuItemHolder> mFeatureMenuItemHolders = new ArrayList<>();

    private boolean mRetunFromActivityResult =false;

    private static final int MENU_TEXT_ACTIVE_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int MENU_TEXT_INACTIVE_BACKGROUND_COLOR = Color.parseColor("#bebebe");

    @Override
    public boolean houseKeepingOnActivityReturnWithResult() {
        return refreshDisplay();
    }

    public static FeatureGroupPagerFragment newInstance(FeatureGroup featureGroup){

        Bundle args = new Bundle();
        args.putSerializable(ARG_FEATURE_GROUP, featureGroup);

        FeatureGroupPagerFragment fragment = new FeatureGroupPagerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feature_group_viewpager_holder,container,false);
    }

    void showEditionDataLoadingProgressBar(){
        mEditionDataLoadingProgressBar.setVisibility(View.VISIBLE);
        mEditionDataLoadingProgressBar.bringToFront();
    }

    void hideEditionDataLoadingProgressBar(){
        mEditionDataLoadingProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        mFeatureGroupMenu = (RecyclerView) view.findViewById(R.id.feature_group_pager_menu_view);
        mFeatureGroupPager = (ViewPager) view.findViewById(R.id.feature_group_view_pager);
        mFeatureMenuView = (LinearLayout) view.findViewById(R.id.feature_menu);
        mViewForEmptyGroup = view.findViewById(R.id.view_for_empty_feature_group);
        mAddPageButton = view.findViewById(R.id.add_feature_to_group_button);
        mEditionDataLoadingProgressBar = view.findViewById(R.id.edition_data_loading_progress_bar);
        mFeatureMenuView.bringToFront();

        mAddPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG, "onClick: mAddPageButton");
                editFeatureGroupMenuItemAction();
            }
        });

        mViewForEmptyGroup.setVisibility(View.GONE);

        if (getArguments() == null) {
            return;
        }

        mFeatureGroup = (FeatureGroup) getArguments().getSerializable(ARG_FEATURE_GROUP);

        if (mFeatureGroup == null) {
            return;
        }

        mFeatureGroupMenu.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mFeatureGroupMenu.setAdapter(new FeatureMenuAdapter());
        FragmentManager fragmentManager = ((ArticleListActivity)getActivity()).getSupportFragmentManager();

        mFragmentStatePagerAdapter = new FragmentStatePagerAdapter(fragmentManager) {

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }

            @Override
            public Fragment getItem(int position) {
                ArticleListFragment articleListFragment =
                        ArticleListFragment.newInstanceForFeatureGroup(mFeatureList.get(position),
                                                FeatureGroupPagerFragment.this);
                return articleListFragment;
            }
            @Override
            public int getCount() {
                return mFeatureList.size();
            }
        };

        mFeatureGroupPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                makeMenuItemActive(position);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        refreshFeatureList();
        mFeatureGroupPager.setPageTransformer(true,new ZoomPageTransformer());
        mFeatureGroupPager.setAdapter(mFragmentStatePagerAdapter);

        if (mFeatureList.size()>0) {

            mFeatureGroupPager.setCurrentItem(0);
            mCurrentMenuItem = mFeatureList.get(0);
            makeMenuItemActive(0);

            mFeatureGroupPager.setVisibility(View.VISIBLE);
            mFeatureGroupMenu.setVisibility(View.VISIBLE);
            mViewForEmptyGroup.setVisibility(View.GONE);
        } else {
            mFeatureGroupPager.setVisibility(View.GONE);
            mFeatureGroupMenu.setVisibility(View.GONE);
            mViewForEmptyGroup.setVisibility(View.VISIBLE);
        }
    }

    private boolean refreshDisplay(){

        ArrayList<Feature> tempFeatureList = new ArrayList<>();
        tempFeatureList.addAll(mFeatureList);

        if(!refreshFeatureList()){
            return false;
        }

        if (tempFeatureList.size() ==
                mFeatureList.size()){
            int i = 0;
            for (; i < tempFeatureList.size(); i++) {
                if (tempFeatureList.get(i).getId() !=
                        mFeatureList.get(i).getId()){
                    break;
                }
            }
            if (i == tempFeatureList.size()){
                return true;
            }
        }

        int newPosition = 0;
        for (int i = 0; i < mFeatureList.size(); i++) {
            if (mCurrentMenuItem!=null &&
                    (mFeatureList.get(i).getId() == mCurrentMenuItem.getId())){
                newPosition = i;
                break;
            }
        }

        final int activePosition = newPosition;
        //Log.d("StackTrace", "refreshDisplay: activePosition: "+activePosition);

        if (mFeatureList.size()>0) {
            new Handler(Looper.getMainLooper()).postAtTime(() -> {
                mCurrentMenuItem = mFeatureList.get(activePosition);
                mFeatureGroupMenu.getAdapter().notifyDataSetChanged();
                //mFeatureMenuItemHolders.clear();
                mFragmentStatePagerAdapter.notifyDataSetChanged();
                mFeatureGroupPager.setCurrentItem(activePosition);
                //makeMenuItemActive(activePosition);
                mFeatureGroupPager.setVisibility(View.VISIBLE);
                mFeatureGroupMenu.setVisibility(View.VISIBLE);
                mViewForEmptyGroup.setVisibility(View.GONE);
            },100);
        } else {
            mFeatureGroupPager.setVisibility(View.GONE);
            mFeatureGroupMenu.setVisibility(View.GONE);
            mViewForEmptyGroup.setVisibility(View.VISIBLE);
        }
        return true;
    }

    private boolean refreshFeatureList() {

        mFeatureGroup = FeatureGroupHelper.findFeatureGroupByTitle(mFeatureGroup.getTitle());
        if (mFeatureGroup == null){
            //Log.d(TAG, "refreshFeatureList: mFeatureGroup == null");
            return false;
        }

        ArrayList<FeatureGroupEntry> featureGroupEntries =
                FeatureGroupEntryHelper.getEntriesForFeatureGroup(mFeatureGroup);
        mFeatureList.clear();

        for (FeatureGroupEntry featureGroupEntry :
                featureGroupEntries) {
            Feature feature =
                    FeatureGroupEntryHelper.getFeatureFromFeatureGroupEntry(featureGroupEntry);
            if (feature !=null){
                mFeatureList.add(feature);
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume: FeatureGroupPagerFragment");
        if (mRetunFromActivityResult) {
            mRetunFromActivityResult = false;
            refreshDisplay();
        }
    }

    private void makeMenuItemActive(int position){

        mCurrentMenuItem = mFeatureList.get(position);
        if (mCurrentMenuItem == null) return;

        mFeatureGroupMenu.scrollToPosition(position);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (FeatureMenuItemHolder featureMenuItemHolder :
                        mFeatureMenuItemHolders) {
                    if (featureMenuItemHolder.getFeatureid() == mCurrentMenuItem.getId()){
                        featureMenuItemHolder.showAsActive();
                    } else {
                        featureMenuItemHolder.showAsInactive();
                    }
                }
            }
        });
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    class FeatureMenuAdapter extends RecyclerView.Adapter<FeatureMenuItemHolder>{

        @NonNull
        @Override
        public FeatureMenuItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new FeatureMenuItemHolder(layoutInflater,viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull FeatureMenuItemHolder featureMenuItemHolder, int i) {
            featureMenuItemHolder.bind(mFeatureList.get(i));
        }

        @Override
        public int getItemCount() {
            return mFeatureList.size();
        }
    }

    class FeatureMenuItemHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{

        private TextView mFeatureMenuItemTitle;
        private View mHorizontalSeparator;
        private Feature mFeature;

        FeatureMenuItemHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_feature_menu_item, parent, false));
            mFeatureMenuItemTitle = itemView.findViewById(R.id.feature_list_menu_item_text);
            mHorizontalSeparator = itemView.findViewById(R.id.menu_heighlighter_seperator);
            itemView.setOnClickListener(this);
            mFeatureMenuItemHolders.add(FeatureMenuItemHolder.this);
        }

        private void showAsActive(){
            if (itemView !=null) {
                itemView.setBackgroundColor(MENU_TEXT_ACTIVE_BACKGROUND_COLOR);
            }
            if (mHorizontalSeparator !=null) {
                mHorizontalSeparator.setVisibility(View.VISIBLE);
            }
            //Log.d(TAG, "showAsActive: "+mFeature.getTitle());
        }
        private void showAsInactive(){
            if (itemView !=null) {
                itemView.setBackgroundColor(MENU_TEXT_INACTIVE_BACKGROUND_COLOR);
            }
            if (mHorizontalSeparator !=null) {
                mHorizontalSeparator.setVisibility(View.GONE);
            }
            //Log.d(TAG, "showAsInactive: "+mFeature.getTitle());
        }

        int getFeatureid(){
            if (mFeature!=null) {
                return mFeature.getId();
            } else {
                return 0;
            }
        }

        private void bind(Feature feature){

            mFeature = feature;

            if (mFeatureGroup.getCategoryIdentifier() ==
                    NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP ||
                mFeatureGroup.getCategoryIdentifier() ==
                        NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_HOMEPAGE) {

                Newspaper newspaper = NewspaperHelper.findNewspaperById(mFeature.getNewsPaperId());

                DisplayUtility.displayHtmlText(mFeatureMenuItemTitle,
                        mFeature.getTitle()+"<br>("+newspaper.getName()+")");
                /*mFeatureMenuItemTitle.setText(
                        mFeature.getTitle()+
                        "<br>"+
                        newspaper.getName()+
                );*/
            } else {
                mFeatureMenuItemTitle.setText(mFeature.getTitle());
            }

            if (mCurrentMenuItem.getId()==mFeature.getId()){
                showAsActive();
            } else {
                showAsInactive();
            }
        }

        @Override
        public void onClick(View view) {
            mFeatureGroupPager.setCurrentItem(mFeatureList.indexOf(mFeature));
        }
    }

    @Override
    public void onDestroy() {
        //Log.d("StackTrace", "onDestroy: FHFPager");
        new Handler(Looper.getMainLooper()).postAtTime(new Runnable() {
            @Override
            public void run() {
                if (mFeatureList!=null) {
                    mFeatureList.clear();
                }
                if (mFeatureGroupPager.getAdapter() !=null) {
                    mFeatureGroupPager.getAdapter().notifyDataSetChanged();
                }
            }
        },100);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_layout_feature_group_pager,menu);
        if (mFeatureGroup != null){
            MenuItem menuItem = menu.findItem(R.id.edit_feature_group_menu_item);

            if (mFeatureGroup.getCategoryIdentifier() == NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_HOMEPAGE){
                menuItem.setTitle("Customize Home page");
            } else if (mFeatureGroup.getCategoryIdentifier() != NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP){
                menuItem.setTitle("Customize "+mFeatureGroup.getTitle()+" "+
                        NewspaperHelper.getNewspaperHomePageTitle(
                                NewspaperHelper.findNewspaperById(mFeatureGroup.getCategoryIdentifier())
                        ));
            } else {
                menuItem.setTitle("Customize "+mFeatureGroup.getTitle()+" news category");
            }
        }
        //menu.findItem(R.id.share_menu_item).setTitle("Share Image");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.edit_feature_group_menu_item:
                editFeatureGroupMenuItemAction();
                return true;
        }
        return false;
    }

    private void editFeatureGroupMenuItemAction() {

        switch (mFeatureGroup.getCategoryIdentifier()){
            case NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_HOMEPAGE:
                editAppHomeFeatureGroup();
                break;
            case NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP:
                editCustomFeatureGroup();
                break;
            default:
                editNewspaperHomeFeatureGroup();
                break;
        }
    }

    private void editCustomFeatureGroup() {
        Intent intent = SettingsActivity.newIntentForNonNewspaperFeatureGroupCustomization(getActivity(),mFeatureGroup);
        if (intent!=null) {
            //Log.d(TAG, "editCustomFeatureGroup: intent!=null");
            startActivityForResult(intent,ArticleListActivity.DEFAULT_REQUEST_CODE);
        }
    }

    private void editAppHomeFeatureGroup() {
        Intent intent = SettingsActivity.newIntentForAppHomePageCustomization(getActivity());
        if (intent!=null) {
            startActivityForResult(intent,ArticleListActivity.DEFAULT_REQUEST_CODE);
        }
    }

    private void editNewspaperHomeFeatureGroup() {
        Newspaper newspaper = NewspaperHelper.findNewspaperById(mFeatureGroup.getCategoryIdentifier());
        if (newspaper!=null) {
            Intent intent = SettingsActivity.newIntentForNewspaperHomePageCustomization(getActivity(), newspaper);
            startActivityForResult(intent,ArticleListActivity.DEFAULT_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("StackTrace", "onActivityResult: from FeatureGroupPagerFragment");
        if (requestCode == ArticleListActivity.DEFAULT_REQUEST_CODE){
            mRetunFromActivityResult = true;
        }
    }
}

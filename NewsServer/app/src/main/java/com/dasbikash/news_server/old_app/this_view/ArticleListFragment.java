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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderHelper;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderService;
import com.dasbikash.news_server.old_app.image_downloader.ImageDownloader;
import com.dasbikash.news_server.old_app.this_data.article.Article;
import com.dasbikash.news_server.old_app.this_data.article.ArticleHelper;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group_entry.FeatureGroupEntryHelper;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageData;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageDataHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.SettingsUtility;
import com.dasbikash.news_server.old_app.this_utility.URLConnectionHelper;
import com.dasbikash.news_server.old_app.this_utility.NetConnectivityUtility;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import java.io.File;
import java.util.ArrayList;

public final class ArticleListFragment extends Fragment {

    private static final String TAG = "StackTrace";

    private static final String ARG_CURRENT_FEATURE =
            "ArticleListFragment.ARG_CURRENT_FEATURE";
    private static final String ARG_FRAGMENT_FOR_FEATURE_GROUP =
            "ArticleListFragment.ARG_FRAGMENT_FOR_FEATURE_GROUP";
    private static final int RESULT_CODE_FOR_AP_ACTIVITY = 512;

    private static final long DATA_RELOAD_REQUEST_INTERVAL_MILLIS = 2* URLConnectionHelper.CONNECTION_TIMEOUT_MILLIS;

    private static Feature sFeature;
    private static Newspaper sNewspaper;


    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingProgressBar;
    private LinearLayout mInitLoadingProgressBarHolder;
    Feature mFeature;
    private Newspaper mNewspaper;
    private Country mCountry;

    private int mLastBoundArticleId=0;
    private ArrayList<ArticlePreviewHolder> mArticlePreviewHolders = new ArrayList<>();
    private ArrayList<Integer> mArticleIdList = new ArrayList<>();
    private ShowProgressbarMillis mProgressBarDisplayTask=null;
    private InitDataLoadingReRequest mInitDataLoadingReRequest = null;

    private boolean mEndOfEditionReached = false;
    private long mEditionLoadRequestTsMillis = 0;
    private boolean mDisplayedOnFeatureGroup;
    private boolean mReInit = false;
    private boolean mEditionDownloadRequestPending = false;

    private FeatureGroupPagerFragment mParentFeatureGroupPagerFragment = null;

    private boolean mOnActivityResult=false;

    private int mImageNotDownloadedCount = 0;

    private final BroadcastReceiver mEditionLoadBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEditionLoadBrodcastMessage(intent);
        }
    };


    private final BroadcastReceiver mImageLoadBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleImageLoadBrodcastMessage(intent);
        }
    };


    private final BroadcastReceiver mNetConAvailableBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleNetConAvailableBroadcast(intent);
        }
    };

    private void handleNetConAvailableBroadcast(Intent intent) {
        //Log.d(TAG, "handleNetConAvailableBroadcast: ");
        if (mInitDataLoadingReRequest != null){
            mReInit = true;
            mInitDataLoadingReRequest.cancel(true);
        } else {
            refreshRelyclerView();
            if (mEditionDownloadRequestPending) {
                mEditionDownloadRequestPending = false;
                placeNextEditionDownloadRequest();
            }
        }
    }

    private void refreshRelyclerView() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mImageNotDownloadedCount >0 &&
                        NetConnectivityUtility.isConnected()){
                    if (NetConnectivityUtility.isOnMobileDataNetwork() &&
                            !SettingsUtility.canDlImageOnDataNet()) {
                        return;
                    }
                    mImageNotDownloadedCount = 0;
                    mArticlePreviewHolders.clear();
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    if (mLastBoundArticleId != 0) {
                        mRecyclerView.scrollToPosition(mArticleIdList.indexOf(mLastBoundArticleId));
                    }
                }
            }
        });
    }

    public static ArticleListFragment newInstance(Feature feature){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CURRENT_FEATURE, feature);
        ArticleListFragment fragment = new ArticleListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ArticleListFragment newInstanceForFeatureGroup(Feature feature,
                                                                 FeatureGroupPagerFragment featureGroupPagerFragment){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CURRENT_FEATURE, feature);
        args.putParcelable(ARG_FRAGMENT_FOR_FEATURE_GROUP, featureGroupPagerFragment);
        ArticleListFragment fragment = new ArticleListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article_list,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);

        if (getArguments()!= null && !(getArguments().isEmpty())) {

            mFeature = (Feature) getArguments().getSerializable(ARG_CURRENT_FEATURE);
            //Log.d("NSUtility", "onViewCreated: mFeature.getTitle(): "+mFeature.getTitle());

            if (getArguments().getParcelable(ARG_FRAGMENT_FOR_FEATURE_GROUP) != null){
                mParentFeatureGroupPagerFragment =(FeatureGroupPagerFragment) getArguments().
                                                        getParcelable(ARG_FRAGMENT_FOR_FEATURE_GROUP);
            }

            if (mFeature==null){
                exit();
            }
            sFeature = mFeature;

            mNewspaper = NewspaperHelper.findNewspaperById(mFeature.getNewsPaperId());
            if (mNewspaper == null){
                exit();
            }
            sNewspaper = mNewspaper;
            mCountry = CountryHelper.findCountryByName(mNewspaper.getCountryName());
            if (mCountry == null){
                exit();
            }
        } else {
            exit();
        }

        if (mParentFeatureGroupPagerFragment == null) {

            StringBuilder pageTitle= new StringBuilder();

            pageTitle.append(mFeature.getTitle());

            pageTitle.append(" | "+mNewspaper.getName());

            ((ArticleListActivity) getActivity()).getSupportActionBar().setTitle(pageTitle.toString());
        }

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mLoadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        mInitLoadingProgressBarHolder = view.findViewById(R.id.init_loading_progress_bar_holder);
        mLoadingProgressBar.setVisibility(View.GONE);
        mInitLoadingProgressBarHolder.setVisibility(View.GONE);
    }

    private void initDataLoading() {

        if (mRecyclerView.getAdapter() == null){

            mArticleIdList = ArticleHelper.findArticleIdsForFeature(mFeature);

            if (mArticleIdList.size()>0){
                initDisplay();
            } else {
                mInitLoadingProgressBarHolder.setVisibility(View.VISIBLE);
                mInitLoadingProgressBarHolder.bringToFront();
            }
        } else { //for resume
            if (!mOnActivityResult && mLastBoundArticleId != 0){
                mRecyclerView.getAdapter().notifyDataSetChanged();
                mRecyclerView.scrollToPosition(mArticleIdList.indexOf(mLastBoundArticleId));
            } else {
                mOnActivityResult = false;
            }
        }

        if(!EditionLoaderService.placeFirstEditionDownloadRequest(mFeature)){
                new InitDataLoadingReRequest().execute();
        }
    }

    private void initDisplay() {

        if (mArticleIdList.size() == 0){
            mArticleIdList = ArticleHelper.findArticleIdsForFeature(mFeature);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new ArticleIdListAdapter());
        mLoadingProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mImageNotDownloadedCount = 0;
    }

    private class InitDataLoadingReRequest extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mInitDataLoadingReRequest = InitDataLoadingReRequest.this;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(DATA_RELOAD_REQUEST_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                NewsServerUtility.logErrorMessage(TAG+":"+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            initDataLoading();
            mInitDataLoadingReRequest = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mReInit){
                mReInit = false;
                mInitDataLoadingReRequest = null;
                initDataLoading();
            }
        }
    }

    private class ArticleIdListAdapter extends RecyclerView.Adapter<ArticlePreviewHolder>{

        public ArticleIdListAdapter() {

        }

        @NonNull
        @Override
        public ArticlePreviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ArticlePreviewHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ArticlePreviewHolder articlePreviewHolder, int position) {

            mLastBoundArticleId = mArticleIdList.get(position);
            articlePreviewHolder.bind(mLastBoundArticleId);

            if (    position == (mArticleIdList.size()-1) &&
                    !mEndOfEditionReached &&
                    mFeature.getLinkVariablePartFormat()!=null ){
                if ((mEditionLoadRequestTsMillis == 0) ||
                   (System.currentTimeMillis() - mEditionLoadRequestTsMillis) > DATA_RELOAD_REQUEST_INTERVAL_MILLIS) {
                    placeNextEditionDownloadRequest();
                }
            }
        }
        @Override
        public int getItemCount() {
            return mArticleIdList.size();
        }
    }

    private void placeNextEditionDownloadRequest() {
        if (EditionLoaderService.placeEditionDownloadRequest(mFeature)) {
            mEditionLoadRequestTsMillis = System.currentTimeMillis();
            showLoadingProgressBar();
        } else {
            mEditionDownloadRequestPending = true;
            mEditionLoadRequestTsMillis = 0;
        }
    }

    private class ArticlePreviewHolder extends RecyclerView.ViewHolder
            implements  View.OnClickListener{

        ImageView mArticlePreviewImageView;
        private TextView mArticleTitleText;
        private TextView mArticlePublicationTimeText;
        private Article mArticle;
        ImageData mPreviewImage;

        String mPublicationTimeString;


        public ArticlePreviewHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {

            super(inflater.inflate(R.layout.view_article_preview,parent,false));

            mArticlePreviewImageView = itemView.findViewById(R.id.article_preview_image);
            mArticleTitleText = itemView.findViewById(R.id.article_title_text);
            mArticlePublicationTimeText = itemView.findViewById(R.id.article_publication_time_text);
            itemView.setOnClickListener(this);
        }

        public void bind(final int articleId){

            if (mArticlePreviewHolders.contains(ArticlePreviewHolder.this)){
                mArticlePreviewHolders.remove(ArticlePreviewHolder.this);
            }

            mArticle = ArticleHelper.findArticleById(articleId);

            mPublicationTimeString =
                    DisplayUtility.getArticlePublicationDateString(mArticle,mNewspaper);

            mPreviewImage = ImageDataHelper.findImageDataById(mArticle.getPreviewImageId());

            mArticlePreviewImageView.setVisibility(View.INVISIBLE);

            mArticleTitleText.setText(mArticle.getTitle());

            if (mPublicationTimeString !=null){
                mArticlePublicationTimeText.setText(mPublicationTimeString);
                mArticlePublicationTimeText.setVisibility(View.VISIBLE);
            } else {
                mArticlePublicationTimeText.setVisibility(View.GONE);
            }


            if (mPreviewImage == null){
                showArticlePreviewWithoutImage();
            } else {
                if (mPreviewImage.getDiskLocation()!=null &&
                        mPreviewImage.getSizeKB() > 0){
                    loadImage(mArticlePreviewImageView, mPreviewImage.getDiskLocation());
                } else {
                    switch (ImageDownloader.placeFileDownloadRequest(mArticle.getPreviewImageId(),mNewspaper)){
                        case INVALID_IMAGE_PARAM:
                            showArticlePreviewWithoutImage();
                            break;
                        case ALREADY_DOWNLOADED:
                            mPreviewImage = ImageDataHelper.findImageDataById(mArticle.getPreviewImageId());
                            loadImage(mArticlePreviewImageView, mPreviewImage.getDiskLocation());
                            break;
                        case NO_NETWORK_CONNECTION:
                        case CANT_DL_ON_DATA_NETWORK:
                            showArticlePreviewWithoutImage();
                            mImageNotDownloadedCount++;
                            break;
                        default:
                            mArticlePreviewHolders.add(ArticlePreviewHolder.this);
                            break;
                    }
                }
            }
            if (mEndOfEditionReached && mParentFeatureGroupPagerFragment!=null &&
                    mArticleIdList.indexOf(articleId) == mArticleIdList.size()-1){
                itemView.setPadding(0,0,0,mParentFeatureGroupPagerFragment.
                                                                                mFeatureGroupMenu.getHeight());
            } else {
                itemView.setPadding(0,0,0,0);
            }
        }

        void showArticlePreviewWithoutImage() {
            mArticlePreviewImageView.setImageResource(R.drawable.app_big_logo_rect_corner);
            mArticlePreviewImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = ArticlePagerActivity.newIntent(getActivity(),mFeature,mArticle.getId());
            startActivityForResult(intent,RESULT_CODE_FOR_AP_ACTIVITY);
            //Log.d(TAG, "onClick: mArticle.getTitle():"+mArticle.getTitle());
            ArticleLoaderHelper.placeArticleDownloadRequest(mArticle,mNewspaper);
        }
    }

    private void loadImage(ImageView imageView,String imageLocation){
        if (imageLocation != null && new File(imageLocation).length()>0){
            imageView.setImageBitmap(BitmapFactory.decodeFile(imageLocation));
            //imageView.setVisibility(View.VISIBLE);
        } else {
            //imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.prothomalo_logo));
            //imageView.setVisibility(View.GONE);
            imageView.setImageResource(R.drawable.app_big_logo_rect_corner);
        }
        imageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume: "+mFeature.getTitle());
        mEditionLoadRequestTsMillis = 0; //To enable edition download request
        registerBrodcastReceivers();
        initDataLoading();
        refreshRelyclerView();
    }

    @Override
    public void onPause() {
        doHouseKeepingBeforePause();
        super.onPause();
        getActivity().overridePendingTransition(
                R.anim.slide_in_right,R.anim.slide_out_left
        );
    }

    void doHouseKeepingBeforePause() {
        unregisterBrodcastReceivers();
        if (mProgressBarDisplayTask !=null){
            mProgressBarDisplayTask.cancel(true);
            mProgressBarDisplayTask = null;
        }
        if (mInitDataLoadingReRequest !=null){
            mInitDataLoadingReRequest.cancel(true);
            mInitDataLoadingReRequest = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d("StackTrace", "onDestroy: Feature: "+mFeature.getTitle()+" NP: "+mNewspaper.getName());
    }

    private void exit() {
        getActivity().finish();
    }


    private void handleEditionLoadBrodcastMessage(Intent intent) {

        if (intent!=null) {

            int receivedFeatureId = EditionLoaderBase.getBrodcastedFeatureId(intent);

            if (receivedFeatureId == mFeature.getId()) {

                if (mInitLoadingProgressBarHolder.getVisibility() == View.VISIBLE){
                    mInitLoadingProgressBarHolder.setVisibility(View.GONE);
                }
                hideLoadingProgressBar();

                switch (EditionLoaderBase.getBroadcastStatus(intent)){
                    case SUCCESS:
                        if (mRecyclerView.getAdapter() == null){
                            initDisplay();
                        } else {
                            ArrayList<Integer> newArticleIdList = ArticleHelper.findArticleIdsForFeature(mFeature);
                            if (newArticleIdList.size() > mArticleIdList.size()){
                                ArrayList<Integer> tempList = new ArrayList<>();
                                for (int i = 0; i <= mArticleIdList.indexOf(mLastBoundArticleId); i++) {
                                    tempList.add(mArticleIdList.get(i));
                                }
                                mArticleIdList.clear();
                                mArticleIdList.addAll(tempList);

                                for (int articleId:
                                        newArticleIdList){
                                    if (!mArticleIdList.contains(articleId)){
                                        mArticleIdList.add(articleId);
                                    }
                                }
                                mRecyclerView.getAdapter().notifyItemChanged(mArticleIdList.indexOf(mLastBoundArticleId)+1);
                            }
                        }
                        break;
                    case END_OF_EDITION:
                        mEndOfEditionReached = true;
                        break;
                }
                mEditionLoadRequestTsMillis = 0;
            }
        }
    }

    private void hideLoadingProgressBar() {
        if (mProgressBarDisplayTask !=null) {
            mProgressBarDisplayTask.cancel(true);
        }
    }

    private void showLoadingProgressBar() {
        new ShowProgressbarMillis().execute();
    }

    private class ShowProgressbarMillis extends AsyncTask<Void,Void,Void>{

        private long mDelayPeriodMillis;

        public ShowProgressbarMillis() {
            mDelayPeriodMillis = DATA_RELOAD_REQUEST_INTERVAL_MILLIS - 1000;
            mProgressBarDisplayTask = ShowProgressbarMillis.this;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showEditionLoadingProgressBar();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(mDelayPeriodMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideEditionLoadingProgressBar();
            mProgressBarDisplayTask = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            hideEditionLoadingProgressBar();
            mProgressBarDisplayTask = null;
        }
    }

    private void showEditionLoadingProgressBar(){
        if (mParentFeatureGroupPagerFragment == null) {
            mLoadingProgressBar.setVisibility(View.VISIBLE);
            mLoadingProgressBar.bringToFront();
        }else {
            mParentFeatureGroupPagerFragment.showEditionDataLoadingProgressBar();
        }
    }

    private void hideEditionLoadingProgressBar(){
        if (mParentFeatureGroupPagerFragment == null) {
            mLoadingProgressBar.setVisibility(View.GONE);
        }else {
            mParentFeatureGroupPagerFragment.hideEditionDataLoadingProgressBar();
        }
    }

    private void handleImageLoadBrodcastMessage(Intent intent) {

        if ((intent!=null) ) {

            if (ImageDownloader.getImageDownloadStatus(intent)) {

                int receivedImageId = ImageDownloader.getBrodcastedImageId(intent);
                ArrayList<ArticlePreviewHolder> currentArticlePreviewHolders =
                        new ArrayList<>();

                ImageData imageData = ImageDataHelper.findImageDataById(receivedImageId);

                for (ArticlePreviewHolder articlePreviewHolder : mArticlePreviewHolders) {
                    if (articlePreviewHolder.mArticle.getPreviewImageId() == receivedImageId) {
                        if (imageData !=null && imageData.getSizeKB()>0){
                            loadImage(
                                    articlePreviewHolder.mArticlePreviewImageView,
                                    imageData.getDiskLocation()
                            );
                        } else {
                            articlePreviewHolder.showArticlePreviewWithoutImage();
                        }
                        currentArticlePreviewHolders.add(articlePreviewHolder);
                    }
                }

                for (ArticlePreviewHolder articlePreviewHolder:
                        currentArticlePreviewHolders) {
                    if (articlePreviewHolder != null) {
                        mArticlePreviewHolders.remove(articlePreviewHolder);
                    }

                }
            }
        }

    }

    private void registerBrodcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mEditionLoadBroadcastReceiver, EditionLoaderBase.getIntentFilterForEditionDownloadBroadcastMessage());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mImageLoadBroadcastReceiver, ImageDownloader.getIntentFilterForImageDownloadBroadcastMessage());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mNetConAvailableBroadcastReceiver, NetConnectivityUtility.getIntentFilterForNetworkAvailableBroadcastReceiver());
    }
    private void unregisterBrodcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mEditionLoadBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mImageLoadBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mNetConAvailableBroadcastReceiver);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Log.d(TAG, "onActivityResult: "+mFeature.getTitle());

        if (requestCode == RESULT_CODE_FOR_AP_ACTIVITY && resultCode== Activity.RESULT_OK && data!=null){

            int intentArticleId = data.getIntExtra(ArticlePagerActivity.EXTRA_CURRENT_ARTICLE_ID_FOR_RESULT_RETURN,0);
            //Log.d(TAG, "onActivityResult: intentArticleId: "+intentArticleId);
            if (intentArticleId >0){
                mOnActivityResult = true;
                if (!mArticleIdList.contains(intentArticleId)){
                    //Log.d(TAG, "onActivityResult: !mArticleIdList.contains(intentArticleId)");
                    int currentArticleIdCount = mArticleIdList.size();
                    ArrayList<Integer> newArticleIdList = ArticleHelper.findArticleIdsForFeature(mFeature);
                    if (newArticleIdList.size()>mArticleIdList.size()){
                        mArticleIdList.clear();
                        mArticleIdList.addAll(newArticleIdList);
                        mRecyclerView.getAdapter().notifyItemChanged(mArticleIdList.indexOf(currentArticleIdCount));
                    }
                }
                //Log.d(TAG, "onActivityResult: scrollToPosition: "+mArticleIdList.indexOf(intentArticleId));
                new Handler(Looper.myLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.scrollToPosition(mArticleIdList.indexOf(intentArticleId));
                    }
                });
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


        inflater.inflate(R.menu.menu_layout_single_feature_article_list, menu);

        MenuItem pinToAppHomeItem = menu.findItem(R.id.add_on_app_home_menu_item);
        MenuItem pinToNewspaperHomeItem = menu.findItem(R.id.add_on_newspaper_home_menu_item);
        MenuItem addToFavouritesItem = menu.findItem(R.id.add_to_favourites_menu_item);
        MenuItem removeFromFavouritesItem = menu.findItem(R.id.remove_from_favourites_menu_item);

        if (mParentFeatureGroupPagerFragment == null) {

            pinToNewspaperHomeItem.setTitle("Pin to "+mNewspaper.getName()+" "+NewspaperHelper.getNewspaperHomePageTitle(mNewspaper));

            if (FeatureGroupHelper.checkIfOnHomeGroup(mFeature)) {
                pinToAppHomeItem.setVisible(false);
            }
            if (FeatureGroupHelper.checkIfOnNewspaperHomeGroup(mFeature)) {
                pinToNewspaperHomeItem.setVisible(false);
            }
        } else {
            addToFavouritesItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            removeFromFavouritesItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            pinToAppHomeItem.setVisible(false);
            pinToNewspaperHomeItem.setVisible(false);
        }

        mFeature = FeatureHelper.findFeatureById(mFeature.getId());
        sFeature = mFeature;
        sNewspaper = NewspaperHelper.findNewspaperById(sFeature.getNewsPaperId());
        if (mFeature.isFavourite()) {
            addToFavouritesItem.setVisible(false);
        }else {
            removeFromFavouritesItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.add_on_app_home_menu_item:
                addToAppHomeAction();
                return true;
            case R.id.add_on_newspaper_home_menu_item:
                addToNewspaperHomeAction();
                return true;
            case R.id.add_to_favourites_menu_item:
                addToFavouritesAction();
                return true;
            case R.id.remove_from_favourites_menu_item:
                removeFromfavouritesAction();
                return true;
        }
        return false;
    }

    private void removeFromfavouritesAction() {

        new AlertDialog.Builder(getActivity())
                .setMessage("Remove \""+sFeature.getTitle()+" | "+sNewspaper.getName()+"\" from favourites?")
                .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                    if (FeatureHelper.removeFeatureFromFavourites(sFeature)) {
                        getActivity().invalidateOptionsMenu();
                        DisplayUtility.showShortToast
                                ("\""+sFeature.getTitle()+" | "+sNewspaper.getName()+"\" removed from favourites.");
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    private void addToFavouritesAction() {

        new AlertDialog.Builder(getActivity())
                .setMessage("Add \""+sFeature.getTitle()+" | "+sNewspaper.getName()+"\" to favourites?")
                .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                    if (FeatureHelper.addFeatureToFavourites(sFeature)) {
                        getActivity().invalidateOptionsMenu();
                        DisplayUtility.showShortToast(
                            "\""+sFeature.getTitle()+" | "+sNewspaper.getName()+"\" added to favourites."
                        );
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    private void addToNewspaperHomeAction() {

        new AlertDialog.Builder(getActivity())
                .setMessage("Pin \""+sFeature.getTitle()+"\" on "+sNewspaper.getName()+" "+
                        NewspaperHelper.getNewspaperHomePageTitle(sNewspaper)+
                        "?")
                .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                    if (FeatureGroupEntryHelper.addFeatureToNewspaperHome(sFeature,sNewspaper)) {
                        getActivity().invalidateOptionsMenu();
                        DisplayUtility.showShortToast(
                            "\""+sFeature.getTitle()+"\" pinned on "+sNewspaper.getName()+" "+
                                    NewspaperHelper.getNewspaperHomePageTitle(sNewspaper)+
                                    "."
                        );
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    private void addToAppHomeAction() {

        new AlertDialog.Builder(getActivity())
                .setMessage("Pin \""+sFeature.getTitle()+"\" on app Home Page?")
                .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                    if(FeatureGroupEntryHelper.addFeatureToAppHome(sFeature)) {
                        getActivity().invalidateOptionsMenu();
                        DisplayUtility.showShortToast("\""+sFeature.getTitle()+"\" pinned on app Home Page.");
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }
}

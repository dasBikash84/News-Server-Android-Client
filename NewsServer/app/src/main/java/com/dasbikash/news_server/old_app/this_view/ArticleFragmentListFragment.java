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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderHelper;
import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderService;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.image_downloader.ImageDownloader;
import com.dasbikash.news_server.old_app.this_data.article.Article;
import com.dasbikash.news_server.old_app.this_data.article.ArticleHelper;
import com.dasbikash.news_server.old_app.this_data.article_fragment.ArticleFragment;
import com.dasbikash.news_server.old_app.this_data.article_fragment.ArticleFragmentHelper;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageData;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageDataHelper;
import com.dasbikash.news_server.old_app.this_data.language.Language;
import com.dasbikash.news_server.old_app.this_data.language.LanguageHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_data.text_data.TextData;
import com.dasbikash.news_server.old_app.this_utility.SettingsUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;
import com.dasbikash.news_server.old_app.this_utility.NetConnectivityUtility;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("ALL")
public class ArticleFragmentListFragment extends Fragment {

    private static final String TAG = "SettingsUtility";
    private static final String ARG_CURRENT_ARTICLE =
            "com.dasbikash.prothomalofeatures.ArticleFragmentListFragment.current_article_id";
    private static final long MAX_IMAGE_SIZE_BYTE = 1024 * 1024;
    private static final String ENABLE_IMAGE_DOWNLOADING_ON_DATA_NETWORK_PROMPT = "Enable image downloading on mobile data network?";
    private static final String  IMAGE_DOWNLOADING_ON_DATA_NETWORK_ENABLED_MSG = "Image downloading enabled on mobile data network.";
    private static final String  IMAGE_DOWNLOADING_SETTING_CHANGE_FAILURE_MSG = "Error occurred! Please retry.";
    private static final String CHANGE_ARTICLE_TEXT_SIZE_PROMPT = "Select desired text size and hit ok.";

    private int mArticleTextFontSize;
    {
        mArticleTextFontSize = SettingsUtility.getArticleTextFontSizeSettingValue();
        if (mArticleTextFontSize == 0){
            mArticleTextFontSize = NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_REGULAR;
        }
        //Log.d(TAG, "mArticleTextFontSize:"+mArticleTextFontSize);
    }

    //private int mHolderIdCount=0;

    Article mArticle;
    private Feature mFeature;
    private Newspaper mNewspaper;
    private Country mCountry;
    private Language mLanguage;


    private RecyclerView mRecyclerView;
    private TextView mArticleParentTreeTextView;
    private TextView mArticleDetailsTextView;
    private TextView mArticlePositionTextView;
    private TextView mArticlePublicationDateTextView;
    private ProgressBar mLoadingProgressBar;
    private LinearLayout mLoadingProgressBarHolder;
    private ViewPager mParentViewPager;

    private List<ArticleFragment> mArticleFragmentList;

    private ArticleAdapter mArticleAdapter;

    private List<ArticleFragmentHolder> mArticleFragmentHolders = new ArrayList<>();

    private boolean mViewInitiated = false;
    private boolean mArticleDownloadrequestPending = false;
    private CallBacks mCallBacks;

    private BroadcastReceiver mArticleLoaderBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent !=null){
                handleArticleDownloadBroadcastMessage(context,intent);
            }
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
    private int mImageNotDownloadedCount=0;
    //private int mLastBountArticleFragmentIndex = -1;

    private void handleNetConAvailableBroadcast(Intent intent) {
        if (mArticleDownloadrequestPending){
            mArticleDownloadrequestPending = false;
            placeArticleDownloadRequest();
        } else{
            refreshRelyclerView();
        }
    }
    public interface CallBacks{
        boolean isEndOfEditionReached();
        int getArticleIdPositionInList(int articleId);
        int getTotalArticleCount();
        void registerIntoArticleFragmentListFragmentList(
                Article article,ArticleFragmentListFragment articleFragmentListFragment);
        void deregisterFromArticleFragmentListFragmentList(Article article);
        boolean displayingSavedArticles();
    }


    public static ArticleFragmentListFragment newInstance(int articleId){

        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT_ARTICLE, articleId);
        ArticleFragmentListFragment fragment = new ArticleFragmentListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBacks = (CallBacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_holder,container,false);
        mParentViewPager = (ViewPager)container;
        return view;
    }

    private boolean checkIfOnScreen(){
        return mCallBacks.getArticleIdPositionInList(mArticle.getId()) ==
                mParentViewPager.getCurrentItem();
    }

    private void setArticleDetailsTextview() {
        mArticleDetailsTextView.setText(mArticle.getTitle());
    }
    void setArticlePositionText(){
        String positionText =
                (mCallBacks.getArticleIdPositionInList(mArticle.getId())+1) +
                        "/"+
                        mCallBacks.getTotalArticleCount();
        if (checkIfBangPaper()){
            positionText = DisplayUtility.englishToBanglaDateString(positionText);
        }
        mArticlePositionTextView.setText(positionText+
                ((mCallBacks.isEndOfEditionReached()|| mCallBacks.displayingSavedArticles())? "":"+")
        );
    }

    private boolean checkIfBangPaper() {
        return mLanguage!=null && mLanguage.getName().matches("Bangla.+?");
    }

    private void setArticlePublicationDateText() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.display_date_format_long));
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(
                TimeZone.getDefault().getID()
        ));

        Calendar articlePublicationDate = simpleDateFormat.getCalendar();
        String dateString = "";

        if (mArticle.getLastModificationTS() !=0L) {

            articlePublicationDate.setTimeInMillis(mArticle.getLastModificationTS());
            dateString = simpleDateFormat.format(articlePublicationDate.getTime());

        } else if (mArticle.getPublicationTS() !=0L) {
            articlePublicationDate.setTimeInMillis(mArticle.getPublicationTS());
            dateString = simpleDateFormat.format(articlePublicationDate.getTime());
        }

        if (dateString.length()>0){
            if (checkIfBangPaper()){
                dateString = DisplayUtility.englishToBanglaDateString(dateString);
            }
            mArticlePublicationDateTextView.setText(dateString);
            mArticlePublicationDateTextView.setVisibility(View.VISIBLE);
        } else {
            mArticlePublicationDateTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void setArticleTitleView(){
        setArticleParentTreeText();
        setArticleDetailsTextview();
        setArticlePublicationDateText();
        setArticlePositionText();
    }

    private void setArticleParentTreeText() {
        if (mCallBacks.displayingSavedArticles()){
            Feature parentFeature = null;
            if (mFeature.getParentFeatureId() != NewsServerDBSchema.NULL_PARENT_FEATURE_ID) {
                parentFeature = FeatureHelper.findFeatureById(mFeature.getParentFeatureId());
            }
            mArticleParentTreeTextView.setText(
                    mFeature.getTitle()+
                    (parentFeature != null? (" | "+parentFeature.getTitle()):"")+
                    " | "+mNewspaper.getName()
            );
            mArticleParentTreeTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        int articleId=0;
        if (getArguments()!= null && !getArguments().isEmpty()){
            articleId = getArguments().getInt(ARG_CURRENT_ARTICLE);
        } else {
            //Log.d(TAG, "onViewCreated: getArguments()== null || getArguments().isEmpty()");
            exit();
        }

        mArticle = ArticleHelper.findArticleById(articleId);
        if (mArticle == null) {
            //Log.d(TAG, "onViewCreated: mArticle == null");
            exit();
        }

        setDataVars();

        mCallBacks.registerIntoArticleFragmentListFragmentList(
                mArticle,ArticleFragmentListFragment.this
        );

        mArticleParentTreeTextView = view.findViewById(R.id.article_parent_tree_textview);
        mArticleDetailsTextView = view.findViewById(R.id.article_details_textview);
        mArticlePublicationDateTextView = view.findViewById(R.id.article_publication_date_text);
        mArticlePositionTextView = view.findViewById(R.id.article_position_text);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mLoadingProgressBar = view.findViewById(R.id.progress_bar_view);
        mLoadingProgressBarHolder = view.findViewById(R.id.article_loading_progress_bar_holder);
        //mArticlePositionTextView.bringToFront();
        mRecyclerView.bringToFront();

        setArticleTitleView();
    }

    private void setDataVars() {
        mFeature = FeatureHelper.findActiveFeatureById(mArticle.getFeatureId());
        if (mFeature == null) {
            //Log.d(TAG, "onViewCreated: mFeature == null");
            exit();
        }

        FeatureHelper.incrementArticleReadCount(mFeature);

        mNewspaper = NewspaperHelper.findNewspaperById(mFeature.getNewsPaperId());
        if (mNewspaper == null) {
            //Log.d(TAG, "onViewCreated: mNewspaper == null");
            exit();
        }

        mCountry = CountryHelper.findCountryByName(mNewspaper.getCountryName());
        if (mCountry == null) exit();

        mLanguage = LanguageHelper.findLanguageForNewspaper(mNewspaper);
        if (mLanguage == null) exit();
    }

    private void exit() {
        getActivity().finish();
    }

    void loadArticleData(){

        //Log.d(TAG, "loadArticleData: mArticle.getTitle():"+mArticle.getTitle());

        if (mArticleFragmentList == null){
            //Log.d(TAG, "loadArticleData: mArticleFragmentList == null mArticle.getTitle():"+mArticle.getTitle());
            mArticleFragmentList = ArticleFragmentHelper.findFragmentsForArticle(mArticle);
        }
        if (mArticleFragmentList != null){
            //Log.d(TAG, "loadArticleData: mArticleFragmentList != null mArticle.getTitle():"+mArticle.getTitle());
            initViewLoader();
        } else {
            //Log.d(TAG, "loadArticleData: mArticleFragmentList == null mArticle.getTitle():"+mArticle.getTitle());
            mRecyclerView.setVisibility(View.GONE);
            mLoadingProgressBarHolder.setVisibility(View.VISIBLE);
            placeArticleDownloadRequest();
        }
    }

    private void placeArticleDownloadRequest() {
        if (checkIfOnScreen()){
            if (!ArticleLoaderHelper.placeArticleDownloadRequestWithHighPriority(mArticle,mNewspaper)){
                mArticleDownloadrequestPending = true;
            }
        } else {
            if(!ArticleLoaderHelper.placeArticleDownloadRequest(mArticle,mNewspaper)){
                mArticleDownloadrequestPending = true;
            }
        }
    }

    private void initViewLoader(){
        mImageNotDownloadedCount = 0;
        //initAdDisplay();
        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(new ArticleAdapter());
            mRecyclerView.setVisibility(View.VISIBLE);
            mLoadingProgressBarHolder.setVisibility(View.GONE);
        } else {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private int mAdCount=0;
    private ArrayList<Integer> mAdPositions = new ArrayList<>();

    private class ArticleAdapter extends RecyclerView.Adapter<ArticleFragmentHolder>{
        @SuppressWarnings("WeakerAccess")
        public ArticleAdapter() {
            mAdPositions.clear();
            switch (mArticleFragmentList.size()){
                case 0:
                    break;
                case 1:
                    mAdCount = 1;
                    mAdPositions.add(1);
                    break;
                case 2:
                case 3:
                    mAdCount = 2;
                    mAdPositions.add(1);
                    mAdPositions.add(3);
                    break;
                case 4:
                case 5:
                    mAdCount = 2;
                    mAdPositions.add(2);
                    mAdPositions.add(4);
                    break;
                case 6:
                case 7:
                case 8:
                    mAdCount = 2;
                    mAdPositions.add(2);
                    mAdPositions.add(6);
                    break;
                case 9:
                case 10:
                case 11:
                    mAdCount = 2;
                    mAdPositions.add(2);
                    mAdPositions.add(7);
                    break;
                default:
                    mAdCount = 3;
                    mAdPositions.add(2);
                    mAdPositions.add(7);
                    mAdPositions.add(12);
                    break;
            }
        }

        @NonNull
        @Override
        public ArticleFragmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ArticleFragmentHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ArticleFragmentHolder articleFragmentHolder, int position) {
            if (mAdPositions.contains(position)){
                articleFragmentHolder.bind(null,mAdPositions.indexOf(position));
            }else {

                switch (mAdPositions.size()){
                    case 1:
                        if (position<mAdPositions.get(0)){
                            articleFragmentHolder.bind(mArticleFragmentList.get(position),0);
                        } else {
                            articleFragmentHolder.bind(mArticleFragmentList.get(position-1),0);
                        }
                        break;
                    case 2:
                        if (position<mAdPositions.get(0)){
                            articleFragmentHolder.bind(mArticleFragmentList.get(position),0);
                        } else if (position<mAdPositions.get(1)){
                            articleFragmentHolder.bind(mArticleFragmentList.get(position-1),0);
                        } else {
                            articleFragmentHolder.bind(mArticleFragmentList.get(position-2),0);
                        }
                        break;
                    case 3:
                        if (position<mAdPositions.get(0)){
                            articleFragmentHolder.bind(mArticleFragmentList.get(position),0);
                        } else if (position<mAdPositions.get(1)){
                            articleFragmentHolder.bind(mArticleFragmentList.get(position-1),0);
                        }else if (position<mAdPositions.get(2)){
                            articleFragmentHolder.bind(mArticleFragmentList.get(position-2),0);
                        } else {
                            articleFragmentHolder.bind(mArticleFragmentList.get(position-3),0);
                        }
                        break;
                }
            }
        }
        @Override
        public int getItemCount() {
            return mArticleFragmentList.size()+ mAdCount;
        }

    }
    private class ArticleFragmentHolder extends RecyclerView.ViewHolder
            implements  View.OnClickListener{

        private ImageView mArticleFragmentImage;
        private TextView mArticleFragmentImageAltText;
        private TextView mArticleFragmentText;
        private ProgressBar mImageProgressBar;
        private ConstraintLayout mArticleFragmentImageBlock;

        private TextView mDlImageAnywayView;
        private TextView mChangeSettingsView;

        private ArticleFragment mArticleFragment;
        private TextData mTextData;
        private ImageData mImageData;

        /*private AdView mAdView1;
        private AdView mAdView2;
        private AdView mAdView3;*/

        public ArticleFragmentHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {

            super(inflater.inflate(R.layout.fragment_article_fragment,parent,false));

            mArticleFragmentImage = itemView.findViewById(R.id.article_fragment_imageview);
            mArticleFragmentImageAltText = itemView.findViewById(R.id.article_fragment_image_alt_text);
            mArticleFragmentText = itemView.findViewById(R.id.article_fragment_textview);
            mImageProgressBar = itemView.findViewById(R.id.fragment_image_loading_progress_bar);
            mDlImageAnywayView = itemView.findViewById(R.id.dl_article_fragment_image_anyway);
            mArticleFragmentImageBlock = itemView.findViewById(R.id.article_fragment_image_block);
            mChangeSettingsView = itemView.findViewById(R.id.change_settings_button);

            /*mAdView1 = itemView.findViewById(R.id.article_adview1);
            mAdView2 = itemView.findViewById(R.id.article_adview2);
            mAdView3 = itemView.findViewById(R.id.article_adview3);*/

            mDlImageAnywayView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImageDownloader.placeUrgentFileDownloadRequest(mImageData.getId(),mNewspaper);
                    mImageNotDownloadedCount--;
                    mArticleFragmentHolders.add(ArticleFragmentHolder.this);
                    mDlImageAnywayView.setVisibility(View.GONE);
                    mChangeSettingsView.setVisibility(View.GONE);
                    mImageProgressBar.setVisibility(View.VISIBLE);
                    //Log.d(TAG, "onClick: mImageNotDownloadedCount: "+ mImageNotDownloadedCount);
                }
            });
            mChangeSettingsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(ENABLE_IMAGE_DOWNLOADING_ON_DATA_NETWORK_PROMPT)
                            .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                                if(SettingsUtility.enableImageDownloadOnDN()) {
                                    DisplayUtility.showShortToast(IMAGE_DOWNLOADING_ON_DATA_NETWORK_ENABLED_MSG);
                                    new Handler(Looper.getMainLooper()).postAtTime(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadArticleData();
                                        }
                                    },2000L);
                                } else {
                                    DisplayUtility.showShortToast(IMAGE_DOWNLOADING_SETTING_CHANGE_FAILURE_MSG);
                                }
                            })
                            .setNegativeButton("No", null)
                            .create()
                            .show();
                    //NewsServerUtility.settingsMenuItemAction(getActivity());
                }
            });

            //mHolderIdCount++;
            mArticleFragmentImage.setOnClickListener(this);
        }

        void bind(final ArticleFragment articleFragment,int adPosition){

            if (articleFragment !=null) {

                if (mArticleFragmentHolders.contains(ArticleFragmentHolder.this)) {
                    mArticleFragmentHolders.remove(ArticleFragmentHolder.this);
                }

                /*mAdView1.setVisibility(View.GONE);
                mAdView2.setVisibility(View.GONE);
                mAdView3.setVisibility(View.GONE);*/

                mArticleFragmentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mArticleTextFontSize);
                mArticleFragmentImageAltText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mArticleTextFontSize);

                mArticleFragment = articleFragment;

                mImageData = articleFragment.getImageData();
                mTextData = articleFragment.getTextData();

                mArticleFragmentImageBlock.setVisibility(View.VISIBLE);
                mArticleFragmentText.setVisibility(View.GONE);
                mArticleFragmentImage.setVisibility(View.GONE);
                mDlImageAnywayView.setVisibility(View.GONE);
                mChangeSettingsView.setVisibility(View.GONE);
                mArticleFragmentImageAltText.setVisibility(View.GONE);
                mImageProgressBar.setVisibility(View.VISIBLE);

                if (mTextData != null && mTextData.getContent().length() > 0) {
                    DisplayUtility.displayHtmlText(mArticleFragmentText, mTextData.getContent().trim());
                    mArticleFragmentText.setVisibility(View.VISIBLE);
                }

                if (mImageData != null) {
                    mImageData = ImageDataHelper.findImageDataById(mImageData.getId());
                } else {
                    hideImageBlock();
                }

                if (mImageData != null) {

                    if (mImageData.getAltText() != null && mImageData.getAltText().trim().length() > 1) {
                        mArticleFragmentImageAltText.setText(mImageData.getAltText().trim());
                        mArticleFragmentImageAltText.setVisibility(View.VISIBLE);
                    }

                    if (mImageData.getDiskLocation() != null && mImageData.getSizeKB() > 0) {
                        displayImage();
                    } else {

                        switch (ImageDownloader.placeFileDownloadRequest(mImageData.getId(), mNewspaper)) {
                            case INVALID_IMAGE_PARAM:
                                hideImageBlock();
                                break;
                            case CANT_DL_ON_DATA_NETWORK:
                                mImageNotDownloadedCount++;

                                //Log.d(TAG, "bind: mImageNotDownloadedCount: "+ mImageNotDownloadedCount);
                                if (checkIfBangPaper()) {
                                    mChangeSettingsView.setText(getActivity().getResources().
                                            getString(R.string.image_dl_change_settings_button_text_ban));
                                }
                                String text = ImageDataHelper.getManualImageDownloadPrompt();
                                if (text.length() > 0) {
                                    if (checkIfBangPaper()) {
                                        mDlImageAnywayView.setText(getActivity().getResources().
                                                getString(R.string.image_dl_disabled_on_data_net_message2_ban));
                                    } else {
                                        mDlImageAnywayView.setText(text);
                                    }
                                } else if (checkIfBangPaper()) {
                                    mDlImageAnywayView.setText(getActivity().getResources().
                                            getString(R.string.image_dl_disabled_on_data_net_message_bng));
                                }
                                mDlImageAnywayView.setVisibility(View.VISIBLE);
                                mChangeSettingsView.setVisibility(View.VISIBLE);
                                mImageProgressBar.setVisibility(View.GONE);
                                break;
                            case ALREADY_DOWNLOADED:
                                mImageData = ImageDataHelper.findImageDataById(mImageData.getId());
                                displayImage();
                                break;
                            case NO_NETWORK_CONNECTION:
                                mImageNotDownloadedCount++;
                                hideImageBlock();
                                break;
                            default:
                                mArticleFragmentHolders.add(ArticleFragmentHolder.this);
                                break;
                        }
                    }
                }
            }else {
                mArticleFragmentImageBlock.setVisibility(View.GONE);
                mArticleFragmentImageAltText.setVisibility(View.GONE);
                mArticleFragmentText.setVisibility(View.GONE);
                /*mAdView1.setVisibility(View.GONE);
                mAdView2.setVisibility(View.GONE);
                mAdView3.setVisibility(View.GONE);

                if (adPosition == 0){
                    mAdView1.setVisibility(View.VISIBLE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView1.loadAd(adRequest);
                } else if (adPosition == 1){
                    mAdView2.setVisibility(View.VISIBLE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView2.loadAd(adRequest);
                } else if (adPosition == 2){
                    mAdView3.setVisibility(View.VISIBLE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView3.loadAd(adRequest);
                }*/
            }

            if (mArticleFragmentList.indexOf(articleFragment) ==
                    mArticleFragmentList.size()-1){
                itemView.setPadding(0,0,0,
                        mArticleDetailsTextView.getHeight()+mArticlePublicationDateTextView.getHeight()+
                                (mArticleParentTreeTextView.getVisibility() == View.GONE ? 0 : mArticleParentTreeTextView.getHeight())
                );
            } else {
                itemView.setPadding(0,0,0,0);
            }
        }

        private void displayImage() {
            File imageFile = new File(mImageData.getDiskLocation());
            if (imageFile.length()>0 && imageFile.length()<MAX_IMAGE_SIZE_BYTE) {
                mArticleFragmentImage.setImageBitmap(BitmapFactory.decodeFile(mImageData.getDiskLocation()));

                mArticleFragmentImage.setVisibility(View.VISIBLE);
                mArticleFragmentImageBlock.setVisibility(View.VISIBLE);
                mDlImageAnywayView.setVisibility(View.GONE);
                mChangeSettingsView.setVisibility(View.GONE);
                mImageProgressBar.setVisibility(View.GONE);
            }else {
                hideImageBlock();
            }
        }

        private void hideImageBlock() {
            mArticleFragmentImageBlock.setVisibility(View.GONE);
            mArticleFragmentImageAltText.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = ArticleImageListActivity.newIntent(getActivity(),mImageData.getId(),mArticle);
            startActivity(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBrodcastReceivers();
        mArticleDownloadrequestPending = false;
        getActivity().overridePendingTransition(
                android.R.anim.slide_in_left,android.R.anim.slide_out_right
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallBacks.deregisterFromArticleFragmentListFragmentList(mArticle);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBrodcastReceivers();
        loadArticleData();
    }

    private void registerBrodcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mArticleLoaderBroadcastReceiver, ArticleLoaderService.getIntentFilterForArticleLoaderBroadcastMessage());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mImageLoadBroadcastReceiver, ImageDownloader.getIntentFilterForImageDownloadBroadcastMessage());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mNetConAvailableBroadcastReceiver, NetConnectivityUtility.getIntentFilterForNetworkAvailableBroadcastReceiver());
    }
    private void unregisterBrodcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mArticleLoaderBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mImageLoadBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mNetConAvailableBroadcastReceiver);
    }

    private void handleArticleDownloadBroadcastMessage(Context context, Intent intent) {

        if(intent== null) return;
        //Log.d(TAG, "handleArticleDownloadBroadcastMessage: mArticle.title: "+mArticle.getTitle());
        int receivedArticleId = ArticleLoaderService.getBrodcastedArticleId(intent);

        if (receivedArticleId == mArticle.getId()){

            mArticleDownloadrequestPending = false;

            Article article = ArticleHelper.findArticleById(mArticle.getId());
            if (article.getLastModificationTS() !=0) {
                mArticle = article;
                setArticlePublicationDateText();
            }

            if (ArticleLoaderService.getArticleDownloadStatus(intent)){
                //Log.d(TAG, "handleArticleDownloadBroadcastMessage: Now going to load mArticle.getTitle():"+mArticle.getTitle());
                loadArticleData();
            } else {
                mArticleDownloadrequestPending = true;
            }
        }
    }
    private void handleImageLoadBrodcastMessage(Intent intent) {

        if ((intent!=null)) {

            int receivedImageId = ImageDownloader.getBrodcastedImageId(intent);

            for (ArticleFragmentHolder articleFragmentHolder :
                    mArticleFragmentHolders) {
                if (articleFragmentHolder.mImageData.getId() == receivedImageId) {
                    if (ImageDownloader.getImageDownloadStatus(intent)) {
                        articleFragmentHolder.mImageData =
                                ImageDataHelper.findImageDataById(receivedImageId);
                        articleFragmentHolder.displayImage();
                    }else {
                        articleFragmentHolder.hideImageBlock();
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().invalidateOptionsMenu();

        inflater.inflate(R.menu.menu_layout_article_view, menu);

        MenuItem saveLocallyMenuItem = menu.findItem(R.id.save_article_locally_menu_item);
        MenuItem deleteFromLocalMenuItem = menu.findItem(R.id.delete_article_from_storage_menu_item);

        if (mArticle.isSavedLocally()){
            saveLocallyMenuItem.setVisible(false);
        } else {
            deleteFromLocalMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.d(TAG, "onOptionsItemSelected: mFeature.getTitle()"+featureForOptionsMenu.getTitle());

        switch (item.getItemId()){
            case R.id.save_article_locally_menu_item:
                saveArticleLocallyAction();
                return true;
            case R.id.delete_article_from_storage_menu_item:
                deleteArticleFromStorageAction();
                return true;
            case R.id.change_text_font_menu_item:
                changeArticleTextFontAction();
                return true;
        }
        return false;
    }

    private void changeArticleTextFontAction() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_change_article_text_size,null);
        RadioButton smallButton = view.findViewById(R.id.article_text_size_small_button);
        RadioButton regularButton = view.findViewById(R.id.article_text_size_regular_button);
        RadioButton largeButton = view.findViewById(R.id.article_text_size_large_button);
        RadioButton extraLargeButton = view.findViewById(R.id.article_text_size_extra_large_button);
        switch (mArticleTextFontSize){
            case NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_SMALL:
                smallButton.toggle();
                break;
            case NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_REGULAR:
                regularButton.toggle();
                break;
            case NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_LARGE:
                largeButton.toggle();
                break;
            case NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_EXTRA_LARGE:
                extraLargeButton.toggle();
                break;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle("Change Article Text Size")
                .setMessage(CHANGE_ARTICLE_TEXT_SIZE_PROMPT)
                .setView(view)
                .setPositiveButton("Ok", (DialogInterface dialogInterface, int i) -> {
                    int desired_font_size=mArticleTextFontSize;
                    if(smallButton.isChecked()){
                        desired_font_size = NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_SMALL;
                    } else if(regularButton.isChecked()){
                        desired_font_size = NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_REGULAR;
                    } else if(largeButton.isChecked()){
                        desired_font_size = NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_LARGE;
                    } else if(extraLargeButton.isChecked()){
                        desired_font_size = NewsServerDBSchema.ARTICLE_TEXT_FONT_SIZE_EXTRA_LARGE;
                    }
                    if (mArticleTextFontSize != desired_font_size){
                        mArticleTextFontSize = desired_font_size;
                        SettingsUtility.setArticleTextFontSizeSettingValue(mArticleTextFontSize);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mParentViewPager.getAdapter().notifyDataSetChanged();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void deleteArticleFromStorageAction() {
        ArticleHelper.deleteArticleFromStorage(mArticle);
        //Log.d("NSUtility", "Article deleted.");
        mArticle = ArticleHelper.findArticleById(mArticle.getId());
        getActivity().invalidateOptionsMenu();
        DisplayUtility.showShortToast(R.string.article_delete_message);
    }

    private void saveArticleLocallyAction() {
        if (mRecyclerView.getAdapter() !=null) {
            ArticleHelper.saveArticleLocally(mArticle);
            //Log.d("StackTrace", "Article Saved. mArticle.getId(): "+mArticle.getId());
            mArticle = ArticleHelper.findArticleById(mArticle.getId());
            getActivity().invalidateOptionsMenu();
            DisplayUtility.showShortToast(R.string.article_save_message);
        } else {
            DisplayUtility.showShortToast(R.string.article_save_failure_message);
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
                    mArticleFragmentHolders.clear();
                    //initAdDisplay();
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    /*if (mLastBountArticleFragmentIndex != -1){
                        mRecyclerView.scrollToPosition(mLastBountArticleFragmentIndex);
                    }*/
                }
            }
        });
    }

}

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
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderBase;
import com.dasbikash.news_server.old_app.edition_loader.EditionLoaderService;
import com.dasbikash.news_server.old_app.this_data.article.Article;
import com.dasbikash.news_server.old_app.this_data.article.ArticleHelper;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.language.Language;
import com.dasbikash.news_server.old_app.this_data.language.LanguageHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.SettingsUtility;
import com.dasbikash.news_server.old_app.this_utility.URLConnectionHelper;
import com.dasbikash.news_server.old_app.this_view.transformer_anims.DepthPageTransformer;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class ArticlePagerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    ArticleFragmentListFragment.CallBacks{

    private static final String TAG = "ArticlePagerActivity";

    private static final String EXTRA_CURRENT_FEATURE =
            "com.dasbikash.news_server.ArticlePagerActivity.CURRENT_FEATURE";
    private static final String EXTRA_CURRENT_ARTICLE_ID =
            "com.dasbikash.news_server.ArticlePagerActivity.CURRENT_ARTICLE_ID";

    static final String EXTRA_CURRENT_ARTICLE_ID_FOR_RESULT_RETURN =
            "com.dasbikash.news_server.ArticlePagerActivity.CURRENT_ARTICLE_ID_FOR_RESULT_RETURN";

    static final String EXTRA_VIEW_SAVED_ARTICLES =
            "com.dasbikash.news_server.ArticlePagerActivity.EXTRA_VIEW_SAVED_ARTICLES";
    public static final int INIT_CUR_ARTICLE_ID_VALUE = -1;
    public static final String LIST_OF_SAVED_ARTICLES_TITLE_TEXT = "List of saved articles";
    public static final String LIST_OF_LOADED_ARTICLES_TITLE_TEXT = "List of loaded articles";
    public static final String LIST_OF_LOADED_ARTICLES_TITLE_TEXT_BAN = "লোড হত্তয়া নিবন্ধের তালিকা";

    private HashMap<Integer,ArticleFragmentListFragment> mArticleFragmentListFragmentMap = new HashMap<>();
    private HashMap<Integer,NavItemHolder> mNavItemHolderMap = new HashMap<>();

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private ArrayList<Integer> mArticleIdList = new ArrayList<>();
    private RecyclerView mNavRecyclerView;
    private ViewPager mArticlePager;
    private ProgressBar mPageProgressBar;
    private FragmentStatePagerAdapter mFragmentStatePagerAdapter;

    private EditionLoadRequestTask mEditionLoadRequestTask;

    private NavItemHolder mLastClickedNavItemHolder;

    private Feature mFeature;
    private Newspaper mNewspaper;
    private Country mCountry;
    private Language mLanguage;
    private int mCurrentArticleId = INIT_CUR_ARTICLE_ID_VALUE;
    private boolean mEndOfEditionReached = false;
    //private SimpleDateFormat mSimpleDateFormat;

    private Intent mIntent;


    private final BroadcastReceiver mEditionLoadBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEditionLoadBrodcastMessage(intent);
        }
    };

    private int mArticleTitleBackground = Color.parseColor("#c1cdc1");
    private TextView mNavDrawerTitleText;
    //mArticleTitleBackground = Color.LTGRAY;

    public static Intent newIntent(Context packageContext, Feature feature, int articleId) {
        Intent intent = new Intent(packageContext, ArticlePagerActivity.class);
        intent.putExtra(EXTRA_CURRENT_FEATURE, feature);
        intent.putExtra(EXTRA_CURRENT_ARTICLE_ID, articleId);
        return intent;
    }

    public static Intent newIntentForSavedArticles(Context packageContext) {
        Intent intent = new Intent(packageContext, ArticlePagerActivity.class);
        intent.putExtra(EXTRA_VIEW_SAVED_ARTICLES, EXTRA_VIEW_SAVED_ARTICLES);
        return intent;
    }

    public void registerIntoArticleFragmentListFragmentList(
            Article article, ArticleFragmentListFragment articleFragmentListFragment){

        mArticleFragmentListFragmentMap.put(
                    article.getId(), articleFragmentListFragment);
    }

    public void deregisterFromArticleFragmentListFragmentList(
            Article article){
        mArticleFragmentListFragmentMap.remove(article.getId());
    }

    public int getArticleIdPositionInList(int articleId){
        return mArticleIdList.indexOf(articleId);
    }

    public int getTotalArticleCount(){
        return mArticleIdList.size();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else {
            getWindow().setStatusBarColor(Color.parseColor(getResources().getString(R.string.colorPrimaryDark2)));
        }

        setContentView(R.layout.activity_article_view_nav_drawer);

        mIntent = getIntent();

        if ((!mIntent.hasExtra(EXTRA_CURRENT_FEATURE) &&
                !mIntent.hasExtra(EXTRA_CURRENT_ARTICLE_ID))&&
                !displayingSavedArticles()){
            finish();
        }

        if (!displayingSavedArticles()){
            mFeature = (Feature) mIntent.getSerializableExtra(EXTRA_CURRENT_FEATURE);
            mCurrentArticleId = mIntent.getIntExtra(EXTRA_CURRENT_ARTICLE_ID,0);

            setDataVars();
            mArticleIdList = ArticleHelper.findArticleIdsForFeature(mFeature);

            if (mFeature == null || mCurrentArticleId <1) finish();
        } else {
            mArticleIdList = ArticleHelper.findSavedArticleIds();
            if (mArticleIdList.size()>0){
                mCurrentArticleId = mArticleIdList.get(0);
            }
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mArticlePager = findViewById(R.id.article_view_pager);
        mPageProgressBar = findViewById(R.id.edition_loading_progress_bar);
        mNavRecyclerView = (RecyclerView) mDrawerLayout.findViewById(R.id.nav_recycler_view);
        mNavDrawerTitleText = mDrawerLayout.findViewById(R.id.article_list_headting);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.nav_drawer_open,R.string.nav_drawer_close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPageProgressBar.setVisibility(View.GONE);

        if (displayingSavedArticles()){
            mNavDrawerTitleText.setText(LIST_OF_SAVED_ARTICLES_TITLE_TEXT);
        }else{
            if (checkIfBangPaper()) {
                mNavDrawerTitleText.setText(LIST_OF_LOADED_ARTICLES_TITLE_TEXT_BAN);
            }else {
                mNavDrawerTitleText.setText(LIST_OF_LOADED_ARTICLES_TITLE_TEXT);
            }
        }


        findViewById(R.id.nav_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeNavigationDrawer();
            }
        });

        FragmentManager fragmentManager =getSupportFragmentManager();

        mFragmentStatePagerAdapter =  new FragmentStatePagerAdapter(fragmentManager) {

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }

            @Override
            public Fragment getItem(int position) {
                if (!displayingSavedArticles() &&
                        !mEndOfEditionReached &&
                        position == (mArticleIdList.size()-1) &&
                        mFeature.getLinkVariablePartFormat()!=null){
                    placeEditionDownloadRequest();
                    //Log.d(TAG, "getItem: More article load request submitted.");
                }
                return ArticleFragmentListFragment.newInstance(mArticleIdList.get(position));
            }
            @Override
            public int getCount() {
                return mArticleIdList.size();
            }
        };

        mArticlePager.setPageTransformer(true,new DepthPageTransformer());

        mArticlePager.setAdapter(mFragmentStatePagerAdapter);

        mArticlePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }
            @Override
            public void onPageSelected(int position) {

                if (mNavItemHolderMap.containsKey(mCurrentArticleId)) {
                    //mNavItemHolderMap.get(mCurrentArticleId).unheighlightText();
                    mNavItemHolderMap.get(mCurrentArticleId).unheighlightText();
                }

                mCurrentArticleId = mArticleIdList.get(position);

                if (mNavItemHolderMap.containsKey(mCurrentArticleId)){
                    //mNavItemHolderMap.get(mCurrentArticleId).heighlightText();
                    mNavItemHolderMap.get(mCurrentArticleId).heighlightText();
                }

                mNavRecyclerView.scrollToPosition(position);

                if (mArticleFragmentListFragmentMap.containsKey(mArticleIdList.get(position))){
                    mArticleFragmentListFragmentMap.get(mArticleIdList.get(position)).loadArticleData();
                }

                //setActionBarTitle();
            }
            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        setActionBarTitle();

        if (mCurrentArticleId != INIT_CUR_ARTICLE_ID_VALUE) {

            mNavRecyclerView.setLayoutManager(new LinearLayoutManager(ArticlePagerActivity.this));
            mNavRecyclerView.setAdapter(new NavItemListAdapter());

            //if (mArticleIdList.indexOf(mCurrentArticleId)==0){
            //}
            mArticlePager.setCurrentItem(mArticleIdList.indexOf(mCurrentArticleId));

            if (SettingsUtility.getNavMenuDisplaySetting()) {
                openNavDrawer();
            }
        }
    }

    public boolean displayingSavedArticles() {
        return mIntent.hasExtra(EXTRA_VIEW_SAVED_ARTICLES);
    }


    private boolean checkIfBangPaper() {
        return mLanguage!=null && mLanguage.getName().matches("Bangla.+?");
    }

    private void setDataVars() {
        mNewspaper = NewspaperHelper.findNewspaperById(mFeature.getNewsPaperId());
        if (mNewspaper == null) finish();

        mCountry = CountryHelper.findCountryByName(mNewspaper.getCountryName());
        if (mCountry == null) finish();

        mLanguage = LanguageHelper.findLanguageForNewspaper(mNewspaper);
        if (mLanguage == null) finish();

        //mSimpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.display_date_format_long));
        //mSimpleDateFormat.setTimeZone(TimeZone.getDefault());
    }


    private void setActionBarTitle() {
        if (!displayingSavedArticles()) {
            getSupportActionBar().setTitle(
                    mFeature.getTitle() + " | " + mNewspaper.getName()
            );
        }else {
            getSupportActionBar().setTitle("Saved Articles");
        }
    }

    @Override
    public boolean isEndOfEditionReached() {
        return mEndOfEditionReached;
    }

    private class NavItemListAdapter extends RecyclerView.Adapter<NavItemHolder>{

        public NavItemListAdapter() {}

        @NonNull
        @Override
        public NavItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(ArticlePagerActivity.this);
            return new NavItemHolder(layoutInflater, parent);
        }
        @Override
        public void onBindViewHolder(@NonNull NavItemHolder navItemHolder, int position) {
            /*if (!mEndOfEditionReached &&
                    position == (mArticleIdList.size()-1) &&
                    mFeature.getLinkVariablePartFormat()!=null){
                placeEditionDownloadRequest();
            }*/
            navItemHolder.bind(mArticleIdList.get(position));
        }
        @Override
        public int getItemCount() {
            return mArticleIdList.size();
        }
    }

    private class NavItemHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener{

        private TextView mNavListItemText;
        private Article mArticle;
        //private int mArticleId = 0;

        public NavItemHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.view_article_view_page_nav_item, parent, false));
            mNavListItemText = itemView.findViewById(R.id.nav_item_text);
            itemView.setOnClickListener(this);
        }

        void bind(int currentArticleId){
            //mArticleId = currentArticleId;
            mArticle = ArticleHelper.findArticleById(currentArticleId);
            if (mArticle != null) {
                updateText(mArticle.getId() == mCurrentArticleId);
                mNavItemHolderMap.put(mArticle.getId(), NavItemHolder.this);
            }
        }

        @Override
        public void onClick(View view) {
            closeNavigationDrawer();
            mArticlePager.setCurrentItem(mArticleIdList.indexOf(mArticle.getId()));
        }

        private void updateText(boolean makeBold){
            if (mArticle != null) {

                StringBuilder textBuilder = new StringBuilder();

                if (makeBold) textBuilder.append("<strong>");

                textBuilder.append((mArticleIdList.indexOf(mArticle.getId()) + 1) + ") ");
                textBuilder.append(mArticle.getTitle());

                if (makeBold) textBuilder.append("</strong>");

                String textForDisplay = textBuilder.toString();
                if (checkIfBangPaper()){
                    DisplayUtility.displayHtmlText(mNavListItemText,DisplayUtility.englishToBanglaDateString(textForDisplay));
                }else {
                    DisplayUtility.displayHtmlText(mNavListItemText,textForDisplay);
                }
                if (makeBold){
                    //mNavListItemText.setBackgroundColor();
                    mNavListItemText.setBackgroundColor(Color.parseColor(
                            getString(R.string.light_button_background)
                    ));
                    mLastClickedNavItemHolder = NavItemHolder.this;
                }else {
                    mNavListItemText.setBackgroundColor(Color.WHITE);
                }
            }
        }

        void heighlightText(){
            updateText(true);
        }

        void unheighlightText(){
            updateText(false);
        }
    }

    private void closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void openNavDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mActionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return NewsServerUtility.handleBasicOptionMenuItemActions(item,ArticlePagerActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout_basic,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        closeNavigationDrawer();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeNavigationDrawer();
        } else {
            if (!displayingSavedArticles()) {
                setResult();
            }
            super.onBackPressed();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (!displayingSavedArticles()) {
            registerBrodcastReceivers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mEditionLoadRequestTask != null) {
            mEditionLoadRequestTask.cancel(true);
            mEditionLoadRequestTask = null;
        }
        if (!displayingSavedArticles()) {
            unregisterBrodcastReceivers();
        }
    }

    private void handleEditionLoadBrodcastMessage(Intent intent) {

        if (intent!=null) {

            int receivedFeatureId = EditionLoaderBase.getBrodcastedFeatureId(intent);

            if (receivedFeatureId == mFeature.getId()) {
                switch (EditionLoaderBase.getBroadcastStatus(intent)){
                    case SUCCESS:
                        ArrayList<Integer> integerArrayList = ArticleHelper.findArticleIdsForFeature(mFeature);

                        int currentArticleCount = mArticleIdList.size();

                        if (integerArrayList.size()>mArticleIdList.size()){

                            for (int newId :
                                    integerArrayList) {
                                if (!mArticleIdList.contains(newId)){
                                    mArticleIdList.add(newId);
                                }
                            }
                            mFragmentStatePagerAdapter.notifyDataSetChanged();
                            mNavRecyclerView.getAdapter().notifyItemChanged(currentArticleCount);
                        }

                        break;
                    case END_OF_EDITION:
                        mEndOfEditionReached = true;
                        break;
                }
                if (mEditionLoadRequestTask != null){
                    mEditionLoadRequestTask.cancel(true);
                }
            }
        }
    }

    private void registerBrodcastReceivers() {
        LocalBroadcastManager.getInstance(ArticlePagerActivity.this).
                registerReceiver(mEditionLoadBroadcastReceiver,
                                        EditionLoaderBase.
                                                getIntentFilterForEditionDownloadBroadcastMessage());
    }
    private void unregisterBrodcastReceivers() {
        LocalBroadcastManager.getInstance(ArticlePagerActivity.this).unregisterReceiver(mEditionLoadBroadcastReceiver);
    }

    private void setResult(){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CURRENT_ARTICLE_ID_FOR_RESULT_RETURN,mCurrentArticleId);
        setResult(Activity.RESULT_OK,intent);
    }

    private void placeEditionDownloadRequest(){
        if (mEditionLoadRequestTask == null){
            new EditionLoadRequestTask().execute();
        }
    }

    class EditionLoadRequestTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mEditionLoadRequestTask= EditionLoadRequestTask.this;
            mPageProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (EditionLoaderService.placeEditionDownloadRequest(mFeature)) {
                try {
                    Thread.sleep(URLConnectionHelper.CONNECTION_TIMEOUT_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mEditionLoadRequestTask = null;
            mPageProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mEditionLoadRequestTask = null;
            mPageProgressBar.setVisibility(View.GONE);
        }
    }
}

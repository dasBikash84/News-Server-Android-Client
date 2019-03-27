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
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.navigation.NavigationView;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.database.NewsServerDBOpenHelper;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.article.ArticleHelper;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.feature.Feature;
import com.dasbikash.news_server.old_app.this_data.feature.FeatureHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroup;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;
import com.dasbikash.news_server.old_app.this_utility.CheckConfigIntegrity;
import com.dasbikash.news_server.old_app.this_utility.SettingsUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;
import com.dasbikash.news_server.old_app.this_utility.display_utility.SerializableItemListDisplayCallbacks;
//import com.google.android.gms.ads.MobileAds;

import java.io.Serializable;
import java.util.ArrayList;

public class ArticleListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    ExtendedNewspaperMenuFragment.CallBacksForPageLoad,
                    WelcomeScreenFragment.CallBacks{

    public static final String EXTRA_FOR_CONFIG_INTEGRITY_RESULT_FRAGMENT =
            "ArticleListActivity.EXTRA_FOR_CONFIG_INTEGRITY_RESULT_FRAGMENT";

    private static final String TAG = "ArticleListActivity";
    //private static final String TAG = "StackTrace";

    public static final long MAX_TIME_BETWEEN_TWO_BACK_PRESSED = 2000L;
    public static final int DEFAULT_REQUEST_CODE = 1234;
    public static final int FAV_LIST_ITEM_CEILLING_FOR_RV = 6;
    public static final int NEWS_CATEGORY_DISPLAY_CEILLING_FOR_RV = 6;
    private static final int FREQUENT_LIST_ITEM_CEILLING_FOR_RV = 6;

    public static final String CLEAR_FREQUENTLY_VIWED_PAGE_LIST_PROMPT = "Clear frequently viwed page list?";
    public static final String LIST_EMPTY_TOAST_MESSAGE = "Currently list is empty!";
    private static final String NO_SAVED_ARTICLE_TOAST_MESSAGE = "No saved article found!";
    public static final String EXIT_TOAST_MESSAGE = "Press back again to exit.";
    public static final String LIST_EMPTIED_TOAST_MESSAGE = "List emptied!";
    private static final String CANCEL_CONFIG_INTEGRITY_CHECK_PROMPT = "Config integrity check running. Do you want to quit it?";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private RecyclerView mNewspaperList;

    private View mNewsCategoryList;
    private View mFavouritesList;
    private View mFrequentlyVisitedList;

    private ArrayList<Newspaper> mNewspapers = new ArrayList<>();
    private ArrayList<FeatureGroup> mNewsCategories = new ArrayList<>();

    private Serializable mCurrentlyLoadedItem;
    private FeatureGroup mHomeFeatureGroup;
    private long mLastBackPressedTime=0L;


    private TextView mAppHomeButton;
    private ImageButton mAppHomeOptionsView;
    private TextView mAppHomeCustomizeTextView;

    private TextView mNewspaperDropDownMenu;
    private ImageButton mNewspaperListOptionsView;
    private TextView mNewspaperListCustomizeTextView;

    private TextView mNewsCategoryDropDownMenu;
    private ImageButton mNewsCategoryListOptionsView;
    private TextView mNewsCategoryListCustomizeTextView;

    private TextView mFavouritesDropDownMenu;
    private TextView mFavouritesListCustomizeTextView;

    private TextView mFrequentlyVisitedDropDownMenu;
    private ImageButton mFrequentlyVisitedListOptionsView;
    private TextView mFrequentlyVisitedListCustomizeTextView;

    private TextView mViewSavedArticlesButton;
    private ImageButton mDeleteSavedArticlesOptionsView;
    private TextView mDeleteSavedArticlesOptionsTextView;

    private LinearLayout mConfigCheckReportDisplayMenu;
    private TextView mViewConfigCheckReport;
    private TextView mViewEnableConfigCheck;
    private TextView mViewDisableConfigCheck;

    private ExtendedNewspaperMenuFragment mExtendedNewspaperMenuFragment;

    private Toast mExitToast;
    private CallBackForChildFragment mCallBackForChildFragment;

    public static Intent newIntentForNotification(Context context) {
        Intent intent = new Intent(context, ArticleListActivity.class);
        intent.putExtra(EXTRA_FOR_CONFIG_INTEGRITY_RESULT_FRAGMENT,EXTRA_FOR_CONFIG_INTEGRITY_RESULT_FRAGMENT);
        return intent;
    }

    @Override
    public void loadHomePageFromWelcomeScreen() {

        refreshNavigationDrawerDisplay();
        setStatusBarColor();

        getSupportActionBar().show();

        loadAppHomePage();

        if (NewsServerDBOpenHelper.isNewDatabaseCreated()){
            openNavigationDrawer();
        }

        if ( SettingsUtility.getNavMenuDisplaySetting()) {
            openNavigationDrawer();
        }

    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(getResources().getString(R.string.colorPrimaryDark2)));
        }
    }

    interface CallBackForChildFragment{
        boolean houseKeepingOnActivityReturnWithResult();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NewsServerUtility.setAppOnForeGround(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_home_nav_drawer);
        //MobileAds.initialize(this, getString(R.string.admob_app_id));

        findMenuViewItems();
        initDrawer();

        if (NewsServerUtility.isDeveloperModeOn()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                CheckConfigIntegrity.isRunning()){
                    new AlertDialog.Builder(ArticleListActivity.this)
                            .setMessage(CANCEL_CONFIG_INTEGRITY_CHECK_PROMPT)
                            .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                                CheckConfigIntegrity.stop();
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .create()
                            .show();
            }else {
                NewsServerUtility.init(getApplicationContext());
            }
            refreshNavigationDrawerDisplay();
            setStatusBarColor();
            Intent intent = getIntent();
            if (intent !=null && intent.hasExtra(EXTRA_FOR_CONFIG_INTEGRITY_RESULT_FRAGMENT)){
                loadConfigIntegrityCheckReportFragment();
            }else {
                loadExtendedNewspaperMenu();
            }
        } else {
            getSupportActionBar().hide();

            FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager.beginTransaction().replace(
                    R.id.activity_nav_drawer_fragment,
                    new WelcomeScreenFragment()
            ).commit();
        }

        setMenuButtonClickListners();
    }

    private void loadConfigIntegrityCheckReportFragment() {

        FragmentManager fragmentManager = getSupportFragmentManager();

        ConfigIntegrityCheckReportFragment configIntegrityCheckReportFragment =
                new ConfigIntegrityCheckReportFragment();

        mCurrentlyLoadedItem = new Serializable() {};

        fragmentManager.beginTransaction().replace(
                R.id.activity_nav_drawer_fragment,
                configIntegrityCheckReportFragment
        ).commit();
        mCallBackForChildFragment = null;
    }

    private void loadExtendedNewspaperMenu() {
        //Log.d("CheckConfigIntegrity", "loadExtendedNewspaperMenu: ");
        FragmentManager fragmentManager = getSupportFragmentManager();

        ExtendedNewspaperMenuFragment extendedNewspaperMenuFragment;

        if (mCurrentlyLoadedItem == null) {
            //Log.d(TAG, "loadExtendedNewspaperMenu: (mCurrentlyLoadedItem == null)");
            extendedNewspaperMenuFragment = new ExtendedNewspaperMenuFragment();
        } else {
            //Log.d(TAG, "loadExtendedNewspaperMenu: (mCurrentlyLoadedItem != null)");
            if ((mCurrentlyLoadedItem instanceof Feature) || (mCurrentlyLoadedItem instanceof FeatureGroup)) {
                extendedNewspaperMenuFragment =
                        ExtendedNewspaperMenuFragment.newInstance(mCurrentlyLoadedItem);
            }else {
                extendedNewspaperMenuFragment = new ExtendedNewspaperMenuFragment();
            }
            mCurrentlyLoadedItem = null;
        }

        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.animator.card_flip_right_in,R.animator.card_flip_right_out)
                .replace(R.id.activity_nav_drawer_fragment,extendedNewspaperMenuFragment)
                 .commit();
        mCallBackForChildFragment = null;
    }

    private void findMenuViewItems() {

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mAppHomeButton = mDrawerLayout.findViewById(R.id.app_home_button);
        mAppHomeOptionsView = mDrawerLayout.findViewById(R.id.app_home_options);
        mAppHomeCustomizeTextView = mDrawerLayout.findViewById(R.id.app_home_customize_text_view);

        mNewspaperDropDownMenu = mDrawerLayout.findViewById(R.id.newspaper_drop_down_menu_item);
        mNewspaperListOptionsView = mDrawerLayout.findViewById(R.id.newspaper_list_options);
        mNewspaperListCustomizeTextView = mDrawerLayout.findViewById(R.id.newspaper_list_customize_text_view);
        mNewspaperList = mDrawerLayout.findViewById(R.id.newspaper_list);

        mNewsCategoryDropDownMenu = mDrawerLayout.findViewById(R.id.news_category_drop_down_menu_item);
        mNewsCategoryListOptionsView = mDrawerLayout.findViewById(R.id.cus_news_category_options);
        mNewsCategoryListCustomizeTextView = mDrawerLayout.findViewById(R.id.cus_news_category_customize_text_view);
        mNewsCategoryList = null;

        mFavouritesDropDownMenu = mDrawerLayout.findViewById(R.id.favourite_features_drop_down_menu_item);
        //mFavouritesListOptionsView = mDrawerLayout.findViewById(R.id.favourites_options);
        mFavouritesListCustomizeTextView = mDrawerLayout.findViewById(R.id.favourites_customize_text_view);
        mFavouritesList = null;


        mFrequentlyVisitedDropDownMenu = mDrawerLayout.findViewById(R.id.most_frequent_features_drop_down_menu_item);
        mFrequentlyVisitedListOptionsView = mDrawerLayout.findViewById(R.id.frequent_features_options);
        mFrequentlyVisitedListCustomizeTextView = mDrawerLayout.findViewById(R.id.frequent_features_customize_text_view);
        mFrequentlyVisitedList = null;

        mViewSavedArticlesButton = mDrawerLayout.findViewById(R.id.saved_articles_menu_item);
        mDeleteSavedArticlesOptionsTextView = mDrawerLayout.findViewById(R.id.delete_saved_articles_text_view);
        mDeleteSavedArticlesOptionsView = mDrawerLayout.findViewById(R.id.delete_saved_articles_options);

        mConfigCheckReportDisplayMenu = mDrawerLayout.findViewById(R.id.config_check_report_display_menu);

        mViewConfigCheckReport = mDrawerLayout.findViewById(R.id.display_config_check_report_fragment_menu_item);
        mViewEnableConfigCheck = mDrawerLayout.findViewById(R.id.enable_config_check_menu_item);
        mViewDisableConfigCheck = mDrawerLayout.findViewById(R.id.disable_config_check_menu_item);

        if (NewsServerUtility.isDeveloperModeOn()) {

            mConfigCheckReportDisplayMenu.setVisibility(View.VISIBLE);
            mViewConfigCheckReport.setVisibility(View.VISIBLE);
            mViewEnableConfigCheck.setVisibility(View.GONE);
            mViewDisableConfigCheck.setVisibility(View.VISIBLE);

        } else {
            mConfigCheckReportDisplayMenu.setVisibility(View.GONE);
        }
    }

    private void initDrawer() {
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,
                R.string.nav_drawer_open,
                R.string.nav_drawer_close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void hideAllDrawerMenuOptionTexts(){
        mAppHomeCustomizeTextView.setVisibility(View.GONE);
        mNewspaperListCustomizeTextView.setVisibility(View.GONE);
        mNewsCategoryListCustomizeTextView.setVisibility(View.GONE);
        mFavouritesListCustomizeTextView.setVisibility(View.GONE);
        mFrequentlyVisitedListCustomizeTextView.setVisibility(View.GONE);
        mDeleteSavedArticlesOptionsTextView.setVisibility(View.GONE);
    }

    private void hideAllRVListsExcept(View view){
        if (mNewspaperList!=null && view!=mNewspaperList) {
            mNewspaperList.setVisibility(View.GONE);
        }
        if (mNewsCategoryList!=null && view !=mNewsCategoryList) {
            mNewsCategoryList.setVisibility(View.GONE);
        }
        if (mFavouritesList != null && view !=mFavouritesList) {
            mFavouritesList.setVisibility(View.GONE);
        }
        if (mFrequentlyVisitedList != null && view !=mFrequentlyVisitedList) {
            mFrequentlyVisitedList.setVisibility(View.GONE);
        }
    }

    private void setMenuButtonClickListners() {
        mNewspaperList.setVisibility(View.GONE);

        mAppHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadAppHomePage();
                hideAllDrawerMenuOptionTexts();
            }
        });
        mAppHomeOptionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAppHomeCustomizeTextView.getVisibility() == View.GONE){
                    hideAllDrawerMenuOptionTexts();
                    mAppHomeCustomizeTextView.setVisibility(View.VISIBLE);
                    mAppHomeCustomizeTextView.bringToFront();
                } else {
                    mAppHomeCustomizeTextView.setVisibility(View.GONE);
                }
            }
        });
        mAppHomeCustomizeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editAppHomeFeatureGroupAction();
                hideAllDrawerMenuOptionTexts();
            }
        });

        mNewspaperDropDownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (mNewspaperList.getVisibility() == View.VISIBLE){
                    mNewspaperList.setVisibility(View.GONE);
                } else {*/
                if (mCurrentlyLoadedItem != null) {
                    loadExtendedNewspaperMenu();
                }
                //}
                hideAllRVListsExcept(null);
                closeNavigationDrawer();
                hideAllDrawerMenuOptionTexts();
            }
        });
        mNewspaperListOptionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNewspaperListCustomizeTextView.getVisibility() == View.GONE){
                    hideAllDrawerMenuOptionTexts();
                    mNewspaperListCustomizeTextView.setVisibility(View.VISIBLE);
                    mNewspaperListCustomizeTextView.bringToFront();
                } else {
                    mNewspaperListCustomizeTextView.setVisibility(View.GONE);
                }
            }
        });
        mNewspaperListCustomizeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customizeNewpaperMenuAction();
                hideAllDrawerMenuOptionTexts();
            }
        });

        mNewsCategoryDropDownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAllDrawerMenuOptionTexts();
                if (mNewsCategoryList.getVisibility() == View.GONE){
                    hideAllRVListsExcept(mNewsCategoryList);
                    mNewsCategoryList.setVisibility(View.VISIBLE);
                } else {
                    mNewsCategoryList.setVisibility(View.GONE);
                }
            }
        });
        mNewsCategoryListOptionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNewsCategoryListCustomizeTextView.getVisibility() == View.GONE){
                    hideAllDrawerMenuOptionTexts();
                    mNewsCategoryListCustomizeTextView.setVisibility(View.VISIBLE);
                    mNewsCategoryListCustomizeTextView.bringToFront();
                } else {
                    mNewsCategoryListCustomizeTextView.setVisibility(View.GONE);
                }
            }
        });
        mNewsCategoryListCustomizeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customizeNewsCategoryAction();
                hideAllDrawerMenuOptionTexts();
            }
        });

        mFavouritesDropDownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFavouritesList =
                    listHeaderClickAction(
                        mFavouritesList,
                        R.id.favorite_features_list,
                        R.id.favorite_features_list_rv,
                        FeatureHelper.getFavouriteFeatures(),
                        FAV_LIST_ITEM_CEILLING_FOR_RV
                );
                hideAllDrawerMenuOptionTexts();
            }
        });

        mFrequentlyVisitedDropDownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFrequentlyVisitedList =
                    listHeaderClickAction(
                            mFrequentlyVisitedList,
                            R.id.most_frequent_features_list,
                            R.id.most_frequent_features_list_rv,
                            FeatureHelper.getFrequentlyViewdFeatures(),
                            FREQUENT_LIST_ITEM_CEILLING_FOR_RV
                    );
                hideAllDrawerMenuOptionTexts();
            }
        });
        mFrequentlyVisitedListOptionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFrequentlyVisitedListCustomizeTextView.getVisibility() == View.GONE){
                    hideAllDrawerMenuOptionTexts();
                    mFrequentlyVisitedListCustomizeTextView.setVisibility(View.VISIBLE);
                    mFrequentlyVisitedListCustomizeTextView.bringToFront();
                } else {
                    mFrequentlyVisitedListCustomizeTextView.setVisibility(View.GONE);
                }
            }
        });
        mFrequentlyVisitedListCustomizeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAllDrawerMenuOptionTexts();
                clearFrequentlyVisitedListAction();
            }
        });

        mViewSavedArticlesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAllDrawerMenuOptionTexts();
                viewSavedArticlesButtonAction();
            }
        });

        mDeleteSavedArticlesOptionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeleteSavedArticlesOptionsTextView.getVisibility() == View.GONE){
                    hideAllDrawerMenuOptionTexts();
                    mDeleteSavedArticlesOptionsTextView.setVisibility(View.VISIBLE);
                    mDeleteSavedArticlesOptionsTextView.bringToFront();
                } else {
                    mDeleteSavedArticlesOptionsTextView.setVisibility(View.GONE);
                }
            }
        });
        mDeleteSavedArticlesOptionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAllDrawerMenuOptionTexts();
                SettingsUtility.deleteSavedArticlesAction(ArticleListActivity.this);
            }
        });
        findViewById(R.id.nav_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeNavigationDrawer();
            }
        });

        mViewConfigCheckReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAllDrawerMenuOptionTexts();
                loadConfigIntegrityCheckReportFragment();
                closeNavigationDrawer();
            }
        });

        mViewEnableConfigCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CheckConfigIntegrity.init(NewsServerUtility.getContext());
                    mViewDisableConfigCheck.setVisibility(View.VISIBLE);
                    mViewEnableConfigCheck.setVisibility(View.GONE);
                }
            }
        });

        mViewDisableConfigCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CheckConfigIntegrity.cancelJob();
                    mViewEnableConfigCheck.setVisibility(View.VISIBLE);
                    mViewDisableConfigCheck.setVisibility(View.GONE);
                }
            }
        });
    }

    private void viewSavedArticlesButtonAction() {
        ArrayList<Integer> savedArticleIdList =
                ArticleHelper.findSavedArticleIds();
        if (savedArticleIdList.size()>0) {
            Intent intent = ArticlePagerActivity.newIntentForSavedArticles(ArticleListActivity.this);
            startActivityForResult(intent, DEFAULT_REQUEST_CODE);
        }else {
            DisplayUtility.showShortToast(NO_SAVED_ARTICLE_TOAST_MESSAGE);
        }
    }

    private View listHeaderClickAction(View listView, @IdRes int liswViewId, @IdRes int rvId,
                                       ArrayList<Feature> featureList,int itemCellingForRV) {
        hideAllRVListsExcept(listView);
        //final View viewItem = listView;
        SerializableItemListDisplayCallbacks<Feature>
                callBacksForGenericFeatureListDisplay =
                new SerializableItemListDisplayCallbacks<Feature>() {
                    @Override
                    public int getListViewId() {
                        return liswViewId;
                    }

                    @Override
                    public int getRecyclerViewId() {
                        return rvId;
                    }

                    @Override
                    public int getIdForItemDisplay() {
                        return R.layout.layout_home_nav_drawer_secondary_menu_item;
                    }

                    @Override
                    public int getIdOfItemTextView() {
                        return R.id.menu_text_item;
                    }

                    @Override
                    public int getIdOfItemHorSeparator() {
                        return R.id.horizontal_separator_item;
                    }

                    @Override
                    public int getIdOfItemImageButton() {
                        return 0;
                    }

                    @Override
                    public int getRVDisplayThresholdCount() {
                        return itemCellingForRV;
                    }

                    @Override
                    public ArrayList<Feature> getSerializableItemListForDisplay() {
                        return featureList;
                    }

                    @Override
                    public String getTextStringForTextView(Feature feature) {

                        if (feature!=null) {

                            StringBuilder titleTextBuilder = new StringBuilder(feature.getTitle());

                            if (feature.getParentFeatureId() !=
                                    NewsServerDBSchema.NULL_PARENT_FEATURE_ID) {
                                Feature parentFeature =
                                        FeatureHelper.findFeatureById(feature.getParentFeatureId());
                                if (parentFeature != null) {
                                    titleTextBuilder.append(
                                            " | " + parentFeature.getTitle()
                                    );
                                }
                            }
                            Newspaper newspaper =
                                    NewspaperHelper.findNewspaperById(feature.getNewsPaperId());
                            if (newspaper != null) {
                                titleTextBuilder.append(
                                        " | " + newspaper.getName()
                                );
                            }
                            return titleTextBuilder.toString();
                        } else {
                            return "";
                        }
                    }

                    @Override
                    public void callBackForTextItemClickAction(Feature feature) {
                        closeNavigationDrawer();
                        //viewItem.setVisibility(View.GONE);
                        loadFeature(feature);
                    }

                    @Override
                    public void callBackForImageButtonItemClickAction(Feature serializableItem) {

                    }
                };

        if (listView == null ||
                listView.getVisibility() == View.GONE){
            if (featureList!=null &&
                    featureList.size()>0){
                listView = DisplayUtility.
                                inflateSerializableItemList(mDrawerLayout,callBacksForGenericFeatureListDisplay);
            } else {
                DisplayUtility.showShortToast(LIST_EMPTY_TOAST_MESSAGE);
            }
        } else{
            listView.setVisibility(View.GONE);
        }
        return listView;
    }

    private void clearFrequentlyVisitedListAction() {

        if (FeatureHelper.getFrequentlyViewdFeatures().size()> 0) {

            new AlertDialog.Builder(ArticleListActivity.this)
                    .setMessage(CLEAR_FREQUENTLY_VIWED_PAGE_LIST_PROMPT)
                    .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                        if(FeatureHelper.clearArticleReadCount()) {
                            if (mFrequentlyVisitedList !=null){
                                mFrequentlyVisitedList.setVisibility(View.GONE);
                            }
                            DisplayUtility.showShortToast(LIST_EMPTIED_TOAST_MESSAGE);
                        }
                    })
                    .setNegativeButton("No", null)
                    .create()
                    .show();
        } else {
            DisplayUtility.showShortToast(LIST_EMPTY_TOAST_MESSAGE);
        }
    }

    private void customizeNewpaperMenuAction() {
        Intent intent = SettingsActivity.
                newIntentForNewspaperMenuCustomization(ArticleListActivity.this);
        if (intent!=null) {
            startActivityForResult(intent,ArticleListActivity.DEFAULT_REQUEST_CODE);
        }
    }

    private void editAppHomeFeatureGroupAction() {
        Intent intent = SettingsActivity.
                newIntentForAppHomePageCustomization(ArticleListActivity.this);
        if (intent!=null) {
            startActivityForResult(intent,ArticleListActivity.DEFAULT_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //NewsServerUtility.enableNotification();
        if (!NewsServerUtility.isDeveloperModeOn() || !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                CheckConfigIntegrity.isRunning())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SettingsUtility.clearCacheData();
                }
            }).start();
        }
        NewsServerUtility.setAppOnForeGround(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void refreshNavigationDrawerDisplay() {
        mNewspapers = NewspaperHelper.getAllActiveNewspapers();
        mNewsCategories = FeatureGroupHelper.getCustomFeatureGroups();

        hideAllRVListsExcept(null);

        mNewspaperList.setLayoutManager(new LinearLayoutManager(ArticleListActivity.this));
        mNewspaperList.setAdapter(new NewspaperListRVAdapter());
        mNewspaperList.setVisibility(View.GONE);

        displayNewsCategoryList();

    }

    private void displayNewsCategoryList() {
        SerializableItemListDisplayCallbacks<FeatureGroup>
                callBacksForNewsCategoryListDisplay =
                new SerializableItemListDisplayCallbacks<FeatureGroup>() {
                    @Override
                    public int getListViewId() {
                        return R.id.news_category_list;
                    }

                    @Override
                    public int getRecyclerViewId() {
                        return R.id.news_category_list_rv;
                    }

                    @Override
                    public int getIdForItemDisplay() {
                        return R.layout.layout_home_nav_drawer_secondary_menu_item;
                    }

                    @Override
                    public int getIdOfItemTextView() {
                        return R.id.menu_text_item;
                    }

                    @Override
                    public int getIdOfItemHorSeparator() {
                        return R.id.horizontal_separator_item;
                    }

                    @Override
                    public int getIdOfItemImageButton() {
                        return 0;
                    }

                    @Override
                    public int getRVDisplayThresholdCount() {
                        return NEWS_CATEGORY_DISPLAY_CEILLING_FOR_RV;
                    }

                    @Override
                    public ArrayList<FeatureGroup> getSerializableItemListForDisplay() {
                        return mNewsCategories;
                    }

                    @Override
                    public String getTextStringForTextView(FeatureGroup featureGroup) {

                        if (featureGroup!=null) {
                            return featureGroup.getTitle();
                        } else {
                            return "";
                        }
                    }

                    @Override
                    public void callBackForTextItemClickAction(FeatureGroup featureGroup) {
                        closeNavigationDrawer();
                        loadFeatureGroup(featureGroup);
                    }

                    @Override
                    public void callBackForImageButtonItemClickAction(FeatureGroup serializableItem) {

                    }
                };
        mNewsCategoryList = DisplayUtility.
                                inflateSerializableItemList(mDrawerLayout,callBacksForNewsCategoryListDisplay);
        mNewsCategoryList.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return NewsServerUtility.handleBasicOptionMenuItemActions(item,ArticleListActivity.this);
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
        } else{
            if ((System.currentTimeMillis() - mLastBackPressedTime <
                    MAX_TIME_BETWEEN_TWO_BACK_PRESSED)){
                cancelExitToast();
                finish();

            } else {
                if (mCurrentlyLoadedItem !=null){
                    loadExtendedNewspaperMenu();
                } else {
                    mExitToast = DisplayUtility.showShortToast(EXIT_TOAST_MESSAGE);
                }
            }
            mLastBackPressedTime = System.currentTimeMillis();
        }
    }

    private void cancelExitToast() {
        new Handler(getMainLooper()).postAtTime(new Runnable() {
            @Override
            public void run() {
                if (mExitToast!=null){
                    mExitToast.cancel();
                }
            }
        }, 50);
    }

    private void loadAppHomePage() {
        //Log.d(TAG, "loadAppHomePage: ");
        if (mHomeFeatureGroup == null) {
            mHomeFeatureGroup = FeatureGroupHelper.getFeatureGroupForHomePage();
        }
        if (mHomeFeatureGroup != null) {
            loadFeatureGroup(mHomeFeatureGroup);
        }
    }

    public void loadNewspaperHomePage(Newspaper newspaper) {

        FeatureGroup newspaperHomeFeatureGroup =
                FeatureGroupHelper.getFeatureGroupForNewsPaperHomePage(newspaper);

        if (newspaperHomeFeatureGroup != null) {
            loadFeatureGroup(newspaperHomeFeatureGroup);
        }
    }

    private void loadFeatureGroup(FeatureGroup featureGroup) {

        if (featureGroup == null){
            return;
        }

        //// New Code

        if ((mCurrentlyLoadedItem instanceof FeatureGroup) &&
                ((FeatureGroup)mCurrentlyLoadedItem).getId() == featureGroup.getId()){
            closeNavigationDrawer();
            return;
        }

        StringBuilder actionBarTitle = new StringBuilder();

        switch (featureGroup.getCategoryIdentifier()){

            case NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_HOMEPAGE:
                actionBarTitle.append(getResources().getString(R.string.app_name));
                break;

            case NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP:
                actionBarTitle.append(featureGroup.getTitle());
                break;

            default:
                actionBarTitle.append(featureGroup.getTitle());
                break;
        }

        getSupportActionBar().setTitle(actionBarTitle.toString());

        FragmentManager fragmentManager = getSupportFragmentManager();

        FeatureGroupPagerFragment featureGroupPagerFragment =
                FeatureGroupPagerFragment.newInstance(featureGroup);

        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.animator.card_flip_right_in,R.animator.card_flip_right_out)
                .replace(R.id.activity_nav_drawer_fragment,featureGroupPagerFragment)
                .commit();

        mCallBackForChildFragment = (CallBackForChildFragment) featureGroupPagerFragment;

        mCurrentlyLoadedItem = featureGroup;
        //addToLoadedObjectList(featureGroup);
        closeNavigationDrawer();
    }

    public void loadFeature(Feature feature){

        if (feature.getId() <1 || feature.getLinkFormat()==null){
            return;
        }

        //// New Code

        if ((mCurrentlyLoadedItem instanceof Feature) &&
        ((Feature)mCurrentlyLoadedItem).getId() == feature.getId()){
            closeNavigationDrawer();
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        ArticleListFragment articleListFragment =
                ArticleListFragment.newInstance(feature);

        Fragment fragment = fragmentManager.findFragmentById(R.id.activity_nav_drawer_fragment);

        /*if (mCurrentlyLoadedItem == null) {
            fragmentManager.beginTransaction().
                    replace(R.id.activity_nav_drawer_fragment, articleListFragment)
                    .addToBackStack(null)
                    .commit();
        }else {*/
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.animator.card_flip_right_in,R.animator.card_flip_right_out)
                .replace(R.id.activity_nav_drawer_fragment, articleListFragment)
                .commit();
        //}

        mCallBackForChildFragment = null;

        mCurrentlyLoadedItem = feature;
        closeNavigationDrawer();
    }

    private void closeNavigationDrawer() {
        hideAllDrawerMenuOptionTexts();
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void openNavigationDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void customizeNewsCategoryAction() {
        //Log.d(TAG, "customizeNewsCategoryAction: ");
        closeNavigationDrawer();
        Intent intent = SettingsActivity.newIntentForNewsCategoryCustomization(ArticleListActivity.this);
        if (intent!=null){
            startActivityForResult(intent,DEFAULT_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("StackTrace", "onActivityResult: from activity requestCode: "+requestCode);
        if (requestCode == DEFAULT_REQUEST_CODE) {
            refreshNavigationDrawerDisplay();
            if (mCallBackForChildFragment !=null){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mCallBackForChildFragment.houseKeepingOnActivityReturnWithResult()){
                            if (mCallBackForChildFragment instanceof FeatureGroupPagerFragment) {
                                loadAppHomePage();
                            }
                        }
                    }
                });
            }
        }
    }

    private class NewspaperListRVAdapter extends RecyclerView.Adapter<NewspaperRVListItemHolder>{

        @NonNull
        @Override
        public NewspaperRVListItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(ArticleListActivity.this);
            return new NewspaperRVListItemHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull NewspaperRVListItemHolder newspaperRVListItemHolder, int position) {
            newspaperRVListItemHolder.bind(mNewspapers.get(position));
        }

        @Override
        public int getItemCount() {
            return mNewspapers.size();
        }
    }

    private NewspaperRVListItemHolder mCurrentNewspaperRVListItemHolder;
    private class NewspaperRVListItemHolder extends RecyclerView.ViewHolder{

        private ConstraintLayout mContentDropDownView;
        private TextView mNewspaperTitleText;
        private ImageButton mShowNpPagesButton;
        private ImageButton mHideNpPagesButton;
        private LinearLayout mNewsPaperContentView;
        private TextView mNewspaperHomeView;
        private RecyclerView mNewspaperFeatureListView;
        private Newspaper mNewspaper;
        //private Language mLanguage;
        private View mHorizontalSeparator;
        private Country mCountry;


        public NewspaperRVListItemHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_newspaper_menu_item, parent, false));

            mContentDropDownView = itemView.findViewById(R.id.newspaper_content_dropdown);
            mNewsPaperContentView = itemView.findViewById(R.id.newspaper_content_view);
            mHorizontalSeparator = itemView.findViewById(R.id.horizontal_separator);
            mNewspaperHomeView = mNewsPaperContentView.findViewById(R.id.newspaper_home_page_view);

            mNewspaperTitleText = itemView.findViewById(R.id.newspaper_title_text_view);
            mShowNpPagesButton = itemView.findViewById(R.id.show_np_pages_button);
            mHideNpPagesButton = itemView.findViewById(R.id.hide_np_pages_button);

            mContentDropDownView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newspaperTitleClickAction();
                }
            });

            mShowNpPagesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newspaperTitleClickAction();
                }
            });

            mHideNpPagesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newspaperTitleClickAction();
                }
            });

            mNewspaperHomeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadNewspaperHomePage(mNewspaper);
                }
            });

            mNewspaperFeatureListView = mNewsPaperContentView.findViewById(R.id.newspaper_feature_list);

        }

        void newspaperTitleClickAction() {
            if (mCurrentNewspaperRVListItemHolder !=
                    NewspaperRVListItemHolder.this){
                if (mCurrentNewspaperRVListItemHolder!=null) {
                    mCurrentNewspaperRVListItemHolder.hideContent();
                }
                mCurrentNewspaperRVListItemHolder =
                        NewspaperRVListItemHolder.this;
                showContent();
            }else {
                if (mNewsPaperContentView.getVisibility() == View.GONE) {
                    showContent();
                } else {
                    hideContent();
                }
            }
        }

        void hideContent(){
            mNewsPaperContentView.setVisibility(View.GONE);
            mShowNpPagesButton.setVisibility(View.VISIBLE);
            mHideNpPagesButton.setVisibility(View.GONE);
        }
        void showContent(){
            mNewsPaperContentView.setVisibility(View.VISIBLE);
            mShowNpPagesButton.setVisibility(View.GONE);
            mHideNpPagesButton.setVisibility(View.VISIBLE);
        }

        void bind(Newspaper newspaper){
            hideContent();
            mNewspaper = newspaper;

            if (mNewspaper == null){
                itemView.setVisibility(View.GONE);
                return;
            }

            mCountry = CountryHelper.findCountryByName(mNewspaper.getCountryName());

            if (mCountry != null){
                mNewspaperTitleText.setText(
                        mNewspaper.getName()+
                                " ("+
                                mCountry.getCountryCode()+
                                ")"
                );
            } else {
                mNewspaperTitleText.setText(mNewspaper.getName());
            }

            mNewsPaperContentView.setVisibility(View.GONE);
            mNewspaperHomeView.setText(
                    NewspaperHelper.getNewspaperHomePageTitle(mNewspaper)
            );

            ArrayList<Feature> parentFeatureList = FeatureHelper.getActiveParentFeaturesForNewspaper(mNewspaper);

            if (parentFeatureList == null){
                itemView.setVisibility(View.GONE);
                return;
            }

            mNewspaperFeatureListView.setLayoutManager(new LinearLayoutManager(ArticleListActivity.this));
            mNewspaperFeatureListView.setAdapter(new ParentFeatureListAdapter(parentFeatureList));

            if (mNewspapers.indexOf(mNewspaper) ==
                    mNewspapers.size()-1){
                mHorizontalSeparator.setVisibility(View.GONE);
            }else {
                mHorizontalSeparator.setVisibility(View.VISIBLE);
            }
        }


    }

    private class ParentFeatureListAdapter extends RecyclerView.Adapter<ParentFeatureHolder>{

        private ArrayList<Feature> mParentFeatureList;

        public ParentFeatureListAdapter(ArrayList<Feature> parentFeatureList) {
            mParentFeatureList = parentFeatureList;
        }

        @NonNull
        @Override
        public ParentFeatureHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(ArticleListActivity.this);
            return new ParentFeatureHolder(layoutInflater, parent);
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
        ArrayList<Feature> mChildFeatureList;

        public ParentFeatureHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_parent_feature_nav_item, parent, false));
            mParentFeatureTitleTextView = itemView.findViewById(R.id.title_text);
            mShowChildFeaturesView = itemView.findViewById(R.id.show_decendents_button);
            mHideChildFeaturesView = itemView.findViewById(R.id. hide_decendents_button);
            mChildrenListView = itemView.findViewById(R.id.children_feature_RV);
        }

        void bind(Feature parentFeature){

            mShowChildFeaturesView.setVisibility(View.VISIBLE);
            mHideChildFeaturesView.setVisibility(View.GONE);
            mChildrenListView.setVisibility(View.GONE);
            //hideChildrenFeatures();
            mChildFeatureList = new ArrayList<>();

            if (parentFeature !=null) {
                mParentFeature = parentFeature;
                mParentFeatureTitleTextView.setText(mParentFeature.getTitle());
                mChildFeatureList = FeatureHelper.getActiveChildFeatures(mParentFeature.getId());
                //mChildFeatureList.add(0,mParentFeature);

                mParentFeatureTitleTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.d(TAG, "onClick: ParentFeatureHolder:" + mParentFeatureTitleTextView.getText());

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

                        } else {
                            if (mParentFeature.getLinkFormat() != null) {
                                closeNavigationDrawer();
                                loadFeature(mParentFeature);
                            }
                        }
                    }
                });

                if (mChildFeatureList.size() > 0){
                    if (mParentFeature.getLinkFormat() != null) {
                        mChildFeatureList.add(0, mParentFeature);
                    }
                    mChildrenListView.setLayoutManager(new LinearLayoutManager(ArticleListActivity.this));
                    mChildrenListView.setAdapter(new ChildFeatureListAdapter(mChildFeatureList));
                    //showChildrenFeatures();//mChildrenListView.setVisibility(View.VISIBLE);

                    mShowChildFeaturesView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showChildrenFeatures();
                        }
                    });
                    mHideChildFeaturesView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hideChildrenFeatures();
                        }
                    });
                } else {
                    mHideChildFeaturesView.setVisibility(View.GONE);
                    mShowChildFeaturesView.setVisibility(View.GONE);
                }
            } else {
                itemView.setVisibility(View.GONE);
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
            LayoutInflater layoutInflater = LayoutInflater.from(ArticleListActivity.this);
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

        public ChildFeatureHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_child_feature_nav_item, parent, false));
            mChildNavListItemText = itemView.findViewById(R.id.child_nav_item_view);
            itemView.setOnClickListener(this);
        }

        void bind(Feature childFeature){
            if (childFeature !=null) {
                mChildFeature =childFeature;
                String featureTitle = "<u>"+mChildFeature.getTitle()+"</u>";
                DisplayUtility.displayHtmlText(mChildNavListItemText,featureTitle);
                //mChildNavListItemText.setText(mChildFeature.getTitle());
            } else {
                itemView.setVisibility(View.GONE);
                //mChildNavListItemText.setText("Empty Title");
            }
        }

        @Override
        public void onClick(View view) {
            //Log.d(TAG, "onClick: ChildFeatureHolder:"+ mChildNavListItemText.getText());
            closeNavigationDrawer();
            loadFeature(mChildFeature);
        }
    }
}
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
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroup;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    //private static final String TAG = "StackTrace";

    public static final String CACHE_CLEAREING_WAIT_MSG =
            "Please wait while cached data is cleared.";

    public static final String ACTION_CUSTOMIZE_NEWSPAPER_HOME_PAGE =
            "SettingsActivity.CUSTOMIZE_NEWSPAPER_HOME_PAGE";
    public static final String ACTION_CUSTOMIZE_NON_NEWSPAPER_FEATURE_GROUP =
            "SettingsActivity.CUSTOMIZE_NON_NEWSPAPER_FEATURE_GROUP";
    public static final String ACTION_CUSTOMIZE_NEWS_CATEGORIES =
            "SettingsActivity.NEWS_CATEGORIES";
    public static final String ACTION_CUSTOMIZE_NEWSPAPER_MENU =
            "SettingsActivity.NEWSPAPER_MENU";
    public static final String EXTRA_CURRENT_NEWSPAPER =
            "SettingsActivity.CURRENT_NEWSPAPER";
    public static final String EXTRA_CURRENT_FEATURE_GROUP =
            "SettingsActivity.CURRENT_FEATURE_GROUP";

    Boolean mWorkInProcess = false;

    public static Intent newIntentForNewspaperHomePageCustomization(Context packageContext, Newspaper newspaper) {
        //Log.d("StackTrace", "newIntentForNewspaperHomePageCustomization: newspaper.getName(): "+newspaper.getName());
        Intent intent = new Intent(packageContext, SettingsActivity.class);
        intent.setAction(ACTION_CUSTOMIZE_NEWSPAPER_HOME_PAGE);
        intent.putExtra(EXTRA_CURRENT_NEWSPAPER,newspaper);
        return intent;
    }

    public static Intent newIntentForNonNewspaperFeatureGroupCustomization(Context packageContext,FeatureGroup featureGroup){
        Intent intent = new Intent(packageContext, SettingsActivity.class);
        intent.putExtra(EXTRA_CURRENT_FEATURE_GROUP,featureGroup);
        intent.setAction(ACTION_CUSTOMIZE_NON_NEWSPAPER_FEATURE_GROUP);
        return intent;
    }

    public static Intent newIntentForAppHomePageCustomization(Context packageContext) {
        FeatureGroup featureGroup = FeatureGroupHelper.getFeatureGroupForHomePage();
        return newIntentForNonNewspaperFeatureGroupCustomization(packageContext,featureGroup);
    }

    public static Intent newIntentForNewsCategoryCustomization(Context packageContext){
        Intent intent = new Intent(packageContext, SettingsActivity.class);
        intent.setAction(ACTION_CUSTOMIZE_NEWS_CATEGORIES);
        return intent;
    }

    public static Intent newIntentForNewspaperMenuCustomization(Context packageContext){
        Intent intent = new Intent(packageContext, SettingsActivity.class);
        intent.setAction(ACTION_CUSTOMIZE_NEWSPAPER_MENU);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else {
            getWindow().setStatusBarColor(Color.parseColor(getResources().getString(R.string.colorPrimaryDark2)));
        }
        setContentView(R.layout.activity_settings);
        mWorkInProcess = false;

        Intent intent = getIntent();
        String intentAction = intent.getAction();

        Fragment fragment = null;

        if (intentAction == null){
            fragment = new SettingsBasePageFragment();
            //Log.d("StackTrace", "onCreate: intentAction == null");
        } else {

            switch (intentAction) {
                case ACTION_CUSTOMIZE_NEWSPAPER_HOME_PAGE:
                    //Log.d("StackTrace", "onCreate: ACTION_CUSTOMIZE_NEWSPAPER_HOME_PAGE");
                    Newspaper newspaper = (Newspaper) intent.getSerializableExtra(EXTRA_CURRENT_NEWSPAPER);
                    if (newspaper != null) {
                        fragment = CustomizeNewspaperHomePageFragment.newInstance(newspaper);
                    }
                    break;
                case ACTION_CUSTOMIZE_NON_NEWSPAPER_FEATURE_GROUP:
                    //Log.d("StackTrace", "onCreate: ACTION_CUSTOMIZE_NON_NEWSPAPER_FEATURE_GROUP");
                    FeatureGroup featureGroup = (FeatureGroup) intent.getSerializableExtra(EXTRA_CURRENT_FEATURE_GROUP);
                    if (featureGroup !=null) {
                        fragment = CustomizeNonNewspaperFeatureGroupFragment.newInstance(featureGroup);
                    }
                    break;
                case ACTION_CUSTOMIZE_NEWS_CATEGORIES:
                    //Log.d("StackTrace", "onCreate: ACTION_CUSTOMIZE_NON_NEWSPAPER_FEATURE_GROUP");
                    fragment = new CustomizeCustomFeatureGroupFragment();
                    break;
                case ACTION_CUSTOMIZE_NEWSPAPER_MENU:
                    //Log.d("StackTrace", "onCreate: ACTION_CUSTOMIZE_NON_NEWSPAPER_FEATURE_GROUP");
                    fragment = new CustomizeNewspaperMenuFragment();
                    break;
            }
        }

        if (fragment == null){
            fragment = new SettingsBasePageFragment();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.
                beginTransaction().
                add(R.id.settings_frame_layout,fragment).
                commit();
    }

    @Override
    public void onBackPressed() {
        if (mWorkInProcess){
            DisplayUtility.showShortToast(CACHE_CLEAREING_WAIT_MSG);
        }else {
            super.onBackPressed();
        }
    }


    public void repllaceFragmentOnFrameAddingToBackStack(Fragment fragment) {

        if (fragment !=null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.
                    beginTransaction().
                    replace(R.id.settings_frame_layout,fragment).
                    addToBackStack(null).
                    commit();
        }
    }

    public void repllaceFragmentOnFrame(Fragment fragment) {

        if (fragment !=null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.
                    beginTransaction().
                    replace(R.id.settings_frame_layout,fragment).
                    commit();
        }
    }
}

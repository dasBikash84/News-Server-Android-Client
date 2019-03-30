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

package com.dasbikash.news_server.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.views.interfaces.NavigationHost;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity implements NavigationHost {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setUpBottomNavigationView();

        if(getSupportFragmentManager().findFragmentById(R.id.main_frame) == null){
            navigateTo(new InitFragment(),false);
        }
    }

    private void setUpBottomNavigationView() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.bottom_menu_item_home:
                    navigateTo(new HomeFragment(),false);
                    return true;
                case R.id.bottom_menu_item_page_group:
                    navigateTo(new PageGroupFragment(),false);
                    return true;
                case R.id.bottom_menu_item_favourites:
                    navigateTo(new FavouritesFragment(),false);
                    return true;
                case R.id.bottom_menu_item_settings:
                    navigateTo(new SettingsFragment(),false);
                    return true;
                case R.id.bottom_menu_item_more:
                    navigateTo(new MoreFragment(),false);
                    return true;
            }
            return false;
        });
    }

    /**
     * Trigger a navigation to the specified fragment, optionally adding a transaction to the back
     * stack to make this navigation reversible.
     *
     * @param fragment
     * @param addToBackstack
     */
    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {

        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_frame, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}

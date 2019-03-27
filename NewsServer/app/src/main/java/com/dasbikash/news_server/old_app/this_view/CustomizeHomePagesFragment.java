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
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.this_data.country.Country;
import com.dasbikash.news_server.old_app.this_data.country.CountryHelper;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroup;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_data.newspaper.NewspaperHelper;

import java.util.ArrayList;

public class CustomizeHomePagesFragment extends Fragment {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "CustomizeHomePagesFragment";

    private Button mCustomizeAppHomePageView;
    private Button mCustomizeNewspaperHomePageView;
    private RecyclerView mNewspaperListView;
    
    private ArrayList<Newspaper> mNewspaperList = new ArrayList<>();

    private SettingsActivity mParentActivity;

    @Override
    public void onAttach(Context context) {
        if (context instanceof SettingsActivity) {
            super.onAttach(context);
            mParentActivity = (SettingsActivity) context;
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customize_home_pages,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mCustomizeAppHomePageView = view.findViewById(R.id.customize_app_home_page);
        mCustomizeNewspaperHomePageView = view.findViewById(R.id.customize_newspaper_home_page);
        mNewspaperListView = view.findViewById(R.id.newspaper_list_view);

        mNewspaperList = NewspaperHelper.getAllActiveNewspapers();
        
        mNewspaperListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mNewspaperListView.setAdapter(new NewspaperListAdapter());
        mNewspaperListView.setVisibility(View.VISIBLE);

        mCustomizeAppHomePageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG, "onClick: ");
                appHomePageModifyAction();
            }
        });

        /*mCustomizeNewspaperHomePageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNewspaperListView.getVisibility() !=View.VISIBLE){
                    mNewspaperListView.setVisibility(View.VISIBLE);
                } else {
                    mNewspaperListView.setVisibility(View.GONE);
                }
            }
        });*/
    }

    private void appHomePageModifyAction() {
        FeatureGroup featureGroup = FeatureGroupHelper.getFeatureGroupForHomePage();
        if (featureGroup !=null) {
            //Log.d(TAG, "appHomePageModifyAction: featureGroup.getTitle(): "+featureGroup.getTitle());
            mParentActivity.repllaceFragmentOnFrameAddingToBackStack(
                    CustomizeNonNewspaperFeatureGroupFragment.newInstance(featureGroup)
            );
        }
    }

    private class NewspaperListAdapter extends RecyclerView.Adapter<NewspaperItemHolder>{

        @NonNull
        @Override
        public NewspaperItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new NewspaperItemHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull NewspaperItemHolder newspaperItemHolder, int position) {
            newspaperItemHolder.bind(mNewspaperList.get(position));
        }

        @Override
        public int getItemCount() {
            return mNewspaperList.size();
        }
    }

    private class NewspaperItemHolder extends RecyclerView.ViewHolder{

        private Newspaper mNewspaper;
        private Button mNewspaperTitleView;

        public NewspaperItemHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_menu_button, parent, false));
            mNewspaperTitleView = itemView.findViewById(R.id.menu_button_item);
            mNewspaperTitleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newspaperHomePageModifyAction();
                }
            });
        }

        private void newspaperHomePageModifyAction() {
            mParentActivity.repllaceFragmentOnFrameAddingToBackStack(
                    CustomizeNewspaperHomePageFragment.newInstance(mNewspaper)
            );
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

            mNewspaperTitleView.
                    setText(mNewspaper.getName()+" ("+country.getCountryCode()+")");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        ((SettingsActivity)getActivity()).getSupportActionBar().setTitle(R.string.customize_home_pages_fragment_title);
    }
}

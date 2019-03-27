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
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroup;
import com.dasbikash.news_server.old_app.this_data.feature_group.FeatureGroupHelper;
import com.dasbikash.news_server.old_app.this_utility.display_utility.DisplayUtility;

import java.util.ArrayList;

public class CustomizeCustomFeatureGroupFragment extends Fragment {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "CCFGFragment";

    public static final String PROMPT_FOR_EMPTY_NEWS_CATEGORY_NAME = "News category name can't be empty! Please provide a valid name:";
    public static final String PROMT_FOR_EXITING_NAME = " already exits! Please provide a diferent name:";
    public static final String ERROR_PROMPT = "Error occured! Please retry:";
    public static final String SUCCESFUL_CATEGORY_CREATION_MESSAGE = "New news category created!";
    public static final String EDIT_TEXT_PLACEHOLDER = "News Category name:";
    public static final String PAGE_TITLE = "Customize custom News categories";
    private static final String PROMPT_FOR_SAME_NEWS_CATEGORY_NAME = "Same name! Please provide a new name or hit cancel to exit.";
    private static final String SUCCESFUL_CATEGORY_RENAME_MESSAGE = "News Category renamed!";
    private static final String PROMT_FOR_INVALID_NAMENAME = "Invalid name! Please select a different name.";
    public static final String RENAME_NEWS_CATEGORY_DIALOG_TITLE = "Rename news category:";


    private ConstraintLayout mAddNewCustomFeatureGroupBlock;
    private ImageButton mAddNewCustomFeatureGroupButton;
    private Button mOperateCustomFeatureGroupListView;
    private RecyclerView mCurrentCustomFeatureGroupListView;

    private ArrayList<FeatureGroup> mCurrentCustomFeatureGroupList = new ArrayList<>();

    private SettingsActivity mParentActivity;

    @Override
    public void onAttach(Context context) {
        if (context instanceof SettingsActivity) {
            //Log.d(TAG, "onAttach: ");
            super.onAttach(context);
            mParentActivity = (SettingsActivity) context;
        } else {
            //Log.d(TAG, "onAttach: getActivity().finish()");
            getActivity().finish();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customize_custom_feature_group,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAddNewCustomFeatureGroupBlock = view.findViewById(R.id.add_new_custom_feature_block);
        mAddNewCustomFeatureGroupButton = view.findViewById(R.id.add_new_custom_feature_button);
        mOperateCustomFeatureGroupListView = view.findViewById(R.id.current_custom_feature_groups);

        mCurrentCustomFeatureGroupListView = view.findViewById(R.id.current_custom_feature_list);

        mAddNewCustomFeatureGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewCustomFeatureGroupAction();
            }
        });

        mAddNewCustomFeatureGroupBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewCustomFeatureGroupAction();
            }
        });

        /*mOperateCustomFeatureGroupListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentCustomFeatureGroupListView.getVisibility() == View.VISIBLE){
                    mCurrentCustomFeatureGroupListView.setVisibility(View.GONE);
                } else {
                    mCurrentCustomFeatureGroupListView.setVisibility(View.VISIBLE);
                }
            }
        });*/

        refreshCurrentCustomFeatureGroupList();

        mCurrentCustomFeatureGroupListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCurrentCustomFeatureGroupListView.setAdapter(new CustomFeatureGroupListAdapter());
        mCurrentCustomFeatureGroupListView.setVisibility(View.VISIBLE);

    }

    private void addNewCustomFeatureGroupAction() {
        showInputNewsCategoryNameDialog(getResources().getString(R.string.add_new_news_category_prompt));

    }

    private void showInputNewsCategoryNameDialog(String promptString) {
        EditText editText = new EditText(getActivity());
        editText.setHint(getPromptMessageForFeatureGroupAddition());
        new AlertDialog.Builder(getActivity())
                .setTitle("Create new news category.")
                .setMessage(promptString)
                .setView(editText)
                .setPositiveButton("Add", (DialogInterface dialogInterface, int i) -> {
                    addNewCustomFeatureGroup(editText.getText().toString());
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void addNewCustomFeatureGroup(String featureGroupName) {

        StringBuilder toastString = new StringBuilder("");

        switch (FeatureGroupHelper.createCustomNewsCategory(featureGroupName.trim())){
            case SUCCESS:
                refreshCurrentFeatureGroupListDisplay();
                showAddNewFeatureToFeatureGroupDialog(featureGroupName.trim());
                //toastString.append(SUCCESFUL_CATEGORY_CREATION_MESSAGE);
                break;
            case EMPTY_NAME:
                showInputNewsCategoryNameDialog(PROMPT_FOR_EMPTY_NEWS_CATEGORY_NAME);
                break;
            case INVALID_NAME:
                showInputNewsCategoryNameDialog(PROMT_FOR_INVALID_NAMENAME);
                break;
            case ALREADY_EXISTS:
                showInputNewsCategoryNameDialog("\""+featureGroupName.trim()+"\""+ PROMT_FOR_EXITING_NAME);
                break;
            case ERROR:
                showInputNewsCategoryNameDialog(ERROR_PROMPT);
                break;
        }
    }

    private void showAddNewFeatureToFeatureGroupDialog(String newlyCreatedFeatureGroupName) {

        new AlertDialog.Builder(getActivity())
                .setTitle("Add pages to news category.")
                .setMessage("New news category named \""+newlyCreatedFeatureGroupName
                        +"\" has been created.Currently it is empty.Do you want to add pages?")
                .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                    FeatureGroup featureGroup =
                            FeatureGroupHelper.findFeatureGroupByTitle(newlyCreatedFeatureGroupName);
                    if (featureGroup !=null) {
                        mParentActivity.repllaceFragmentOnFrameAddingToBackStack(
                                CustomizeNonNewspaperFeatureGroupFragment.newInstance(featureGroup)
                        );
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    private String getPromptMessageForFeatureGroupAddition() {
        return EDIT_TEXT_PLACEHOLDER;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((SettingsActivity)getActivity()).getSupportActionBar().setTitle(
                PAGE_TITLE
        );
    }

    private void refreshCurrentCustomFeatureGroupList() {
        mCurrentCustomFeatureGroupList = FeatureGroupHelper.getAllCustomFeatureGroups();
    }

    private void refreshCurrentFeatureGroupListDisplay() {
        refreshCurrentCustomFeatureGroupList();
        mCurrentCustomFeatureGroupListView.getAdapter().notifyDataSetChanged();
    }

    private class CustomFeatureGroupListAdapter extends RecyclerView.Adapter<CustomFeatureGroupHolder>{

        @NonNull
        @Override
        public CustomFeatureGroupHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CustomFeatureGroupHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomFeatureGroupHolder customFeatureGroupHolder, int position) {
            customFeatureGroupHolder.bind(mCurrentCustomFeatureGroupList.get(position));
        }

        @Override
        public int getItemCount() {
            return mCurrentCustomFeatureGroupList.size();
        }
    }

    private class CustomFeatureGroupHolder extends RecyclerView.ViewHolder{

        private FeatureGroup mFeatureGroup;
        private TextView mTitleText;
        private Switch mActivate;
        private ImageButton mRename;
        private ImageButton mDelete;
        private boolean mToastEnabled = true;


        public CustomFeatureGroupHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.layout_custom_feature_group_holder, parent, false));
            mTitleText = itemView.findViewById(R.id.feture_group_title_text);
            mActivate = itemView.findViewById(R.id.activate_feature_group_view);
            mRename = itemView.findViewById(R.id.rename_feature_group_view);
            mDelete = itemView.findViewById(R.id.remove_feature_button);

            mTitleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mParentActivity.repllaceFragmentOnFrameAddingToBackStack(
                            CustomizeNonNewspaperFeatureGroupFragment.newInstance(mFeatureGroup)
                    );
                }
            });

            mActivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b){
                        if (!activateFeatureGroupAction()){
                            mActivate.setChecked(!b);
                        }
                    }else {
                        if (!deactivateFeatureGroupAction()){
                            mActivate.setChecked(!b);
                        }
                    }
                }
            });

            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteFeatureGroupAction();
                }
            });

            mRename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    renameFeatureGroupAction();
                }
            });
        }

        private void renameFeatureGroupAction() {
            showRenameNewsCategoryNameDialog(getResources().getString(R.string.rename_new_news_category_prompt));
        }

        private void showRenameNewsCategoryNameDialog(String promptString) {
            EditText editText = new EditText(getActivity());
            editText.setText(mFeatureGroup.getTitle());
            new AlertDialog.Builder(getActivity())
                    .setTitle(RENAME_NEWS_CATEGORY_DIALOG_TITLE)
                    .setMessage(promptString)
                    .setView(editText)
                    .setPositiveButton("Ok", (DialogInterface dialogInterface, int i) -> {
                        renameFeatureGroup(editText.getText().toString());
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        }

        private void renameFeatureGroup(String newName) {
            if (newName.trim().length() == 0){
                showRenameNewsCategoryNameDialog(PROMPT_FOR_EMPTY_NEWS_CATEGORY_NAME);
                return;
            }
            if (newName.trim().equalsIgnoreCase(mFeatureGroup.getTitle())){
                showRenameNewsCategoryNameDialog(PROMPT_FOR_SAME_NEWS_CATEGORY_NAME);
                return;
            }

            StringBuilder toastString = new StringBuilder("");

            switch (FeatureGroupHelper.renameCustomNewsCategory(mFeatureGroup,newName.trim())){
                case SUCCESS:
                    toastString.append(SUCCESFUL_CATEGORY_RENAME_MESSAGE);
                    refreshCurrentFeatureGroupListDisplay();
                    break;
                case ALREADY_EXISTS:
                    showRenameNewsCategoryNameDialog(PROMPT_FOR_SAME_NEWS_CATEGORY_NAME);
                    break;
                case ERROR:
                    showRenameNewsCategoryNameDialog(ERROR_PROMPT);
                    break;
            }
            if (toastString.length()>0) {
                DisplayUtility.showShortToast(toastString.toString());
            }
        }

        private void deleteFeatureGroupAction() {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getPromptMessageForFeatureGroupDeletion())
                    .setPositiveButton("Yes", (DialogInterface dialogInterface, int i) -> {
                        deleteFeatureGroup();
                    })
                    .setNegativeButton("No", null)
                    .create()
                    .show();
        }

        private String getPromptMessageForFeatureGroupDeletion() {
            return "Delete \""+mFeatureGroup.getTitle()+"\"?";
        }

        private void deleteFeatureGroup() {

            if (FeatureGroupHelper.deleteFeatureGroup(mFeatureGroup)){
                refreshCurrentFeatureGroupListDisplay();
                DisplayUtility.showShortToast(getMessageForSuccessfulToast());
            } else {
                DisplayUtility.showShortToast(getMessageForFailureToast());
            }
        }

        private String getMessageForFailureToast() {
            return "Error occured, please retry.";
        }

        private String getMessageForSuccessfulToast() {
            return "\""+mFeatureGroup.getTitle()+"\" deleted.";

        }


        private boolean deactivateFeatureGroupAction() {
            if (FeatureGroupHelper.deactivateFeatureGroup(mFeatureGroup)){
                if (mToastEnabled) {
                    DisplayUtility.showShortToast("\"" + mFeatureGroup.getTitle() + "\" deactivated.");
                }
                return true;
            }
            return false;
        }

        private boolean activateFeatureGroupAction() {
            if (FeatureGroupHelper.activateFeatureGroup(mFeatureGroup)){
                if (mToastEnabled) {
                    DisplayUtility.showShortToast("\"" + mFeatureGroup.getTitle() + "\" activated.");
                }
                return true;
            }
            return false;
        }

        void bind(FeatureGroup featureGroup){
            if (featureGroup == null ||
                    featureGroup.getCategoryIdentifier() != NewsServerDBSchema.GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP){
                itemView.setVisibility(View.GONE);
                return;
            }
            mFeatureGroup = featureGroup;
            mTitleText.setText(mFeatureGroup.getTitle());
            mToastEnabled = false;
            mActivate.setChecked(mFeatureGroup.isActive());
            mToastEnabled = true;
        }
    }

}

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

package com.dasbikash.news_server.old_app.this_utility.display_utility;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.io.Serializable;
import java.util.ArrayList;

public class SerializableItemListRVAdapter extends RecyclerView.Adapter<SerializableItemListRVItemHolder>{

    ArrayList<Serializable> mSerializableItemList = new ArrayList<Serializable>();
    SerializableItemListDisplayCallbacks<Serializable> mSerializableItemListDisplayCallbacks;

    public SerializableItemListRVAdapter(SerializableItemListDisplayCallbacks<Serializable>
                                                 serializableItemListDisplayCallbacks) {
                 mSerializableItemList = serializableItemListDisplayCallbacks.getSerializableItemListForDisplay();
                 mSerializableItemListDisplayCallbacks =
                 serializableItemListDisplayCallbacks;
     }

    @NonNull
    @Override
    public SerializableItemListRVItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(NewsServerUtility.getContext());
        return new SerializableItemListRVItemHolder(layoutInflater, viewGroup, mSerializableItemListDisplayCallbacks);
    }

    @Override
    public void onBindViewHolder(@NonNull SerializableItemListRVItemHolder serializableItemListRVItemHolder, int position) {
        //boolean isLast = ;
        serializableItemListRVItemHolder.bind(mSerializableItemList.get(position),position == (mSerializableItemList.size()-1));
    }

    @Override
    public int getItemCount() {
        return mSerializableItemList.size();
    }
}

class SerializableItemListRVItemHolder extends RecyclerView.ViewHolder{

    private Serializable mSerializable;
    private TextView mSerializableItemTitleView;
    private ImageButton mImageButton;
    private View mSeperator;
    private SerializableItemListDisplayCallbacks<Serializable> mCallBacksForReqActions;

    @SuppressLint("ResourceType")
    public SerializableItemListRVItemHolder(@NonNull LayoutInflater inflater, ViewGroup parent,
                                            SerializableItemListDisplayCallbacks<Serializable>
                                                    serializableItemListDisplayCallbacks) {

        super(inflater.inflate(serializableItemListDisplayCallbacks.getIdForItemDisplay(), parent, false));
        mCallBacksForReqActions = serializableItemListDisplayCallbacks;

        if (mCallBacksForReqActions.getIdOfItemTextView() !=0) {
            mSerializableItemTitleView = itemView.findViewById(mCallBacksForReqActions.getIdOfItemTextView());
            mSerializableItemTitleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallBacksForReqActions.callBackForTextItemClickAction(mSerializable);
                }
            });
        }


        if (mCallBacksForReqActions.getIdOfItemImageButton() !=0) {

            mImageButton = itemView.findViewById(mCallBacksForReqActions.getIdOfItemImageButton());

            mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallBacksForReqActions.callBackForImageButtonItemClickAction(mSerializable);
                }
            });
        }

        if (mCallBacksForReqActions.getIdOfItemHorSeparator() !=0) {
            mSeperator = itemView.findViewById(mCallBacksForReqActions.getIdOfItemHorSeparator());
        }
    }

    public void bind(Serializable serializableItem,boolean isLast) {

        mSerializable = serializableItem;

        if (mSerializable != null){

            if (mSerializableItemTitleView !=null) {
                DisplayUtility.displayHtmlText
                        (mSerializableItemTitleView,
                                mCallBacksForReqActions.
                                        getTextStringForTextView(mSerializable));
            }

            if (mSeperator !=null) {
                if (isLast) {
                    mSeperator.setVisibility(View.GONE);
                } else {
                    mSeperator.setVisibility(View.VISIBLE);
                }
            }
        } else {
            itemView.setVisibility(View.GONE);
        }
    }
}
/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
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

package com.dasbikash.news_server.custom_views;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ViewPagerTitleScroller extends RecyclerView {

    public static final String TAG = "ViewPagerTitleScroller";

    private final List<PageKey> mPageKeys = new ArrayList<>();
    private ListAdapter<PageKey, TitleViewHolder> mListAdapter;

    @LayoutRes
    private Integer textViewId = null;

    @ColorInt
    private int onColor = Color.WHITE;
    @ColorInt
    private int offColor = Color.GRAY;

    private TitleClickListner mTitleClickListner;

    public ViewPagerTitleScroller(@NonNull Context context) {
        super(context);
        doInit(context);
    }
    public ViewPagerTitleScroller(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        doInit(context);
    }
    public ViewPagerTitleScroller(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        doInit(context);
    }

    private void doInit(Context context) {
        this.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mListAdapter = new ListAdapter<PageKey, TitleViewHolder>(new DiffUtil.ItemCallback<PageKey>() {
            @Override
            public boolean areItemsTheSame(@NonNull PageKey oldItem, @NonNull PageKey newItem) {
                return newItem.equals(oldItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull PageKey oldItem, @NonNull PageKey newItem) {
                return newItem.equals(oldItem);
            }
        }) {
            @NonNull
            @Override
            public TitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (textViewId ==null){
                    return new TitleViewHolder(new TextView(context));
                }else {
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    return new TitleViewHolder(
                            layoutInflater.inflate(textViewId,parent,false)
                    );
                }
            }

            @Override
            public void onBindViewHolder(@NonNull TitleViewHolder holder, int position) {
                holder.bind(mPageKeys.get(position));
                if (position == 0 && mTitleViewHolders.size() <2){
                    holder.setTextColorToOnColor();
                }
            }
        };
        this.setAdapter(mListAdapter);
    }

    public void initView(List<PageKey> pageKeys,
                         @LayoutRes int layoutId,
                         TitleClickListner titleClickListner){
        textViewId = layoutId;
        setTitleList(pageKeys);
        mTitleClickListner = titleClickListner;
    }

    public void initView(List<PageKey> pageKeys,
                         @LayoutRes int layoutId,
                         TitleClickListner titleClickListner,
                         @ColorInt int onColor,
                         @ColorInt int offColor){
        initView(pageKeys,layoutId, titleClickListner);
        this.onColor = onColor;
        this.offColor = offColor;
    }

    public void setTitleList(List<PageKey> pageKeys) {
        mPageKeys.clear();
        mPageKeys.addAll(pageKeys);
        mListAdapter.submitList(mPageKeys);
    }

    private List<TitleViewHolder> mTitleViewHolders = new ArrayList<>();

    private class TitleViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private PageKey mPageKey;

        private TitleViewHolder(View view) {
            super(view);
            textView  = (TextView) itemView;
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doOnTitleClick(mPageKey);
                }
            });
            mTitleViewHolders.add(this);
        }

        void bind(PageKey pageKey){
            mPageKey = pageKey;
            textView.setText(pageKey.getKeyString());
            if (activeItem !=null && activeItem == mPageKey){
                setTextColorToOnColor();
            }else {
                setTextColorToOffColor();
            }
        }

        void setTextColorToOffColor(){
            textView.setTextColor(offColor);
        }

        void setTextColorToOnColor(){
            textView.setTextColor(onColor);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TitleViewHolder holder = (TitleViewHolder) o;
            return Objects.equals(mPageKey, holder.mPageKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mPageKey);
        }
    }

    void doOnTitleClick(PageKey pageKey){

        makeItemActive(pageKey);

        if (mTitleClickListner ==null) {
            Log.d(TAG, "Empty click action.");
        }else {
            mTitleClickListner.onTitleClick(pageKey);
        }
    }

    private void makeItemActive(PageKey pageKey){
        for (TitleViewHolder holder :
                mTitleViewHolders) {
            if (holder.mPageKey.equals(pageKey)){
                holder.setTextColorToOnColor();
            }else {
                holder.setTextColorToOffColor();
            }
        }
    }

    private PageKey activeItem;

    public void setCurrentItem(PageKey pageKey) {
        activeItem = pageKey;
        scrollToPosition(mPageKeys.indexOf(pageKey));
        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        makeItemActive(pageKey);
                    }
                });
    }

    public interface TitleClickListner {
        void onTitleClick(PageKey pageKey);
    }

    public interface PageKey {
        String getKeyString();
    }
}

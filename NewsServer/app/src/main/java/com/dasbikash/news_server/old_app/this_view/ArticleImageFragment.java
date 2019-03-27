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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.image_downloader.ImageDownloader;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageData;
import com.dasbikash.news_server.old_app.this_data.image_data.ImageDataHelper;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;

import java.io.File;

import cn.bluemobi.dylan.photoview.library.PhotoView;


public class ArticleImageFragment extends Fragment {

    private static final String ARG_CURRENT_IMAGE_DATA =
            "com.dasbikash.prothomalofeatures.ArticleImageFragment.current_image_id";
    private static final String ARG_CURRENT_NEWSPAPER =
            "com.dasbikash.prothomalofeatures.ArticleImageFragment.ARG_CURRENT_NEWSPAPER";
    private static final int MAX_IMAGE_SIZE_BYTES = 2*1024 * 1024;

    private static final String TAG = " ArticleImageFrag";

    private ProgressBar mProgressBar;
    private PhotoView mImageView;
    private TextView mAltTextView;
    private LinearLayout mProgressBarHolder;
    private ScrollView mAltTextWrapper;

    private BitmapFactory.Options mBitmapOptions;
    private ImageData mImageData;
    private Newspaper mNewspaper;


    private final BroadcastReceiver mImageLoadBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleImageLoadBrodcastMessage(intent);
        }
    };

    public static ArticleImageFragment newInstance(ImageData imageData, Newspaper newspaper){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CURRENT_IMAGE_DATA, imageData);
        args.putSerializable(ARG_CURRENT_NEWSPAPER, newspaper);
        ArticleImageFragment fragment = new ArticleImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article_image,container,false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments()!=null && !getArguments().isEmpty()){
            mImageData = (ImageData) getArguments().getSerializable(ARG_CURRENT_IMAGE_DATA);
            mNewspaper = (Newspaper) getArguments().getSerializable(ARG_CURRENT_NEWSPAPER);
        } else {
            getActivity().finish();
        }

        //mProgressBar = view.findViewById(R.id.progress_bar_view);
        mImageView = view.findViewById(R.id.article_image);
        mAltTextWrapper = view.findViewById(R.id.image_alt_text_wrapper);
        mAltTextView = view.findViewById(R.id.image_alt_text);
        mProgressBarHolder = view.findViewById(R.id.init_loading_progress_bar_holder);
        mProgressBarHolder.bringToFront();

        //mAltTextView.setVisibility(View.GONE);

        mBitmapOptions = new BitmapFactory.Options();
        mBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        mImageData = ImageDataHelper.findImageDataById(mImageData.getId());

        if (mImageData.getDiskLocation() !=null){
            try {
                displayImage();
            } catch (Throwable throwable){
                throwable.printStackTrace();
            }
            mProgressBarHolder.setVisibility(View.GONE);
        } else {
            ImageDownloader.placeUrgentFileDownloadRequest(mImageData.getId(),mNewspaper);
            //Log.d(TAG, "onViewCreated: Image Dl req for ID: "+mImageData.getId());
            //Log.d(TAG, "onViewCreated: Image Dl req for link: "+mImageData.getLink());
        }
    }

    private void displayImage() {
        //mProgressBar.setVisibility(View.GONE);
        if (mImageData.getAltText() !=null &&
                mImageData.getAltText().trim().length()>0){
            mAltTextView.setText(mImageData.getAltText());
        } else {
            mAltTextView.setVisibility(View.GONE);
            mAltTextWrapper.setVisibility(View.GONE);
        }
        if (mImageData.getDiskLocation() == null) return;
        File imageFile = new File(mImageData.getDiskLocation());
        if (imageFile.length() > MAX_IMAGE_SIZE_BYTES) {
            return;
        }
        mImageView.setImageBitmap(
                BitmapFactory.decodeFile(imageFile.getPath(), mBitmapOptions));
        mProgressBarHolder.setVisibility(View.GONE);
        //mImageView.setVisibility(View.VISIBLE);
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_layout_basic,menu);
        menu.findItem(R.id.share_menu_item).setTitle("Share Image");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.email_team:
                startActivity(OptionsIntentBuilder.getEmailDeveloperIntent(getActivity()));
                return true;
            case R.id.call_team:
                startActivity(OptionsIntentBuilder.getCallDeveloperIntent(getActivity()));
                return true;
            case R.id.share_menu_item:
                if (mImageData.getLink()!=null) {
                    startActivity(OptionsIntentBuilder.getShareArticleImageIntent(getActivity(), mImageData));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    @Override
    public void onPause() {
        super.onPause();
        unregisterBrodcastReceivers();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBrodcastReceivers();
    }

    private void registerBrodcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mImageLoadBroadcastReceiver, ImageDownloader.getIntentFilterForImageDownloadBroadcastMessage());
    }
    private void unregisterBrodcastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mImageLoadBroadcastReceiver);
    }
    private void handleImageLoadBrodcastMessage(Intent intent) {

        if (intent!=null) {
            int receivedImageId = ImageDownloader.getBrodcastedImageId(intent);
            //Log.d(TAG, "handleImageLoadBrodcastMessage: receivedImageId: "+receivedImageId);

            if (receivedImageId== mImageData.getId()){
                if (ImageDownloader.getImageDownloadStatus(intent)) {
                    //Log.d(TAG, "handleImageLoadBrodcastMessage: Successful DL");
                    mImageData = ImageDataHelper.findImageDataById(receivedImageId);
                    displayImage();
                } else {
                    new Handler(getActivity().getMainLooper()).postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            ImageDownloader.placeUrgentFileDownloadRequest(mImageData.getId(),mNewspaper);
                            //Log.d(TAG, "goOn: placing re DL request for ID:"+mImageData.getId());
                            //Log.d(TAG, "goOn: placing re DL request for Link:"+mImageData.getLink());
                        }
                    }, 1000);
                }
            }
        }
    }
}

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

package com.dasbikash.news_server.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.ShareCompat;

import com.dasbikash.news_server.R;

@SuppressWarnings("ConstantConditions")
public abstract class OptionsIntentBuilderUtility {

    public static String getRawAppLink(Context context){
        return  "https://play.google.com/store/apps/details?id="+context.getPackageName();
    }

    private static String getAppLink(Context context){
        return  "<a href=\"https://play.google.com/store/apps/details?id="+
                context.getPackageName()+
                "\">"+context.getResources().getString(R.string.app_name)+
                "</a>";
    }

    public static Intent getEmailDeveloperIntent(Activity activity){
        return ShareCompat.IntentBuilder.from(activity)
                .setType("text/html")
                .setSubject(activity.getResources().getString(R.string.email_developer_subject))
                .addEmailTo(activity.getResources().getString(R.string.developer_email_address))
                .setChooserTitle(activity.getResources().getString(R.string.email_com_chooser_text))
                .createChooserIntent();
    }

    public static Intent getShareAppIntent(Activity activity){
        return ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setSubject(activity.getResources().getString(R.string.email_share_app_subject))
                .setChooserTitle(activity.getResources().getString(R.string.share_app_chooser_text))
                .setText(getAppLink(activity))
                .createChooserIntent();
    }

}

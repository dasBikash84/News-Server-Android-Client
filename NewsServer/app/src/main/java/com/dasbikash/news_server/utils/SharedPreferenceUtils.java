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

package com.dasbikash.news_server.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dasbikash.news_server.R;

import java.util.ResourceBundle;

public class SharedPreferenceUtils {

    private static final String GLOBAL_SETTINGS_UPDATE_TIME_STAMP =
            "com.dasbikash.news_server.utils.SharedPreferenceUtils.GLOBAL_SETTINGS_UPDATE_TIME_STAMP";

    public static void saveGlobalSettingsUpdateTimestamp(Context context,long time){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(GLOBAL_SETTINGS_UPDATE_TIME_STAMP, time);
        editor.apply();
    }

    public static long getGlobalSettingsUpdateTimestamp(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getLong(GLOBAL_SETTINGS_UPDATE_TIME_STAMP,0L);
    }
}

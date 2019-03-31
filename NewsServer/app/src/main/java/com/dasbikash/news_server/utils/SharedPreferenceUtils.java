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

public class SharedPreferenceUtils {

    public static void saveGlobalSettingsUpdateTimestamp(Context context,long time){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.SP_FILE_KEY), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.APP_SETTINGS_UPDATE_TIME_STAMP_SP_KEY), time);
        editor.apply();
    }

    public static long getAppSettingsUpdateTimestamp(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.SP_FILE_KEY), Context.MODE_PRIVATE);
        return sharedPref.getLong(context.getString(R.string.APP_SETTINGS_UPDATE_TIME_STAMP_SP_KEY),0L);
    }
}

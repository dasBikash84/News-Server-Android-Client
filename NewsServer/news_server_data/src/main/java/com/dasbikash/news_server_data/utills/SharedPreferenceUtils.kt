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

package com.dasbikash.news_server.utils

import android.content.Context
import com.dasbikash.news_server_data.R

internal object SharedPreferenceUtils {

    private val TAG = "SharedPrefUtilsTest"
    const val SP_FILE_KEY = "com.dasbikash.news_server.SP_FILE_KEY"

    const val DEFAULT_STRING = ""
    const val DEFAULT_LONG = 0L
    const val DEFAULT_INT = 0
    const val DEFAULT_FLOAT = 0F
    const val DEFAULT_BOOLEAN = false

    /**
     * Supports Long,Int,Float,String and Boolean data storing
     * */
    //@JvmStatic
    private fun <T : Any> saveData(context: Context, data: T, key: String) {

        val sharedPref = context.getSharedPreferences(
                SP_FILE_KEY, Context.MODE_PRIVATE)

        val editor = sharedPref.edit()

        when (data::class) {
            Long::class -> editor.putLong(key, data as Long)
            Int::class -> editor.putInt(key, data as Int)
            Float::class -> editor.putFloat(key, data as Float)
            String::class -> editor.putString(key, data as String)
            Boolean::class -> editor.putBoolean(key, data as Boolean)
            else -> throw IllegalArgumentException()
        }
        editor.apply()
    }

    /**
     * Supports Long,Int,Float,String and Boolean data storing
     * Has to provide default data of esired type
     * */
    //@JvmStatic
    private fun getData(context: Context, defaultValue: Any, key: String): Any {

        val sharedPref =
                context.getSharedPreferences(SP_FILE_KEY, Context.MODE_PRIVATE)

        when (defaultValue::class) {
            Long::class -> return sharedPref.getLong(key, defaultValue as Long)
            Int::class -> return sharedPref.getInt(key, defaultValue as Int)
            Float::class -> return sharedPref.getFloat(key, defaultValue as Float)
            String::class -> return sharedPref.getString(key, defaultValue as String)
            Boolean::class -> return sharedPref.getBoolean(key, defaultValue as Boolean)
            else -> throw IllegalArgumentException()
        }
    }

    fun saveGlobalSettingsUpdateTimestamp(context: Context, time: Long) {
        saveData(context, time, context.getString(R.string.APP_SETTINGS_UPDATE_TIME_STAMP_SP_KEY))
    }

    fun getLocalAppSettingsUpdateTimestamp(context: Context): Long {
        return getData(context, DEFAULT_LONG, context.getString(R.string.APP_SETTINGS_UPDATE_TIME_STAMP_SP_KEY)) as Long
    }


}

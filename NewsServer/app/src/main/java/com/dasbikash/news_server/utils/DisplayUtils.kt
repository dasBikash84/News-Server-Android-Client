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
import android.text.Html
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.dasbikash.news_server.R
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.models.room_entity.SavedArticle
import com.dasbikash.news_server_data.utills.SharedPreferenceUtils
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object DisplayUtils {

    private val ARTCILE_TEXT_SIZE_SP_KEY =
            "com.dasbikash.news_server.utils.DisplayUtils.ARTCILE_TEXT_SIZE_SP_KEY"
    val MIN_ARTICLE_TEXT_SIZE = 10
    val MAX_ARTICLE_TEXT_SIZE = 26
    val DEFAULT_ARTICLE_TEXT_SIZE = 16

    private val TWO_DAYS_IN_MS = (2 * 24 * 60 * 60 * 1000).toLong()
    private val DAY_IN_MS = (24 * 60 * 60 * 1000).toLong()
    private val TWO_HOURS_IN_MS = (2 * 60 * 60 * 1000).toLong()
    private val HOUR_IN_MS = (60 * 60 * 1000).toLong()
    private val TWO_MINUTES_IN_MS = (2 * 60 * 1000).toLong()
    private val MINUTE_IN_MS = (60 * 1000).toLong()
    private val SECOND_IN_MS: Long = 1000
    val JUST_NOW_TIME_STRING = "Just now"
    val JUST_NOW_TIME_STRING_BANGLA = "এইমাত্র পাওয়া"
    val MINUTES_TIME_STRING = "minutes"
    val MINUTE_TIME_STRING = "minute"
    val MINUTE_TIME_STRING_BANGLA = "মিনিট"
    val AGO_TIME_STRING = "ago"
    val AGO_TIME_STRING_BANGLA = "আগে"
    val HOURS_TIME_STRING = "hours"
    val HOUR_TIME_STRING = "hour"
    val HOUR_TIME_STRING_BANGLA = "ঘণ্টা"
    val YESTERDAY_TIME_STRING = "Yesterday"
    val YESTERDAY_TIME_STRING_BANGLA = "গতকাল"

    private var generatedViewId = AtomicInteger(0)

    fun getNextViewId(context: Context):Int{
        var nextId: Int

        do {
            nextId = generatedViewId.incrementAndGet()
            try {
                context.resources.getResourceName(nextId)
            }catch (ex:Exception){
                return nextId
            }
        }while (true)
    }


    private val BANGLA_UNICODE_ZERO: Char = 0x09E6.toChar()
    private val BANGLA_UNICODE_NINE: Char = 0x09EF.toChar()
    private val ENGLISH_UNICODE_ZERO: Char = 0x0030.toChar()
    private val ENGLISH_UNICODE_NINE: Char = 0x0039.toChar()

    private val MONTH_NAME_TABLE =  arrayOf(
                                        arrayOf("জানুয়ারী", "Jan"),
                                        arrayOf("জানুয়ারি", "Jan"),
                                        arrayOf("ফেব্রুয়ারী", "Feb"),
                                        arrayOf("ফেব্রুয়ারি", "Feb"),
                                        arrayOf("মার্চ", "Mar"),
                                        arrayOf("এপ্রিল", "Apr"),
                                        arrayOf("মে", "May"),
                                        arrayOf("জুন", "Jun"),
                                        arrayOf("জুলাই", "Jul"),
                                        arrayOf("আগস্ট", "Aug"),
                                        arrayOf("আগষ্ট", "Aug"),
                                        arrayOf("অগস্ট", "Aug"),
                                        arrayOf("সেপ্টেম্বর", "Sep"),
                                        arrayOf("অক্টোবর", "Oct"),
                                        arrayOf("নভেম্বর", "Nov"),
                                        arrayOf("ডিসেম্বর", "Dec")
                                    )
    private val DAY_NAME_TABLE = arrayOf(
                                        arrayOf("শনিবার", "Sat"),
                                        arrayOf("রবিবার", "Sun"),
                                        arrayOf("সোমবার", "Mon"),
                                        arrayOf("মঙ্গলবার", "Tue"),
                                        arrayOf("বুধবার", "Wed"),
                                        arrayOf("বৃহস্পতিবার", "Thu"),
                                        arrayOf("শুক্রবার", "Fri")
                                    )

    private val AM_PM_MARKER_TABLE = arrayOf(
                                        arrayOf("পূর্বাহ্ণ", "AM"),
                                        arrayOf("অপরাহ্ণ", "PM"),
                                        arrayOf("পূর্বাহ্ণ", "am"),
                                        arrayOf("অপরাহ্ণ", "pm")
                                    )

    val TAG = "DisplayUtils"

    fun dpToPx(dp: Int,context: Context): Float =
        (dp * context.getResources().getDisplayMetrics().density)

    fun pxToDp(px: Int,context: Context): Float =
        (px / context.getResources().getDisplayMetrics().density)

    fun getArticlePositionString(positionString:String, language: Language):String{

        if (!language.name!!.contains("English") && !language.name!!.contains("english")){
            return replaceEnglishDigits(positionString)
        }
        return positionString
    }

    fun getSavedArticlePublicationDateString(savedArticle: SavedArticle, language: Language, context: Context): String? {
        return getArticlePublicationDateStringFromPublicationTime(savedArticle.publicationTime,language,context)
    }


    fun getArticlePublicationDateString(article: Article, language: Language, context: Context): String? {
        return getArticlePublicationDateStringFromPublicationTime(article.publicationTime,language,context)
    }

    private fun getArticlePublicationDateStringFromPublicationTime(publicationTime:Date?,language: Language, context: Context):String?{

        val simpleDateFormat = SimpleDateFormat(context.getResources().getString(R.string.display_date_format_long))

        var diffTs = System.currentTimeMillis()
        var publicationTimeString: String? = null

        publicationTime?.let {

            diffTs = diffTs - it.time

            if (diffTs <= MINUTE_IN_MS) {
                publicationTimeString = JUST_NOW_TIME_STRING
            } else if (diffTs < HOUR_IN_MS) {
                publicationTimeString = (diffTs / MINUTE_IN_MS).toInt().toString() + " " +
                        (if (diffTs > TWO_MINUTES_IN_MS) MINUTES_TIME_STRING else MINUTE_TIME_STRING) +
                        " " + AGO_TIME_STRING
            } else if (diffTs < DAY_IN_MS) {
                publicationTimeString = (diffTs / HOUR_IN_MS).toString() + " " +
                        (if (diffTs > TWO_HOURS_IN_MS) HOURS_TIME_STRING else HOUR_TIME_STRING) +
                        " " + AGO_TIME_STRING
            } else if (diffTs < TWO_DAYS_IN_MS) {
                publicationTimeString = YESTERDAY_TIME_STRING
            } else {
                publicationTimeString = simpleDateFormat.format(publicationTime?.time)
            }
        }

        if (!language.name!!.contains("English") && !language.name!!.contains("english")){
            return convertToBanglaTimeString(publicationTimeString!!)
        }

        return publicationTimeString

    }

    private fun convertToBanglaTimeString(publicationTimeStringInput: String): String {

        var publicationTimeString = publicationTimeStringInput

        if (publicationTimeString == JUST_NOW_TIME_STRING) return JUST_NOW_TIME_STRING_BANGLA
        if (publicationTimeString == YESTERDAY_TIME_STRING) return YESTERDAY_TIME_STRING_BANGLA
        if (publicationTimeString.contains(AGO_TIME_STRING)) {
            publicationTimeString = publicationTimeString.replace(AGO_TIME_STRING, AGO_TIME_STRING_BANGLA)
            if (publicationTimeString.contains(MINUTES_TIME_STRING)) {
                publicationTimeString = publicationTimeString.replace(MINUTES_TIME_STRING, MINUTE_TIME_STRING_BANGLA)
            } else {
                publicationTimeString = publicationTimeString.replace(MINUTE_TIME_STRING, MINUTE_TIME_STRING_BANGLA)
            }
            if (publicationTimeString.contains(HOURS_TIME_STRING)) {
                publicationTimeString = publicationTimeString.replace(HOURS_TIME_STRING, HOUR_TIME_STRING_BANGLA)
            } else {
                publicationTimeString = publicationTimeString.replace(HOUR_TIME_STRING, HOUR_TIME_STRING_BANGLA)
            }
            publicationTimeString = replaceEnglishDigits(publicationTimeString)
            return publicationTimeString
        }

        return englishToBanglaDateString(publicationTimeString)
    }

    private fun replaceEnglishDigits(string: String): String {

        val chars = string.toCharArray()

        for (i in chars.indices) {
            val ch = chars[i]
            if (ch <= ENGLISH_UNICODE_NINE && ch >= ENGLISH_UNICODE_ZERO) {
                chars[i] = (ch + BANGLA_UNICODE_ZERO.toInt() - ENGLISH_UNICODE_ZERO.toInt()).toChar()
            }
        }

        return String(chars)
    }

    fun englishToBanglaDateString(dateStringInput: String): String {
        var dateString = dateStringInput
        dateString = replaceEnglishMonthName(dateString)
        dateString = replaceEnglishDigits(dateString)
        dateString = replaceEnglishDayName(dateString)
        dateString = replaceAMPMMarkerEngToBan(dateString)
        return dateString
    }

    private fun replaceAMPMMarkerEngToBan(str: String): String {
        for (i in AM_PM_MARKER_TABLE.indices) {
            if (str.contains(AM_PM_MARKER_TABLE[i][1])) {
                return str.replace(AM_PM_MARKER_TABLE[i][1], AM_PM_MARKER_TABLE[i][0])
            }
        }
        return str
    }

    private fun replaceEnglishMonthName(str: String): String {
        for (i in MONTH_NAME_TABLE.indices) {
            if (str.contains(MONTH_NAME_TABLE[i][1])) {
                return str.replace(MONTH_NAME_TABLE[i][1], MONTH_NAME_TABLE[i][0])
            }
        }
        return str
    }

    private fun replaceEnglishDayName(str: String): String {
        for (i in DAY_NAME_TABLE.indices) {
            if (str.contains(DAY_NAME_TABLE[i][1])) {
                return str.replace(DAY_NAME_TABLE[i][1], DAY_NAME_TABLE[i][0])
            }
        }
        return str
    }

    @Suppress("DEPRECATION")
    fun displayHtmlText(textView: TextView, text: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textView.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            textView.text = Html.fromHtml(text)
        }
    }

    fun getArticleTextSize(context: Context):Int{
        val textSize = SharedPreferenceUtils
                            .getData(context,SharedPreferenceUtils.DefaultValues.DEFAULT_INT, ARTCILE_TEXT_SIZE_SP_KEY)
                                as Int
        if (textSize == 0){
            setArticleTextSize(context,DEFAULT_ARTICLE_TEXT_SIZE)
            return DEFAULT_ARTICLE_TEXT_SIZE
        }
        return textSize
    }

    fun setArticleTextSize(context: Context,textSize:Int){
        var effectiveTextSize:Int
        when{
            textSize> MAX_ARTICLE_TEXT_SIZE -> effectiveTextSize = MAX_ARTICLE_TEXT_SIZE
            textSize < MIN_ARTICLE_TEXT_SIZE -> effectiveTextSize = MIN_ARTICLE_TEXT_SIZE
            else ->{
                effectiveTextSize = textSize
            }
        }
        SharedPreferenceUtils.saveData(context,effectiveTextSize, ARTCILE_TEXT_SIZE_SP_KEY)
    }

    fun showShortSnack(coordinatorLayout: CoordinatorLayout,message: String) {
        Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
                .show()
    }

    fun showLongSnack(coordinatorLayout: CoordinatorLayout,message: String) {
        Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .show()
    }

    fun showShortToast(context: Context,message: String) {
        Toast
                .makeText(context,message, Toast.LENGTH_SHORT)
                .show()
    }

    fun showLongToast(context: Context,message: String) {
        Toast
                .makeText(context,message, Toast.LENGTH_LONG)
                .show()
    }

}
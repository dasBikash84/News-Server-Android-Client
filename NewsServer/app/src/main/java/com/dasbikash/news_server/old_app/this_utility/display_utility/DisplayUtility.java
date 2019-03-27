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

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.article.Article;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_utility.CalenderUtility;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


@SuppressWarnings({"deprecation", "unchecked"})
public final class DisplayUtility {

    private static final String TAG = "Stack1Trace1";

    private static final long TWO_DAYS_IN_MS = 2*24*60*60*1000;
    private static final long DAY_IN_MS = 24*60*60*1000;
    private static final long TWO_HOURS_IN_MS = 2*60*60*1000;
    private static final long HOUR_IN_MS = 60*60*1000;
    private static final long TWO_MINUTES_IN_MS = 2*60*1000;
    private static final long MINUTE_IN_MS = 60*1000;
    private static final long SECOND_IN_MS = 1000;
    public static final String JUST_NOW_TIME_STRING = "Just now";
    public static final String JUST_NOW_TIME_STRING_BANGLA = "এইমাত্র পাওয়া";
    public static final String MINUTES_TIME_STRING = "minutes";
    public static final String MINUTE_TIME_STRING = "minute";
    public static final String MINUTE_TIME_STRING_BANGLA = "মিনিট";
    public static final String AGO_TIME_STRING = "ago";
    public static final String AGO_TIME_STRING_BANGLA = "আগে";
    public static final String HOURS_TIME_STRING = "hours";
    public static final String HOUR_TIME_STRING = "hour";
    public static final String HOUR_TIME_STRING_BANGLA = "ঘণ্টা";
    public static final String YESTERDAY_TIME_STRING = "Yesterday";
    public static final String YESTERDAY_TIME_STRING_BANGLA = "গতকাল";

    private static final char BANGLA_UNICODE_ZERO = 0x09E6;
    private static final char BANGLA_UNICODE_NINE = 0x09EF;
    private static final char ENGLISH_UNICODE_ZERO = 0x0030;
    private static final char ENGLISH_UNICODE_NINE = 0x0039;

    private static final String INVALID_NAME_CHECKER_REGEX =
            "(?i)(f[uc]{1,}k)|(s[uc]{1,}k)|(d[ic]{1,}k)|(c[un]{2,}t)|(p[us]{1,}sy)|(penis)|(vagina)";

    private static final String[][] MONTH_NAME_TABLE = {
            {"জানুয়ারী","Jan"},
            {"জানুয়ারি","Jan"},
            {"ফেব্রুয়ারী","Feb"},
            {"ফেব্রুয়ারি","Feb"},
            {"মার্চ","Mar"},
            {"এপ্রিল","Apr"},
            {"মে","May"},
            {"জুন","Jun"},
            {"জুলাই","Jul"},
            {"আগস্ট","Aug"},
            {"আগষ্ট","Aug"},
            {"অগস্ট","Aug"},
            {"সেপ্টেম্বর","Sep"},
            {"অক্টোবর","Oct"},
            {"নভেম্বর","Nov"},
            {"ডিসেম্বর","Dec"}
    };
    private static final String[][] DAY_NAME_TABLE = {
            {"শনিবার","Sat"},
            {"রবিবার","Sun"},
            {"সোমবার","Mon"},
            {"মঙ্গলবার","Tue"},
            {"বুধবার","Wed"},
            {"বৃহস্পতিবার","Thu"},
            {"শুক্রবার","Fri"}
    };

    private static final String[][] AM_PM_MARKER_TABLE = {
            {"পূর্বাহ্ণ","AM"},
            {"অপরাহ্ণ","PM"},
            {"পূর্বাহ্ণ","am"},
            {"অপরাহ্ণ","pm"}
    };

    private static String replaceBanglaMonthName(String str){
        for (int i = 0; i < MONTH_NAME_TABLE.length; i++) {
            if (str.contains(MONTH_NAME_TABLE[i][0])){
                return str.replace(MONTH_NAME_TABLE[i][0],MONTH_NAME_TABLE[i][1]);
            }
        }
        return str;
    }

    private static String replaceAMPMMarkerBanToEng(String str){
        for (int i = 0; i < AM_PM_MARKER_TABLE.length; i++) {
            if (str.contains(AM_PM_MARKER_TABLE[i][0])){
                return str.replace(AM_PM_MARKER_TABLE[i][0],AM_PM_MARKER_TABLE[i][1]);
            }
        }
        return str;
    }

    private static String replaceAMPMMarkerEngToBan(String str){
        for (int i = 0; i < AM_PM_MARKER_TABLE.length; i++) {
            if (str.contains(AM_PM_MARKER_TABLE[i][1])){
                return str.replace(AM_PM_MARKER_TABLE[i][1],AM_PM_MARKER_TABLE[i][0]);
            }
        }
        return str;
    }

    private static String replaceEnglishMonthName(String str){
        for (int i = 0; i < MONTH_NAME_TABLE.length; i++) {
            if (str.contains(MONTH_NAME_TABLE[i][1])){
                return str.replace(MONTH_NAME_TABLE[i][1],MONTH_NAME_TABLE[i][0]);
            }
        }
        return str;
    }

    private static String replaceEnglishDayName(String str){
        for (int i = 0; i < DAY_NAME_TABLE.length; i++) {
            if (str.contains(DAY_NAME_TABLE[i][1])){
                return str.replace(DAY_NAME_TABLE[i][1],DAY_NAME_TABLE[i][0]);
            }
        }
        return str;
    }

    private static String replaceBanglaDigits(String string){

        char[] chars = string.toCharArray();

        for (int i = 0; i <chars.length; i++) {
            char ch = chars[i];
            if (ch <= BANGLA_UNICODE_NINE && ch >= BANGLA_UNICODE_ZERO) {
                chars[i] = (char) (ch + ENGLISH_UNICODE_ZERO - BANGLA_UNICODE_ZERO);
            }
        }

        return new String(chars);
    }

    private static String replaceEnglishDigits(String string){

        char[] chars = string.toCharArray();

        for (int i = 0; i <chars.length; i++) {
            char ch = chars[i];
            if (ch <= ENGLISH_UNICODE_NINE && ch >= ENGLISH_UNICODE_ZERO) {
                chars[i] = (char) (ch + BANGLA_UNICODE_ZERO - ENGLISH_UNICODE_ZERO);
            }
        }

        return new String(chars);
    }

    private static String convertToBanglaTimeString(String publicationTimeString) {

        if (publicationTimeString.equals(JUST_NOW_TIME_STRING))     return JUST_NOW_TIME_STRING_BANGLA;
        if (publicationTimeString.equals(YESTERDAY_TIME_STRING))    return YESTERDAY_TIME_STRING_BANGLA;
        if (publicationTimeString.contains(AGO_TIME_STRING)){
            publicationTimeString = publicationTimeString.replace(AGO_TIME_STRING,AGO_TIME_STRING_BANGLA);
            if (publicationTimeString.contains(MINUTES_TIME_STRING)) {
                publicationTimeString = publicationTimeString.replace(MINUTES_TIME_STRING, MINUTE_TIME_STRING_BANGLA);
            }else {
                publicationTimeString = publicationTimeString.replace(MINUTE_TIME_STRING, MINUTE_TIME_STRING_BANGLA);
            }
            if (publicationTimeString.contains(HOURS_TIME_STRING) ){
                publicationTimeString = publicationTimeString.replace(HOURS_TIME_STRING, HOUR_TIME_STRING_BANGLA);
            }else{
                publicationTimeString = publicationTimeString.replace(HOUR_TIME_STRING, HOUR_TIME_STRING_BANGLA);
            }
            publicationTimeString = replaceEnglishDigits(publicationTimeString);
            return publicationTimeString;
        }

        return englishToBanglaDateString(publicationTimeString);
    }

    public static String banglaToEnglishDateString(String dateString){
        dateString = replaceBanglaMonthName(dateString);
        dateString = replaceBanglaDigits(dateString);
        dateString = replaceAMPMMarkerBanToEng(dateString);
        return dateString;
    }

    public static String englishToBanglaDateString(String dateString){
        dateString = replaceEnglishMonthName(dateString);
        dateString = replaceEnglishDigits(dateString);
        dateString = replaceEnglishDayName(dateString);
        dateString = replaceAMPMMarkerEngToBan(dateString);
        return dateString;
    }

    public static void displayHtmlText(TextView textView,String text){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(text));
        }
    }

    public static View inflateSerializableItemList(View parentView, SerializableItemListDisplayCallbacks callBacks){
        View viewForReturn;
        ListView listView = parentView.findViewById(callBacks.getListViewId());
        RecyclerView recyclerView = parentView.findViewById(callBacks.getRecyclerViewId());

        if (callBacks.getSerializableItemListForDisplay().size()>
                callBacks.getRVDisplayThresholdCount()){
            recyclerView.setVisibility(View.GONE);
            listView.setAdapter(new SerializableListListAdapter(callBacks));
            listView.setVisibility(View.VISIBLE);
            viewForReturn = listView;
        } else {
            listView.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(NewsServerUtility.getContext()));
            recyclerView.setAdapter(new SerializableItemListRVAdapter(callBacks));
            recyclerView.setVisibility(View.VISIBLE);
            viewForReturn = recyclerView;
        }
        return viewForReturn;
    }
    public static int dpToPx(int dp)
    {
        return (int) (dp * NewsServerUtility.getContext().getResources().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / NewsServerUtility.getContext().getResources().getDisplayMetrics().density);
    }

    public static String getArticlePublicationDateString(Article article, Newspaper newspaper){

        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(NewsServerUtility.getContext().getResources().
                                        getString(R.string.display_date_format_short));

        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(
                TimeZone.getDefault().getID()
        ));

        Calendar publicationDate = Calendar.getInstance();
        publicationDate.setTimeZone(TimeZone.getTimeZone(
                TimeZone.getDefault().getID()
        ));
        publicationDate.setTimeInMillis(article.getPublicationTS());

        Calendar nowDate = CalenderUtility.getCurrentTime();
        nowDate.setTimeZone(TimeZone.getTimeZone(
                TimeZone.getDefault().getID()
        ));

        long diffTs = nowDate.getTimeInMillis() - publicationDate.getTimeInMillis();
        //Log.d(TAG, "getArticlePublicationDateString: diffTs: "+diffTs);
        //Log.d(TAG, "getArticlePublicationDateString: System.currentTimeMillis()-article.getPublicationTS(): "+
                                //(System.currentTimeMillis()-article.getPublicationTS()));

        String publicationTimeString = null;

        if (publicationDate.getTimeInMillis() != 0L) {
            if (diffTs<= MINUTE_IN_MS){
                publicationTimeString = JUST_NOW_TIME_STRING;
            } else if (diffTs < HOUR_IN_MS){
                publicationTimeString = (int)(diffTs/MINUTE_IN_MS)+" "+
                                        (diffTs>TWO_MINUTES_IN_MS? MINUTES_TIME_STRING : MINUTE_TIME_STRING)+
                                        " "+AGO_TIME_STRING;
            }else if (diffTs < DAY_IN_MS){
                publicationTimeString = (diffTs/HOUR_IN_MS)+" "+
                                        (diffTs>TWO_HOURS_IN_MS? HOURS_TIME_STRING : HOUR_TIME_STRING)+
                                        " "+AGO_TIME_STRING;
            }else if ( nowDate.get(Calendar.DAY_OF_YEAR) - publicationDate.get(Calendar.DAY_OF_YEAR) == 1){
                publicationTimeString = YESTERDAY_TIME_STRING;
            } else {
                publicationTimeString = simpleDateFormat.format(publicationDate.getTime());
            }
        }else {
            return null;
        }

        if (newspaper.getLanguageId() == NewsServerDBSchema.LANGUAGE_CODE_ENGLISH_UK ||
                newspaper.getLanguageId() == NewsServerDBSchema.LANGUAGE_CODE_ENGLISH_US){
            return publicationTimeString;
        }

        String returnString= convertToBanglaTimeString(publicationTimeString);
        //Log.d(TAG, "getArticlePublicationDateString: returnString: "+returnString);

        return returnString;//convertToBanglaTimeString(publicationTimeString);
    }

    public static boolean checkIfValidTextInput(String featureGroupName) {
        return !featureGroupName.matches(INVALID_NAME_CHECKER_REGEX);
    }

    public static Toast showShortToast(CharSequence message) {
        Toast toast = null;
        try{
            toast = Toast.makeText(NewsServerUtility.getContext(),
                    message,Toast.LENGTH_SHORT);
            toast.show();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return toast;
    }

    public static Toast showLongToast(CharSequence message) {
        Toast toast = null;
        try{
            toast = Toast.makeText(NewsServerUtility.getContext(),
                    message,Toast.LENGTH_LONG);
            toast.show();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return toast;
    }

    public static Toast showShortToast(@StringRes int redId) {
        Toast toast = null;
        try{
            toast = Toast.makeText(NewsServerUtility.getContext(),
                    redId,Toast.LENGTH_SHORT);
            toast.show();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return toast;
    }

    public static Toast showLongToast(@StringRes int redId) {
        Toast toast=null;

        try{
            toast = Toast.makeText(NewsServerUtility.getContext(),
                redId,Toast.LENGTH_LONG);
            toast.show();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return toast;
    }
}

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

package com.dasbikash.news_server.old_app.article_loader;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dasbikash.news_server.old_app.article_loader.anando_bazar.AnandoBazarArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.bd_pratidin.BdPratidinArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.bhorer_kagoj.BhorerKagojArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.bonik_barta.BonikBartaArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.daily_mirror.DailyMirrorArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.daily_sun.DailySunArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.dawn_pak.DawnPakArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.dhaka_tribune.DhakaTribuneArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.jugantor.JugantorArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.kaler_kantho.KalerKanthoArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.new_age.NewAgeArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.the_financial_express.TheFinancialExpressArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.the_indian_express.TheIndianExpressArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.doinick_ittefaq.DoinickIttefaqArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.prothom_alo.ProthomaloArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.the_daily_star.TheDailyStarArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.the_gurdian.TheGurdianArticleLoader;
import com.dasbikash.news_server.old_app.article_loader.the_times_of_india.TheTimesOfIndiaArticleLoader;
import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;
import com.dasbikash.news_server.old_app.this_data.article.Article;
import com.dasbikash.news_server.old_app.this_data.newspaper.Newspaper;
import com.dasbikash.news_server.old_app.this_utility.NewsServerUtility;

public class ArticleLoaderService extends IntentService {

    private static final String TAG = "StackTrace";
    //private static final String TAG = "ArticleLoaderService";

    private static final String EXTRA_CURRENT_ARTICLE =
            "ArticleLoaderService.EXTRA_CURRENT_ARTICLE";

    private static final String EXTRA_CURRENT_NEWSPAPER =
            "ArticleLoaderService.EXTRA_CURRENT_NEWSPAPER";

    private static final String ARTICLE_DOWNLOAD_INTENT_FILTER =
            "ArticleLoaderService.article_download_intent_filter";
    private static final String EXTRA_DOWNLOADED_ARTICLE_ID =
            "ArticleLoaderService.downloaded_article_id";

    private static final String EXTRA_ARTICLE_DOWNLOAD_FLAG =
                "ArticleLoaderService.article_download_flag";

    private static ArticleLoaderService sArticleLoaderService;

    public static Intent formIntent(Article article, Newspaper newspaper) {
        Intent intent = new Intent(NewsServerUtility.getContext(), ArticleLoaderService.class);
        intent.putExtra(EXTRA_CURRENT_ARTICLE, article);
        intent.putExtra(EXTRA_CURRENT_NEWSPAPER, newspaper);
        return intent;
    }

    public ArticleLoaderService() {
        super("ArticleLoaderService");
    }

    private static boolean needToDownload(int articleLinkHashCode){

        final SQLiteDatabase dbCon = NewsServerUtility.getDatabaseCon();

        String sqlCheckArticleFragmentStatus = "SELECT * FROM "+ NewsServerDBSchema.ArticleFragmentTable.NAME +
                                                    " WHERE "+
                                                    NewsServerDBSchema.ArticleFragmentTable.Cols.ArticleLinkHashCode.NAME+
                                                    " = "+articleLinkHashCode+";";

        try (Cursor cursor = dbCon.rawQuery(sqlCheckArticleFragmentStatus,null)){
            if (cursor.getCount() > 0) {
                return false;
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return true;
    }

    private boolean loadArticle(Article article, Newspaper newspaper) {

        switch (newspaper.getId()){
            case NewsServerDBSchema.NEWSPAPER_ID_THE_GURDIAN:
                return new TheGurdianArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_PROTHOM_ALO:
                return new ProthomaloArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_ANANDO_BAZAR:
                return new AnandoBazarArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_THE_DAILY_STAR:
                return new TheDailyStarArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_THE_INDIAN_EXPRESS:
                return new TheIndianExpressArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_DOINICK_ITTEFAQ:
                return new DoinickIttefaqArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_THE_TIMES_OF_INDIA:
                return new TheTimesOfIndiaArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_DAILY_MIRROR:
                return new DailyMirrorArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_DHAKA_TRIBUNE:
                return new DhakaTribuneArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_BD_PROTIDIN:
                return new BdPratidinArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_DAWN_PAK:
                return new DawnPakArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_KALER_KANTHO:
                return new KalerKanthoArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_JUGANTOR:
                return new JugantorArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_THE_FINANCIAL_EXPRESS:
                return new TheFinancialExpressArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_BONIK_BARTA:
                return new BonikBartaArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_BHORER_KAGOJ:
                return new BhorerKagojArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_NEW_AGE:
                return new NewAgeArticleLoader().downloadArticle(article,newspaper);

            case NewsServerDBSchema.NEWSPAPER_ID_DAILY_SUN:
                //Log.d(TAG, "loadArticle: case NewsServerDBSchema.NEWSPAPER_ID_DAILY_SUN:");
                return new DailySunArticleLoader().downloadArticle(article,newspaper);

        }
        return false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        intent = ArticleLoaderHelper.getNextIntent();

        if (intent != null) {

            Article article = (Article) intent.getSerializableExtra(EXTRA_CURRENT_ARTICLE);
            Newspaper newspaper = (Newspaper) intent.getSerializableExtra(EXTRA_CURRENT_NEWSPAPER);

            if (needToDownload(article.getLinkHashCode())){
                generateBroadcastMessage(article,loadArticle(article,newspaper));
            } else {
                generateBroadcastMessage(article,true);
            }
        }
    }

    static void generateBroadcastMessage(Article article, boolean downloadResult) {
        Intent broadcastIntent = new Intent(ARTICLE_DOWNLOAD_INTENT_FILTER);
        broadcastIntent.putExtra(EXTRA_DOWNLOADED_ARTICLE_ID,article.getId());
        if (downloadResult){
            broadcastIntent.putExtra(EXTRA_ARTICLE_DOWNLOAD_FLAG,"Success");
        } else {
            //Log.d(TAG, "generateBroadcastMessage: Failure");
        }
        LocalBroadcastManager.getInstance(NewsServerUtility.getContext()).sendBroadcast(broadcastIntent);
    }

    public static IntentFilter getIntentFilterForArticleLoaderBroadcastMessage(){
        return new IntentFilter(ArticleLoaderService.ARTICLE_DOWNLOAD_INTENT_FILTER);
    }

    public static int getBrodcastedArticleId(Intent intent) {
        if (intent!=null) {
            return intent.getIntExtra(EXTRA_DOWNLOADED_ARTICLE_ID, 0);
        }
        return 0;
    }

    public static boolean getArticleDownloadStatus(Intent intent){
        if (intent!=null){
            return intent.hasExtra(EXTRA_ARTICLE_DOWNLOAD_FLAG);
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sArticleLoaderService = ArticleLoaderService.this;
        //Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sArticleLoaderService = null;
        ArticleLoaderHelper.removeLoaderHelper();
        //Log.d(TAG, "onDestroy: ");
    }

    public static synchronized boolean isRunning(){
        //Log.d(TAG, "isRunning: ");
        return !(sArticleLoaderService == null);
    }
    public static void stopArticleLoaderService(){
        if (sArticleLoaderService!=null) {
            sArticleLoaderService.stopSelf();
            //Log.d(TAG, "stopEditionLoaderService: ");
        }
    }
}

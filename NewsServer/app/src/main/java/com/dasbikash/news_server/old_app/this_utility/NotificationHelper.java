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

package com.dasbikash.news_server.old_app.this_utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.dasbikash.news_server.R;
import com.dasbikash.news_server.old_app.this_view.ArticleListActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationHelper {
    //private static final String TAG = "NotificationHelper";

    private static final String NOTIFICATION_CHANNEL_FIRST_PART = "NotificationHelper";

    public static void  generateNotification(String notificationTitle, String notificationTextContent) {

        int uniqueID = (int) (Math.random()*1000000);

        String channelId = createNotificationChennel();

        NotificationCompat.Builder notification = new NotificationCompat.
                                                    Builder(NewsServerUtility.getContext(),channelId);

        notification.setAutoCancel(true);
        notification.setSmallIcon(R.mipmap.ic_notification);
        notification.setTicker("Ticker for Notification");
        notification.setWhen(System.currentTimeMillis());
        //noinspection ConstantConditions
        notification.setContentTitle(notificationTitle);
        notification.setContentText(notificationTextContent);
        notification.setDefaults(Notification.DEFAULT_SOUND);

        Intent intent = ArticleListActivity.newIntentForNotification(NewsServerUtility.getContext());
        PendingIntent pendingIntent = PendingIntent.getActivity(NewsServerUtility.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        //Builds notification and issues it
        NotificationManager notificationManager = (NotificationManager) NewsServerUtility.getContext().getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager !=null) {
            notificationManager.notify(uniqueID, notification.build());
        }

    }

    private static String createNotificationChennel(){

        NotificationManager notificationManager = (NotificationManager) NewsServerUtility.getContext().
                                                                                getSystemService(NOTIFICATION_SERVICE);

        // The id of the channel.
        String channelId = NOTIFICATION_CHANNEL_FIRST_PART+ (int) (Math.random()*1000000);

        // The user-visible name of the channel.
        CharSequence name = NewsServerUtility.getContext().getResources().getString(R.string.channel_name)+(int) (Math.random()*1000000);

        // The user-visible description of the channel.
        String description = NewsServerUtility.getContext().getResources().getString(R.string.channel_description);

        int importance = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_LOW;
        }

        if (Build.VERSION.SDK_INT>=26){
            NotificationChannel mChannel = new NotificationChannel(channelId, name,importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            //mChannel.enableVibration(true);
            //mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            if (notificationManager !=null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }
        return channelId;
    }
}

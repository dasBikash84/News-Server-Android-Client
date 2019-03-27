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

import android.content.Context;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.TimeZone;


public final class CalenderUtility {

    public static final String TAG = "StackTrace";

    private static final String[] TIME_SERVER_LIST =
    {   "pool.ntp.org",
        "time-a.nist.gov",
        "time-a-g.nist.gov",
        "time-b-g.nist.gov",
        "utcnist2.colorado.edu"
    };

    private static final int ONE_DAY = 24*60*60*1000;

    public static final int INTERNATE_TIME = 0;
    private static final int SYSTEM_TIME = 1;
    private static final int UNDEFINED_TIME = 3;

    public static int sTimeUpdateStatus = UNDEFINED_TIME;
    private static long sCurrentTimeStampMillis = 0;

    private static CalenderUtility sCalenderUtility;

    private static Long sTimeDiff;

    private CalenderUtility(){}

    public static Calendar getCurrentTime(){

        Calendar now = Calendar.getInstance();
        if (sTimeDiff !=null) {
            now.setTimeInMillis(now.getTimeInMillis() + sTimeDiff);
        }
        return now;
    }

    public static void updateTime(){
        if (sTimeUpdateStatus !=INTERNATE_TIME){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateNowMillis();
                }
            }).start();
        }
    }

   @SuppressWarnings("unchecked")
    private static void updateNowMillis(){

        if (sCalenderUtility == null){
            sCalenderUtility = new CalenderUtility();
        }

        for (String serverAddress:
                TIME_SERVER_LIST ){

            ReadInternetTime2 readInternetTime2 =
                    sCalenderUtility.getNewTimeRead2Thread(serverAddress);

            try {
                readInternetTime2.start();
                Thread.sleep(3000);
            } catch (Exception e) {
                //Log.d(TAG,"Got interrupted sCurrentTimeStampMillis:"+sCurrentTimeStampMillis);
                e.printStackTrace();
            }
            if (sTimeUpdateStatus != INTERNATE_TIME){
                readInternetTime2.interrupt();
            } else {
                //Log.d(TAG,"Time read from: "+serverAddress);
                break;
            }
        }
        if (sTimeUpdateStatus != INTERNATE_TIME) {
            sCurrentTimeStampMillis = Calendar.getInstance().getTimeInMillis();
            sTimeDiff = 0L;
            sTimeUpdateStatus = SYSTEM_TIME;
        }

    }


    private ReadInternetTime2 getNewTimeRead2Thread(String serverAddress){
        return new ReadInternetTime2(serverAddress,Thread.currentThread());
    }

    private class ReadInternetTime2 extends Thread{
        private String mServerAddress;
        private Thread mCallerThread;

        public ReadInternetTime2(String serverAddress, Thread callerThred){
            super();
            mServerAddress = serverAddress;
            mCallerThread = callerThred;
        }

        @Override
        public void run() {

            NTPUDPClient timeClient = new NTPUDPClient();

            try {
                InetAddress inetAddress = InetAddress.getByName(mServerAddress);
                TimeInfo timeInfo = timeClient.getTime(inetAddress);

                sCurrentTimeStampMillis = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                sTimeDiff = sCurrentTimeStampMillis - Calendar.getInstance().getTimeInMillis();
                sTimeUpdateStatus = INTERNATE_TIME;
                mCallerThread.interrupt();

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /*static class ReadInternetTime extends AsyncTask<Object,Object,Object>{

        @Override
        protected Object doInBackground(Object... objects) {

            String timeServerAddress = (String) objects[0];
            Thread callerThread = (Thread)objects[1];

            NTPUDPClient timeClient = new NTPUDPClient();
            //Log.d(TAG,"Going to read internet time from: "+timeServerAddress);

            try {
                InetAddress inetAddress = InetAddress.getByName(timeServerAddress);
                TimeInfo timeInfo = timeClient.getTime(inetAddress);

                sCurrentTimeStampMillis = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                sTimeDiff = sCurrentTimeStampMillis - Calendar.getInstance().getTimeInMillis();
                sTimeUpdateStatus = INTERNATE_TIME;
                callerThread.interrupt();

            } catch (Throwable e) {
                //Log.d(TAG,"Error:"+e.getMessage());
                e.printStackTrace();
            }
            //Log.d(TAG,"Internet time read async task finished.");
            return null;
        }
    }*/

    public static boolean checkIfSameDay(Calendar day1,Calendar day2){
        Context context = NewsServerUtility.getContext();
        day1.setTimeZone(TimeZone.getTimeZone(
                TimeZone.getDefault().getID()//getResources().getString(R.string.default_time_zone)
        ));
        day2.setTimeZone(TimeZone.getTimeZone(
                TimeZone.getDefault().getID()//getResources().getString(R.string.default_time_zone)
        ));

        return (day1.get(Calendar.YEAR) == day2.get(Calendar.YEAR)) &&
                (day1.get(Calendar.DAY_OF_YEAR) == day2.get(Calendar.DAY_OF_YEAR));
    }

    public static int getDifferenceInDays(Calendar day1,Calendar day2){
        return (int)(Math.abs(day1.getTimeInMillis() - day2.getTimeInMillis())/ONE_DAY);


    }
}

/*
  Pebble_sd - a simple accelerometer based seizure detector that runs on a
  Pebble smart watch (http://getpebble.com).

  See http://openseizuredetector.org for more information.

  Copyright Graham Jones, 2015.

  This file is part of pebble_sd.

  Pebble_sd is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  Pebble_sd is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with pebble_sd.  If not, see <http://www.gnu.org/licenses/>.

*/


package net.homeip.ennismore.benplayer;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.io.*;
import java.util.*;
import java.util.StringTokenizer;

import android.text.format.Time;

import org.json.JSONObject;
import org.json.JSONArray;


/**
 * Based on example at:
 * http://stackoverflow.com/questions/14309256/using-nanohttpd-in-android
 * and
 * http://developer.android.com/guide/components/services.html#ExtendingService
 */
public class BenServer extends Service {
    // Notification ID
    private int NOTIFICATION_ID = 1;

    private NotificationManager mNM;

    private WebServer webServer = null;
    private final static String TAG = "SdServer";
    private Timer dataLogTimer = null;
    private HandlerThread thread;
    private WakeLock mWakeLock = null;
    private BenUtil mUtil;

    private final IBinder mBinder = new SdBinder();

    /**
     * class to handle binding the MainApp activity to this service
     * so it can access mSdData.
     */
    public class SdBinder extends Binder {
        BenServer getService() {
            return BenServer.this;
        }
    }

    /**
     * Constructor for SdServer class - does not do much!
     */
    public BenServer() {
        super();
        Log.v(TAG, "BenServer Created");
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "sdServer.onBind()");
        return mBinder;
    }


    /**
     * onCreate() - called when services is created.  Starts message
     * handler process to listen for messages from other processes.
     */
    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");

        mUtil = new BenUtil(getApplicationContext());

        // Create a wake lock, but don't use it until the service is started.
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
    }

    /**
     * onStartCommand - start the web server and the message loop for
     * communications with other processes.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand() - SdServer service starting");

        // Update preferences.
        Log.v(TAG, "onStartCommand() - calling updatePrefs()");
        updatePrefs();

        // Display a notification icon in the status bar of the phone to
        // show the service is running.
        Log.v(TAG, "showing Notification");
        showNotification();

        // Start the web server
        startWebServer();

        // Apply the wake-lock to prevent CPU sleeping (very battery intensive!)
        if (mWakeLock != null) {
            mWakeLock.acquire();
            Log.v(TAG, "Applied Wake Lock to prevent device sleeping");
        } else {
            Log.d(TAG, "mmm...mWakeLock is null, so not aquiring lock.  This shouldn't happen!");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy(): SdServer Service stopping");
        // release the wake lock to allow CPU to sleep and reduce
        // battery drain.
        if (mWakeLock != null) {
            mWakeLock.release();
            Log.v(TAG, "Released Wake Lock to allow device to sleep.");
        } else {
            Log.d(TAG, "mmm...mWakeLock is null, so not releasing lock.  This shouldn't happen!");
        }


        try {
            // Cancel the notification.
            Log.v(TAG, "onDestroy(): cancelling notification");
            mNM.cancel(NOTIFICATION_ID);
            // Stop web server
            Log.v(TAG, "onDestroy(): stopping web server");
            stopWebServer();
            // stop this service.
            Log.v(TAG, "onDestroy(): calling stopSelf()");
            stopSelf();

        } catch (Exception e) {
            Log.v(TAG, "Error in onDestroy() - " + e.toString());
        }
    }


    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        Log.v(TAG, "showNotification()");
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notification = builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.star_of_life_24x24)
                .setTicker("BenPlayer")
                .setAutoCancel(false)
                .setContentTitle("BenPlayer")
                .setContentText("Listening on http://" + mUtil.getLocalIpAddress() + ":8080")
                .build();

        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNM.notify(NOTIFICATION_ID, notification);
    }


    /**
     * Start the web server (on port 8080)
     */
    protected void startWebServer() {
        Log.v(TAG, "startWebServer()");
        if (webServer == null) {
            webServer = new WebServer();
            try {
                webServer.start();
            } catch (IOException ioe) {
                Log.w(TAG, "startWebServer(): Error: " + ioe.toString());
            }
            Log.w(TAG, "startWebServer(): Web server initialized.");
        } else {
            Log.v(TAG, "startWebServer(): server already running???");
        }
    }

    /**
     * Stop the web server - FIXME - doesn't seem to do anything!
     */
    protected void stopWebServer() {
        Log.v(TAG, "stopWebServer()");
        if (webServer != null) {
            webServer.stop();
            if (webServer.isAlive()) {
                Log.v(TAG, "stopWebServer() - server still alive???");
            } else {
                Log.v(TAG, "stopWebServer() - server died ok");
            }
            webServer = null;
        }
    }


    /**
     * updatePrefs() - update basic settings from the SharedPreferences
     * - defined in res/xml/prefs.xml
     */
    public void updatePrefs() {
        Log.v(TAG, "updatePrefs()");
        SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        try {
            //mSdDataSourceName = SP.getString("DataSource","undefined");
            //Log.v(TAG,"updatePrefs() - DataSource = "+mSdDataSourceName);

        } catch (Exception ex) {
            Log.v(TAG, "updatePrefs() - Problem parsing preferences!");
            Toast toast = Toast.makeText(getApplicationContext(), "Problem Parsing Preferences - Something won't work - Please go back to Settings and correct it!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    /**
     * Play YouTube video id idStr using the system YouTube App.
     * FIXME:  Make it use the YouTube API so we can have more control later.
     *
     * @param idStr - the YouTube ID of the video to be played.
     */
    private void playVideo(String idStr) {
        Intent intent;
        // switch the phone screen on.
        Log.v(TAG,"playVideo() - Switching screen on with ScreenOnActivity");
        intent = new Intent(getApplicationContext(),ScreenOnActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Start the youtube app to play the video.
        Log.v(TAG,"playVideo() - Playing Video "+idStr);
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + idStr));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("force_fullscreen",true);
        startActivity(intent);
    }

    /**
     * Class describing the seizure detector web server - appears on port
     * 8080.
     */
    private class WebServer extends NanoHTTPD {
        private String TAG = "WebServer";

        public WebServer() {
            // Set the port to listen on (8080)
            super(8080);
        }

        @Override
        public Response serve(String uri, Method method,
                              Map<String, String> header,
                              Map<String, String> parameters,
                              Map<String, String> files) {
            Log.v(TAG, "WebServer.serve() - uri=" + uri + " Method=" + method.toString());
            String answer = "Error - you should not see this message! - Something wrong in WebServer.serve()";

            Iterator it = parameters.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object value = parameters.get(key);
                Log.v(TAG, "Request parameters - key=" + key + " value=" + value);
            }


            if (uri.equals("/")) uri = "/index.html";
            switch (uri) {
                case "/play":
                    String idStr = "unknwon";
                    if (parameters.containsKey("id")) {
                        idStr = parameters.get("id");
                    } else {
                        idStr = "unknown";
                    }

                    Log.v(TAG, "WebServer.serve() - Playing video " + idStr);
                    try {
                        answer = "playing video " + idStr;
                        playVideo(idStr);
                    } catch (Exception ex) {
                        Log.v(TAG, "Error Playing video" + idStr + " - " + ex.toString());
                        answer = "Error playing video " + idStr + ": " + ex.toString();
                    }

                    break;
                case "/isBenPlayer":
                    answer = "True";
                    break;
                default:
                    if (uri.startsWith("/index.html") ||
                            uri.startsWith("/favicon.ico") ||
                            uri.startsWith("/js/") ||
                            uri.startsWith("/css/") ||
                            uri.startsWith("/img/")) {
                        //Log.v(TAG,"Serving File");
                        answer = "serveFile not implemented on this server!!";
                    } else {
                        Log.v(TAG, "WebServer.serve() - Unknown uri -" +
                                uri);
                        answer = "Unknown URI: ";
                    }
            }

            return new NanoHTTPD.Response(answer);
        }
    }
}

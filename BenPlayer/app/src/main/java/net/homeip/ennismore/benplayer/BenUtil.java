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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import net.homeip.ennismore.benplayer.BenServer;
import net.homeip.ennismore.benplayer.BenServiceConnection;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * OsdUtil - OpenSeizureDetector Utilities
 * Deals with starting and stopping the background service and binding to it to receive data.
 */
public class BenUtil {
    /**
     * Based on http://stackoverflow.com/questions/7440473/android-how-to-check-if-the-intent-service-is-still-running-or-has-stopped-running
     */
    private Context mContext;
    private String TAG = "OsdUtil";

    public BenUtil(Context context) {
        mContext = context;
    }

    public boolean isServerRunning() {
        //Log.v(TAG,"isServerRunning()................");
        ActivityManager manager =
                (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            //Log.v(TAG,"Service: "+service.service.getClassName());
            if ("net.homeip.ennismore.benplayer.BenServer"
                    .equals(service.service.getClassName())) {
                //Log.v(TAG,"Yes!");
                return true;
            }
        }
        //Log.v(TAG,"No!");
        return false;
    }

    /**
     * Start the SdServer service
     */
    public void startServer() {
        Log.v(TAG,"startServer() - starting server....");
        // Start the server
        Intent sdServerIntent;
        sdServerIntent = new Intent(mContext, BenServer.class);
        sdServerIntent.setData(Uri.parse("Start"));
        showToast("Starting Service...");
        mContext.startService(sdServerIntent);
    }

    /**
     * Stop the SdServer service
     */
    public void stopServer() {
        Log.v(TAG, "stopping Server...");

        // then send an Intent to stop the service.
        Intent sdServerIntent;
        sdServerIntent = new Intent(mContext, BenServer.class);
        sdServerIntent.setData(Uri.parse("Stop"));
        mContext.stopService(sdServerIntent);
    }


    /**
     * bind an activity to to an already running server.
     */
    public void bindToServer(Activity activity, BenServiceConnection sdServiceConnection) {
        Log.v(TAG, "bindToServer() - binding to SdServer");
        Intent intent = new Intent(sdServiceConnection.mContext, BenServer.class);
        activity.bindService(intent, sdServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * unbind an activity from server
     */
    public void unbindFromServer(Activity activity, BenServiceConnection sdServiceConnection) {
        // unbind this activity from the service if it is bound.
        if (sdServiceConnection.mBound) {
            Log.v(TAG, "unbindFromServer() - unbinding");
            try {
                activity.unbindService(sdServiceConnection);
                sdServiceConnection.mBound = false;
            } catch (Exception ex) {
                Log.e(TAG, "unbindFromServer() - error unbinding service - " + ex.toString());
            }
        } else {
            Log.v(TAG, "unbindFromServer() - not bound to server - ignoring");
        }
    }

    public String getAppVersionName() {
        String versionName = "unknown";
        // From http://stackoverflow.com/questions/4471025/
        //         how-can-you-get-the-manifest-version-number-
        //         from-the-apps-layout-xml-variable
        final PackageManager packageManager = mContext.getPackageManager();
        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
                versionName = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.v(TAG, "failed to find versionName");
                versionName = null;
            }
        }
        return versionName;
    }

    /**
     * get the ip address of the phone.
     * Based on http://stackoverflow.com/questions/11015912/how-do-i-get-ip-address-in-ipv4-format
     */
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    //Log.v(TAG,"ip1--:" + inetAddress);
                    //Log.v(TAG,"ip2--:" + inetAddress.getHostAddress());

                    // for getting IPV4 format
                    if (!inetAddress.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(
                            inetAddress.getHostAddress())) {

                        String ip = inetAddress.getHostAddress().toString();
                        //Log.v(TAG,"ip---::" + ip);
                        return ip;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    /**
     * Display a Toast message on screen.
     * @param msg - message to display.
     */
    public void showToast(String msg) {
        Toast.makeText(mContext, msg,
                Toast.LENGTH_LONG).show();
    }

}

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

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;



/**
 * Defines callbacks for service binding, passed to bindService()
 */
public class BenServiceConnection implements ServiceConnection {
    private String TAG = "SdServiceConnection";
    public BenServer mSdServer = null;
    public boolean mBound = false;
    public Context mContext;

    public BenServiceConnection(Context context) {
        mContext = context;
    }

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        BenServer.SdBinder binder = (BenServer.SdBinder) service;
        mSdServer = binder.getService();
        mBound = true;
        if (mSdServer != null) {
            Log.v(TAG, "onServiceConnected() - Asking server to update its settings");
            mSdServer.updatePrefs();
        } else {
            Log.v(TAG, "onServiceConnected() - mSdServer is null - this is wrong!");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        Log.v(TAG, "onServiceDisonnected()");
        mBound = false;
    }




}

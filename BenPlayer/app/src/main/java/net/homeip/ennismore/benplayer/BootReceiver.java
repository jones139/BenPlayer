package net.homeip.ennismore.benplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by graham on 26/12/15.
 */
public class BootReceiver extends BroadcastReceiver {
    private String TAG = "BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG,"onReceive()");
        Intent myIntent = new Intent(context, BenServer.class);
        context.startService(myIntent);

    }
}

package net.homeip.ennismore.benplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoPlaybackActivity extends Activity {
    private View mContentView;
    private String TAG = "VideoPlayBackActivity";
    private MediaPlayer mMediaPlayer;
    private BenUtil mUtil;
    private BenServiceConnection mConnection;
    public final static String BROADCAST_ID = "net.homeip.ennismore.benplayer.videoctrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_playback);

        mContentView = findViewById(R.id.fullscreen_content);

        mUtil = new BenUtil(getApplicationContext());

        Log.v(TAG,"onCreate - registering Broadcast Receiver");
        // Register for broadcasts from the BenServer service.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ID);
        registerReceiver(bReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy() - unregistering broadcast receiver");
        unregisterReceiver(bReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();


        Uri mediaUri = Uri.parse(getIntent().getExtras().getString("MediaUri"));
        Log.v(TAG, "mediaUri=" + mediaUri.toString());

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    SurfaceHolder surface = ((SurfaceView) (findViewById(R.id.fullscreen_content))).getHolder();
                    mediaPlayer.setSurface(surface.getSurface());
                    mediaPlayer.start();
                }
            });
            mMediaPlayer.setDataSource(getApplicationContext(), mediaUri);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "Error Playing Video - " + e.toString());
            e.printStackTrace();
            showToast("Error Playing Video - " + e.toString());
        }



    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "BroadCastReceiver.onReceive()");
            if(intent.getAction().equals(BROADCAST_ID)) {
                String cmd = intent.getExtras().getString("cmd");
                Log.v(TAG,"onReceive() - cmd= "+cmd);
                if (cmd.equalsIgnoreCase("stop")) {
                    Log.v(TAG,"stop command received...");
                    mMediaPlayer.stop();
                    finish();
                }
                if (cmd.equalsIgnoreCase("forward")) {
                    Log.v(TAG, "forward command received...");
                    int pos = mMediaPlayer.getCurrentPosition();
                    pos = pos + 30*1000;  // forward 30 sec.
                    mMediaPlayer.seekTo(pos);
                }

                if (cmd.equalsIgnoreCase("backward")) {
                    Log.v(TAG,"backward command received...");
                    int pos = mMediaPlayer.getCurrentPosition();
                    pos = pos - 30*1000;  // forward 30 sec.
                    if (pos < 0) pos = 0;
                    mMediaPlayer.seekTo(pos);
                }

            }
        }
    };


    private void showToast(String msg) {
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.show();

    }


}

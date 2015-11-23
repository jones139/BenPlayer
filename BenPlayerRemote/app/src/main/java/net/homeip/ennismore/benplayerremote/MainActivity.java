package net.homeip.ennismore.benplayerremote;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivity";
    private ArrayList<String> videoIds;
    private String benPlayerIp = "0.0.0.0";
    private String benPlayerPort = "80";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        videoIds = new ArrayList<String>();
        setContentView(R.layout.activity_main);

        Button b;
        ((ImageButton) findViewById(R.id.imageButton1)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imageButton2)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imageButton3)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imageButton4)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imageButton5)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imageButton6)).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");
        updatePrefs();

    }

    @Override
    public void onClick(View v) {
        int buttonNo = 0;
        if (v == findViewById(R.id.imageButton1)) buttonNo = 1;
        if (v == findViewById(R.id.imageButton2)) buttonNo = 2;
        if (v == findViewById(R.id.imageButton3)) buttonNo = 3;
        if (v == findViewById(R.id.imageButton4)) buttonNo = 4;
        if (v == findViewById(R.id.imageButton5)) buttonNo = 5;
        if (v == findViewById(R.id.imageButton6)) buttonNo = 6;

        Log.v(TAG, "ButtonNo = " + buttonNo);
        AsyncTask pv = new PlayVideo().execute(videoIds.get(buttonNo));
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
            //benPlayerIp = SP.getString("BenPlayerIp", "10.51.118.50");
            benPlayerIp = SP.getString("BenPlayerIp", "192.168.42.129");
            benPlayerPort = SP.getString("BenPlayerPort", "8080");
            videoIds.add(SP.getString("VideoId0", "xB5ceAruYrI"));
            videoIds.add(SP.getString("VideoId1", "9ogQ0uge06o"));
            videoIds.add(SP.getString("VideoId2", "IXGgDIj5KvA"));
            videoIds.add(SP.getString("VideoId3", "eXvD86nAvK4"));
            videoIds.add(SP.getString("VideoId4", "NRuKbQqHHGc"));
            videoIds.add(SP.getString("VideoId5", "OQmvg_nKOoc"));
            videoIds.add(SP.getString("VideoId6", "5HDw7sQE2H0"));

            Log.v(TAG, "benPlayerIp = " + benPlayerIp);
            Log.v(TAG, "benPlayerPort = " + benPlayerPort);
            for (int i = 0; i < videoIds.size(); i++) {
                Log.v(TAG, "videoId(" + i + ") = " + videoIds.get(i));
            }
            //Log.v(TAG,"updatePrefs() - DataSource = "+mSdDataSourceName);

        } catch (Exception ex) {
            Log.v(TAG, "updatePrefs() - Problem parsing preferences!" + ex.toString());
            Toast toast = Toast.makeText(getApplicationContext(), "Problem Parsing Preferences - Something won't work - Please go back to Settings and correct it!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Make Network connection to a BenPlayer instance to play a video
     */
    public class PlayVideo extends AsyncTask<String, Integer, String> {
        private String mMsg;
        @Override
        protected String doInBackground(String... params) {
            String videoId = params[0];
            Log.v(TAG, "PlayVideo.doInBackground() - videoId = " + videoId);

            String url = "http://"+benPlayerIp+":"+benPlayerPort+"/play?id="+videoId;
            Log.v(TAG,"url="+url);
            byte[] result = null;
            final DefaultHttpClient client = new DefaultHttpClient();
            final HttpGet getRequest = new HttpGet(url);
            try {
                HttpResponse response = client.execute(getRequest);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    Log.w(TAG, "Error " + statusCode +
                            " playing video using " + url);
                    mMsg = mMsg + "Error playing video using " + url + " Status Code = " + statusCode;
                    return "Failed";
                }

            } catch (Exception e) {
                // You Could provide a more explicit error message for IOException
                getRequest.abort();
                Log.e(TAG, "Something went wrong while" +
                        " playing video using url: " + url + ": Error is: " + e.toString());
                return "Failed";
            }
            return "Success";
        }

    }


}

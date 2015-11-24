package net.homeip.ennismore.benplayerremote;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    private String mYouTubeApiKey = "AIzaSyDlpSLX40vE-5mCZZCc6ILvHHCPQBPS4Jk";
    private ArrayList<String> videoIds;
    private String benPlayerIp = "0.0.0.0";
    private String benPlayerPort = "80";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        videoIds = new ArrayList<String>();
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");
        updatePrefs();

        ImageButton b;
        AsyncTask at;
        b = ((ImageButton) findViewById(R.id.imageButton0));
        b.setOnClickListener(this);
        at = new ThumbnailRetriever(b).execute(videoIds.get(0));
        // Button 1
        b = ((ImageButton) findViewById(R.id.imageButton1));
        b.setOnClickListener(this);
        at = new ThumbnailRetriever(b).execute(videoIds.get(1));
        // Button 2
        b = ((ImageButton) findViewById(R.id.imageButton2));
        b.setOnClickListener(this);
        at = new ThumbnailRetriever(b).execute(videoIds.get(2));
        // Button 3
        b = ((ImageButton) findViewById(R.id.imageButton3));
        b.setOnClickListener(this);
        at = new ThumbnailRetriever(b).execute(videoIds.get(3));
        // Button 4
        b = ((ImageButton) findViewById(R.id.imageButton4));
        b.setOnClickListener(this);
        at = new ThumbnailRetriever(b).execute(videoIds.get(4));
        // Button 5
        b = ((ImageButton) findViewById(R.id.imageButton5));
        b.setOnClickListener(this);
        at = new ThumbnailRetriever(b).execute(videoIds.get(5));
        // Button 6
        b = ((ImageButton) findViewById(R.id.imageButton6));
        b.setOnClickListener(this);
        at = new ThumbnailRetriever(b).execute(videoIds.get(6));

        Button bb = (Button) findViewById(R.id.settingsButton);
        bb.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int buttonNo = 0;

        if (v == findViewById(R.id.settingsButton)) {
            Log.v(TAG, "settings button clicked");
            try {
                Intent intent = new Intent(
                        MainActivity.this,
                        PrefsActivity.class);
                startActivity(intent);
            } catch (Exception ex) {
                Log.v(TAG, "exception starting settings activity " + ex.toString());
            }
            return;
        }

        if (v == findViewById(R.id.imageButton0)) buttonNo = 0;
        if (v == findViewById(R.id.imageButton1)) buttonNo = 1;
        if (v == findViewById(R.id.imageButton2)) buttonNo = 2;
        if (v == findViewById(R.id.imageButton3)) buttonNo = 3;
        if (v == findViewById(R.id.imageButton4)) buttonNo = 4;
        if (v == findViewById(R.id.imageButton5)) buttonNo = 5;
        if (v == findViewById(R.id.imageButton6)) buttonNo = 6;

        Log.v(TAG, "ButtonNo = " + buttonNo);
        showToast("Playing Video Number "+buttonNo);
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

        /**
         * Send a 'play' command to the BenPlayer for the specified video Id.
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(String... params) {
            String videoId = params[0];
            Log.v(TAG, "PlayVideo.doInBackground() - videoId = " + videoId);

            String url = "http://" + benPlayerIp + ":" + benPlayerPort + "/play?id=" + videoId;
            Log.v(TAG, "url=" + url);
            byte[] result = null;
            final DefaultHttpClient client = new DefaultHttpClient();
            final HttpGet getRequest = new HttpGet(url);
            try {
                HttpResponse response = client.execute(getRequest);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    Log.w(TAG, "PlayVideo.doInBackground - Error " + statusCode +
                            " playing video using " + url);
                    return "Error " + statusCode +
                            " playing video using " + url;
                } else {
                    Log.v(TAG,"PlayVideo.doInBackground - Playing Video");
                    return ("Playing Video....");
                }

            } catch (Exception e) {
                // You Could provide a more explicit error message for IOException
                getRequest.abort();
                Log.e(TAG, "PlayVideo.doInBackground() - Something went wrong while" +
                        " playing video using url: " + url + ": Error is: " + e.toString());
                return ("Something went wrong while" +
                        " playing video using url: " + url + ": Error is: " + e.toString());

            }
        }

        /**
         * When we have finished sending the command to play the video, display the resulting message on screen.
         * @param msg
         */
        @Override
        protected void onPostExecute(String msg) {
            showToast(msg);
        }

    }


    /**
     * Retrieve thumbnail image of a YouTube video and modify an ImageButton to use the
     * thumbnail image.
     * Usage:
     * AsyncTask at = new ThumbnailReceiver(ImageButton ib);
     * at.execute(Strinv videoId);
     */
    public class ThumbnailRetriever extends AsyncTask<String, Integer, Bitmap> {
        private String mMsg;
        private ImageButton mImageButton;

        public ThumbnailRetriever(ImageButton ib) {
            mImageButton = ib;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String videoId = params[0];
            Log.v(TAG, "getThumbnail.doInBackground() - videoId = " + videoId);

            String url = "http://img.youtube.com/vi/" + videoId + "/0.jpg";
            Log.v(TAG, "url=" + url);
            byte[] result = null;
            final DefaultHttpClient client = new DefaultHttpClient();
            final HttpGet getRequest = new HttpGet(url);
            try {
                HttpResponse response = client.execute(getRequest);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    Log.w(TAG, "Error " + statusCode +
                            " retrieving thumbnail using " + url);
                    return null;
                } else {
                    final HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream inputStream = null;
                        try {
                            // getting contents from the stream
                            inputStream = entity.getContent();
                            // decoding stream data back into image Bitmap that android understands
                            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            Log.v(TAG, "getThumbnail.doInBackground() - Returning Bitmap");
                            return bitmap;
                        } catch (Exception ex) {
                            Log.v(TAG, "getThumbnail.doInBackground() - Error - " + ex.toString());
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            entity.consumeContent();
                            return null;
                        }
                    }
                }

            } catch (Exception e) {
                // You Could provide a more explicit error message for IOException
                getRequest.abort();
                Log.e(TAG, "Something went wrong while" +
                        " playing video using url: " + url + ": Error is: " + e.toString());
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //Log.v(TAG, "onPostExecute()");
            super.onPostExecute(bitmap);
            Log.v(TAG, "onPostExecute()");
            try {
                if (bitmap != null) {
                    Log.v(TAG, "setting imagebutton image");
                    mImageButton.setImageBitmap(bitmap);
                } else {
                    Log.v(TAG, "onPostExecute - null image received");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onPostExecute()");
                e.printStackTrace();
            }
        }

    }

    /**
     * Display a Toast message on screen.
     *
     * @param msg - message to display.
     */
    public void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg,
                Toast.LENGTH_LONG).show();
    }


}

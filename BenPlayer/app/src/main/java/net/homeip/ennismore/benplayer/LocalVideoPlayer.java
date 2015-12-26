package net.homeip.ennismore.benplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * Created by graham on 25/11/15.
 */
public class LocalVideoPlayer {
    private String TAG = "LocalVideoPlayer";
    private Context mContext;
    private MediaPlayer mMediaPlayer;


    public LocalVideoPlayer(Context context) {
        mContext = context;
    }

    public Uri getMediaUri(long mediaId) {
        Uri baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri uri = ContentUris.withAppendedId(baseUri,mediaId);
        Log.v(TAG,"getMediaUri() - mediaId="+mediaId+" - uri = "+uri.toString());
        return uri;
    }

    public String getLocalVideoList() {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            // query failed, handle error.
            Log.e(TAG, "Error querying local videos");
        } else if (!cursor.moveToFirst()) {
            // no media on the device
            Log.i(TAG, "No Videos found");
        } else {
            try {

                int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.TITLE);
                int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Video.Media._ID);
                do {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    JSONObject lvObj = new JSONObject();
                    lvObj.put("id", id);
                    lvObj.put("title", title);
                    Log.v(TAG, "id=" + id + " title=" + title);
                    ja.put(lvObj);
                    // ...process entry...
                } while (cursor.moveToNext());
                jo.put("LocalVideos", ja);
                return (jo.toString());
            } catch (JSONException ex) {
                Log.v(TAG, "Error creating JSON object - " + ex.toString());
                return null;
            }
        }
        return null;
    }

    public void playLocalVideo(String idStr) {
        Intent intent;
        // switch the phone screen on.
        Log.v(TAG,"playLocalVideo() - Switching screen on with ScreenOnActivity");
        intent = new Intent(mContext,ScreenOnActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

        // Start the video player app.
        Log.v(TAG,"playLocalVideo() - Playing Video "+idStr);
        long id = Long.parseLong(idStr);
        Uri uri = getMediaUri(id);
        intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("force_fullscreen",true);
        mContext.startActivity(intent);

    }

    public void stopLocalVideo() {
        // FIXME - this doesn't work!!!
        Intent intent;

        // Start the video player app.
        Log.v(TAG,"stopLocalVideo() - Stopping Video ");
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("force_fullscreen",true);
        mContext.startActivity(intent);

    }

}

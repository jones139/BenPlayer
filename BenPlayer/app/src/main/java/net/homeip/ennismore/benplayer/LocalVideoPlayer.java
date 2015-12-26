package net.homeip.ennismore.benplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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

    private class LocalVideo {
        public long id;
        public String title;

        public LocalVideo(long id, String title) {
            this.id = id;
            this.title = title;
        }
    }


    public LocalVideoPlayer(Context context) {
        mContext = context;
    }

    public String getLocalVideoList() {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            // no media on the device
        } else {
            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            do {
                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                LocalVideo lv = new LocalVideo(thisId,thisTitle);
                ja.put(lv);
                // ...process entry...
            } while (cursor.moveToNext());
        }
        try {
            jo.put("LocalVideos", ja);
            return (jo.toString());
        } catch (JSONException ex) {
            Log.v(TAG, "Error creating JSON object - "+ex.toString());
            return null;
        }
    }

    public void playLocalVideo(long id) {
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

        mMediaPlayer = new MediaPlayer();
        //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mContext, contentUri);
        } catch (IOException ex) {
            Log.v(TAG,"Error Playing Video - "+ex.toString());
        }
    }
}

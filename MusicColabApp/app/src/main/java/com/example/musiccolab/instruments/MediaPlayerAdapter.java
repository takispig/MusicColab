package com.example.musiccolab.instruments;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class MediaPlayerAdapter {

    private MediaPlayer mp;
    private final boolean testingMode;
    Integer id;

    public MediaPlayerAdapter(Context context, Integer id, boolean testingMode) {
        this.testingMode = testingMode;
        if (!this.testingMode) {
            this.id = id;
            mp = MediaPlayer.create(context, id);
            mp.setOnErrorListener((mp, what, extra) -> true);
        }
    }

    public void start() {
        if (!testingMode) {
            try {
                mp.start();
            } catch (IllegalStateException e) {
                Log.e(getClass().getSimpleName(), "IllegalStateException by MediaPlayer");
            }
        }
    }


    public void stop() {
        if (!testingMode) {
            try {
                if (mp.isPlaying()) {
                    mp.pause();
                    mp.reset();
                    // mp.release();
                }
            } catch (IllegalStateException e) {
                Log.e(getClass().getSimpleName(), "IllegalStateException by MediaPlayer");
            }
        }
    }
}
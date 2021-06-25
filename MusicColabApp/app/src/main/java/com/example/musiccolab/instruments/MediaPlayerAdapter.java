package com.example.musiccolab.instruments;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaPlayerAdapter {

    private MediaPlayer mp;
    private final boolean testingMode;

    public MediaPlayerAdapter(Context context, int id, boolean testingMode) {
        this.testingMode = testingMode;
        if (!this.testingMode) {
            mp = MediaPlayer.create(context, id);
        }
    }

    public void start() {
        if (!testingMode) {
            mp.start();
        }
    }

    public void stop() {
        if (!testingMode) {
            try {
                if(mp.isPlaying()) {
                    mp.stop();
                    mp.prepare();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
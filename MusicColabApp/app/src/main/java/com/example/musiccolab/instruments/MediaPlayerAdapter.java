package com.example.musiccolab.instruments;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaPlayerAdapter {

    private MediaPlayer mp;
    private final boolean testingMode;
    public int user;
    public String tone;

    public MediaPlayerAdapter(Context context, int id, boolean testingMode,int user,String tone) {
        this.testingMode = testingMode;
        if (!this.testingMode) {
            mp = MediaPlayer.create(context, id);
            mp.setOnErrorListener((mp, what, extra) -> true);
            this.user=user;
            this.tone=tone;
        }
    }

    public void start() {
        if (!testingMode) {
            mp.start();
        }
    }


    public void stop() {
        if (!testingMode) {
            if(mp.isPlaying()) {
                mp.pause();
                mp.reset();
                mp.release();
            }
        }
    }
}
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
}
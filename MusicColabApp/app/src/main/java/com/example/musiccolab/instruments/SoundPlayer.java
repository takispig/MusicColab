package com.example.musiccolab.instruments;

import android.media.SoundPool;

import com.example.musiccolab.Lobby;
import com.example.musiccolab.Login;
import com.example.musiccolab.R;

import java.util.HashMap;

public class SoundPlayer {
    private static final short NETWORK_THREAD_ACTION_SEND_TONE = 7;
    public static final byte NETWORK_THREAD_DEFAULT_TONE_TYPE = (byte) 8;
    public static final byte NETWORK_THREAD_DEFAULT_TONE_ACTION = (byte) 10;
    public static final int SOUND_PRIORITY = 1;
    public static final int LEFT_VOLUME = 1;
    public static final int RIGHT_VOLUME = 1;
    public static final int LOOP = 0;
    public static final int RATE = 1;
    private final Lobby lobby;
    private HashMap<String, Integer> soundIDS;
    private SoundPool soundPool;

    public SoundPlayer(Lobby lobby) {
        this.lobby = lobby;
    }

    public void generateToneList() {
        soundPool = new SoundPool.Builder().setMaxStreams(5).build();
        soundIDS = new HashMap<>();
        soundIDS.put("piano0", R.raw.p_c);
        soundIDS.put("piano1", R.raw.p_d);
        soundIDS.put("piano2", R.raw.p_e);
        soundIDS.put("piano3", R.raw.p_f);
        soundIDS.put("piano4", R.raw.p_g);
        soundIDS.put("piano5", R.raw.p_a);
        soundIDS.put("piano6", R.raw.p_h);
        soundIDS.put("piano7", R.raw.p_c2);
        soundIDS.put("drums0", R.raw.drum_a);
        soundIDS.put("drums1", R.raw.drum_b);
        soundIDS.put("therm0", R.raw.c);
        soundIDS.put("therm1", R.raw.d);
        soundIDS.put("therm2", R.raw.e);
        soundIDS.put("therm3", R.raw.f);
        soundIDS.put("therm4", R.raw.g);
        soundIDS.put("therm5", R.raw.a);
        soundIDS.put("therm6", R.raw.h);
        soundIDS.put("therm7", R.raw.c2);
    }

    public void sendToneToServer(String toneAsString) {
        Login.networkThread.action = NETWORK_THREAD_ACTION_SEND_TONE;
        Login.networkThread.toneType = NETWORK_THREAD_DEFAULT_TONE_TYPE;
        Login.networkThread.toneAction = NETWORK_THREAD_DEFAULT_TONE_ACTION;
        Login.networkThread.data = toneAsString;
        Login.networkThread.start();
    }

    public void playToneFromServer(String toneAsString) {
        Integer resId = soundIDS.get(toneAsString);
        if (resId != null) {
            int soundPoolID = soundPool.load(lobby, resId, SOUND_PRIORITY);
            soundPool.play(soundPoolID, LEFT_VOLUME, RIGHT_VOLUME, SOUND_PRIORITY, LOOP, RATE);
            System.out.println("Played: " + toneAsString + " (id: " + resId + ")");
        }
    }

    // TODO implement pausing of piano tones
    public void pause(String s) {
        Integer resId = soundIDS.get(s);
        if (resId != null) {
            int soundPoolID = soundPool.load(lobby, resId, SOUND_PRIORITY);
            soundPool.pause(soundPoolID);
        }
    }
}
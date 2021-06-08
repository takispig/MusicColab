package com.example.musiccolab.instruments;

import com.example.musiccolab.Lobby;
import com.example.musiccolab.Login;
import com.example.musiccolab.R;

import java.util.HashMap;
import java.util.Optional;

import android.media.MediaPlayer;

public class SoundPlayer {
    private static final short NETWORK_THREAD_ACTION_SEND_TONE = 7;
    public static final byte NETWORK_THREAD_DEFAULT_TONE_TYPE = (byte) 8;
    public static final byte NETWORK_THREAD_DEFAULT_TONE_ACTION = (byte) 10;
    private final Lobby lobby;
    private HashMap<String, MediaPlayer> sounds;
    private final HashMap<String, Long> rtt;

    public SoundPlayer(Lobby lobby) {
        this.lobby = lobby;
        rtt = new HashMap<>();
    }

    public void generateToneList() {
        sounds = new HashMap<>();
        sounds.put("piano0", MediaPlayer.create(lobby, R.raw.p_c));
        sounds.put("piano1", MediaPlayer.create(lobby, R.raw.p_d));
        sounds.put("piano2", MediaPlayer.create(lobby, R.raw.p_e));
        sounds.put("piano3", MediaPlayer.create(lobby, R.raw.p_f));
        sounds.put("piano4", MediaPlayer.create(lobby, R.raw.p_g));
        sounds.put("piano5", MediaPlayer.create(lobby, R.raw.p_a));
        sounds.put("piano6", MediaPlayer.create(lobby, R.raw.p_h));
        sounds.put("piano7", MediaPlayer.create(lobby, R.raw.p_c2));
        sounds.put("drums0", MediaPlayer.create(lobby, R.raw.drum_a));
        sounds.put("drums1", MediaPlayer.create(lobby, R.raw.drum_b));
        sounds.put("therm0", MediaPlayer.create(lobby, R.raw.c));
        sounds.put("therm1", MediaPlayer.create(lobby, R.raw.d));
        sounds.put("therm2", MediaPlayer.create(lobby, R.raw.e));
        sounds.put("therm3", MediaPlayer.create(lobby, R.raw.f));
        sounds.put("therm4", MediaPlayer.create(lobby, R.raw.g));
        sounds.put("therm5", MediaPlayer.create(lobby, R.raw.a));
        sounds.put("therm6", MediaPlayer.create(lobby, R.raw.h));
        sounds.put("therm7", MediaPlayer.create(lobby, R.raw.c2));
    }

    public void sendToneToServer(String toneAsString) {
        Login.networkThread.action = NETWORK_THREAD_ACTION_SEND_TONE;
        Login.networkThread.toneType = NETWORK_THREAD_DEFAULT_TONE_TYPE;
        Login.networkThread.toneAction = NETWORK_THREAD_DEFAULT_TONE_ACTION;
        Login.networkThread.data = toneAsString;
        long timeStampSend = System.nanoTime();
        rtt.put(toneAsString, timeStampSend);
    }

    public void playToneFromServer(String toneAsString) {
        long timeStampReceived = System.nanoTime();
        long duration = -1;
        Optional<MediaPlayer> tone = Optional.ofNullable(sounds.get(toneAsString));
        if (tone.isPresent()) {
            Optional<Long> timeStampSendOptional = Optional.ofNullable(rtt.remove(toneAsString));
            if (timeStampSendOptional.isPresent()) {
                duration = timeStampSendOptional.get() - timeStampReceived;
            }
            tone.get().start();
            System.out.println("-----> -----> -----> -----> -----> Played: " + toneAsString + " (" + duration + "ns)");
        }
    }
}
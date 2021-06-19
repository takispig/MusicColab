package com.example.musiccolab.instruments;

import com.example.musiccolab.CommunicationHandling;
import com.example.musiccolab.Lobby;
import com.example.musiccolab.Login;
import com.example.musiccolab.R;

import java.util.HashMap;
import java.util.Optional;

public class SoundPlayer {
    private static final short NETWORK_THREAD_ACTION_SEND_TONE = 7;
    public static final byte NETWORK_THREAD_DEFAULT_TONE_TYPE = (byte) 8;
    public static final byte NETWORK_THREAD_DEFAULT_TONE_ACTION = (byte) 10;
    public static CommunicationHandling NETWORK_THREAD = Login.networkThread;
    private final Lobby lobby;
    private HashMap<String, MediaPlayerAdapter> sounds;
    private boolean testingMode = false;

    public SoundPlayer(Lobby lobby) {
        this.lobby = lobby;
    }

    public void activateTestingMode(CommunicationHandling dummyNetworkThread) {
        NETWORK_THREAD = dummyNetworkThread;
        testingMode = true;
    }

    public void generateToneList() {
        sounds = new HashMap<>();
        sounds.put("piano0", new MediaPlayerAdapter(lobby, R.raw.p_c, testingMode));
        sounds.put("piano1", new MediaPlayerAdapter(lobby, R.raw.p_d, testingMode));
        sounds.put("piano2", new MediaPlayerAdapter(lobby, R.raw.p_e, testingMode));
        sounds.put("piano3", new MediaPlayerAdapter(lobby, R.raw.p_f, testingMode));
        sounds.put("piano4", new MediaPlayerAdapter(lobby, R.raw.p_g, testingMode));
        sounds.put("piano5", new MediaPlayerAdapter(lobby, R.raw.p_a, testingMode));
        sounds.put("piano6", new MediaPlayerAdapter(lobby, R.raw.p_h, testingMode));
        sounds.put("piano7", new MediaPlayerAdapter(lobby, R.raw.p_c2, testingMode));
        sounds.put("drums0", new MediaPlayerAdapter(lobby, R.raw.drum_a, testingMode));
        sounds.put("drums1", new MediaPlayerAdapter(lobby, R.raw.drum_b, testingMode));
        sounds.put("drums2", new MediaPlayerAdapter(lobby, R.raw.drum_c, testingMode));
        sounds.put("therm0", new MediaPlayerAdapter(lobby, R.raw.c, testingMode));
        sounds.put("therm1", new MediaPlayerAdapter(lobby, R.raw.d, testingMode));
        sounds.put("therm2", new MediaPlayerAdapter(lobby, R.raw.e, testingMode));
        sounds.put("therm3", new MediaPlayerAdapter(lobby, R.raw.f, testingMode));
        sounds.put("therm4", new MediaPlayerAdapter(lobby, R.raw.g, testingMode));
        sounds.put("therm5", new MediaPlayerAdapter(lobby, R.raw.a, testingMode));
        sounds.put("therm6", new MediaPlayerAdapter(lobby, R.raw.h, testingMode));
        sounds.put("therm7", new MediaPlayerAdapter(lobby, R.raw.c2, testingMode));
    }

    public void sendToneToServer(String toneAsString) {
        Optional<MediaPlayerAdapter> tone = Optional.ofNullable(sounds.get(toneAsString));
        tone.ifPresent(MediaPlayerAdapter::start);
        NETWORK_THREAD.action = NETWORK_THREAD_ACTION_SEND_TONE;
        NETWORK_THREAD.toneType = NETWORK_THREAD_DEFAULT_TONE_TYPE;
        NETWORK_THREAD.toneAction = NETWORK_THREAD_DEFAULT_TONE_ACTION;
        NETWORK_THREAD.data = toneAsString;
    }

    public void playToneFromServer(String toneAsString) {
        Optional<MediaPlayerAdapter> tone = Optional.ofNullable(sounds.get(toneAsString));
        tone.ifPresent(MediaPlayerAdapter::start);
    }
}
package com.example.musiccolab.instruments;

import com.example.musiccolab.CommunicationHandling;
import com.example.musiccolab.Lobby;
import com.example.musiccolab.Login;
import com.example.musiccolab.R;

import java.util.HashMap;
import java.util.Optional;

public class SoundPlayer {
    private static final short NETWORK_THREAD_ACTION_SEND_TONE = 7;
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
        sounds.put("piano0", new MediaPlayerAdapter(lobby, R.raw.piano0_c4, testingMode));
        sounds.put("piano1", new MediaPlayerAdapter(lobby, R.raw.piano1_d4, testingMode));
        sounds.put("piano2", new MediaPlayerAdapter(lobby, R.raw.piano2_e4, testingMode));
        sounds.put("piano3", new MediaPlayerAdapter(lobby, R.raw.piano3_f4, testingMode));
        sounds.put("piano4", new MediaPlayerAdapter(lobby, R.raw.piano4_g4, testingMode));
        sounds.put("piano5", new MediaPlayerAdapter(lobby, R.raw.piano5_a4, testingMode));
        sounds.put("piano6", new MediaPlayerAdapter(lobby, R.raw.piano6_h4, testingMode));
        sounds.put("piano7", new MediaPlayerAdapter(lobby, R.raw.piano7_c5, testingMode));
        sounds.put("drums0", new MediaPlayerAdapter(lobby, R.raw.drum_a, testingMode));
        sounds.put("drums1", new MediaPlayerAdapter(lobby, R.raw.drum_b, testingMode));
        sounds.put("drums2", new MediaPlayerAdapter(lobby, R.raw.drum_c, testingMode));
        sounds.put("therm0", new MediaPlayerAdapter(lobby, R.raw.theremin0_c4, testingMode));
        sounds.put("therm1", new MediaPlayerAdapter(lobby, R.raw.theremin1_d4, testingMode));
        sounds.put("therm2", new MediaPlayerAdapter(lobby, R.raw.theremin2_e4, testingMode));
        sounds.put("therm3", new MediaPlayerAdapter(lobby, R.raw.theremin3_f4, testingMode));
        sounds.put("therm4", new MediaPlayerAdapter(lobby, R.raw.theremin4_g4, testingMode));
        sounds.put("therm5", new MediaPlayerAdapter(lobby, R.raw.theremin5_a4, testingMode));
        sounds.put("therm6", new MediaPlayerAdapter(lobby, R.raw.theremin6_h4, testingMode));
        sounds.put("therm7", new MediaPlayerAdapter(lobby, R.raw.theremin7_c5, testingMode));
    }

    public void sendToneToServer(String toneAsString, int toneAction) {
        playTone(toneAsString,toneAction);
        NETWORK_THREAD.action = NETWORK_THREAD_ACTION_SEND_TONE;
        NETWORK_THREAD.toneAction = (byte) toneAction;
        NETWORK_THREAD.data = toneAsString;
    }

    public void playTone(String toneAsString,int toneAction) {
        Optional<MediaPlayerAdapter> tone = Optional.ofNullable(sounds.get(toneAsString));
        if(toneAsString.startsWith("therm")){
            sounds.forEach((t,mp)->{ if(t.startsWith("therm"))mp.stop();});
        }
        if(toneAction==1) tone.ifPresent(MediaPlayerAdapter::start);
        else if(toneAction==0) tone.ifPresent(MediaPlayerAdapter::stop);
    }
}
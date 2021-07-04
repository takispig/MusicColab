package com.example.musiccolab.instruments;

import com.example.musiccolab.CommunicationHandling;
import com.example.musiccolab.Lobby;
import com.example.musiccolab.Login;
import com.example.musiccolab.R;

import java.util.ArrayList;
import java.util.HashMap;

public class SoundPlayer {
    private static final short NETWORK_THREAD_ACTION_SEND_TONE = 7;
    public static CommunicationHandling NETWORK_THREAD = Login.networkThread;
    private final Lobby lobby;
    private boolean testingMode = false;
    private ArrayList<MediaPlayerAdapter> currentlyPlaying = new ArrayList<>();
    private HashMap<String, Integer> sounds = new HashMap<>();

    public SoundPlayer(Lobby lobby) {
        this.lobby = lobby;
        sounds.put("piano0", R.raw.piano0_c4);
        sounds.put("piano1", R.raw.piano1_d4);
        sounds.put("piano2", R.raw.piano2_e4);
        sounds.put("piano3", R.raw.piano3_f4);
        sounds.put("piano4", R.raw.piano4_g4);
        sounds.put("piano5", R.raw.piano5_a4);
        sounds.put("piano6", R.raw.piano6_h4);
        sounds.put("piano7", R.raw.piano7_c5);
        sounds.put("drums0", R.raw.drum_a);
        sounds.put("drums1", R.raw.drum_b);
        sounds.put("drums2", R.raw.drum_c);
        sounds.put("therm0", R.raw.theremin0_c4);
        sounds.put("therm1", R.raw.theremin1_d4);
        sounds.put("therm2", R.raw.theremin2_e4);
        sounds.put("therm3", R.raw.theremin3_f4);
        sounds.put("therm4", R.raw.theremin4_g4);
        sounds.put("therm5", R.raw.theremin5_a4);
        sounds.put("therm6", R.raw.theremin6_h4);
        sounds.put("therm7", R.raw.theremin7_c5);
    }

    public void activateTestingMode(CommunicationHandling dummyNetworkThread) {
        NETWORK_THREAD = dummyNetworkThread;
        testingMode = true;
    }


    public void sendToneToServer(String toneAsString, int toneAction) {
        if (sounds.containsKey(toneAsString)) {
            playTone(toneAsString, NETWORK_THREAD.userID, toneAction);
            NETWORK_THREAD.action = NETWORK_THREAD_ACTION_SEND_TONE;
            NETWORK_THREAD.toneAction = (byte) toneAction;
            NETWORK_THREAD.data = toneAsString;
        }
    }

    public void playTone(String toneAsString, int user, int toneAction) {
        if (toneAction == 1) {
            if (toneAsString.equals("therm")) {
                stopTheremin(user);
                return;
            }
            if (sounds.get(toneAsString) == null) {
                System.out.println("Wrong tone data");
                return;
            }
            MediaPlayerAdapter tone = new MediaPlayerAdapter(lobby, sounds.get(toneAsString), testingMode, user, toneAsString);
            if (toneAsString.startsWith("therm")) stopTheremin(user);
            currentlyPlaying.add(tone);
            tone.start();
        } else if (toneAction == 0) {
            synchronized (currentlyPlaying) {
                MediaPlayerAdapter playing = null;
                for (MediaPlayerAdapter mp : currentlyPlaying) {
                    if (mp.tone.equals(toneAsString) && mp.user == user) {
                        mp.stop();
                        playing = mp;
                        break;
                    }
                }
                currentlyPlaying.remove(playing);
            }
        }
    }

    public void stopTheremin(int user) {
        synchronized (currentlyPlaying) {
            MediaPlayerAdapter playing = null;
            for (MediaPlayerAdapter mp : currentlyPlaying) {
                if (mp.tone.startsWith("therm") && mp.user == user) {
                    mp.stop();
                    playing = mp;
                    break;
                }
            }
            currentlyPlaying.remove(playing);
        }
    }

    public void stopEverything() {
        synchronized (currentlyPlaying) {
            currentlyPlaying.forEach(mp -> mp.stop());
            currentlyPlaying.clear();
        }
    }
}
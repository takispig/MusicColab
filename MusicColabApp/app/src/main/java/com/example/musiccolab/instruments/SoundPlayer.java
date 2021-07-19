package com.example.musiccolab.instruments;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.example.musiccolab.CommunicationHandling;
import com.example.musiccolab.Lobby;
import com.example.musiccolab.Login;
import com.example.musiccolab.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SoundPlayer {

    public static final int TONE_ACTION_START = 1;
    public static final int TONE_ACTION_STOP = 0;

    @VisibleForTesting
    protected static CommunicationHandling NETWORK_THREAD = Login.networkThread;

    private static final short NETWORK_THREAD_ACTION_SEND_TONE = 7;
    private final Lobby lobby;
    private boolean testingMode = false;
    private final HashMap<String, Integer> sounds = new HashMap<>();
    private final HashMap<Integer, LinkedList<String>> usersAndTheirTones = new HashMap<>();
    private final HashMap<String, MediaPlayerAdapter> listOfCurrentPlayingMPAs = new HashMap<>();

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
        playTone(toneAsString, NETWORK_THREAD.userID, toneAction);
        NETWORK_THREAD.action = NETWORK_THREAD_ACTION_SEND_TONE;
        NETWORK_THREAD.toneAction = (byte) toneAction;
        NETWORK_THREAD.data = toneAsString;
    }

    public void playTone(String toneAsString, int user_ID, int toneAction) {
        if (!usersAndTheirTones.containsKey(user_ID)) {
            usersAndTheirTones.put(user_ID, new LinkedList<>());
        }
        if (toneAction == TONE_ACTION_START) {
            startTone(toneAsString, user_ID);
        } else if (toneAction == TONE_ACTION_STOP) {
            stopTone(toneAsString, user_ID);
        }
    }

    private void startTone(String toneAsString, int user_ID) {
        if (!sounds.containsKey(toneAsString)) {
            if (!testingMode) {
                Log.e(getClass().getSimpleName(), "No such tone found: " + toneAsString + ", user=" + user_ID + ", toneAction=" + TONE_ACTION_START);
            }
            return;
        }

        // drum tones are single tones only and therefore cannot be held and stopped
        if (!toneAsString.startsWith(Drums.DRUMS_SOUND_ID_PREFIX)) {
            // to avoid double theremin tones
            if (toneAsString.startsWith(Theremin.THEREMIN_SOUND_ID_PREFIX)) {
                stopTheremin(user_ID);
            }
            String mpa_ID = "" + user_ID + "-" + toneAsString;
            if (!Objects.requireNonNull(usersAndTheirTones.get(user_ID)).contains(mpa_ID)) {
                MediaPlayerAdapter mpa = new MediaPlayerAdapter(lobby, sounds.get(toneAsString), testingMode);
                mpa.start();
                Objects.requireNonNull(usersAndTheirTones.get(user_ID)).add(mpa_ID);
                listOfCurrentPlayingMPAs.put(mpa_ID, mpa);
            }
        } else {
            MediaPlayerAdapter mpa = new MediaPlayerAdapter(lobby, sounds.get(toneAsString), testingMode);
            mpa.start();
        }
    }

    private void stopTone(String toneAsString, int user_ID) {
        LinkedList<String> tempToneListOfOneUser = usersAndTheirTones.get(user_ID);
        assert tempToneListOfOneUser != null;
        if (tempToneListOfOneUser.contains(user_ID + "-" + toneAsString)) {
            Objects.requireNonNull(listOfCurrentPlayingMPAs.get(user_ID + "-" + toneAsString)).stop();
            listOfCurrentPlayingMPAs.remove(user_ID + "-" + toneAsString);
            Objects.requireNonNull(usersAndTheirTones.get(user_ID)).remove(user_ID + "-" + toneAsString);
        }
    }

    /**
     * Stopping all Theremin tones played by the same user to avoid overlapping sounds. This method
     * should only be called, if new Theremin tones are played. Otherwise use
     * playTone(therm..., ..., SoundPlayer.TONE_ACTION_STOP) instead.
     *
     * @param user_ID the id of the current user playing a new Theremin tone
     */
    public void stopTheremin(int user_ID) {
        List<String> tonesToBeDeleted = new ArrayList<>();
        LinkedList<String> mpa_ID_list = Objects.requireNonNull(usersAndTheirTones.get(user_ID));
        for (String mpa_ID : mpa_ID_list) {
            if (mpa_ID.startsWith(user_ID + "-" + Theremin.THEREMIN_SOUND_ID_PREFIX)) {
                Objects.requireNonNull(listOfCurrentPlayingMPAs.get(mpa_ID)).stop();
                listOfCurrentPlayingMPAs.remove(mpa_ID);
                tonesToBeDeleted.add(mpa_ID);
            }
        }
        mpa_ID_list.removeAll(tonesToBeDeleted);
    }

    public void stopEverything() {
        Log.i(getClass().getSimpleName(), "Stopping everything...");
        for (String id : listOfCurrentPlayingMPAs.keySet()) {
            Objects.requireNonNull(listOfCurrentPlayingMPAs.get(id)).stop();
        }
        listOfCurrentPlayingMPAs.clear();
        for (Integer user_ID : usersAndTheirTones.keySet()) {
            Objects.requireNonNull(usersAndTheirTones.get(user_ID)).clear();
        }
    }
}
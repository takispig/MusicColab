 package com.example.musiccolab.instruments;

import com.example.musiccolab.CommunicationHandling;
import com.example.musiccolab.Lobby;
import com.example.musiccolab.Login;
import com.example.musiccolab.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SoundPlayer {
    private static final short NETWORK_THREAD_ACTION_SEND_TONE = 7;
    public static CommunicationHandling NETWORK_THREAD = Login.networkThread;
    private final Lobby lobby;
    private boolean testingMode = false;
    private ArrayList<MediaPlayerAdapter> currentlyPlaying = new ArrayList<>();

    public SoundPlayer(Lobby lobby) {
        this.lobby = lobby;
    }

    public void activateTestingMode(CommunicationHandling dummyNetworkThread) {
        NETWORK_THREAD = dummyNetworkThread;
        testingMode = true;
    }


    public void sendToneToServer(String toneAsString, int toneAction) {
        playTone(toneAsString,NETWORK_THREAD.username, toneAction);
        NETWORK_THREAD.action = NETWORK_THREAD_ACTION_SEND_TONE;
        NETWORK_THREAD.toneAction = (byte) toneAction;
        NETWORK_THREAD.data = toneAsString;
    }

    public void playTone(String toneAsString,String user, int toneAction) {
        if(toneAction==1){
            MediaPlayerAdapter tone = getMediaPlayerAdapter(toneAsString,user);
            if(tone==null){
                System.out.println("Wrong tone data");
                return;
            }
            if(toneAsString.startsWith("therm")){
                MediaPlayerAdapter playing=null;
                for (MediaPlayerAdapter mp:currentlyPlaying){
                    if(mp.tone.startsWith("therm")&&mp.user.equals(user)) {
                        mp.stop();
                        playing=mp;
                        break;
                    }
                }
                currentlyPlaying.remove(playing);
            }
            currentlyPlaying.add(tone);
            tone.start();
        }
        else if(toneAction==0){
            MediaPlayerAdapter playing=null;
            for (MediaPlayerAdapter mp:currentlyPlaying){
                if(mp.tone.equals(toneAsString)&&mp.user.equals(user)) {
                    mp.stop();
                    playing=mp;
                    break;
                }
            }
            currentlyPlaying.remove(playing);
        }
    }

    public MediaPlayerAdapter getMediaPlayerAdapter(String toneAsString,String user){
        int soundFile;
        switch (toneAsString){
            case "piano0":
                soundFile = R.raw.piano0_c4;
                break;
            case "piano1":
                soundFile = R.raw.piano1_d4;
                break;
            case "piano2":
                soundFile = R.raw.piano2_e4;
                break;
            case "piano3":
                soundFile = R.raw.piano3_f4;
                break;
            case "piano4":
                soundFile = R.raw.piano4_g4;
                break;
            case "piano5":
                soundFile = R.raw.piano5_a4;
                break;
            case "piano6":
                soundFile = R.raw.piano6_h4;
                break;
            case "piano7":
                soundFile = R.raw.piano7_c5;
                break;
            case "drums0":
                soundFile = R.raw.drum_a;
                break;
            case "drums1":
                soundFile = R.raw.drum_b;
                break;
            case "drums2":
                soundFile = R.raw.drum_c;
                break;
            case "therm0":
                soundFile = R.raw.theremin0_c4;
                break;
            case "therm1":
                soundFile = R.raw.theremin1_d4;
                break;
            case "therm2":
                soundFile = R.raw.theremin2_e4;
                break;
            case "therm3":
                soundFile = R.raw.theremin3_f4;
                break;
            case "therm4":
                soundFile = R.raw.theremin4_g4;
                break;
            case "therm5":
                soundFile = R.raw.theremin5_a4;
                break;
            case "therm6":
                soundFile = R.raw.theremin6_h4;
                break;
            case "therm7":
                soundFile = R.raw.theremin7_c5;
                break;
            default:
                return null;
        }
        return new MediaPlayerAdapter(lobby,soundFile,testingMode,user,toneAsString);
    }
}
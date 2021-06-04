package com.example.musiccolab.instruments;

import android.annotation.SuppressLint;
import android.hardware.SensorEvent;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.example.musiccolab.Lobby;
import com.example.musiccolab.R;

import java.util.ArrayList;
import java.util.List;

public class Piano implements Instrument, View.OnClickListener {

    private final Animation scaleDown;
    private final SoundPool soundPool;
    private List<Integer> soundIDs;
    private static final String INSTRUMENT_NAME = "Piano";
    private static final int FREEZE_DURATION_IN_MS = 300;

    @SuppressLint("ClickableViewAccessibility")
    public Piano(InstrumentGUIBox instrumentGUI, Lobby lobby) {
        List<Button> pianoKeys = instrumentGUI.getPianoKeys();

        scaleDown = AnimationUtils.loadAnimation(lobby, R.anim.scale_down);

        soundPool = new SoundPool.Builder().setMaxStreams(5).build();

        createSoundIDsList();
        for (int i = 0; i < pianoKeys.size(); i++) {
            Button btn = pianoKeys.get(i);
            int soundPoolID = soundPool.load(lobby, soundIDs.get(i), 1);
            btn.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        btn.startAnimation(scaleDown);
                        soundPool.play(soundPoolID, 1, 1, 0, 0, 1);
                        instrumentGUI.setTextInCenter(btn.getText() + " pressed");
                        break;
                    case MotionEvent.ACTION_UP:
                        freeze();
                        btn.clearAnimation();
                        soundPool.pause(soundPoolID);
                        instrumentGUI.setTextInCenter(btn.getText() + " released");
                        break;
                    default:
                        // instrumentGUI.setTextInCenter("no key pressed");
                        break;
                }
                return true;
            });
        }
        instrumentGUI.setPianoKeysVisible();
    }

    private void freeze() {
        try {
            Thread.sleep(FREEZE_DURATION_IN_MS);
        } catch (InterruptedException e) {
            // TODO log error
        }
    }

    private void createSoundIDsList() {
        soundIDs = new ArrayList<>();
        soundIDs.add(R.raw.p_c);
        soundIDs.add(R.raw.p_d);
        soundIDs.add(R.raw.p_e);
        soundIDs.add(R.raw.p_f);
        soundIDs.add(R.raw.p_g);
        soundIDs.add(R.raw.p_a);
        soundIDs.add(R.raw.p_h);
        soundIDs.add(R.raw.p_c2);
    }

    @Override
    public void reCalibrate(SensorEvent event) {
        // do nothing, or maybe changing landscape from vertical to horizontal?
    }

    @Override
    public void reCalibrate() {
        // do nothing, or maybe changing landscape from vertical to horizontal?
    }

    @Override
    public void action(SensorEvent event) {
        // do nothing
    }

    @Override
    public String getInstrumentName() {
        return INSTRUMENT_NAME;
    }

    @Override
    public String getInstrumentType() {
        return InstrumentType.PIANO;
    }

    @Override
    public int getSensorType() {
        return 0;
    }

    @Override
    public void onClick(View v) {
        // do nothing
    }
}
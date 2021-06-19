package com.example.musiccolab.instruments;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.example.musiccolab.Lobby;
import com.example.musiccolab.R;

import java.util.List;

public class Piano implements Instrument, View.OnClickListener {

    private final Animation scaleDown;
    private static final String INSTRUMENT_NAME = "Piano";
    private static final int FREEZE_DURATION_IN_MS = 300;
    List<Button> pianoKeys;

    @SuppressLint("ClickableViewAccessibility")
    public Piano(InstrumentGUIBox instrumentGUI, Lobby lobby, SoundPlayer sp) {
        pianoKeys = instrumentGUI.getPianoKeys();

        scaleDown = AnimationUtils.loadAnimation(lobby, R.anim.scale_down);

        for (int i = 0; i < pianoKeys.size(); i++) {
            Button btn = pianoKeys.get(i);
            int finalI = i;
            btn.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        btn.startAnimation(scaleDown);
                        sp.sendToneToServer("piano"+finalI);
                        instrumentGUI.setTextInCenter(btn.getText() + " pressed");
                        break;
                    case MotionEvent.ACTION_UP:
                        freeze();
                        btn.clearAnimation();
                        //   sp.pause("piano" + finalI);
                        instrumentGUI.setTextInCenter(btn.getText() + " released");
                        break;
                    default:
                        //      instrumentGUI.setTextInCenter("no key pressed");
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

    @Override
    public void reCalibrate(SensorEventAdapter event) {
        // do nothing, or maybe changing landscape from vertical to horizontal?
    }

    @Override
    public void reCalibrate() {
        // do nothing, or maybe changing landscape from vertical to horizontal?
    }

    @Override
    public void action(SensorEventAdapter event) {
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
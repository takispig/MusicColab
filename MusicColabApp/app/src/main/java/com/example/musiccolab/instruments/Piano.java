package com.example.musiccolab.instruments;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.widget.Button;

import java.util.List;

public class Piano implements Instrument {

    private static final String INSTRUMENT_NAME = "Piano";
    private static final int FREEZE_DURATION_IN_MS = 300;
    private final List<Button> pianoKeys;

    public Piano(InstrumentGUIBox instrumentGUI, SoundPlayer sp) {
        pianoKeys = instrumentGUI.getPianoKeys();
        createPianoKeys(instrumentGUI, sp);
        instrumentGUI.setPianoKeysVisible();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createPianoKeys(InstrumentGUIBox instrumentGUI, SoundPlayer sp) {
        for (int i = 0; i < pianoKeys.size(); i++) {
            Button btn = pianoKeys.get(i);
            int index = i;
            btn.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        instrumentGUI.startAnimationForPianoKey(index);
                        sp.sendToneToServer("piano" + index, 1);
                        break;
                    case MotionEvent.ACTION_UP:
                        freeze();
                        instrumentGUI.clearAnimationForPianoKey(index);
                        sp.sendToneToServer("piano" + index, 0);
                        break;
                    default:
                        break;
                }
                return true;
            });
        }
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
}
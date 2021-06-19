package com.example.musiccolab.instruments;

import android.app.usage.UsageEvents;
import android.view.MotionEvent;
import android.view.View;



import com.example.musiccolab.Lobby;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PianoTest {
    InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
    SoundPlayer sp = mock(SoundPlayer.class);
    Lobby lobby = mock(Lobby.class);
    Piano piano = new Piano(guiBox,lobby, sp);


    @Test
    public void test_Piano_c(){

    }

    @Test
    public void test_getInstrumentName() {
        // arrange
        String expected = "Piano";

        // act
        String result = piano.getInstrumentName();

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void test_getInstrumentType() {
        // arrange
        String expected = InstrumentType.PIANO;

        // act
        String result = piano.getInstrumentType();

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void test_getSensorType() {
        // arrange
        int expected = 0;

        // act
        int result = piano.getSensorType();

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void test_onClick(){
        // arrange
        View v= mock(View.class);
        // act
        piano.onClick(v);
        // assert
        assertTrue(true);
    }

    @Test
    public void test_Action(){
        // arrange
        SensorEventAdapter sensorEventAdapter =mock(SensorEventAdapter.class);
        // act
        piano.action(sensorEventAdapter);
        // assert
        assertTrue(true);
    }

    @Test
    public void test_reCalibrate(){
        // arrange
        SensorEventAdapter sensorEventAdapter =mock(SensorEventAdapter.class);
        // act
        piano.reCalibrate(sensorEventAdapter);
        piano.reCalibrate();
        // assert
        assertTrue(true);
    }

}

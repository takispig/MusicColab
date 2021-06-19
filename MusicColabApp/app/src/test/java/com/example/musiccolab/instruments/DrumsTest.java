package com.example.musiccolab.instruments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DrumsTest {

    @Test
    public void test_getInstrumentName() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Drums drums = new Drums(guiBox, sp);
        String expected = "Drums";

        // act
        String result = drums.getInstrumentName();

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void test_getInstrumentType() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Drums drums = new Drums(guiBox, sp);
        String expected = InstrumentType.DRUMS;

        // act
        String result = drums.getInstrumentType();

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void test_getSensorType() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Drums drums = new Drums(guiBox, sp);
        int expected = Sensor.TYPE_ACCELEROMETER;

        // act
        int result = drums.getSensorType();

        // assert
        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_action_wrongSensorType_exceptionThrown() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        SensorEvent event = mock(SensorEvent.class);
        Integer wrongSensorType = -1;
        event.sensor = sensor;
        when(sensor.getType()).thenReturn(wrongSensorType);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        // when no exception is thrown, this test must fail
        fail();
    }
}
package com.example.musiccolab.instruments;

import android.hardware.Sensor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DrumsTest {

    public static final String DRUMS_0 = "drums0";
    public static final String DRUMS_1 = "drums1";
    public static final String DRUMS_2 = "drums2";
    public static final float POSITIVE_FORCE = (float) Drums.MAX_SENSOR_INTENSITY + 1;
    public static final float NEGATIVE_FORCE = (float) (Drums.MAX_SENSOR_INTENSITY + 1) * -1;
    public static final float GRAVITY_FORCE = (float) Drums.GRAVITY;
    public static final float OTHER_FORCE = 0;
    public static final float NEGATIVE_GRAVITY_FORCE = (float) -Drums.GRAVITY;

    private Sensor sensor;
    private SoundPlayer sp;
    private Drums drums;

    @Before
    public void setUp() {
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        sp = mock(SoundPlayer.class);
        drums = new Drums(guiBox, sp);
        sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
    }

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

    @Test
    public void test_action_gravityIsXNegative_forceIsYPositive_playDrums2() {
        // arrange
        float[] forceValues = {NEGATIVE_GRAVITY_FORCE, POSITIVE_FORCE, OTHER_FORCE};
        SensorEventAdapter event = new SensorEventAdapter(forceValues, sensor);

        // act
        drums.action(event);
        drums.reCalibrate();
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_2, 1);
    }

    @Test
    public void test_action_gravityIsX_forceIsYNegative_playDrums1() {
        // arrange
        float[] forceValues = {GRAVITY_FORCE, NEGATIVE_FORCE, OTHER_FORCE};
        SensorEventAdapter event = new SensorEventAdapter(forceValues, sensor);

        // act
        drums.action(event);
        drums.reCalibrate();
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_1, 1);
    }

    @Test
    public void test_action_gravityIsX_forceIsZ_playDrums0() {
        // arrange
        float[] forceValues = {GRAVITY_FORCE, OTHER_FORCE, NEGATIVE_FORCE};
        SensorEventAdapter event = new SensorEventAdapter(forceValues, sensor);

        // act
        drums.action(event);
        drums.reCalibrate();
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_0, 1);
    }

    @Test
    public void test_action_gravityIsYNegative_forceIsXPositive_reCalibrate_playDrums2() {
        // arrange
        float[] forceValues = {POSITIVE_FORCE, NEGATIVE_GRAVITY_FORCE, OTHER_FORCE};
        SensorEventAdapter event = new SensorEventAdapter(forceValues, sensor);

        // act
        drums.action(event);
        drums.reCalibrate();
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_2, 1);
    }

    @Test
    public void test_action_gravityIsY_forceIsXNegative_playDrums1() {
        // arrange
        float[] forceValues = {NEGATIVE_FORCE, GRAVITY_FORCE, OTHER_FORCE};
        SensorEventAdapter event = new SensorEventAdapter(forceValues, sensor);

        // act
        drums.action(event);
        drums.reCalibrate();
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_1, 1);
    }

    @Test
    public void test_action_gravityIsY_forceIsZ_playDrums0() {
        // arrange
        float[] forceValues = {OTHER_FORCE, GRAVITY_FORCE, NEGATIVE_FORCE};
        SensorEventAdapter event = new SensorEventAdapter(forceValues, sensor);

        // act
        drums.action(event);
        drums.reCalibrate();
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_0, 1);
    }

    @Test
    public void test_action_gravityIsZ_forceIsXPositive_playDrums2() {
        // arrange
        float[] forceValues = {POSITIVE_FORCE, OTHER_FORCE, GRAVITY_FORCE};
        SensorEventAdapter event = new SensorEventAdapter(forceValues, sensor);

        // act
        drums.action(event);
        drums.reCalibrate();
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_2, 1);
    }

    @Test
    public void test_action_gravityIsZ_forceIsXNegative_playDrums1() {
        // arrange
        float[] forceValues = {NEGATIVE_FORCE, OTHER_FORCE, GRAVITY_FORCE};
        SensorEventAdapter event = new SensorEventAdapter(forceValues, sensor);

        // act
        drums.action(event);
        drums.reCalibrate();
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_1, 1);
    }

    @Test
    public void test_action_gravityIsZ_forceIsY_playDrums0() {
        // arrange
        float[] forceValues = {OTHER_FORCE, NEGATIVE_FORCE, GRAVITY_FORCE};
        SensorEventAdapter event = new SensorEventAdapter(forceValues, sensor);

        // act
        drums.action(event);
        drums.reCalibrate();
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_0, 1);
    }
}
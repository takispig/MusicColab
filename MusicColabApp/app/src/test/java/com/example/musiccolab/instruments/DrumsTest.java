package com.example.musiccolab.instruments;

import android.hardware.Sensor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DrumsTest {

    public static final String DRUMS_0 = "drums0";
    public static final String DRUMS_1 = "drums1";
    public static final String DRUMS_2 = "drums2";

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
        Integer wrongSensorType = -1;
        when(sensor.getType()).thenReturn(wrongSensorType);
        SensorEventAdapter event = new SensorEventAdapter(null, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        // when no exception is thrown, this test must fail
        fail();
    }

    @Test
    public void test_action_gravityIsXNegative_forceIsYPositive_playDrums2() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        float gravityForce = (float) -Drums.GRAVITY;
        float testingForce = (float) Drums.MAX_SENSOR_INTENSITY + 1;
        float otherForce = 0;
        float[] zeroValues = {gravityForce, testingForce, otherForce};
        SensorEventAdapter event = new SensorEventAdapter(zeroValues, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_2);
    }

    @Test
    public void test_action_gravityIsX_forceIsYNegative_playDrums1() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        float gravityForce = (float) Drums.GRAVITY;
        float testingForce = (float) (Drums.MAX_SENSOR_INTENSITY + 1) * -1;
        float otherForce = 0;
        float[] zeroValues = {gravityForce, testingForce, otherForce};
        SensorEventAdapter event = new SensorEventAdapter(zeroValues, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_1);
    }

    @Test
    public void test_action_gravityIsX_forceIsZ_playDrums0() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        float gravityForce = (float) Drums.GRAVITY;
        float testingForce = (float) (Drums.MAX_SENSOR_INTENSITY + 1) * -1;
        float otherForce = 0;
        float[] zeroValues = {gravityForce, otherForce, testingForce};
        SensorEventAdapter event = new SensorEventAdapter(zeroValues, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_0);
    }

    @Test
    public void test_action_gravityIsYNegative_forceIsXPositive_reCalibrate_playDrums2() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        float gravityForce = (float) -Drums.GRAVITY;
        float testingForce = (float) Drums.MAX_SENSOR_INTENSITY + 1;
        float otherForce = 0;
        float[] zeroValues = {testingForce, gravityForce, otherForce};
        SensorEventAdapter event = new SensorEventAdapter(zeroValues, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);
        drums.reCalibrate();

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_2);
    }

    @Test
    public void test_action_gravityIsY_forceIsXNegative_playDrums1() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        float gravityForce = (float) Drums.GRAVITY;
        float testingForce = (float) (Drums.MAX_SENSOR_INTENSITY + 1) * -1;
        float otherForce = 0;
        float[] zeroValues = {testingForce, gravityForce, otherForce};
        SensorEventAdapter event = new SensorEventAdapter(zeroValues, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_1);
    }

    @Test
    public void test_action_gravityIsY_forceIsZ_playDrums0() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        float gravityForce = (float) Drums.GRAVITY;
        float testingForce = (float) (Drums.MAX_SENSOR_INTENSITY + 1) * -1;
        float otherForce = 0;
        float[] zeroValues = {otherForce, gravityForce, testingForce};
        SensorEventAdapter event = new SensorEventAdapter(zeroValues, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_0);
    }

    @Test
    public void test_action_gravityIsZ_forceIsXPositive_playDrums2() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        float gravityForce = (float) Drums.GRAVITY;
        float testingForce = (float) Drums.MAX_SENSOR_INTENSITY + 1;
        float otherForce = 0;
        float[] zeroValues = {testingForce, otherForce, gravityForce};
        SensorEventAdapter event = new SensorEventAdapter(zeroValues, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_2);
    }

    @Test
    public void test_action_gravityIsZ_forceIsXNegative_playDrums1() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        float gravityForce = (float) Drums.GRAVITY;
        float testingForce = (float) (Drums.MAX_SENSOR_INTENSITY + 1) * -1;
        float otherForce = 0;
        float[] zeroValues = {testingForce, otherForce, gravityForce};
        SensorEventAdapter event = new SensorEventAdapter(zeroValues, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_1);
    }

    @Test
    public void test_action_gravityIsZ_forceIsY_playDrums0() {
        // arrange
        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
        SoundPlayer sp = mock(SoundPlayer.class);
        Sensor sensor = mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        float gravityForce = (float) Drums.GRAVITY;
        float testingForce = (float) (Drums.MAX_SENSOR_INTENSITY + 1) * -1;
        float otherForce = 0;
        float[] zeroValues = {otherForce, testingForce, gravityForce};
        SensorEventAdapter event = new SensorEventAdapter(zeroValues, sensor);
        Drums drums = new Drums(guiBox, sp);

        // act
        drums.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(DRUMS_0);
    }
}
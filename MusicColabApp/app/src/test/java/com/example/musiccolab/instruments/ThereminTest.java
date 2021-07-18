package com.example.musiccolab.instruments;

import android.hardware.Sensor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ThereminTest {
    public static final int THEREMIN_INIT_VALUE = 81;
    InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
    SoundPlayer sp = mock(SoundPlayer.class);
    Theremin theremin = new Theremin(guiBox, sp);
    Sensor sensor = mock(Sensor.class);

    @Test
    public void test_getInstrumentName() {
        // arrange
        String expected = "Theremin";

        // act
        String result = theremin.getInstrumentName();

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void test_getInstrumentType() {
        // arrange
        String expected = InstrumentType.THEREMIN;

        // act
        String result = theremin.getInstrumentType();

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void test_getSensorType() {
        // arrange
        int expected = Sensor.TYPE_LIGHT;

        // act
        int result = theremin.getSensorType();

        // assert
        assertEquals(expected, result);
    }

    @Test
    public void test_action_c() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{0}, sensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer("therm0", 1);
    }

    @Test
    public void test_action_d() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{10}, sensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer("therm1", 1);
    }

    @Test
    public void test_action_e() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{20}, sensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer("therm2", 1);
    }

    @Test
    public void test_action_f() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{30}, sensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer("therm3", 1);
    }

    @Test
    public void test_action_g() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{40}, sensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer("therm4", 1);
    }

    @Test
    public void test_action_a() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{50}, sensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer("therm5", 1);
    }

    @Test
    public void test_action_h() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{60}, sensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer("therm6", 1);
    }

    @Test
    public void test_action_c2() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{70}, sensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer("therm7", 1);
    }

    @Test
    public void test_action_0() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{80}, sensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(1)).sendToneToServer(Theremin.THEREMIN_STOP, 0);
    }

    @Test
    public void test_action_wrongSensor_soundPlayerNotCalled() {
        // arrange
        when(sensor.getType()).thenReturn(Sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{THEREMIN_INIT_VALUE}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);

        Sensor wrongSensor = mock(Sensor.class);
        when(wrongSensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        event = new SensorEventAdapter(new float[]{80}, wrongSensor);

        // act
        theremin.action(event);

        // assert
        verify(sp, times(0)).sendToneToServer(anyString(), anyInt());
    }
}
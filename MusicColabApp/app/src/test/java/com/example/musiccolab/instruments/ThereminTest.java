package com.example.musiccolab.instruments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThereminTest {
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
    public void test_action_c() throws Exception {
        // arrange
        when(sensor.getType()).thenReturn(sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{85}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);

        event = new SensorEventAdapter(new float[]{0}, sensor);
        // act
        theremin.action(event);
        // assert
        assertEquals("c Theremin", theremin.stringToDisplay);
    }

    @Test
    public void test_action_d() throws Exception {
        // arrange
        when(sensor.getType()).thenReturn(sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{85}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{10}, sensor);
        // act
        theremin.action(event);
        // assert
        assertEquals("d Theremin", theremin.stringToDisplay);
    }
    @Test
    public void test_action_e() throws Exception {
        // arrange
        when(sensor.getType()).thenReturn(sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{85}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{20}, sensor);
        // act
        theremin.action(event);
        // assert
        assertEquals("e Theremin", theremin.stringToDisplay);
    }
    @Test
    public void test_action_f() throws Exception {
        // arrange
        when(sensor.getType()).thenReturn(sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{85}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{30}, sensor);
        // act
        theremin.action(event);
        // assert
        assertEquals("f Theremin", theremin.stringToDisplay);
    }
    @Test
    public void test_action_g() throws Exception {
        // arrange
        when(sensor.getType()).thenReturn(sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{85}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{40}, sensor);
        // act
        theremin.action(event);
        // assert
        assertEquals("g Theremin", theremin.stringToDisplay);
    }
    @Test
    public void test_action_a() throws Exception {
        // arrange
        when(sensor.getType()).thenReturn(sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{85}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{50}, sensor);
        // act
        theremin.action(event);
        // assert
        assertEquals("a Theremin", theremin.stringToDisplay);
    }
    @Test
    public void test_action_h() throws Exception {
        // arrange
        when(sensor.getType()).thenReturn(sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{85}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{60}, sensor);
        // act
        theremin.action(event);
        // assert
        assertEquals("h Theremin", theremin.stringToDisplay);
    }
    @Test
    public void test_action_c2() throws Exception {
        // arrange
        when(sensor.getType()).thenReturn(sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{85}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{70}, sensor);
        // act
        theremin.action(event);
        // assert
        assertEquals("c2 Theremin", theremin.stringToDisplay);
    }
    @Test
    public void test_action_0() throws Exception {
        // arrange
        when(sensor.getType()).thenReturn(sensor.TYPE_LIGHT);
        SensorEventAdapter event = new SensorEventAdapter(new float[]{85}, sensor);
        theremin.reCalibrate();//Useless but for 100% coverage
        theremin.reCalibrate(event);
        event = new SensorEventAdapter(new float[]{80}, sensor);
        // act
        theremin.action(event);
        // assert
        assertEquals("0 Theremin", theremin.stringToDisplay);
    }
}

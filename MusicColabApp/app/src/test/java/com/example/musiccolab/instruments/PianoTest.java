package com.example.musiccolab.instruments;

import android.widget.Button;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PianoTest {
    private final InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
    private final SoundPlayer sp = mock(SoundPlayer.class);
    private final Piano piano = new Piano(guiBox, sp);

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

//    @Test
//    public void test_PianoKeys() {
//        // arrange
//        InstrumentGUIBox guiBox = mock(InstrumentGUIBox.class);
//        List<Button> pianoKeys = new ArrayList<>();
//        when(guiBox.getPianoKeys()).thenReturn(pianoKeys);
//        SoundPlayer sp = mock(SoundPlayer.class);
//        Piano piano = new Piano(guiBox, sp);
//
//        // act
//
//        // assert
//    }
}
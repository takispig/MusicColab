package com.example.musiccolab.instruments;

import com.example.musiccolab.CommunicationHandling;
import com.example.musiccolab.Lobby;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SoundPlayerTest {

    private static CommunicationHandling dummy;
    private static final int randomInt = new Random().nextInt();

    private static SoundPlayer createSoundPlayer() {
        dummy = new CommunicationHandlingDummy();
        dummy.userID = randomInt;
        Lobby lobby = new Lobby();
        SoundPlayer sp = new SoundPlayer(lobby);
        sp.activateTestingMode(dummy);
        return sp;
    }

    @ParameterizedTest
    @ValueSource(strings = {"piano0", "piano1", "piano2", "piano3", "piano4", "piano5", "piano6", "piano7", "drums0", "drums1", "drums2", "therm0", "therm1", "therm2", "therm3", "therm4", "therm5", "therm6", "therm7"})
    public void test_sendToneToServer(String expectedTone) {
        // arrange
        SoundPlayer sp = createSoundPlayer();

        // act
        sp.sendToneToServer(expectedTone, 1);

        // assert
        assertEquals(expectedTone, dummy.data);
    }

//    @Test
//    public void test_stopEverything() {
//        // arrange
//        SoundPlayer sp = createSoundPlayer();
//        MediaPlayerAdapter mpa = mock(MediaPlayerAdapter.class);
//        sp.currentlyPlaying.clear();
//        sp.currentlyPlaying.add(mpa);
//
//        // act
//        sp.stopEverything();
//
//        // assert
//        verify(mpa, times(1)).stop();
//    }
//
//    @Test
//    public void test_stopTheremin() {
//        // arrange
//        SoundPlayer sp = createSoundPlayer();
//        MediaPlayerAdapter mpa = mock(MediaPlayerAdapter.class);
//        when(mpa.getTone()).thenReturn("therm");
//        when(mpa.getUser()).thenReturn(randomInt);
//        sp.currentlyPlaying.clear();
//        sp.currentlyPlaying.add(mpa);
//
//        // act
//        sp.stopTheremin(randomInt);
//
//        // assert
//        verify(mpa, times(1)).stop();
//    }
//
//    @Test
//    public void test_stopTheremin_2() {
//        // arrange
//        SoundPlayer sp = createSoundPlayer();
//        MediaPlayerAdapter mpa = mock(MediaPlayerAdapter.class);
//        when(mpa.getTone()).thenReturn("therm");
//        when(mpa.getUser()).thenReturn(randomInt);
//        sp.currentlyPlaying.clear();
//        sp.currentlyPlaying.add(mpa);
//
//        // act
//        sp.sendToneToServer(Theremin.THEREMIN_STOP, 1);
//
//        // assert
//        assertEquals(0, sp.currentlyPlaying.size());
//    }
//
//    @Test
//    public void test_playTone() {
//        // arrange
//        SoundPlayer sp = createSoundPlayer();
//        MediaPlayerAdapter mpa = mock(MediaPlayerAdapter.class);
//        when(mpa.getTone()).thenReturn("drums0");
//        when(mpa.getUser()).thenReturn(randomInt);
//        sp.currentlyPlaying.clear();
//        sp.currentlyPlaying.add(mpa);
//
//        // act
//        sp.playTone("drums0", randomInt, 0);
//
//        // assert
//        verify(mpa, times(1)).stop();
//    }
}
package com.example.musiccolab.instruments;

import com.example.musiccolab.CommunicationHandling;
import com.example.musiccolab.Lobby;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SoundPlayerTest {

    private static SoundPlayer sp;
    private static CommunicationHandling dummy;
    private final int randomInt = new Random().nextInt();
    private final String randomString = new Random().doubles().toString();

    @BeforeAll
    static void setUp() {
        dummy = new CommunicationHandlingDummy();
        Lobby lobby = new Lobby();
        sp = new SoundPlayer(lobby);
        sp.activateTestingMode(dummy);
    }

    @ParameterizedTest
    @ValueSource(strings = {"piano0", "piano1", "piano2", "piano3", "piano4", "piano5", "piano6", "piano7", "drums0", "drums1", "drums2", "therm0", "therm1", "therm2", "therm3", "therm4", "therm5", "therm6", "therm7"})
    public void test_sendToneToServer(String expectedTone) {
        // arrange

        // act
        sp.sendToneToServer(expectedTone, 1);

        // assert
        assertEquals(expectedTone, dummy.data);
    }

    @Test
    public void test_stopEverything() {
        // arrange
        MediaPlayerAdapter mpa = mock(MediaPlayerAdapter.class);
        sp.currentlyPlaying.clear();
        sp.currentlyPlaying.add(mpa);

        // act
        sp.stopEverything();

        // assert
        verify(mpa, times(1)).stop();
    }

    @Test
    public void test_stopTheremin() {
        // arrange
        MediaPlayerAdapter mpa = mock(MediaPlayerAdapter.class);
        when(mpa.getTone()).thenReturn("therm");
        when(mpa.getUser()).thenReturn(randomInt);
        sp.currentlyPlaying.clear();
        sp.currentlyPlaying.add(mpa);

        // act
        sp.stopTheremin(randomInt);

        // assert
        verify(mpa, times(1)).stop();
    }
}
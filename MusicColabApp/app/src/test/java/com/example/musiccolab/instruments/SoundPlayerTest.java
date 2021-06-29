package com.example.musiccolab.instruments;

import com.example.musiccolab.CommunicationHandling;
import com.example.musiccolab.Lobby;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.Assert.assertEquals;

public class SoundPlayerTest {

    private static SoundPlayer sp;
    private static CommunicationHandling dummy;

    @BeforeAll
    static void setUp() {
        dummy = new CommunicationHandlingDummy();
        Lobby lobby = new Lobby();
        sp = new SoundPlayer(lobby);
        sp.activateTestingMode(dummy);
        //sp.generateToneList();
    }

    @ParameterizedTest
    @ValueSource(strings = {"piano0", "piano1", "piano2", "piano3", "piano4", "piano5", "piano6", "piano7", "drums0", "drums1", "drums2", "therm0", "therm1", "therm2", "therm3", "therm4", "therm5", "therm6", "therm7"})
    public void test_sendToneToServer(String expectedTone) {
        // arrange

        // act
        sp.sendToneToServer(expectedTone,1);

        // assert
        assertEquals(expectedTone, dummy.data);
    }
}
package com.example.musiccolab;

import com.example.musiccolab.instruments.SoundPlayer;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommunicationHandlingTest {

    @Test
    public void test_start_full_cycle() {
        //login
        // arrange
        CommunicationHandling ch= new CommunicationHandling(Thread.currentThread());
        ch.username = "marc";
        ch.password = "1";
        ch.action = 1;
        // act
        ch.start();
        try {
            synchronized (Thread.currentThread()) {
                // Set as connection timeout 2 seconds
                Thread.currentThread().wait(2000);
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        // assert
        assertEquals(1,ch.confirmation);

        //create lobby
        // arrange
        ch.lobbyName = "lobbyName";
        // act
        ch.action = 4;
        try {
            synchronized (Thread.currentThread()) {
                // Set as connection timeout 2 seconds
                Thread.currentThread().wait(2000);
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        // assert
        assertEquals(4,ch.confirmation);

        // send tone
        // arrange
        ch.soundPlayer = mock(SoundPlayer.class);
        ch.toneAction = (byte) 1;
        ch.data = "piano0";
        // act
        ch.action = 7;
        try {
            synchronized (Thread.currentThread()) {
                // Set as connection timeout 2 seconds
                Thread.currentThread().wait(2000);
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        // assert
        assertEquals(7,ch.confirmation);

        //leave lobby
        // arrange
        // act
        ch.action = 6;
        try {
            synchronized (Thread.currentThread()) {
                // Set as connection timeout 2 seconds
                Thread.currentThread().wait(2000);
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        // assert
        assertEquals(6,ch.confirmation);

        // logout
        // arrange
        // act
        ch.action = 2;
        try {
            synchronized (Thread.currentThread()) {
                // Set as connection timeout 2 seconds
                Thread.currentThread().wait(2000);
            }
        } catch (InterruptedException e) {
            System.out.println("Error with waiting of main thread.");
        }
        // assert
        assertEquals(2,ch.confirmation);

        // clean
        // arrange
        // act
        CommunicationHandling.wipeData(2,ch);
        // assert
        assertEquals(null,ch.username);
    }

}

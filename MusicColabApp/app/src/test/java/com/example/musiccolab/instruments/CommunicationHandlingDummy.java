package com.example.musiccolab.instruments;

import com.example.musiccolab.CommunicationHandling;

/**
 * Dummy class to mock CommunicationHandling for testing purposes
 */
public class CommunicationHandlingDummy extends CommunicationHandling {

    public short action = 0;
    public byte toneType;
    public byte toneAction;
    public String data;

    public CommunicationHandlingDummy() {
        super(null);
    }
}
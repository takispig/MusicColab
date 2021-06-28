package com.example.musiccolab;

import java.nio.ByteBuffer;

public class ClientState {

    public final static int DISCONNECTED = 0;
    public final static int loggedIn = 1;
    public final static int loggedOut = 2;
    public final static int inLobby = 3;
    public final static int notInLobby = 4;
    private final String[] stateAsMessage = {"DISCONNECTED", "loggedIn", "loggedOut", "inLobby", "notInLobby"};
    private boolean changed = false;

    private int state;
    private int previousState = -1;

    public ClientState() {
        this.previousState = DISCONNECTED;
        this.state = DISCONNECTED;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
        if(changed)
            previousState = state;
        changed = true;
    }

    public int getPreviousState() {
        return previousState;
    }

    public String getClientStateAsMessage(int state){
        return state < 4 && state > -1? stateAsMessage[state] : "";
    }
}

package main.java.com.example.musiccolab;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;

public class MusicJoiner {
    private Lobby lobby;
    private byte toneType;
    private byte toneAction;
    private String toneData;
    private short playersNumber;
    private LinkedList<Player> players;
    private ArrayList<SocketChannel> clientChannels = new ArrayList<>();
    private ArrayList<String> clientResponses =new ArrayList<>();
    private SocketChannel senderChannel;

    public MusicJoiner(Lobby lobby, byte toneAction, byte toneType, String toneData, SocketChannel channel) {

        this.lobby = lobby;
        this.toneAction = toneAction;
        this.toneType = toneType;
        this.toneData = toneData;
        playersNumber = (short) lobby.getMax_players();
        players = lobby.getPlayers();
        senderChannel = channel;
    }

    public void handleToneData(){
        for(Player player: lobby.getPlayers()){
            clientChannels.add(player.getPlayerChannel());
            clientResponses.add(toneData + "," + toneType + "," + toneAction);
        }
    }

    public ArrayList<SocketChannel> getClientChannels(){return clientChannels;}
    public ArrayList<String> getClientResponses(){return clientResponses;}
}

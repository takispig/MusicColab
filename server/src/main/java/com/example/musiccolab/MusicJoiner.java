package com.example.musiccolab;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class MusicJoiner {
    public static short playersNumber;


    public static int handleToneData(Charset messageCharset, Lobby lobby, byte toneAction, String toneData, short action, Player sender) {
        if(lobby != null) {
            playersNumber = (short) lobby.getMax_players();
            System.out.println("Get tone data: " + toneData);
            for (Player player : lobby.getPlayers()) {
                if (player.state.getState() == ClientState.inLobby && sender.getId() != player.getId())
                    if (sendTonToClient(messageCharset, player.getPlayerChannel(), toneData + "," + player.getId() + "," + toneAction, action) == -1){
                        return -2;
                    }
            }
            return 0;
        }
        return -1;
    }

    public static int sendTonToClient(Charset messageCharset, SocketChannel clientChannel, String message, short action) {
        short dataLength = (short) message.length();
        ByteBuffer messageBuffer = ByteBuffer.allocate(6 + dataLength);

        messageBuffer.put(ShortToByte((short)12845));
        messageBuffer.put(ShortToByte(action));
        messageBuffer.put(ShortToByte(dataLength));
        messageBuffer.put(message.getBytes(messageCharset));
        messageBuffer.flip();

        try {
            if (clientChannel != null)
                clientChannel.write(messageBuffer);
        } catch (IOException e) {
            return -1;
        }
        messageBuffer.clear();
        return 0;
    }

    public static byte[] ShortToByte(short value){
        byte[] temp = new byte[2];
        temp[0] = (byte)(value & 0xff);
        temp[1] = (byte)((value >> 8) & 0xff);

        return temp;
    }
}

package main.java.com.example.musiccolab;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class MusicJoiner {
    public static short playersNumber;


    public static void handleToneData(Charset messageCharset, Lobby lobby, byte toneAction, byte toneType, String toneData, short action){
        if(lobby != null) {
            playersNumber = (short) lobby.getMax_players();
            System.out.println("Get tone data: " + toneData);
            for (Player player : lobby.getPlayers()) {
                if (player.state.getState() == ClientState.inLobby)
                    sendTonToClient(messageCharset, player.getPlayerChannel(), toneData + "," + toneType + "," + toneAction, action);
            }
        }
    }

    public static void sendTonToClient(Charset messageCharset, SocketChannel clientChannel, String message, short action){
        short dataLength = (short) message.length();
        ByteBuffer messageBuffer = ByteBuffer.allocate(6 + dataLength);

        messageBuffer.put(ShortToByte((short)12845));
        messageBuffer.put(ShortToByte(action));
        messageBuffer.put(ShortToByte(dataLength));
        messageBuffer.put(message.getBytes(messageCharset));
        messageBuffer.flip();
        try {
            clientChannel.write(messageBuffer);
            messageBuffer.clear();
        }
        catch (IOException e){
            System.out.println("Error by sending message to client.");
        }
    }

    public static byte[] ShortToByte(short value){
        byte[] temp = new byte[2];
        temp[0] = (byte)(value & 0xff);
        temp[1] = (byte)((value >> 8) & 0xff);

        return temp;
    }
}

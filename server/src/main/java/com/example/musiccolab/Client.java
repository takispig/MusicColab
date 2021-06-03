package main.java.com.example.musiccolab;
import java.nio.charset.*;
import java.util.*;

public class Client{

    private static Charset messageCharset = null;
    private static CharsetDecoder decoder = null;
    private static byte [] clientName = null;


    final private List<Short> codesList = new ArrayList<Short>();
    final private List<Short> errorCodesList = new ArrayList<Short>();
    private boolean neededAction = false;
    final private short protocolName = 12845;
    private short action = 4;
    private String email = "zead1@gmail.com";
    private String userName = "zead1";
    private String password = "@1";
    private byte toneAction = 1;
    private byte toneType = 1;
    private String toneData = "dataExample2";
    private String lobbyName = "example";
    private String lobbyID = "0";

    public Client(){
        for(short index = 1; index < 11; index++) {
            codesList.add(index);
            errorCodesList.add( (short) (index + 10));
        }
    }

    public static void main(String [] args) {
        
        Client client = new Client();
        CommunicationHandling communicationHandling = new CommunicationHandling("127.0.0.1", 8080);
        communicationHandling.register(client.email, client.userName, client.password);
        communicationHandling.login(client.userName, client.password);
        //communicationHandling.logout(client.userName, client.password);
        communicationHandling.createLobby("0");
        //communicationHandling.joinLobby(0);
        //communicationHandling.leaveLobby(0);
        //communicationHandling.logout(client.userName, client.password);
        //communicationHandling.sendTone(client.toneData, client.toneType, client.toneAction);

        Scanner getInput = new Scanner(System.in);
        short input = getInput.nextShort();
    }
}
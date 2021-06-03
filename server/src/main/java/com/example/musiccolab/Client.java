package main.java.com.example.musiccolab;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Client{

    private static Charset messageCharset = null;
    private static CharsetDecoder decoder = null;
    private static byte [] clientName = null;

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
    private int lobbyID = 0;


    public static void main(String [] args) throws InterruptedException {
        Client client = new Client();

        CommunicationHandling communicationHandling = new CommunicationHandling(Thread.currentThread(), args[0], Integer.parseInt(args[1]));

        communicationHandling.email = client.email;
        communicationHandling.username = client.userName;
        communicationHandling.password = client.password;
        communicationHandling.action = 3;
        communicationHandling.start();
        TimeUnit.SECONDS.sleep(60);
        communicationHandling.action = 1;
        TimeUnit.SECONDS.sleep(60);
        communicationHandling.lobbyNameOrID = client.lobbyName;
        communicationHandling.action = 4;
        TimeUnit.SECONDS.sleep(60);
        communicationHandling.toneType = client.toneType;
        communicationHandling.toneAction = client.toneAction;
        communicationHandling.data = client.toneData;
        communicationHandling.action = 7;

        Scanner getInput = new Scanner(System.in);
        short input = getInput.nextShort();
    }
}
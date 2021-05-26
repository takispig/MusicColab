package main.java.com.example.musiccolab;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CommunicationHandling {
    private Charset messageCharset = null;
    private Socket socket = null;
    private BufferedOutputStream out;
    private BufferedReader in;

    private String IP;
    private int port;


    final private List<Short> codesList = new ArrayList<Short>();
    final private List<Short> errorCodesList = new ArrayList<Short>();
    final private short protocolName = 12845;
    private short action;

    public static List<String> toneList = new LinkedList<>();
    public static boolean ToneDataEventChecker = false;//set false when tone list is empty.
    private boolean threadExist = false;
    private ToneListener toneThread = null;


    /**
     *  Please creat two a boolean, static eventChecker variable.
     *      boolean : ToneDataEventChecker
     *      boolean : eventChecker
     *  You should check these variables temporary to know if there are a message received fro  Server.
     *
     *
     *  I did that locally. But it shouldn't be so.
     */


    public CommunicationHandling(String IP_address, int port){
        this.IP = IP_address;
        this.port = port;
        for(short index = 1; index < 11; index++) {
            codesList.add(index);
            errorCodesList.add( (short) (index + 10));
        }
    }


    public String register(String email, String userName, String password){
        action = 3;
        return loginSystem(action, email, userName, password);
    }

    public String login(String userName, String password){
        action = 1;
        return loginSystem(action, "", userName, password);
    }

    public String logout(String userName, String password){
        action = 2;
        return loginSystem(action, "", userName, password);
    }

    public String createLobby(String lobbyName){
        action = 4;
        return lobby(action, lobbyName);
    }

    public String joinLobby(int lobbyId){
        action = 5;
        return lobby(action, Integer.toString(lobbyId));
    }

    public String leaveLobby(int lobbyId){
        action = 6;
        return lobby(action, Integer.toString(lobbyId));
    }

    public String sendTone(String data, byte toneType, byte toneAction){
        action = 7;
        short dataLength = (short) (data.length() + 2);
        ByteBuffer buffer = ByteBuffer.allocate(6 + 2 + dataLength);

        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));
        buffer.put(toneAction);
        buffer.put(toneType);
        buffer.put(data.getBytes(messageCharset));
        buffer.flip();
        try {
            out.write(buffer.array());
            out.flush();
        }catch (IOException e){
            System.out.println("Error");
        }
        buffer.clear();
        return "";
    }

//______________________________________________________________________________________________________________________
/////////////////////////                              Lobby functions                         /////////////////////////
//______________________________________________________________________________________________________________________
    private String lobby(short action, String lobbyNameOrID){
        ByteBuffer buffer = null;
            try{sendLobbyMessage(action, lobbyNameOrID);}
            catch (IOException e){
                System.err.println("Can not write in buffer.");
                return "Error";
            }
            String[] actionDataLength = analyseResponse(messageCharset);
            if(!threadExist && Integer.parseInt(actionDataLength[0]) < 10 && action != 6){
                toneThread = new ToneListener(IP, port, out, in);
                toneThread.start();
                threadExist = true;
            }
            if(Integer.parseInt(actionDataLength[1]) >= 0){
                System.out.println(actionDataLength[2]);
                return actionDataLength[2];
            }
            else{return "Error";}
    }

    private void sendLobbyMessage(short action, String lobbyNameOrID) throws IOException {
        short dataLength;
        ByteBuffer buffer = null;

        dataLength = (short) lobbyNameOrID.length();
        buffer = ByteBuffer.allocate(6 + dataLength);

        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));
        buffer.put(lobbyNameOrID.getBytes(messageCharset));

        buffer.flip();
        out.write(buffer.array());
        out.flush();
        buffer.clear();
    }

//______________________________________________________________________________________________________________________
/////////////////////////                           Login System functions                     /////////////////////////
//______________________________________________________________________________________________________________________
    private String loginSystem(short action, String email, String userName, String password){
        String response;
        ByteBuffer buffer = null;
        boolean NoConnection = false;
        if(action != 2)
            NoConnection = buildConnection() == 0 && connectToServer() == 0;
        if(NoConnection || action == 2){
            try{sendLoginSystemMessage(action, email, userName, password);}
            catch (IOException e){
                System.err.println("Can not write in buffer.");
                return "Error";
            }
            String[] actionDataLength = analyseResponse(messageCharset);
            if(Integer.parseInt(actionDataLength[1]) >= 0){
                try{ response = actionDataLength[2];
                    if(action == 3 || action == 2){
                        socket.close();
                        in.close();
                        out.close();
                    }
                }
                catch (IOException e){
                    System.err.println("Can not read from buffer.");
                    return "Error";
                }
                System.out.println(response);
                return response;
            } else{return "Error";}
        }else {return "Error";}

    }

    private void sendLoginSystemMessage(short action, String email, String username, String password) throws IOException {
        short dataLength;
        byte emailLength, userNameLength, passwordLength, size = 2;
        emailLength = 0;

        String message = "";
        ByteBuffer buffer = null;

        if(action == 3) {
            emailLength = (byte) email.length();
            message = email;
            size = 3;
        }
        userNameLength = (byte) username.length();
        passwordLength = (byte) password.length();
        message += username+password;
        dataLength = (short) message.length();
        buffer = ByteBuffer.allocate(6 + size + dataLength);

        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));
        if(action == 3) buffer.put(emailLength);
        buffer.put(userNameLength);
        buffer.put(passwordLength);
        buffer.put(message.getBytes(messageCharset));

        try {
            out.write(buffer.array());
            out.flush();
        }catch (IOException e){
            System.out.println("Error");
        }
        buffer.clear();
    }

//______________________________________________________________________________________________________________________
/////////////////////////                             read header function                     /////////////////////////
//______________________________________________________________________________________________________________________
    private String[] analyseResponse(Charset messageCharset) {
        String[] temp;

        try {
            String message = in.readLine();
            String[] parsedMessage = message.split(",");
            //
            short action = 0;
            if (protocolName == Integer.parseInt(parsedMessage[0])) {
                action = (short) Integer.parseInt(parsedMessage[1]);
                if (codesList.contains(action) || errorCodesList.contains(action)) {
                    temp = new String[]{Integer.toString(action), parsedMessage[2], parsedMessage[3]};
                    return temp;

                } else {
                    System.err.println("Server sent unknown action");
                    return new String[]{Integer.toString(action), "-2"};
                }
            } else {
                System.err.println("Message from foreign device.");
                return new String[]{Integer.toString(action), "-1"};
            }
        }
        catch (IOException e){
            System.err.println("Can not read buffer.");
            return new String[]{Integer.toString(action), "-3"};
        }
    }
//______________________________________________________________________________________________________________________
/////////////////////////                             connection functions                     /////////////////////////
//______________________________________________________________________________________________________________________
    private byte buildConnection(){
        InetSocketAddress remoteAddress = null;

        try {
            messageCharset = Charset.forName("US-ASCII");
        } catch(UnsupportedCharsetException uce) {
            System.err.println("Cannot create charset for this application. Exiting...");
            return -1;
        }

        try {
            remoteAddress = new InetSocketAddress(IP, port);
        } catch(IllegalArgumentException | SecurityException e) {
            System.err.println("Can not connect to Server.");
            return -1;
        }
        System.out.println("Connecting to server " + IP + ":" + port);

        try {
            socket = new Socket();
            socket.connect(remoteAddress);
            out = new BufferedOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(IOException e) {
            System.err.println("Connecting to Server failed.");
            return -1;
        }
        return 0;
    }

    private byte connectToServer() {
        try {
            System.out.println(in.readLine());
        }catch (IOException e){
            System.out.println("error");
        }
        return 0;
    }
//______________________________________________________________________________________________________________________
/////////////////////////                             side functions                           /////////////////////////
//______________________________________________________________________________________________________________________

    private byte[] convertShortToByte(short value){
        byte[] temp = new byte[2];
        temp[0] = (byte)(value & 0xff);
        temp[1] = (byte)((value >> 8) & 0xff);
        return temp;
    }

    private static short getShort(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }
}

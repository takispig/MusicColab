package com.example.musiccolab;

import static xdroid.toaster.Toaster.toast;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


class RegisterThread extends Thread{
    @Override
    public void run() {
        CommunicationHandling.getInstance().register(CommunicationHandling.email,CommunicationHandling.userName,CommunicationHandling.password);
    }
}

class LoginThread extends Thread{
    @Override
    public void run() {
        CommunicationHandling.getInstance().login(CommunicationHandling.userName,CommunicationHandling.password);
    }
}

class LogoutThread extends Thread{
    @Override
    public void run() {
        CommunicationHandling.getInstance().logout(CommunicationHandling.userName,CommunicationHandling.password);
    }
}

class CreateThread extends Thread{
    @Override
    public void run() {
        CommunicationHandling.getInstance().createLobby(CommunicationHandling.lobbyName);
    }
}

class JoinThread extends Thread{
    @Override
    public void run() {
        CommunicationHandling.getInstance().joinLobby(CommunicationHandling.lobbyID);
    }
}

class leaveThread extends Thread{
    @Override
    public void run() {
        CommunicationHandling.getInstance().leaveLobby(CommunicationHandling.lobbyID);
    }
}

public class CommunicationHandling {

    private static CommunicationHandling communicationHandling = null;
    public static synchronized CommunicationHandling getInstance() {
        if (communicationHandling == null) communicationHandling = new CommunicationHandling();
        return communicationHandling;
    }

    private static Charset messageCharset = null;
    private static Socket socket = null;
    private static BufferedOutputStream out;
    private static BufferedReader in;

    static String IP = "10.0.2.2"; //35.207.116.16
    //static String IP = "35.207.116.16";
    static int port = 8080;


    final private static List<Short> codesList = new ArrayList<Short>();
    final private static List<Short> errorCodesList = new ArrayList<Short>();
    final private static short protocolName = 12845;
    private static short action;
    public static String password = null;
    public static String userName = null;
    public static String email = null;
    public static int lobbyID = -1;
    public static String lobbyName = null;
    public static int confirmation = 0;
    public static boolean admin = false;
    public static List<Integer> lobbyIDs = new ArrayList<>();

    public static List<String> toneList = new LinkedList<>();
    public static boolean ToneDataEventChecker = false;//set false when tone list is empty.
    private static boolean threadExist = false;
    private static ToneListener toneThread = null;

    /**
     *  Please create two a boolean, static eventChecker variable.
     *      boolean : ToneDataEventChecker
     *      boolean : eventChecker
     *  You should check these variables temporary to know if there are a message received fro  Server.
     *
     *
     *  I did that locally. But it shouldn't be so.
     */


    public CommunicationHandling() {
        for(short index = 1; index < 11; index++) {
            codesList.add(index);
            errorCodesList.add( (short) (index + 10));
        }
    }

    public void register(String email, String userName, String password){
        action = 3;
        if(loginSystem(action, email, userName, password).equals("Error")){
            toast("Register Failed\nPlease try again");
        }
    }

    public void login(String userName, String password){
        action = 1;
        if(loginSystem(action, "", userName, password).equals("Error")){
            toast("Login Failed\nPlease try again");
        }
    }

    public void logout(String userName, String password){
        action = 2;
        if(loginSystem(action, "", userName, password).equals("Error")){
            toast("Logout Failed\nPlease try again");
        }
    }

    public void createLobby(String lobbyName){
        action = 4;
        System.out.println("In CreateLobby");
        if(lobby(action, lobbyName).equals("Error")){
            toast("Create Lobby Failed\nPlease try again");
        }
    }

    public void joinLobby(int lobbyId){
        action = 5;
        if(lobby(action,Integer.toString(lobbyId)).equals("Error")){
            toast("Join Lobby Failed\nPlease try again");
        }
    }

    public void leaveLobby(int lobbyId){
        action = 6;
        System.out.println("In Leave Lobby");
        if(lobby(action,Integer.toString(lobbyId)).equals("Error")){
            toast("Leave Lobby Failed\nPlease try again");
        }
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
    private static String lobby(short action, String lobbyNameOrID){
        ByteBuffer buffer = null;
        try{sendLobbyMessage(action, lobbyNameOrID);}
        catch (IOException e){
            System.err.println("Can not write in buffer.");
            return "Error";
        }
        String[] actionDataLength = analyseResponse(messageCharset);
        confirmation = Integer.parseInt(actionDataLength[0]);
        System.out.println("confirmation-code: " + confirmation + " (actionDataLength[0])");
        System.out.println("Server's response: " + actionDataLength[2]);
        if (action == 6) {
            // if leaveLobby then get things done manually :'( (please server-team don't change your responses)
            System.out.println(actionDataLength.length + "  " + actionDataLength[2].split(" ")[4]);
            if (actionDataLength.length == 5)
                if (actionDataLength[2].split(" ")[4].equals("out.")) {
                confirmation = 6;
            }
        }
        if (confirmation == 4) {
            // if join lobby successful, then parse the lobbyID from the response
            lobbyID = Integer.parseInt(actionDataLength[2].split(" ")[1]);
        }
        if (Integer.parseInt(actionDataLength[0]) > 10) return "Error";
        if(!threadExist && Integer.parseInt(actionDataLength[0]) < 10 && action != 6){
            toneThread = new ToneListener(IP, port, out, in);
            toneThread.start();
            threadExist = true;
        }
        else if(threadExist && Integer.parseInt(actionDataLength[0]) < 10 && action == 6){
            toneThread.stop();
            threadExist = false;
        }
        if(Integer.parseInt(actionDataLength[1]) >= 0){
            System.out.println("confirmation-code: " + confirmation + " (actionDataLength[0])");
            System.out.println("Server's response: " + actionDataLength[2]);
            return actionDataLength[2] + actionDataLength[0];
        }
        else {
            return "Error";
        }
    }

    private static void sendLobbyMessage(short action, String lobbyNameOrID) throws IOException {
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
            try{
                sendLoginSystemMessage(action, email, userName, password);
            }
            catch (IOException e){
                System.err.println("Can not write in buffer.");
                return "Error";
            }
            String[] actionDataLength = analyseResponse(messageCharset);
            confirmation = Integer.parseInt(actionDataLength[0]);
            System.out.println("confirmation-code: " + confirmation + " (actionDataLength[0])");
            if(Integer.parseInt(actionDataLength[1]) >= 0){
                try {
                    response = actionDataLength[2];
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
                System.out.println("Server's response: " + actionDataLength.length);
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
    private static String[] analyseResponse(Charset messageCharset) {
        String[] temp;

        try {
            String message = in.readLine();
            System.out.println("Message: " + message);
            String[] parsedMessage = message.split(",");

            if (action == 1) {
                // get lobbyids after login
                CommunicationHandling.getInstance().getLobbyIds(parsedMessage);
                System.out.println("LobbyIDs: " + lobbyIDs);
            }
            // I am desperate...i try to make LeaveLobby work
//            if (action == 6) {
//                try {
//                    if (Integer.parseInt(message.split(" ")[1]) == lobbyID) {
//                        CommunicationHandling.confirmation = 6;
//                    }
//                } catch (ArrayIndexOutOfBoundsException e){
//                    e.printStackTrace();
//                    System.out.println("Leave Lobby didn't work well");
//                }
//            }
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

    private void getLobbyIds(String[] response) {
        if (response.length > 4) {
            for (int i=0; i<response.length-5; i++) {
                lobbyIDs.add(Integer.parseInt(response[i+4].replaceAll("\\s","")));
            }
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

    private static byte[] convertShortToByte(short value){
        byte[] temp = new byte[2];
        temp[0] = (byte)(value & 0xff);
        temp[1] = (byte)((value >> 8) & 0xff);
        return temp;
    }

    private static short getShort(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }
}






// Zead's Version




//package com.example.musiccolab;
//
//import java.io.*;
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.SocketChannel;
//import java.nio.charset.*;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
//public class CommunicationHandling implements Runnable{
//    private static Charset messageCharset = null;
//    private static CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
//    private static CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
//    private SocketChannel clientChannel = null;
//    private InetSocketAddress remoteAddress = null;
//    private Selector selector = null;
//
//    private String IP;
//    private int port;
//
//
//    final private List<Short> codesList = new ArrayList<Short>();
//    final private List<Short> errorCodesList = new ArrayList<Short>();
//    final private short protocolName = 12845;
//
//    public short action = 0;
//
//    public String email;
//    public String username;
//    public String password;
//
//    public String lobbyNameOrID;
//    public List<Integer> IdList = new LinkedList<>();
//
//    public byte toneAction;
//    public byte toneType;
//    public String data;
//
//    public Thread communicationThread = null;
//    private Thread mainThread = null;
//    public static String result = "";
//
//
//    public CommunicationHandling(Thread thread, String IP_address, int port){
//        mainThread = thread;
//        this.IP = IP_address;
//        this.port = port;
//        for(short index = 1; index < 11; index++) {
//            codesList.add(index);
//            errorCodesList.add( (short) (index + 10));
//        }
//    }
//
//
//    @Override
//    public void run(){
//        buildConnection();
//        connectToServer();
//        while (true) {
//            try{
//                selector.select();
//            }catch (IOException e) {
//                System.out.println("Problem with selector");
//            }
//
//            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
//
//            while (selectedKeys.hasNext()) {
//                SelectionKey key = (SelectionKey) selectedKeys.next();
//                clientChannel = (SocketChannel) key.channel();
//                ByteBuffer buffer;
//                if (key.isConnectable()) {
//                    buffer = ByteBuffer.allocate(100);
//                    try {
//
//                        clientChannel.finishConnect();
//                        clientChannel.read(buffer);
//                        buffer.flip();
//                        System.out.println(messageCharset.decode(buffer));
//                    }catch (IOException e){
//                        System.out.println("Problem with finishConnect");
//                    }
//                }
//                else if (key.isReadable()) {
//                    short[] actionAndDataLength = analyseMainBuffer(messageCharset, clientChannel);
//                    if(actionAndDataLength[1] > 0) {
//                        handleAction(actionAndDataLength[0], actionAndDataLength[1]);
//                    }
//                    else{
//                        try{
//                            clientChannel.read(ByteBuffer.allocate(1000));
//                        }catch (IOException e){
//                            System.err.println("Error with empty channel.");
//                        }
//                    }
//
//                }
//                selectedKeys.remove();
//            }
//            if(codesList.contains(action)){
//                sendMessageByAction(action);
//                action = 0;
//            }
//        }
//    }
//
//    public void start() throws InterruptedException {
//        communicationThread = new Thread(this, "secondaryThread");
//        communicationThread.start();
//    }
//
//    private void sendMessageByAction(short action){
//        if(action == 1 || action == 2 || action == 3){
//            try{sendLoginSystemMessage(action, email, username, password);}
//            catch (IOException e){
//                System.err.println("Can not write in buffer.");
//            }
//        }
//        else if(action == 4 || action == 5 || action == 6){
//            try{sendLobbyMessage(action, lobbyNameOrID);}
//            catch (IOException e){
//                System.err.println("Can not write in buffer.");
//            }
//        }
//        else if(action == 7){
//            try{sendTone(action);}
//            catch (IOException e){
//                System.err.println("Can not write in buffer.");
//            }
//        }
//    }
//
//    private void handleAction(short action, short messageLength){
//        if(action == 1 || action == 2 || action == 3 || action == 11 || action == 12 || action == 13){
//            loginSystem(action, messageLength);
//        }
//        else if(action == 4 || action == 5 || action == 6 || action == 14 || action == 15 || action == 16){
//            lobby(action, messageLength);
//        }
//        else if(action == 7 || action == 17){
//            getData(messageLength);
//        }
//    }
//
//
//    //______________________________________________________________________________________________________________________
///////////////////////////                              Lobby functions                         /////////////////////////
////______________________________________________________________________________________________________________________
//    private void music(){
//        System.out.println("Do you hear our music?");
//    }
//
//    private void getData(short dataLength) {
//        if (dataLength >= 0) {
//            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
//            try{
//                clientChannel.read(buffer);
//                buffer.flip();
//                result = messageCharset.decode(buffer).toString();
//                music();
//            }catch (IOException e){
//                System.err.println("Can not read from buffer.");
//            }
//        }
//    }
//
//    private void sendTone(short action) throws IOException {
//        short dataLength = (short) (data.length());
//        ByteBuffer buffer = ByteBuffer.allocate(6 + 2 + dataLength);
//
//        buffer.put(convertShortToByte(protocolName));
//        buffer.put(convertShortToByte(action));
//        buffer.put(convertShortToByte(dataLength));
//        buffer.put(toneAction);
//        buffer.put(toneType);
//        buffer.put(data.getBytes(messageCharset));
//
//        buffer.flip();
//        clientChannel.write(buffer);
//        buffer.clear();
//    }
//    //______________________________________________________________________________________________________________________
///////////////////////////                              Lobby functions                         /////////////////////////
////______________________________________________________________________________________________________________________
//    private void lobby(short action, short dataLength){
//        if(dataLength >= 0){
//            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
//            try{
//                clientChannel.read(buffer);
//                buffer.flip();
//                result = messageCharset.decode(buffer).toString();
//            }
//            catch (IOException e){
//                System.err.println("Can not read from buffer.");
//            }
//        } else{result = "There are no data."; mainThread.notify();}
//
//    }
//
//    private void sendLobbyMessage(short action, String lobbyNameOrID) throws IOException {
//        short dataLength;
//        ByteBuffer buffer = null;
//
//        dataLength = (short) lobbyNameOrID.length();
//        buffer = ByteBuffer.allocate(6 + dataLength);
//
//        buffer.put(convertShortToByte(protocolName));
//        buffer.put(convertShortToByte(action));
//        buffer.put(convertShortToByte(dataLength));
//        buffer.put(lobbyNameOrID.getBytes(messageCharset));
//
//        buffer.flip();
//        clientChannel.write(buffer);
//        buffer.clear();
//    }
//
//    //______________________________________________________________________________________________________________________
///////////////////////////                           Login System functions                     /////////////////////////
////______________________________________________________________________________________________________________________
//    private void loginSystem(short action, short dataLength){
//        if(dataLength >= 0){
//            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
//            String[] response;
//            try{
//                clientChannel.read(buffer);
//                buffer.flip();
//                if(action == 3 || action == 2 || action == 13 || action == 12){
//                    result = messageCharset.decode(buffer).toString();
//                    clientChannel.close();
//                    if(action == 3 || action == 13) {
//                        buildConnection();
//                        connectToServer();
//                    }
//                    if(action == 2)
//                        Thread.currentThread().stop();
//                }
//                else if(action == 1){
//                    response = messageCharset.decode(buffer).toString().split(",");
//                    for(byte index = 0; index < response.length; index++){
//                        if(index == 0)
//                            result = response[index];
//                        else
//                            IdList.add(Integer.parseInt(response[index]));
//                    }
//                }else{
//                    result = messageCharset.decode(buffer).toString();
//                }
//            }
//            catch (IOException e){
//                System.err.println("Can not read from buffer.");
//            }
//        } else{result = "There are no data.";}
//
//    }
//
//    private void sendLoginSystemMessage(short action, String email, String username, String password) throws IOException {
//        short dataLength;
//        byte emailLength, userNameLength, passwordLength, size = 2;
//        emailLength = 0;
//
//        String message = "";
//        ByteBuffer buffer = null;
//
//        if(action == 3) {
//            emailLength = (byte) email.length();
//            message = email;
//            size = 3;
//        }
//        userNameLength = (byte) username.length();
//        passwordLength = (byte) password.length();
//        message += username+password;
//        dataLength = (short) message.length();
//        buffer = ByteBuffer.allocate(6 + size + dataLength);
//
//        buffer.put(convertShortToByte(protocolName));
//        buffer.put(convertShortToByte(action));
//        buffer.put(convertShortToByte(dataLength));
//        if(action == 3) buffer.put(emailLength);
//        buffer.put(userNameLength);
//        buffer.put(passwordLength);
//        buffer.put(message.getBytes(messageCharset));
//
//        buffer.flip();
//        clientChannel.write(buffer);
//        buffer.clear();
//    }
//
//    //______________________________________________________________________________________________________________________
///////////////////////////                             read header function                     /////////////////////////
////______________________________________________________________________________________________________________________
//    private short[] analyseMainBuffer(Charset messageCharset, SocketChannel clientChannel) {
//        short[] temp;
//        ByteBuffer mainBuffer;
//        short action = 0;
//
//        try {
//            mainBuffer = ByteBuffer.allocate(2);
//            clientChannel.read(mainBuffer);
//            mainBuffer.flip();
//
//            short nameOfProtocol = getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset));
//            if (protocolName == nameOfProtocol) {
//                mainBuffer.clear();
//
//                clientChannel.read(mainBuffer);
//                mainBuffer.flip();
//                action = getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset));
//                if (codesList.contains(action) || errorCodesList.contains(action)) {
//                    mainBuffer.clear();
//                    clientChannel.read(mainBuffer);
//                    mainBuffer.flip();
//                    temp = new short[]{action, getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset))};
//                    return temp;
//
//                } else {
//                    System.err.println("Server sent unknown action.");
//                    return new short[]{action, 0};
//                }
//            }else{
//                System.err.println("Response is not from Server.");
//                return new short[]{action, 0};
//            }
//        }
//        catch (IOException e){
//            System.err.println("Error with reading buffer in analyseMainBuffer.");
//            return new short[]{action, 0};
//        }
//    }
//    //______________________________________________________________________________________________________________________
///////////////////////////                             connection functions                     /////////////////////////
////______________________________________________________________________________________________________________________
//    private void buildConnection(){
//
//        try {
//            messageCharset = StandardCharsets.US_ASCII;
//        } catch(UnsupportedCharsetException uce) {
//            System.err.println("Cannot create charset for this application. Exiting...");
//            return;
//        }
//
//        try {
//            remoteAddress = new InetSocketAddress(IP, port);
//        } catch(IllegalArgumentException | SecurityException e) {
//            System.err.println("Can not connect to Server.");
//            return;
//        }
//        decoder = messageCharset.newDecoder();
//
//        try {
//            selector = Selector.open();
//        } catch(IOException e) {
//            System.err.println("Connecting to Server failed.");
//            return;
//        }
//        System.out.println("Connecting to server " + IP + ":" + port);
//    }
//
//    private void connectToServer() {
//        try {
//            clientChannel = SocketChannel.open();
//            clientChannel.configureBlocking(false);
//            clientChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
//            clientChannel.connect(remoteAddress);
//        }catch (IOException e){
//            System.out.println("error");
//        }
//    }
////______________________________________________________________________________________________________________________
///////////////////////////                             side functions                           /////////////////////////
////______________________________________________________________________________________________________________________
//
//    private byte[] convertShortToByte(short value){
//        byte[] temp = new byte[2];
//        temp[0] = (byte)(value & 0xff);
//        temp[1] = (byte)((value >> 8) & 0xff);
//        return temp;
//    }
//
//    private static short getShort(byte[] b) {
//        return (short) (((b[1] << 8) | b[0] & 0xff));
//    }
//}

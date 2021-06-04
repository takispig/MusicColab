package com.example.musiccolab;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CommunicationHandling implements Runnable{
    private Charset messageCharset = null;
    private CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
    private CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
    private SocketChannel clientChannel = null;
    private InetSocketAddress remoteAddress = null;
    private Selector selector = null;

    private final String IP = "10.0.2.2";
    private int port = 8080;

    final private List<Short> codesList = new ArrayList<Short>();
    final private List<Short> errorCodesList = new ArrayList<Short>();
    final private short protocolName = 12845;

    public short action = 0;
    public String email = null;
    public String username = null;
    public String password = null;
    public String lobbyName = null;
    public int lobbyID = -1;
    public boolean admin = false;
    public List<Integer> IdList = new LinkedList<>();

    public byte toneAction;
    public byte toneType;
    public String data;

    public Thread communicationThread = null;
    private Thread mainThread = null;
    public String result = "";
    public int confirmation;
    public boolean threadExist = false;


    public CommunicationHandling(Thread thread) {
        mainThread = thread;
        for(short index = 1; index < 11; index++) {
            codesList.add(index);
            errorCodesList.add( (short) (index + 10));
        }
    }


    @Override
    public void run(){
        if(action == 3 || action == 1){
            buildConnection();
            connectToServer();
        }
        while (true) {
            try{
                selector.select();
            }catch (IOException e) {
                System.out.println("Problem with selector");
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();
                clientChannel = (SocketChannel) key.channel();
                ByteBuffer buffer;
                if (key.isConnectable()) {
                    buffer = ByteBuffer.allocate(100);
                    try {
                        clientChannel.finishConnect();
                        clientChannel.read(buffer);
                        buffer.flip();
                        System.out.println(messageCharset.decode(buffer));
                    }catch (IOException e){
                        System.out.println("Problem with finishConnect");
                    }
                }
                else if (key.isReadable()) {
                    short[] actionAndDataLength = analyseMainBuffer(messageCharset, clientChannel);
                    if(actionAndDataLength[1] > 0) {
                        confirmation = actionAndDataLength[0];
                        handleAction(actionAndDataLength[0], actionAndDataLength[1]);
                    }
                    else{
                        try{
                            clientChannel.read(ByteBuffer.allocate(1000));
                        }catch (IOException e){
                            System.err.println("Error with empty channel.");
                        }
                    }

                }
                selectedKeys.remove();
            }
            if(codesList.contains(action)){
                sendMessageByAction(action);
                action = 0;
            }
        }
    }

    public void start() {
        communicationThread = new Thread(this, "secondaryThread");
        try{
            communicationThread.start();
            threadExist = true;
        }catch (Exception e){
            System.out.println("Error with starting network thread.");
        }
    }

    private void sendMessageByAction(short action){
        if(action == 1 || action == 2 || action == 3){
            try{sendLoginSystemMessage(action, email, username, password);}
            catch (IOException e){
                System.err.println("Can not write in buffer.");
            }
        }
        else if(action == 4 || action == 5 || action == 6){
            try{
                if(action == 4)
                    sendLobbyMessage(action, lobbyName);
                else
                    sendLobbyMessage(action, Integer.toString(lobbyID));
            }
            catch (IOException e){
                System.err.println("Can not write in buffer.");
            }
        }
        else if(action == 7){
            try{sendTone(action);}
            catch (IOException e){
                System.err.println("Can not write in buffer.");
            }
        }
    }

    private void handleAction(short action, short messageLength){
        if(action == 1 || action == 2 || action == 3 || action == 11 || action == 12 || action == 13){
            loginSystem(action, messageLength);
            synchronized (mainThread) {
                mainThread.notify();
            }
            if(confirmation == 2) {
                System.out.println(confirmation);
                try {
                    synchronized (Thread.currentThread()) {
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Error with waiting of main thread.");
                    e.printStackTrace();
                }
            }

        }
        else if(action == 4 || action == 5 || action == 6 || action == 14 || action == 15 || action == 16){
            lobby(action, messageLength);
            synchronized (mainThread) {
                mainThread.notify();
            }
        }
        else if(action == 7 || action == 17){
            getData(messageLength);
        }
    }


    //______________________________________________________________________________________________________________________
/////////////////////////                              Lobby functions                         /////////////////////////
//______________________________________________________________________________________________________________________
    private void music(){
        System.out.println("Do you hear our music?");
    }

    private void getData(short dataLength) {
        if (dataLength >= 0) {
            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
            try{
                clientChannel.read(buffer);
                buffer.flip();
                result = messageCharset.decode(buffer).toString();
                music();
            }catch (IOException e){
                System.err.println("Can not read from buffer.");
            }
        }
    }

    private void sendTone(short action) throws IOException {
        short dataLength = (short) (data.length());
        ByteBuffer buffer = ByteBuffer.allocate(6 + 2 + dataLength);

        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));
        buffer.put(toneAction);
        buffer.put(toneType);
        buffer.put(data.getBytes(messageCharset));

        buffer.flip();
        clientChannel.write(buffer);
        buffer.clear();
    }
    //______________________________________________________________________________________________________________________
/////////////////////////                              Lobby functions                         /////////////////////////
//______________________________________________________________________________________________________________________
    private void lobby(short action, short dataLength){
        if(dataLength >= 0){
            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
            try{
                clientChannel.read(buffer);
                buffer.flip();
                result = messageCharset.decode(buffer).toString();
                if(action == 4) {
                    int a = result.indexOf(" ");
                    lobbyID = Integer.parseInt(result.substring(a + 1, a + 2));
                }
            }
            catch (IOException e){
                System.err.println("Can not read from buffer.");
            }
        } else{result = "There are no data."; mainThread.notify();}

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
        clientChannel.write(buffer);
        buffer.clear();
    }

    //______________________________________________________________________________________________________________________
/////////////////////////                           Login System functions                     /////////////////////////
//______________________________________________________________________________________________________________________
    private void loginSystem(short action, short dataLength){
        if(dataLength >= 0){
            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
            String[] response;
            try{
                clientChannel.read(buffer);
                buffer.flip();
                if(action == 3 || action == 2 || action == 13 || action == 12){
                    result = messageCharset.decode(buffer).toString();
                    clientChannel.close();
                }
                else if(action == 1){
                    response = messageCharset.decode(buffer).toString().split(",");
                    for(int index = 0; index < response.length; index++){
                        if(index == 0)
                            result = response[index];
                        else
                            IdList.add(Character.getNumericValue(response[index].charAt(0)));
                    }
                }else{
                    result = messageCharset.decode(buffer).toString();
                }
            }
            catch (IOException e){
                System.err.println("Can not read from buffer.");
            }
        } else{result = "There are no data.";}

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

        buffer.flip();
        clientChannel.write(buffer);
        buffer.clear();
    }

    //______________________________________________________________________________________________________________________
/////////////////////////                             read header function                     /////////////////////////
//______________________________________________________________________________________________________________________
    private short[] analyseMainBuffer(Charset messageCharset, SocketChannel clientChannel) {
        short[] temp;
        ByteBuffer mainBuffer;
        short action = 0;

        try {
            mainBuffer = ByteBuffer.allocate(2);
            clientChannel.read(mainBuffer);
            mainBuffer.flip();

            short nameOfProtocol = getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset));
            if (protocolName == nameOfProtocol) {
                mainBuffer.clear();

                clientChannel.read(mainBuffer);
                mainBuffer.flip();
                action = getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset));
                if (codesList.contains(action) || errorCodesList.contains(action)) {
                    mainBuffer.clear();
                    clientChannel.read(mainBuffer);
                    mainBuffer.flip();
                    temp = new short[]{action, getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset))};
                    return temp;

                } else {
                    System.err.println("Server sent unknown action.");
                    return new short[]{action, 0};
                }
            }else{
                System.err.println("Response is not from Server.");
                return new short[]{action, 0};
            }
        }
        catch (IOException e){
            System.err.println("Error with reading buffer in analyseMainBuffer.");
            return new short[]{action, 0};
        }
    }
    //______________________________________________________________________________________________________________________
/////////////////////////                             connection functions                     /////////////////////////
//______________________________________________________________________________________________________________________
    private void buildConnection(){

        try {
            messageCharset = StandardCharsets.US_ASCII;
        } catch(UnsupportedCharsetException uce) {
            System.err.println("Cannot create charset for this application. Exiting...");
            return;
        }

        try {
            remoteAddress = new InetSocketAddress(IP, port);
        } catch(IllegalArgumentException | SecurityException e) {
            System.err.println("Can not connect to Server.");
            return;
        }
        decoder = messageCharset.newDecoder();

        try {
            selector = Selector.open();
        } catch(IOException e) {
            System.err.println("Error with selector.");
            return;
        }
        System.out.println("Connecting to server " + IP + ":" + port);
    }

    private void connectToServer() {
        try {
            clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
            clientChannel.connect(remoteAddress);
        }catch (IOException e){
            System.err.println("Connecting to Server failed.");
        }
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

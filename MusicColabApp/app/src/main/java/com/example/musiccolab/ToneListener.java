package com.example.musiccolab;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ToneListener implements Runnable{
    public Thread thread = null;
    private Charset messageCharset = null;
    private BufferedOutputStream out;
    private BufferedReader in;

    public static boolean sendData = false;//set it ture when you want to sent data.
    public static String data;
    public static byte toneType;
    public static byte toneAction;

    private String IP;
    private int port;


    final private List<Short> codesList = new ArrayList<Short>();
    final private List<Short> errorCodesList = new ArrayList<Short>();
    final private short protocolName = 12845;
    private short action;

    public ToneListener(String IP, int port, BufferedOutputStream out, BufferedReader in){
        this.IP = IP;
        this.port = port;
        this.out = out;
        this.in = in;
        for(short index = 1; index < 11; index++) {
            codesList.add(index);
            errorCodesList.add( (short) (index + 10));
        }
    }

    @Override
    public void run() {
        action = 0;
        while (true) {
            getData();
            if (!sendData)
                waitForTone(action);
            else {
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
                } catch (IOException e) {
                    System.out.println("Error");
                }
                buffer.clear();
            }
        }
    }

    private void getData() {
        String response = "";
        String[] actionDataLength = analyseResponse(messageCharset);
        if (Integer.parseInt(actionDataLength[1]) >= 0) {
            response = actionDataLength[2];
            System.out.println(response);
            CommunicationHandling.toneList.add(response);
            CommunicationHandling.ToneDataEventChecker = true;
        }
    }

    public void start(){
        if (thread == null) {
            thread = new Thread(this, "ToneThread");
        }
        thread.start();
    }

    public void stop(){
        thread.stop();
    }


    private void waitForTone(short action) {
        short dataLength = 0;
        ByteBuffer buffer = null;

        buffer = ByteBuffer.allocate(6);
        buffer.put(convertShortToByte(protocolName));
        buffer.put(convertShortToByte(action));
        buffer.put(convertShortToByte(dataLength));

        buffer.flip();
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
        String response = "";
        String[] parsedResponse;
        String[] temp;

        try {
            response = in.readLine();
            parsedResponse = response.split(",");
            short action = 0;
            if (protocolName == Integer.parseInt(parsedResponse[0])) {
                action = (short) Integer.parseInt(parsedResponse[1]);
                if (codesList.contains(action) || errorCodesList.contains(action)) {
                    temp = new String[]{Integer.toString(action),
                            Integer.toString(Integer.parseInt(parsedResponse[2])), parsedResponse[3]};
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
            System.err.println("Can not read from channel.");
            return new String[]{Integer.toString(action), "-3"};
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

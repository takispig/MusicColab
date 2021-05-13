package src;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.*;
import java.util.Iterator;
import java.util.Set;

public class Client{

    private static Charset messageCharset = null;
    private static CharsetDecoder decoder = null;
    private static byte [] clientName = null;


    public short action = 8;
    public String email = "abc@gmail.com";
    public String userName = "abc";
    public String password = "@zfh165.)";
    public byte toneAction = 1;
    public byte toneType = 1;
    public String toneData = "dataExample";
    public String lobbyName = "example";
    public String lobbyID = "4564";

    /**
     * Explains how to use this program
     *
     */
    private static void printUsage() {

        System.err.println("Usage: java SMTPClient <address> <port>");
    }


    public byte[] convertShortToByte(short value){
        byte[] temp = new byte[2];
        temp[0] = (byte)(value & 0xff);
        temp[1] = (byte)((value >> 8) & 0xff);

        return temp;
    }

    public static void main(String [] args){
        SocketChannel clientChannel = null;
        InetSocketAddress remoteAddress = null;
        Selector selector = null;

        try {
            messageCharset = Charset.forName("US-ASCII");
        } catch(UnsupportedCharsetException uce) {
            System.err.println("Cannot create charset for this application. Exiting...");
            System.exit(1);
        }

        decoder = messageCharset.newDecoder();

        try {
            clientName = java.net.InetAddress.getLocalHost().getHostName().getBytes(messageCharset);
        } catch (UnknownHostException e) {
            System.err.println("Cannot determine name of host. Exiting...");
            System.exit(1);
        }


        if(args.length != 2) {
            printUsage();
            System.exit(1);
        }

        try {
            remoteAddress = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        } catch(IllegalArgumentException e) {
            printUsage();
            System.exit(1);
        } catch(SecurityException e) {
            printUsage();
            System.exit(1);
        }

        System.out.println("Connecting to server " + args[0] + ":" + args[1]);

        try {
            selector = Selector.open();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

        try {
            clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
            clientChannel.connect(remoteAddress);

        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        while(true)
        {
            try {
                if(selector.select() == 0)
                    continue;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while(iter.hasNext()) {

                SelectionKey key = iter.next();

                try {

                    if(key.isConnectable())
                    {
                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.finishConnect();
                    }

                    if(key.isReadable()) {

                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer mainBuffer = ByteBuffer.allocate(100);
                        clientChannel.read(mainBuffer);
                        mainBuffer.flip();
                        System.out.println(messageCharset.decode(mainBuffer).toString());

                        short protocolName = 12845;
                        short dataLength;
                        byte userNameLength;
                        byte passwordLength;


                        Client client = new Client();
                        String message;
                        ByteBuffer buffer = null;
                        if(client.action == 3){
                            byte emailLength = (byte) client.email.length();
                            userNameLength = (byte) client.userName.length();
                            passwordLength = (byte) client.password.length();
                            message = client.email+client.userName+client.password;
                            dataLength = (short) message.length();
                            buffer = ByteBuffer.allocate(6 + 3 + dataLength);

                            buffer.put(client.convertShortToByte(protocolName));
                            buffer.put(client.convertShortToByte(client.action));
                            buffer.put(client.convertShortToByte(dataLength));
                            buffer.put(emailLength);
                            buffer.put(userNameLength);
                            buffer.put(passwordLength);
                            buffer.put(message.getBytes(messageCharset));
                        }
                        else if(client.action == 2 || client.action == 1){
                            userNameLength = (byte) client.userName.length();
                            passwordLength = (byte) client.password.length();
                            message = client.userName+client.password;
                            dataLength = (short) message.length();
                            buffer = ByteBuffer.allocate(6 + 2 + dataLength);

                            buffer.put(client.convertShortToByte(protocolName));
                            buffer.put(client.convertShortToByte(client.action));
                            buffer.put(client.convertShortToByte(dataLength));
                            buffer.put(userNameLength);
                            buffer.put(passwordLength);
                            buffer.put(message.getBytes(messageCharset));
                        }
                        else if(client.action == 4 || client.action == 5 || client.action == 6 ||
                                client.action == 8 || client.action == 9 || client.action == 10){
                            if(client.action == 4)
                                message = client.lobbyName;
                            else
                                message = client.lobbyID;
                            dataLength = (short) message.length();
                            buffer = ByteBuffer.allocate(6 + dataLength);

                            buffer.put(client.convertShortToByte(protocolName));
                            buffer.put(client.convertShortToByte(client.action));
                            buffer.put(client.convertShortToByte(dataLength));
                            buffer.put(message.getBytes(messageCharset));
                        }
                        if(client.action == 7){
                            message = client.toneData;
                            dataLength = (short) (message.length() + 2);
                            buffer = ByteBuffer.allocate(6 + 2 + dataLength);

                            buffer.put(client.convertShortToByte(protocolName));
                            buffer.put(client.convertShortToByte(client.action));
                            buffer.put(client.convertShortToByte(dataLength));
                            buffer.put(client.toneAction);
                            buffer.put(client.toneType);
                            buffer.put(message.getBytes(messageCharset));
                        }
                        buffer.flip();
                        channel.write(buffer);
                        buffer.clear();

                    }

                } catch(IOException ioe) {
                    ioe.printStackTrace();
                    System.exit(1);
                }
            }
            iter.remove();
        }
    }
}
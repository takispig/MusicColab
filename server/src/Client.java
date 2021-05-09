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

    /**
     * Explains how to use this program
     *
     */
    private static void printUsage() {

        System.err.println("Usage: java SMTPClient <address> <port>");
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

        int count = 0;
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        String msg = "";
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
                        //SMTPClientState state = new SMTPClientState();
                        //generateMail(state);
                        //key.attach(state);
                    }

                    if(key.isReadable())
                    {
                        //SMTPClientState state = (SMTPClientState) key.attachment();
                        SocketChannel channel = (SocketChannel) key.channel();

                        //if(!readCommandLine(channel, state.getByteBuffer()))
                            //continue;

                        if(count == 0) {
                            channel.read(buffer);
                            buffer.flip();
                            msg = messageCharset.decode(buffer).toString();
                            System.out.println(msg);
                            buffer.clear();
                            String query = "Can I create an account in your System?";
                            buffer.put(query.getBytes(messageCharset));
                            buffer.flip();
                            channel.write(getLoginBuffer());
                            buffer.clear();
                        }

                        else {
                            channel.read(buffer);
                            buffer.flip();
                            msg = messageCharset.decode(buffer).toString();
                            System.out.println(msg);
                            buffer.clear();
                            channel.close();
                        }

                        count++;
                    }

                } catch(IOException ioe) {
                    ioe.printStackTrace();
                    System.exit(1);
                }
            }
            iter.remove();
        }
    }
    private static ByteBuffer getLoginBuffer(){
        ByteBuffer wbuffer =  ByteBuffer.allocate(100);

        wbuffer.put("12845".getBytes());
        wbuffer.put("1".getBytes());
        wbuffer.put(Integer.toString("nils".getBytes().length).getBytes());
        wbuffer.put(Integer.toString("1234".getBytes().length).getBytes());
        wbuffer.put("nils".getBytes());
        wbuffer.put("1234".getBytes());

        wbuffer.flip();

        return wbuffer;
    }
}
package src;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;

import static java.lang.System.exit;

public class Communication {
    private static Charset messageCharset = null;
    private static CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
    private static CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
    private ServerSocketChannel serverChannel = null;
    private InetSocketAddress serverAddress = null;
    private Selector selector = null;
    private ByteBuffer buffer = null;

    private static void printUsage() {

        System.err.println("Usage: MusicCoLabServer needs <address> <port>");
    }


    public void CheckParameters(int length){
        //we need address and port, so we have two parameters.
        if(length != 2){
            printUsage();
            exit(1);
        }
    }

    public void defineCharType(String Address, String Port){
        try {
            messageCharset = Charset.forName("US-ASCII");
        } catch(UnsupportedCharsetException uce) {
            System.err.println("Cannot create charset for this application. Exiting...");
            System.exit(1);
        }
        decoder = messageCharset.newDecoder();
        encoder = messageCharset.newEncoder();

        try {
            serverAddress = new InetSocketAddress(Address, Integer.parseInt(Port));
        } catch (IllegalArgumentException e) {
            printUsage();
            exit(1);
        } catch (SecurityException e) {
            printUsage();
            exit(1);
        }
    }

    public void OpenSelectorAndSetupSocket(){
        try {
            selector = Selector.open();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverChannel.socket().bind(serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }
    }

    //bufferHandleIsNeeded?

    private void handleConnectionWhenAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel sChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = sChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE); //AddState as third parameter.

        String message = "Hi, i'm your Server.";
        buffer.put(message.getBytes(messageCharset));
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    private void handleConnectionWhenReadable(SelectionKey key) throws IOException {
        //int state = (Integer) key.attachment(); //To save the state of all clients. Integer --> Class
        SocketChannel clientChannel = (SocketChannel) key.channel();
        clientChannel.read(buffer);
        buffer.flip();
        String msg = messageCharset.decode(buffer).toString();
        System.out.println(msg);
        buffer.clear();

        msg = "yes you can.";
        buffer.put(msg.getBytes(messageCharset));
        buffer.flip();
        clientChannel.write(buffer);
        buffer.clear();
        clientChannel.close();

    }

    public void handleConnection() throws IOException {
        //Create a buffer. We should deal with buffer size
        buffer = ByteBuffer.allocate(10000);

        System.out.println("Waiting for connection: ");

        while (true) {
            selector.select();

            Iterator selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();

                if (key.isAcceptable()) {
                    handleConnectionWhenAcceptable(key);

                } else if (key.isReadable()) {
                    handleConnectionWhenReadable(key);
                }
                selectedKeys.remove();
            }
        }
    }
}

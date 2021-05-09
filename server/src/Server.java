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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.lang.System.exit;

public class Server {
    private static Charset messageCharset = null;
    private static CharsetDecoder decoder = null;//Network order = Byte --> Characters = Host order
    private static CharsetEncoder encoder = null;//Characters = Host order -->  Network order = Byte
    private ServerSocketChannel serverChannel = null;
    private InetSocketAddress serverAddress = null;
    private Selector selector = null;
    //TODO NEW
    private static boolean runnning = true;
    private static boolean finished = false;

    private int playerId;
    private List<Integer> idList = new LinkedList<>();

    private static void printUsage() {

        System.err.println("Usage: MusicCoLabServer needs <address> <port>");
    }

    private int getPlayerId(){
        Random rand = new Random();
        int id = rand.nextInt(Integer.MAX_VALUE);
        while (idList.contains(id))
            id = rand.nextInt(Integer.MAX_VALUE);
        return id;
    }

    public void CheckParameters(int length){
        //we need address and port, so we have two parameters.
        if(length != 2){
            printUsage();
            exit(1);
        }
    }

    public void defineCharType(String address, int port){
        try {
            messageCharset = Charset.forName("US-ASCII");
        } catch(UnsupportedCharsetException uce) {
            System.err.println("Cannot create charset for this application. Exiting...");
            System.exit(1);
        }
        decoder = messageCharset.newDecoder();
        encoder = messageCharset.newEncoder();

        try {
            serverAddress = new InetSocketAddress(address, port);
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

        playerId = getPlayerId();

        String message = "Welcome in MusicCoLab Server.";
        ByteBuffer tempBuffer = ByteBuffer.allocate(message.length());
        tempBuffer.put(message.getBytes(messageCharset));
        tempBuffer.flip();
        channel.write(tempBuffer);
        tempBuffer.clear();
    }

    /**
     * According to the return value of function "analyseMainBuffer" send an error message or
     * handle the received action.
     */
    private void handleConnectionWhenReadable(SelectionKey key) throws IOException {
        //int state = (Integer) key.attachment(); //To save the state of all clients. Integer --> Class

        SocketChannel clientChannel = (SocketChannel) key.channel();
        //Read the first 6 indexes. (Protocol name, Action and data length. 2 Bytes each)

        Protocol protocol = new Protocol();
        int result = protocol.analyseMainBuffer(messageCharset, clientChannel);
        if(result == -1) {
            protocol.SendErrorToClient(messageCharset, clientChannel, "You are not our customer.");
            clientChannel.close();
        }
        else if(result == -2) {
            protocol.SendErrorToClient(messageCharset, clientChannel, "Action is not known.");
            clientChannel.close();
        }
        else
            protocol.handleAction(messageCharset, clientChannel, result, playerId);
    }

    //TODO CHANGED
    public void handleConnection() throws IOException {
        System.out.println("Waiting for connection: ");

        while (runnning) {
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
        finished = true;
    }

    //TODO NEW
    public void finishServer() {
        runnning = false;
        selector.wakeup();
        while (!finished) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            serverChannel.close();
            selector.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}

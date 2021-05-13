import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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
    private String email = "abc@gmail.com";
    private String userName = "abc";
    private String password = "@zfh165.)";
    private byte toneAction = 1;
    private byte toneType = 1;
    private String toneData = "dataExampleghahgfavf4564";
    private String lobbyName = "example";
    private String lobbyID = "0";

    public Client(){
        for(short index = 1; index < 11; index++) {
            codesList.add(index);
            errorCodesList.add( (short) (index + 10));
        }
    }

    private static void printUsage() {

        System.err.println("Usage: java SMTPClient <address> <port>");
    }


    public byte[] convertShortToByte(short value){
        byte[] temp = new byte[2];
        temp[0] = (byte)(value & 0xff);
        temp[1] = (byte)((value >> 8) & 0xff);

        return temp;
    }

    private static short getShort(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }

    private void sendQueryToServer(short action, SocketChannel channel) throws IOException {
        short dataLength;
        byte userNameLength;
        byte passwordLength;



        String message;
        ByteBuffer buffer = null;
        if(action == 3){
            byte emailLength = (byte) email.length();
            userNameLength = (byte) userName.length();
            passwordLength = (byte) password.length();
            message = email+userName+password;
            dataLength = (short) message.length();
            buffer = ByteBuffer.allocate(6 + 3 + dataLength);

            buffer.put(convertShortToByte(protocolName));
            buffer.put(convertShortToByte(action));
            buffer.put(convertShortToByte(dataLength));
            buffer.put(emailLength);
            buffer.put(userNameLength);
            buffer.put(passwordLength);
            buffer.put(message.getBytes(messageCharset));
        }
        else if(action == 2 || action == 1){
            userNameLength = (byte) userName.length();
            passwordLength = (byte) password.length();
            message = userName+password;
            dataLength = (short) message.length();
            buffer = ByteBuffer.allocate(6 + 2 + dataLength);

            buffer.put(convertShortToByte(protocolName));
            buffer.put(convertShortToByte(action));
            buffer.put(convertShortToByte(dataLength));
            buffer.put(userNameLength);
            buffer.put(passwordLength);
            buffer.put(message.getBytes(messageCharset));
        }
        else if(action == 4 || action == 5 || action == 6 ||
                action == 8 || action == 9 || action == 10){
            if(action == 4)
                message = lobbyName;
            else
                message = lobbyID;
            dataLength = (short) message.length();
            buffer = ByteBuffer.allocate(6 + dataLength);

            buffer.put(convertShortToByte(protocolName));
            buffer.put(convertShortToByte(action));
            buffer.put(convertShortToByte(dataLength));
            buffer.put(message.getBytes(messageCharset));
        }
        if(action == 7){
            message = toneData;
            dataLength = (short) (message.length() + 2);
            buffer = ByteBuffer.allocate(6 + 2 + dataLength);

            buffer.put(convertShortToByte(protocolName));
            buffer.put(convertShortToByte(action));
            buffer.put(convertShortToByte(dataLength));
            buffer.put(toneAction);
            buffer.put(toneType);
            buffer.put(message.getBytes(messageCharset));
        }
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    private short[] analyseResponse(Charset messageCharset, SocketChannel clientChannel) throws IOException {
        short[] temp;

        ByteBuffer mainBuffer = ByteBuffer.allocate(2);
        clientChannel.read(mainBuffer);
        mainBuffer.flip();

        short action = 0;
        if (protocolName == getShort(messageCharset.decode(mainBuffer).toString().getBytes(messageCharset))) {
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
                return new short[]{action, -2};
            }
        } else {
            return new short[]{action, -1};
        }
    }

    public static void main(String [] args){
        SocketChannel channel = null;
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
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
            channel.connect(remoteAddress);

        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        boolean flag = true;
        while(true)
        {
            try {
                if(selector.select() == 0)
                    continue;
            } catch (IOException e) {
                e.printStackTrace();
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while(iter.hasNext()) {

                SelectionKey key = iter.next();

                try {

                    if(key.isConnectable())
                    {
                        channel = (SocketChannel) key.channel();
                        channel.finishConnect();
                    }
                    else if(key.isReadable()) {

                        ByteBuffer helloBuffer = ByteBuffer.allocate(100);
                        channel = (SocketChannel) key.channel();
                        Client client = new Client();
                        short[] actionDataLength;
                        if(flag) {
                            channel.read(helloBuffer);
                            helloBuffer.flip();
                            System.out.println(messageCharset.decode(helloBuffer).toString());
                            helloBuffer.clear();
                            flag = false;
                        }

                        System.out.println("\nPlease enter \"0\" to be able to receive response from  server or \n" +
                                "enter an action to send it to Server: ");
                        Scanner getInput = new Scanner(System.in);
                        short input = getInput.nextShort();
                        if(input > 0) {
                            client.sendQueryToServer(input, channel);
                        }
                        else{
                            actionDataLength = client.analyseResponse(messageCharset, channel);
                            ByteBuffer responseBuffer = ByteBuffer.allocate(actionDataLength[1]);
                            if(actionDataLength[1] == -1)
                                System.out.println("Response from foreign server!");
                            else if(actionDataLength[1] == -2)
                                System.out.println("Server sent unknown action!");
                            else{
                                if(actionDataLength[0] == 1 || actionDataLength[0] == 2 || actionDataLength[0] == 3 ||
                                        actionDataLength[0] == 11 || actionDataLength[0] == 12 || actionDataLength[0] == 13){
                                    channel.read(responseBuffer);
                                    responseBuffer.flip();
                                    System.out.println("Action: " + actionDataLength[0] + "\n" +
                                            "Data length: " + actionDataLength[1]);
                                    if(actionDataLength[0] > 10)
                                        System.out.println("Error message: " + messageCharset.decode(responseBuffer).toString());
                                    else
                                        System.out.println("Response: " + messageCharset.decode(responseBuffer).toString());
                                    if(actionDataLength[0] == 3) {
                                        channel.close();
                                        channel = SocketChannel.open();
                                        channel.configureBlocking(false);
                                        channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
                                        channel.connect(remoteAddress);
                                        flag = true;
                                    }
                                    if(!flag) {
                                        System.out.println("\nPlease enter \"0\" to be able to receive response from  server or \n" +
                                                "enter an action to send it to Server: ");
                                        input = getInput.nextShort();
                                        if (input > 0) {
                                            client.sendQueryToServer(input, channel);
                                        }
                                    }
                                    else if(actionDataLength[0] == 2)
                                        System.exit(1);
                                }
                                else if(actionDataLength[0] == 4 || actionDataLength[0] == 5 || actionDataLength[0] == 6 ||
                                        actionDataLength[0] == 8 || actionDataLength[0] == 9 || actionDataLength[0] == 10){
                                    channel.read(responseBuffer);
                                    responseBuffer.flip();
                                    System.out.println("Action: " + actionDataLength[0] + "\n" +
                                            "Data length: " + actionDataLength[1]);
                                    if(actionDataLength[0] > 10)
                                        System.out.println("Error message: " + messageCharset.decode(responseBuffer).toString());
                                    else
                                        System.out.println("Response: " + messageCharset.decode(responseBuffer).toString());
                                    System.out.println("\nPlease enter \"0\" to be able to receive response from  server or \n" +
                                            "enter an action to send it to Server: ");
                                    input = getInput.nextShort();
                                    if(input > 0) {
                                        client.sendQueryToServer(input, channel);
                                    }
                                }
                                else{
                                    channel.read(responseBuffer);
                                    responseBuffer.flip();
                                    System.out.println("Action: " + actionDataLength[0] + "\n" +
                                            "Data length: " + actionDataLength[1]);
                                    if(actionDataLength[0] > 10)
                                        System.out.println("Error message: " + messageCharset.decode(responseBuffer).toString());
                                    else
                                        System.out.println("Response: " + messageCharset.decode(responseBuffer).toString());
                                    System.out.println("\nPlease enter \"0\" to be able to receive response from  server or \n" +
                                            "enter an action to send it to Server: ");
                                    input = getInput.nextShort();
                                    if(input > 0) {
                                        client.sendQueryToServer(input, channel);
                                    }
                                }
                            }
                        }
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
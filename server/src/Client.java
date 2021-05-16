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
                    }

                    if(key.isReadable())
                    {
                        SocketChannel channel = (SocketChannel) key.channel();

                        //register
                        //login
                        //logout
                        //creat lobby
                        //join lobby
                        //leave lobby
                        //tone
                        //game start
                        //game end
                        //game restart
                        String query = "Can I create an account in your System?";
                            buffer.put(query.getBytes(messageCharset));
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
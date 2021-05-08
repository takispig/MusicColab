package com.example.musiccolab;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client Class right now is operating just a basic communication with
 * the Server. This is just an abstract version with the basic functionalities.
 * Send:        "Hallo Server! Shall i come in?"
 * Receive:     <answer from Server>
 */

public class Client extends AppCompatActivity implements Runnable {

    //String localhost = "192.168.178.41";  // my pc's ip ... didn't work
    //String localhost = "127.0.0.1";       // general loopback ip ... didn't work
    //String localhost = "localhost";
    String localhost = "10.0.2.2";          // localhost for android devices (finally)
    int port = 3001;      // 3001, 8080, 1201, etc...

    private Context context;
    private Socket socket;

    Client (Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(localhost, port);
            System.out.println("Connected? Yes!\n");    // for debugging
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String userInput = "Hallo Server! Shall i come in?";
            out.println(userInput);
            String answer = in.readLine();
            System.out.println(answer);
            runOnUiThread(() -> Toast.makeText(context, "Server's response:\n" + answer, Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            System.out.println("Error occurred 'probably' in Socket\nCheck host, port, and if Server is running.\n");
            e.printStackTrace();
        }
    }

}




//
//public class Client extends AsyncTask<Void, Void, String> {
//
//    private static Charset messageCharset = null;
//    private static CharsetDecoder decoder = null;
//    private static byte [] clientName = null;
//
//    String localhost = "192.168.154.41";
//    int port = 3001;
//
//    private final Context context;
//
//    public Client (Context context) {
//        // get the context from the PreLobby class to display the Toast
//        this.context = context;
//    }
//
//    /**
//     * execute it when async communication is done
//     *
//     */
//    @Override
//    protected void onPostExecute(String msg) {
//        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//    }
//
//    /**
//     *
//     * @return server response
//     */
//    @Override
//    protected String doInBackground(Void... voids) {
//
//        Toast.makeText(context, "I'm in\n", Toast.LENGTH_SHORT).show();
//
//        SocketChannel clientChannel = null;
//        InetSocketAddress remoteAddress = null;
//        Selector selector = null;
//
//        try {
//            messageCharset = Charset.forName("US-ASCII");
//        } catch(UnsupportedCharsetException uce) {
//            System.err.println("Cannot create charset for this application. Exiting...");
//            System.exit(1);
//        }
//
//        decoder = messageCharset.newDecoder();
//
//        try {
//            clientName = java.net.InetAddress.getLocalHost().getHostName().getBytes(messageCharset);
//        } catch (UnknownHostException e) {
//            System.err.println("Cannot determine name of host. Exiting...");
//            System.exit(2);
//        }
//
//        try {
//            remoteAddress = new InetSocketAddress(localhost, port);
//        } catch(IllegalArgumentException e) {
//            System.exit(3);
//        } catch(SecurityException e) {
//            System.exit(4);
//        }
//
//        System.out.println("Connecting to server " + localhost + ":" + port);
//
//        try {
//            selector = Selector.open();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//            System.exit(5);
//        }
//
//        try {
//            clientChannel = SocketChannel.open();
//            clientChannel.configureBlocking(false);
//            clientChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
//            clientChannel.connect(remoteAddress);
//
//        } catch(IOException e) {
//            e.printStackTrace();
//            System.exit(6);
//        }
//
//        int count = 0;
//        ByteBuffer buffer = ByteBuffer.allocate(1000);
//        String msg = "Didn't work if you see this\n\n";
//        while(true)
//        {
//            try {
//                if(selector.select() == 0)
//                    continue;
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            Set<SelectionKey> selectedKeys = selector.selectedKeys();
//            Iterator<SelectionKey> iter = selectedKeys.iterator();
//
//            while(iter.hasNext()) {
//
//                SelectionKey key = iter.next();
//
//                try {
//                    System.out.println("1");
//                    if(key.isConnectable())
//                    {
//                        SocketChannel channel = (SocketChannel) key.channel();
//                        channel.finishConnect();
//                    }
//                    System.out.println("2");
//                    if(key.isReadable())
//                    {
//                        System.out.println("3");
//                        SocketChannel channel = (SocketChannel) key.channel();
//
//                        if (count == 0) {
//                            System.out.println("4");
//                            channel.read(buffer);
//                            buffer.flip();
//                            msg = messageCharset.decode(buffer).toString();
//                            System.out.println("Message says: " + msg);
//                            msg="123\n\n";
//                            buffer.clear();
//                            String query = "Can I create an account in your System?";
//                            buffer.put(query.getBytes(messageCharset));
//                            buffer.flip();
//                            channel.write(buffer);
//                            buffer.clear();
//                        }
//
//                        else {
//                            System.out.println("5");
//                            channel.read(buffer);
//                            buffer.flip();
//                            msg = messageCharset.decode(buffer).toString();
//                            msg="456\n\n";
//                            System.out.println("Message says: " + msg);
//                            buffer.clear();
//                            channel.close();
//                        }
//                        System.out.println("6");
//                        count++;
//                    }
//
//                } catch(IOException ioe) {
//                    ioe.printStackTrace();
//                    System.exit(7);
//                }
//            }
//            iter.remove();
//            return msg;
//        }
//    }
//
//}
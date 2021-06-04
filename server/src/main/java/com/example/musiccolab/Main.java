package main.java.com.example.musiccolab;

import main.java.com.example.musiccolab.exceptions.IPAddressException;
import main.java.com.example.musiccolab.exceptions.SocketBindException;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final String DEFAULT_ADDRESS = "127.0.0.1";
    private static final int DEFAULT_PORT = 8080;

    private static boolean exit = false;
    private static boolean finish = false;
    private static boolean close = false;
    private static boolean error = false;
    private static boolean invalidInput = false;
    private static int serverNumber = 1;
    private static String address = "";
    private static int port = 0;
    private static Server currentServer;
    private static Thread serverThread;
    private static Scanner input;

    private static long time = 0;
    private static int restart = 0;

    public static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public static void main(String[] args) {
        ActionLog.initLogger(logr);

        init(args);

        while (!exit && !error()) {

            input();

            serverThread = new Thread(Main::runServer, "server-" + serverNumber++);
            serverThread.start();

            loop();
        }

        exit();

    }

    private static void init(String[] args) {
        if (args.length == 0) {
            address = DEFAULT_ADDRESS;
            port = DEFAULT_PORT;
        } else if (args.length == 2) {
            address = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                invalidInput = true;
            }
        } else {
            invalidInput = true;
        }
        input = new Scanner(System.in);
    }

    private static void input() {
        if (!invalidInput)
            return;
        while (invalidInput) {
            System.out.println("An error occurred with the specified IP address and port!\nTry a new input.");
            System.out.println("This server will exit after 30 seconds without further interaction.");
            System.out.print("address: ");
            long Itime = System.currentTimeMillis();
            boolean BAddress = false;
            boolean b = true;
            while (!BAddress && (b = System.currentTimeMillis() - Itime < 30000)) {
                try {
                    if (System.in.available() > 0) {
                        address = input.nextLine();
                        BAddress = true;
                    } else {
                        Thread.sleep(20);
                    }
                } catch (InterruptedException | IOException ignored) {
                }
            }
            if (!b) {
                System.out.println();
                exit();
            }
            Itime = System.currentTimeMillis();
            System.out.print("port: ");
            boolean BPort = false;
            while (!BPort && (b = System.currentTimeMillis() - Itime < 30000)) {
                try {
                    if (System.in.available() > 0) {
                        try {
                            port = Integer.parseInt(input.nextLine());
                            invalidInput = false;
                        } catch (NumberFormatException ignored) {
                        }
                        BPort = true;
                    } else {
                        Thread.sleep(20);
                    }
                } catch (InterruptedException | IOException ignored) {
                }
            }
            if (!b) {
                System.out.println();
                exit();
            }
            invalidInput = false;
        }
    }

    private static void runServer() {
        //System.out.println("Creating new server...");
        logr.log(Level.INFO,"CREATING NEW SERVER");
        currentServer = new Server();

        try {
            currentServer.setupServerAddress(address, port);

            currentServer.defineCharType();

            //System.out.println("Setting up new server...");
            logr.log(Level.INFO,"SETTING UP SERVER: IP = " + address.toString() + " PORT = " + port);
            currentServer.OpenSelectorAndSetupSocket();

            currentServer.handleConnection();

            return;
        } catch (IPAddressException | SocketBindException e) {
            invalidInput = true;
        } catch (IOException | UnsupportedCharsetException e) {
            logr.log(Level.SEVERE, e.getMessage());
            System.err.println("ERROR: ----------------------------------------------------");
            e.printStackTrace();
            System.err.println("-----------------------------------------------------------");
        } catch (Exception e) {
            logr.log(Level.SEVERE, e.getMessage());
            System.err.println("UNEXPECTED ERROR: -----------------------------------------");
            e.printStackTrace();
            System.err.println("-----------------------------------------------------------");
        }
        currentServer.setFinishedTrue();
        close = true;
    }

    private static void loop() {
        while (!finish) {
            try {
                if (System.in.available() > 0) {
                    String nextLine = input.nextLine();
                    switch (nextLine) {
                        case "restart":
                            closeServer();
                            break;
                        case "exit":
                            closeServer();
                            exit = true;
                            break;
                        default:
                            System.out.println("Undefined input");
                            break;
                    }
                } else {
                    Thread.sleep(500);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            if (close) {
                close = false;
                closeServer();
            }
        }
        finish = false;
    }

    private static boolean error() {
        if (System.currentTimeMillis() - time < 10000)
            restart++;
        else restart = 0;
        time = System.currentTimeMillis();
        if (restart > 3) {
            error = true;
            //System.out.println("An error has occurred!");
            logr.log(Level.SEVERE,"ERROR");
        }
        return restart >= 4;
    }

    private static void closeServer() {
        //System.out.println("Closing Server...");
        logr.log(Level.INFO, "CLOSING SERVER");
        currentServer.finishServer();
        boolean closed = false;
        while (!closed) {
            try {
                serverThread.join(5000);
                serverThread.interrupt();
                Thread.sleep(1000);
                closed = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        finish = true;
    }

    private static void exit() {
        input.close();
        logr.log(Level.INFO,"EXIT");
        //System.out.println("Exiting...");
        if (!error)
            System.exit(0);
        else
            System.exit(1);
    }

}

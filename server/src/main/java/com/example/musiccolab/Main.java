package com.example.musiccolab;

import com.example.musiccolab.exceptions.IPAddressException;
import com.example.musiccolab.exceptions.SocketBindException;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    // DEFAULT VALUES //
    private static final String DEFAULT_ADDRESS = "130.149.80.94"; // vm: 130.149.80.94
    private static final int DEFAULT_PORT = 1200;
    // // // // // // //

    /**
     * exit program?
     */
    private static boolean exit = false;
    /**
     * has server finished?
     */
    private static boolean finished = false;
    /**
     * close server
     */
    private static boolean close = false;
    /**
     * error has occurred
     */
    private static boolean error = false;
    /**
     * invalid input
     */
    private static boolean invalidInput = false;

    private static int serverNumber = 1;
    private static String address = "";
    private static int port = 0;
    private static Server currentServer;
    private static Thread serverThread;
    private static Scanner input;
    private static long time = 0;
    private static int restart = 0;


    ////
    // for testing
    static int genPlayer = 1;
    static int genLobby = 1;

    ////

    public static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * main loop
     * @param args optional: ip address & port
     */
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

    /**
     * set up basic input & output
     * @param args optional: ip address & port
     */
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

    /**
     * read in ip address & port if necessary
     */
    private static void input() {
        if (!invalidInput)
            return;
        while (invalidInput) {
            System.out.println("An error occurred with the specified IP address and port!\nTry a new input.");
            System.out.println("This server will exit after 30 seconds without further interaction.");

            readIpAddress();

            readPort();

            invalidInput = false;
        }
    }

    /**
     * read ip address from console
     */
    private static void readIpAddress() {
        long currentTime = System.currentTimeMillis();
        System.out.print("address: ");
        boolean inputAddress = false;
        boolean b = true;
        while (!inputAddress && (b = System.currentTimeMillis() - currentTime < 30000)) {
            try {
                if (System.in.available() > 0) {
                    address = input.nextLine();
                    inputAddress = true;
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
    }

    /**
     * read port from console
     */
    private static void readPort() {
        long currentTime = System.currentTimeMillis();
        System.out.print("port: ");
        boolean inputPort = false;
        boolean b = true;
        while (!inputPort && (b = System.currentTimeMillis() - currentTime < 30000)) {
            try {
                if (System.in.available() > 0) {
                    try {
                        port = Integer.parseInt(input.nextLine());
                        invalidInput = false;
                    } catch (NumberFormatException ignored) {
                    }
                    inputPort = true;
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
    }

    /**
     * run server thread
     */
    private static void runServer() {
        //System.out.println("Creating new server...");
        logr.log(Level.INFO,"CREATING NEW SERVER");
        currentServer = new Server();

        try {
            currentServer.setupServerAddress(address, port);

            currentServer.defineCharType();

            //System.out.println("Setting up new server...");
            logr.log(Level.INFO,"SETTING UP SERVER: IP = " + address + " PORT = " + port);
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

    /**
     * loop to react to input on the console
     */
    private static void loop() {
        while (!finished) {

            listenConsole();

            if (close) {
                close = false;
                closeServer();
            }
        }
        finished = false;
    }

    /**
     * listen to input on the console
     */
    private static void listenConsole() {
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
                    case "lobbies":
                        printLobbies();
                        break;
                    case "updateLobbies":
                        currentServer.getProtocol().updateLobbyNameList();
                        break;
                    case "createLobby":
                        createLobby();
                        break;
                    case "players":
                        printPlayers();
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
    }

    // for testing
    private static void createLobby() {
        Lobby lobby = null;
        try {
            lobby = new Lobby(new Player("Player-" + genPlayer, "password", "e@mail"+ (genPlayer++)+".com", Server.createPlayerId(), null), "Lobby-"+(genLobby++), Server.createLobbyId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Server.lobbyMap.put(lobby.getLobby_id(), lobby);
        Server.lobbyList.add(lobby);
        currentServer.getProtocol().updateLobbyNameList();
    }

    private static void printLobbies() {
        System.out.print("Lobbies: ");
        for (Lobby l : Server.lobbyList) {
            System.out.print(l.getLobbyName() + ", ");
        }
        System.out.println();
    }

    private static void printPlayers() {
        System.out.println(currentServer.playersLoggedin);
        for (Player p : currentServer.playersLoggedin) {
            System.out.print(p.getName() + ", ");
        }
        System.out.println();
        var entrySet = currentServer.loggedInPlayers.entrySet();
        for (var k : entrySet) {
            System.out.print(k.getValue().getName() + ", ");
        }
        System.out.println();
    }

    /**
     * trys to figure out if a serious error has occurred
     * @return result of that question
     */
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

    /**
     * finish current server thread & close all connections
     */
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
        finished = true;
    }

    /**
     * exit program
     */
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

package src;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 8080;

    private static boolean exit = false;
    private static boolean finish = false;
    private static boolean error = false;
    private static boolean invalidInput = false;
    private static boolean bindError = false;
    private static int serverNumber = 1;
    private static String address = "";
    private static String portString = "";
    private static int port = 0;
    private static Server currentServer;
    private static Thread serverThread;
    private static Scanner input;

    private static long time = 0;
    private static int restart = 0;

    public static void main(String args[]) {

        init(args);

        while (!exit && !error()) {

            input();

            serverThread = new Thread(() -> runServer(), "server-"+serverNumber++);
            serverThread.start();

            loop();
        }

        exit();

    }

    private static void init(String args[]) {
        if (args.length == 0) {
            address = DEFAULT_ADDRESS;
            port = DEFAULT_PORT;
        } else if (args.length == 2) {
            address = args[0];
            portString = args[1];
        } else {
            invalidInput = true;
        }
        input = new Scanner(System.in);
    }

    private static void input() {
        if (port != 0)
            return;
        if (!invalidInput) {
            try {
                port = Integer.parseInt(portString);
                return;
            } catch (NumberFormatException e) {
            }
        }
        System.err.println("Invalid input!");
        System.out.println("This server will exit after 30 seconds without further interaction. -- NOT IMPLEMENTED YET");
        System.out.print("address: ");
        address = input.nextLine();
        System.out.print("port: ");



    }

    private static void runServer() {
        System.out.println("Creating new server...");
        currentServer = new Server();

        try {

            currentServer.defineCharType(address, port);

            currentServer.OpenSelectorAndSetupSocket();
            System.out.println("Setting up new server...");

            currentServer.handleConnection();

        } catch (UnsupportedCharsetException | SQLException | ClassNotFoundException e) {
            //TODO DO NOTHING -- test with wrong charset...
            e.printStackTrace();
        } catch (IllegalArgumentException | SecurityException e) {
            port = 0;
            invalidInput = true;
            e.printStackTrace();
            //TODO INVALID INPUT --
        } catch (IOException e) {
            //TODO DO NOTHING...
            e.printStackTrace();
        }
        finish = true;
    }



    private static void loop() {
        // TODO BAD...
        while (!finish && (serverThread != null && serverThread.isAlive())) {
            try {
                if (System.in.available() > 0) {
                    String nextLine = input.nextLine();
                    switch (nextLine) {
                        case "restart":
                            closeServer();
                            break;
                        case "exit":
                            exit = true;
                            closeServer();
                            break;
                        default:
                            System.out.println("Undefined input");
                            break;
                    }
                } else {
                    Thread.sleep(500);
                    //System.out.println(serverThread.isAlive());
                }

            } catch (IOException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        finish = false;
        return;
    }

    private static boolean error(){
        if (System.currentTimeMillis() - time < 10000)
            restart++;
        else
            restart = 0;
        time = System.currentTimeMillis();
        if (restart > 2)
            System.err.println("An error has occurred!");
        return restart < 3 ? false : true;
    }

    private static void closeServer() {
        finish = true;
        System.out.println("Closing Server...");
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
    }

    private static void exit() {
        input.close();
        System.out.println("\n\nExiting...");

        if (!error)
            System.exit(0);
        else
            System.exit(1);
    }
}

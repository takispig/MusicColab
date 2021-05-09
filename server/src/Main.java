import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 8080;

    private static boolean exit = false;
    private static boolean finish = false;
    private static int serverNumber = 1;
    private static String address;
    private static int port;
    private static Server currentServer;
    private static Thread serverThread;

    private static long time = 0;
    private static int restart = 0;

    public static void main(String args[]) {

        init(args);

        Scanner input = new Scanner(System.in);

        while (!exit && startServer()) {

            finish = false;

            serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runServer();
                }
            }, "server-"+serverNumber++);
            serverThread.start();

            while (!finish) {
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
                    }

                } catch (IOException | InterruptedException exception) {
                    exception.printStackTrace();
                }

            }

        }
        input.close();
        if (restart > 2)
            System.out.println("An error has occurred!");
        System.out.println("Exit...");
        System.exit(0);
    }

    private static void init(String args[]) {
        if (args.length == 2) {
            address = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.exit(1);
            }

        } else {
            address = DEFAULT_ADDRESS;
            port = DEFAULT_PORT;
        }
    }

    private static void runServer() {
        try {
            currentServer = new Server();
            System.out.println("Creating new server...");

            currentServer.defineCharType(address, port);

            currentServer.OpenSelectorAndSetupSocket();
            System.out.println("Setting up new server...");

            currentServer.handleConnection();

        } catch (IOException exception) {
            exception.printStackTrace();
            finish = true;
        }
    }

    private static boolean startServer(){
        if (System.currentTimeMillis() - time < 10000)
            restart++;
        else
            restart = 0;
        time = System.currentTimeMillis();
        return restart < 3 ? true : false;
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

}

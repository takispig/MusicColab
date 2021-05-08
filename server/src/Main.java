import java.io.IOException;

public class Main {

    private static boolean finished = false;

    public static void main(String args[]) {

        while (!finished) {
            try {

                Server server = new Server();
                System.out.println("Creating new Server...");

                server.CheckParameters(args.length);

                server.defineCharType(args[0], args[1]);

                server.OpenSelectorAndSetupSocket();
                System.out.println("set");

                server.handleConnection();

            } catch (IOException exception) {

                exception.printStackTrace();
            }

        }

        System.exit(0);
    }
}

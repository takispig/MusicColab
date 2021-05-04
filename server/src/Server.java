package src;

import java.io.IOException;

public class Server {

    public static void main(String args[]) throws IOException {
        Communication communication = new Communication();

        communication.CheckParameters(args.length);

        communication.defineCharType(args[0], args[1]);

        communication.OpenSelectorAndSetupSocket();

        communication.handleConnection();
    }
}

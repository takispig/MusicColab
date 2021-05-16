package main.java.com.example.musiccolab;

import java.io.IOException;
import java.sql.SQLException;

public class Server {

    public static void main(String args[]) throws IOException, SQLException, ClassNotFoundException {

        Communication communication = new Communication();

        communication.CheckParameters(args.length);

        communication.defineCharType(args[0], args[1]);

        communication.OpenSelectorAndSetupSocket();

        communication.handleConnection();
    }
}

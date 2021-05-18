package main.java.com.example.musiccolab;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.*;
import java.util.Iterator;
import java.util.Map;

public class LoginSystem {
    //HashMap to save the players which are logged in



    /**
     * check if the player(name,email) already registered, if so add new player to the loggedInPlayers
     * @param name
     * @param passwort
     * @param channel
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static boolean login(String name, String passwort, SocketChannel channel) throws SQLException, ClassNotFoundException, IOException {
        //check for registration
        if(checkLogin(name,passwort)){
            //create new player
            ResultSet res = DataBase.getUserlogin(name,passwort);
            Player player = new Player(name,passwort,res.getString(3),res.getInt(1),channel);
            player.setLoggedIn();
            //add data to List
            Server.loggedInPlayers.put(res.getInt(1),player);
            return true;
        } else throw new RuntimeException("User not registered!");

    }

    /**
     * check if the player is registered and logged in, if so delete player from loggedInPlayers and the game
     * @param name
     * @param passwort
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static boolean logout(String name,String passwort) throws SQLException, ClassNotFoundException {
        //check in list
        if(checkLogin(name, passwort) & Server.loggedInPlayers.get(getId(name, passwort)) != null){
            //del player from list
            Player player = Server.loggedInPlayers.get(getId(name,passwort));
            player = null;
            Server.loggedInPlayers.remove(getId(name, passwort));
            return true;
            //TODO: OWN Exeption so the Server dont crash

        } else throw new RuntimeException("User not logged in!");
    }

    /**
     * check if the player is registered, if not add player to database
     * @param name
     * @param email
     * @param passwort
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static boolean register(String name, String email, String passwort) throws SQLException, ClassNotFoundException {
        //check for registration
        if(!checkForRegistration(name, email)){
            //add data to DB
            DataBase.addUser(name, email, passwort);
            return true;
        } else {
            //TODO: OWN Exeption so the Server dont crash
            throw new RuntimeException("User already registered!");
        }
    }

    /**
     * check if the user-data is already in the database
     * @param name
     * @param email
     * @return true if user-data is in database; false if not
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private static boolean checkForRegistration(String name, String email) throws SQLException, ClassNotFoundException {
        ResultSet res = DataBase.getUser(name, email);
        return res.next();
    }

    private static boolean checkLogin(String name, String passwort) throws SQLException, ClassNotFoundException {
        ResultSet res = DataBase.getUserlogin(name, passwort);
        if (res.next()){
            return true;
        } else return false;
    }


    /**
     * get the id from the user-data from the database
     * @param name
     * @param passwort
     * @return id from the user-data
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private static int getId(String name, String passwort) throws SQLException, ClassNotFoundException {
        ResultSet res = DataBase.getUserlogin(name, passwort);
        if(!res.next()){
            //TODO: OWN Exeption so the main.java.com.example.musiccolab.Server dont crash
            throw new RuntimeException("User not found!");
        }
        return res.getInt(1);
    }

    public static Player getPlayerByChannel(SocketChannel channel){
        Iterator<Map.Entry<Integer,Player>> i = Server.loggedInPlayers.entrySet().iterator();
        while(i.hasNext()){
            Map.Entry<Integer,Player> entry = i.next();
            if(entry.getValue().getPlayerChannel() == channel){
                return entry.getValue();
            }
        }
        //TODO: New Exeption (Own Exeption)
        return null;
    }
}

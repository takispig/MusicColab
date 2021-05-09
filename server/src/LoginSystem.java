package src;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.sql.*;
import java.util.HashMap;

public class LoginSystem {
    //HashMap to save the players which are logged in
    static HashMap<Integer,Player> loggedInPlayers = new HashMap<>();


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
            //add data to List
            loggedInPlayers.put(res.getInt(1),player);
            return true;
        } else throw new RuntimeException("User not registered!");

    }

    /**
     * check if the player is registered and logged in, if so delete player from loggedInPlayers and the game
     * @param name
     * @param email
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static boolean logout(String name,String email) throws SQLException, ClassNotFoundException {
        //check in list
        if(checkForRegistration(name, email) & loggedInPlayers.get(getId(name, email)) != null){
            //del player from list
            loggedInPlayers.remove(getId(name, email));
            //TODO: Delete player
            return true;
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
            throw new RuntimeException("User not registered!");
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
        if (res.next()){
            return true;
        } else return false;
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
     * @param email
     * @return id from the user-data
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private static int getId(String name, String email) throws SQLException, ClassNotFoundException {
        ResultSet res = DataBase.getUser(name, email);
        if(!res.next()){
            throw new RuntimeException("User not found!");
        }
        return res.getInt(1);
    }

    public static Player getPlayerByChannel(SocketChannel channel){
        for(int i = 0;i<loggedInPlayers.size();i++){
            if(loggedInPlayers.get(i).getPlayerChannel()==channel){
                return loggedInPlayers.get(i);
            }
        }
        return null;
    }
}

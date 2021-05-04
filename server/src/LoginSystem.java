package src;

import java.sql.*;
import java.util.HashMap;

public class LoginSystem {
    //HashMap to save the players which are logged in
    static HashMap<Integer,Player> loggedInPlayers = new HashMap<>();


    /**
     * check if the player(name,email) already registered, if so add new player to the loggedInPlayers
     * @param name
     * @param email
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void login(String name, String email) throws SQLException, ClassNotFoundException {
        //check for registration
        if(checkForRegistration(name,email)){
            //TODO: player initialising
            //create new player
            int playerId = getId(name,email);
            Player player = new Player();
            //add data to List
            loggedInPlayers.put(playerId,player);
        } else throw new RuntimeException("User not registered!");

    }

    /**
     * check if the player is registered and logged in, if so delete player from loggedInPlayers and the game
     * @param name
     * @param email
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void logout(String name,String email) throws SQLException, ClassNotFoundException {
        //check in list
        if(checkForRegistration(name, email) & loggedInPlayers.get(getId(name, email)) != null){
            //del player from list
            loggedInPlayers.remove(getId(name, email));
            //TODO: Delete player
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
    public static void register(String name, String email, String passwort) throws SQLException, ClassNotFoundException {
        //check for registration
        if(!checkForRegistration(name, email)){
            //add data to DB
            DataBase.addUser(name, email, passwort);
        } else throw new RuntimeException("User not registered!");
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
}

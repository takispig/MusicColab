package main.java.com.example.musiccolab;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

/**
 * Class to manage login/logout and register.
 */
public final class LoginSystem {
    /**
     * .
     */
    private static final int COL_INT_ID = 1; // id
    /**
     * .
     */
    private static final int COL_INT_EMAIL = 3; // email
    private LoginSystem() {
    }


    /**
     * check if the player(name,email) already registered,
     * if so add new player to the loggedInPlayers.
     * @param name from user
     * @param passwort from user
     * @param channel from user
     * @return true if all goes fine, false if not
     * @throws SQLException if something goes wrong with SQL
     * @throws ClassNotFoundException if something goes wrong with driver
     */
    public static boolean login(final String name, final String passwort,
                                final SocketChannel channel)
            throws SQLException, ClassNotFoundException, IOException {
        //check for registration
        if (checkLogin(name, passwort)) {
            //create new player
            ResultSet res = DataBase.getUserlogin(name, passwort);
            Player player = new Player(name, passwort,
                    res.getString(COL_INT_EMAIL),
                    res.getInt(COL_INT_ID), channel);
            player.setLoggedIn();
            //add data to List
            Server.loggedInPlayers.put(res.getInt(COL_INT_ID), player);
            return true;
        } else {
            return false;
        }

    }

    /**
     * check if the player is registered and logged in,
     * if so delete player from loggedInPlayers and the game.
     * @param name from user
     * @param passwort from user
     * @return true if all goes fine, false if not
     * @throws SQLException if something goes wrong with SQL
     * @throws ClassNotFoundException if something goes wrong with driver
     */
    public static boolean logout(final String name, final String passwort)
            throws SQLException, ClassNotFoundException {
        //check in list
        if (checkLogin(name, passwort)
                & Server.loggedInPlayers.get(getId(name, passwort)) != null) {
            //del player from list
            Player player = Server.loggedInPlayers.get(getId(name, passwort));
            player = null;
            Server.loggedInPlayers.remove(getId(name, passwort));
            return true;
        } else {
            return false;
        }
    }

    /**
     * check if the player is registered, if not add player to database.
     * @param name from user
     * @param passwort from user
     * @param email from user
     * @return true if all goes fine, false if not
     * @throws SQLException if something goes wrong with SQL
     * @throws ClassNotFoundException if something goes wrong with driver
     * @throws ClassNotFoundException
     */
    public static boolean register(final String name,
                                   final String email, final String passwort)
            throws SQLException, ClassNotFoundException {
        //check for registration
        if (!checkForRegistration(name, email)) {
            //add data to DB
            DataBase.addUser(name, email, passwort);
            return true;
        } else {
            return false;
        }
    }

    /**
     * check if the user-data is already in the database.
     * @param name from user
     * @param email from user
     * @return true if all goes fine, false if not
     * @throws SQLException if something goes wrong with SQL
     * @throws ClassNotFoundException if something goes wrong with driver
     */
    private static boolean checkForRegistration(final String name,
                                                final String email)
            throws SQLException, ClassNotFoundException {
        ResultSet res = DataBase.getUser(name, email);
        return res.next();
    }

    /**
     * check if the user-data is already loged in.
     * @param name from user
     * @param passwort from user
     * @return true if all goes fine, false if not
     * @throws SQLException if something goes wrong with SQL
     * @throws ClassNotFoundException if something goes wrong with driver
     */
    private static boolean checkLogin(final String name, final String passwort)
            throws SQLException, ClassNotFoundException {
        ResultSet res = DataBase.getUserlogin(name, passwort);
        return res.next();
    }


    /**
     * get the id from the user-data from the database.
     * @param name
     * @param passwort
     * @return id from the user-data
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private static int getId(final String name, final String passwort)
            throws SQLException, ClassNotFoundException {
        ResultSet res = DataBase.getUserlogin(name, passwort);
        return res.getInt(res.next() ? res.getInt(COL_INT_ID) : -1);
    }

    /**
     * method to get the player from "on" the channel.
     * @param channel from searched player
     * @return Player which is on the channel
     */
    public static Player getPlayerByChannel(final SocketChannel channel) {
        Iterator<Map.Entry<Integer, Player>> i
                = Server.loggedInPlayers.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<Integer, Player> entry = i.next();
            if (entry.getValue().getPlayerChannel() == channel) {
                return entry.getValue();
            }
        }
        return null;
    }
}

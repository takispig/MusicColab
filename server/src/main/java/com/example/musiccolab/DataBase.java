package com.example.musiccolab;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * To get this run, you need to install the org.sqlite.JDBC Driver.
 * You can find this here:
 * https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc/3.34.0
 *
 * Download the file then go to Modules/Dependencies (Hotkey:Ctrl+Alt+Shift+S)
 * -> add the File
 *
 *
 * The path to the sqlite-Database have to be set -> PATH_TO_DB
 */



public final class DataBase {
    //Constants to specify various values
    /**
     * .
     */
    private static final String PATH_TO_DB =
            "jdbc:sqlite:src/main/res/database/identifier.sqlite";
    /**
     * .
     */
    private static final String TABLE_NAME = "User";
    /**
     * .
     */
    private static final String NAME_COL_NAME = "name";
    /**
     * .
     */
    private static final String EMAIL_COL_NAME = "email";
    /**
     * .
     */
    private static final String PASSWORD_COL_NAME = "passwort";
    /**
     * .
     */
    private static final int COL_INT_ID = 1; // id
    /**
     * .
     */
    private static final int COL_INT_EMAIL = 3; // email
    /**
     * .
     */
    private static final int COL_INT_NAME = 2; // id
    /**
     * .
     */
    private static final int COL_INT_PW = 4; // email
    /**
     * .
     */
    private static Connection con;
    /**
     * .
     */
    private static boolean hasData = true;

    private DataBase() {
    }

    /**
     * return all users in the database.
     * @return ResultSet with all users
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static ResultSet displayUsers()
            throws ClassNotFoundException, SQLException {
        if (con == null) {
            getConnection();
        }
        Statement state = null;
        state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM " + TABLE_NAME);
        return res;
    }

    /**
     * Connect to the Database.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static void getConnection()
            throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection(PATH_TO_DB);
        initialise();

    }

    /**
     * Init the database if the database is empty with a default table.
     * @throws SQLException
     */
    private static void initialise() throws SQLException {
        if (!hasData) {
            hasData = true;
            Statement state = con.createStatement();
            ResultSet res = state.executeQuery("SELECT name FROM"
                    + " sqlite_master WHERE type='table' AND name="
                    + TABLE_NAME);
            if (!res.next()) {
                System.out.println("Building User table with standard values");
                Statement state2 = con.createStatement();
                //create new Table
                state.execute("CREATE TABLE " + TABLE_NAME + "(id integer, "
                        + NAME_COL_NAME + " varchar(60)," + EMAIL_COL_NAME
                        + " varchar (60)," + PASSWORD_COL_NAME
                        + " varchar (60)," + "primary key(id));");
                PreparedStatement prep =
                        con.prepareStatement("INSERT INTO User (name,email,passwort) "
                                + "values(?, ?, ?); ");
                prep.setString(1, "Max Mustermann");
                prep.setString(2, "maxmustermann@mail.de");
                prep.setString(3, "1234");
                prep.execute();
            }
        }
    }

    /**
     * Add a user to the database.
     * @param name
     * @param email
     * @param passwort
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void addUser(final String name, final String email,
                               final String passwort, final String securityQuestion)
            throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement prep = con.prepareStatement("INSERT INTO "
                + TABLE_NAME + " (name,email,passwort,securityquestion) values(?, ?, ?, ?);");

        prep.setString(1, name);
        prep.setString(2, email);
        prep.setString(3, passwort);
        prep.setString(4, securityQuestion);

        prep.execute();


    }

    /**
     * Get a user by giving name and email.
     * @param name from user
     * @param email from user
     * @return ResultSet with user-name and user-email
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static ResultSet getUser(final String name, final String email)
            throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM "
                + TABLE_NAME + " WHERE " + NAME_COL_NAME + " = '" + name
                + "' AND " + EMAIL_COL_NAME + " ='" + email + "'");
        return res;
    }

    /**
     * @param name from User
     * @param password from User
     * @return Select User-Infos from db
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static ResultSet getUserlogin(final String name,
                                         final String password)
            throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM " + TABLE_NAME
                + " WHERE " + NAME_COL_NAME + " = '" + name
                + "' AND " + PASSWORD_COL_NAME + " ='" + password + "'");
        return res;
    }

    /**
     * Delete user from Database.
     * @param name from User
     * @param email from user
     * @param password from User
     * @throws SQLException
     * @throws ClassNotFoundException
    */
    public static void delUser(final String name, final String email,
                               final String password)
            throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        PreparedStatement prep = con.prepareStatement("DELETE FROM "
                + TABLE_NAME + " WHERE " + NAME_COL_NAME
                + " = ? AND " + EMAIL_COL_NAME + " = ? AND "
                + PASSWORD_COL_NAME + " = ?");
        prep.setString(1, name);
        prep.setString(2, email);
        prep.setString(3, password);
        prep.execute();
    }

    public static void resetPasswort(String username, String email, String password) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        System.out.println("ResetPasword");
        PreparedStatement prep = con.prepareStatement("UPDATE "
                + TABLE_NAME + " SET " + PASSWORD_COL_NAME + " = ? WHERE " + NAME_COL_NAME
                + " = ? AND " + EMAIL_COL_NAME + " = ?");
        prep.setString(1, password);
        prep.setString(2, username);
        prep.setString(3, email);
        System.out.println(prep.toString());
        prep.execute();

    }
    public static boolean checksecurityQuestion(final String username, final String email, final String securityQuestion) throws SQLException, ClassNotFoundException {
        if(con == null){
            getConnection();
        }

        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM "+ TABLE_NAME + " WHERE name = '"+ username + "' AND email = '"+ email +
                "' AND securityquestion = '"+ securityQuestion +"'");


        return res.next();
    }

    public static boolean checkUsername(final String username) throws SQLException, ClassNotFoundException {
        if(con == null){
            getConnection();
        }

        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM "+ TABLE_NAME + " WHERE name = '"+ username +"'" );

        return res.next();
    }
    /*
    public static int getID(final String name, final String password)
            throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT id FROM " + TABLE_NAME
                + " WHERE " + NAME_COL_NAME + " = '" + name
                + "' AND "  + PASSWORD_COL_NAME + " = '" + password + "'");
        return res.getInt(1);
    }
     */ //Not used yet
}

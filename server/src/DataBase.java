import java.sql.*;

/**
 * To get this run, you need to install the org.sqlite.JDBC Driver. You can find this here:
 * https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc/3.34.0
 *
 * Download the file then go to Modules/Dependencies (Hotkey:Ctrl+Alt+Shift+S)  -> add the File
 *
 *
 * The path to the sqlite-Database have to be set -> PATH_TO_DB
 */



public class DataBase {
    //Constants to specify various values
    private final static String PATH_TO_DB = "jdbc:sqlite:database/identifier.sqlite";
    private final static String TABLE_NAME = "User";
    private final static String NAME_COL_NAME = "name";
    private final static String EMAIL_COL_NAME = "email";
    private final static String PASSWORT_COL_NAME = "passwort";
    private static Connection con;
    private static boolean hasData = true;



    /**
     * return all users in the database
     * @return ResultSet with all users
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static ResultSet displayUsers() throws ClassNotFoundException, SQLException{
        if(con == null){
            getConnection();
        }

        Statement state = null;
        state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM"+ TABLE_NAME);

        return res;
    }

    /**
     * Connect to the Database
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static void getConnection() throws ClassNotFoundException,SQLException{
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection(PATH_TO_DB);
        initialise();

    }

    /**
     * Init the database if the database is empty with a default table
     * @throws SQLException
     */
    private static void initialise() throws SQLException {
        if(!hasData){
            hasData = true;

            Statement state = con.createStatement();
            ResultSet res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name="+ TABLE_NAME);
            if(!res.next()){
                System.out.println("Building th User table with standard values");
                Statement state2 = con.createStatement();
                //create new Table
                state.execute("CREATE TABLE "+TABLE_NAME+"(id integer,"
                        + NAME_COL_NAME +" varchar(60),"+ EMAIL_COL_NAME +" varchar (60),"+ PASSWORT_COL_NAME +" varchar (60),"+"primary key(id));");
                PreparedStatement prep = con.prepareStatement("INSERT INTO User values(?,?,?,?);");
                prep.setString(2,"Max Mustermann");
                prep.setString(3,"maxmustermann@mail.de");
                prep.setString(4,"1234");
                prep.execute();
            }
        }
    }

    /**
     * Add a user to the database
     * @param name
     * @param email
     * @param passwort
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void addUser(String name, String email, String passwort) throws SQLException, ClassNotFoundException {
        if(con == null){
            getConnection();
        }

        PreparedStatement prep = con.prepareStatement("INSERT INTO "+ TABLE_NAME + " values(?,?,?,?);");
        prep.setInt(1, Communication.createPlayerId());
        prep.setString(2,name);
        prep.setString(3,email);
        prep.setString(4,passwort);
        prep.execute();


    }

    /**
     * Get a user by giving name and email
     * @param name
     * @param email
     * @return ResultSet with user-name and user-email
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static ResultSet getUser(String name,String email) throws SQLException, ClassNotFoundException {
        if(con == null){
            getConnection();
        }

        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM "+ TABLE_NAME +" WHERE "+ NAME_COL_NAME +" = '"+ name +
                "' AND "+EMAIL_COL_NAME+" ='" + email +"'");

        return res;
    }

    public static ResultSet getUserlogin(String name, String passwort) throws SQLException, ClassNotFoundException {
        if(con == null){
            getConnection();
        }
        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+ NAME_COL_NAME +" = '"+ name +
                "' AND "+PASSWORT_COL_NAME+" ='"+ passwort+"'");
        return res;
    }
    /**
     * Delete user from Database
     * @param name
     * @param email
     * @param passwort
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void delUser(String name, String email, String passwort) throws SQLException, ClassNotFoundException {
        if(con == null){
            getConnection();
        }

        PreparedStatement prep = con.prepareStatement("DELETE FROM "+ TABLE_NAME +" WHERE "+ NAME_COL_NAME +
                " = ? AND "+ EMAIL_COL_NAME+" = ? AND "+ PASSWORT_COL_NAME+ " = ?");
        prep.setString(1,name);
        prep.setString(2,email);
        prep.setString(3,passwort);
        prep.execute();
    }


    public static int getID(String name, String passwort) throws SQLException, ClassNotFoundException {
        if(con == null){
            getConnection();
        }

        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT id FROM "+ TABLE_NAME +" WHERE "+ NAME_COL_NAME + " = '" + name +
                "' AND "+ PASSWORT_COL_NAME + " = '"+ passwort + "'");
        return res.getInt(1);
    }
}

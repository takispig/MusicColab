package test;

import main.java.com.example.musiccolab.DataBase;
import main.java.com.example.musiccolab.LoginSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class LoginSystemTest {
    String testName = "Karl";
    String testEmail = "karl@mail.de";
    String testPassword = "1234";
<<<<<<< Updated upstream:server/src/test/LoginSystemTest.java
    String securityQuestion = ""; //todo
=======
    String securityQuestion = "Sicherheit";
>>>>>>> Stashed changes:Server/src/test/java/com/example/musiccolab/LoginSystemTest.java

    @AfterEach
    void cleanUp(){
        System.out.println("Cleaning up");
        try {
            DataBase.delUser(testName,testEmail,testPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    @Test
    void loginWithoutRegister() throws IOException {
        SocketChannel testChannel = SocketChannel.open();

        try {
            assertFalse(LoginSystem.login(testName,testPassword,testChannel)!=null);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        testChannel.close();
    }

    @Test
    void loginWithRegister() throws IOException {
        SocketChannel testChannel = SocketChannel.open();
        try {
            assertTrue(LoginSystem.register(testName,testEmail,testPassword,securityQuestion));
            assertTrue(LoginSystem.login(testName,testPassword,testChannel) != null);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        testChannel.close();
    }

}
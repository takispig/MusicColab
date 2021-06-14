package test.java.com.example.musiccolab;

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
    void loginWithoutRegister() {
        SocketChannel testChannel = null;

        try {
            assertFalse(LoginSystem.login(testName,testPassword,testChannel));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void loginWithRegister() {


        try {
            assertTrue(LoginSystem.register(testName,testEmail,testPassword));
            assertTrue(LoginSystem.loginWithoutChannel(testName,testPassword));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
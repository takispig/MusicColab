package com.example.musiccolab;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DataBaseTest {
    String testName = "Manuel";
    String testEmail = "Manuel@mail.de";
    String testPassword = "1234";
    String sec = "Sicherheit";

    @Test
    void addUser() {


        try {
            DataBase.addUser(testName,testEmail,testPassword,sec);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            assertTrue(DataBase.getUser(testName,testEmail).next());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            assertTrue(DataBase.getUserlogin(testName,testPassword).next());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    void delUser(){
        try {
            DataBase.delUser(testName,testEmail,testPassword);
            assertFalse(DataBase.getUser(testName,testEmail).next());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
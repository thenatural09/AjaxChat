package com.company;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Troy on 10/17/16.
 */
public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn,"Troy");
        User user = Main.selectUser(conn,"Troy");
        conn.close();
        assertTrue(user != null);
        assertTrue(user.name.equals("Troy"));

    }

    @Test
    public void testMessage() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn,"Troy");
        User user = Main.selectUser(conn,"Troy");
        Main.insertMessage(conn,"Hello world!",user.id);
        ArrayList<Message> messages = Main.selectMessages(conn);
        conn.close();
        assertTrue(messages.size() == 1);
    }
}
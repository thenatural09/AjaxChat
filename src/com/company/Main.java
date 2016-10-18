package com.company;

import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import spark.Spark;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY,name VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS messages (id IDENTITY,text VARCHAR,time TIMESTAMP,user_id INT)");
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);
        Spark.externalStaticFileLocation("public");
        Spark.init();

        Spark.get(
                "/messages",
                (request,response) -> {
                    ArrayList<Message> msgs = selectMessages(conn);
                    JsonSerializer serializer = new JsonSerializer();
                    MessagesWrapper wrapper = new MessagesWrapper(msgs);
                    return serializer.deep(true).serialize(wrapper);
                }
        );

        Spark.post(
                "/messages",
                (request,response) -> {
                    String body = request.body();
                    JsonParser parser = new JsonParser();
                    HashMap<String,String> m = parser.parse(body);
                    User user = selectUser(conn,m.get("author"));
                    if(user == null) {
                        insertUser(conn,m.get("author"));
                        user = selectUser(conn,m.get("text"));
                    }
                    insertMessage(conn,m.get("text"),user.id);
                    return "";
                }
        );
    }

    public static void insertMessage (Connection conn, String text, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO messages VALUES (null,?,CURRENT_TIMESTAMP(),?)");
        stmt.setString(1,text);
        stmt.setInt(2,userId);
        stmt.execute();
    }

    static ArrayList<Message> selectMessages(Connection conn) throws SQLException {
        ArrayList<Message> messages = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM messages INNER JOIN users ON messages.user_id = users.id");
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("messages.id");
            String text = results.getString("messages.text");
            Timestamp time = results.getTimestamp("messages.time");
            String author = results.getString("users.name");
            Message message = new Message(id,author,text,time);
            messages.add(message);
        }
        return messages;
    }

    public static void insertUser (Connection conn,String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(null,?)");
        stmt.setString(1,name);
        stmt.execute();
    }

    public static User selectUser (Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1,name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            return new User(id,name);
        }
        return null;
    }
}

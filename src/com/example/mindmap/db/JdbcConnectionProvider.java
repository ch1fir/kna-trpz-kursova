package com.example.mindmap.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnectionProvider {

    private static final String URL = "jdbc:mysql://localhost:3306/mindmapdb";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // драйвер вже працює
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

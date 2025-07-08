package com.panchoarc.restoreit.db.impl;

import com.panchoarc.restoreit.db.DatabaseConnectorStrategy;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgreSQLConnector implements DatabaseConnectorStrategy {
    @Override
    public boolean canConnect(String host, String port, String db, String user, String password) {
        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, db);
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}

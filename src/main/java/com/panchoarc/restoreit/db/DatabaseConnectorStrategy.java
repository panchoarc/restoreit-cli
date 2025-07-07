package com.panchoarc.restoreit.db;

public interface DatabaseConnector {

    boolean canConnect(String host, String port, String db, String user, String password);
}

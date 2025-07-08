package com.panchoarc.restoreit.db;

public interface DatabaseConnectorStrategy {

    boolean canConnect(String host, String port, String db, String user, String password);
}

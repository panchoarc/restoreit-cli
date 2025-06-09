package com.panchoarc.restoreit.db.impl;

import com.panchoarc.restoreit.db.DatabaseConnector;

import java.io.File;

public class SQLiteConnector implements DatabaseConnector {
    @Override
    public boolean canConnect(String host, String port, String db, String user, String password) {
        return new File(db).exists();
    }
}

package com.panchoarc.restoreit.db;

import com.panchoarc.restoreit.enums.DatabaseType;
import com.panchoarc.restoreit.db.impl.*;

public class DatabaseConnectorFactory {
    public static DatabaseConnector getConnector(DatabaseType type) {
        return switch (type) {
            case MYSQL -> new MySQLConnector();
            case POSTGRESQL -> new PostgreSQLConnector();
            case MONGODB -> new MongoDBConnector();
            case SQLITE -> new SQLiteConnector();
        };
    }
}
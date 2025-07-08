package com.panchoarc.restoreit.db;

import com.panchoarc.restoreit.db.impl.MongoDBConnector;
import com.panchoarc.restoreit.db.impl.MySQLConnector;
import com.panchoarc.restoreit.db.impl.PostgreSQLConnector;
import com.panchoarc.restoreit.db.impl.SQLiteConnector;
import com.panchoarc.restoreit.enums.DatabaseType;

public class DatabaseConnectorFactory {
    public static DatabaseConnectorStrategy getConnector(DatabaseType type) {
        return switch (type) {
            case MYSQL -> new MySQLConnector();
            case POSTGRESQL -> new PostgreSQLConnector();
            case MONGODB -> new MongoDBConnector();
            case SQLITE -> new SQLiteConnector();
        };
    }
}
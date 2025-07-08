package com.panchoarc.restoreit.db.impl;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.panchoarc.restoreit.db.DatabaseConnectorStrategy;

import java.util.Collections;

public class MongoDBConnector implements DatabaseConnectorStrategy {
    @Override
    public boolean canConnect(String host, String port, String db, String user, String password) {
        try {
            MongoClientSettings settings;

            if (user != null && !user.isBlank() && password != null && !password.isBlank()) {
                MongoCredential credential = MongoCredential.createCredential(user, "admin", password.toCharArray());
                settings = MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Collections.singletonList(new ServerAddress(host, Integer.parseInt(port)))))
                        .credential(credential)
                        .build();
            } else {
                settings = MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Collections.singletonList(new ServerAddress(host, Integer.parseInt(port)))))
                        .build();
            }

            try (MongoClient client = MongoClients.create(settings)) {
                MongoDatabase database = client.getDatabase(db);
                database.listCollectionNames().first(); // Trigger connection
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

}

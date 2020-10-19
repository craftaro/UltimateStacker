package com.songoda.ultimatestacker.database.migrations;

import com.songoda.core.database.DataMigration;
import com.songoda.core.database.MySQLConnector;
import com.songoda.ultimatestacker.UltimateStacker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _2_EntityStacks extends DataMigration {

    public _2_EntityStacks() {
        super(2);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        String autoIncrement = UltimateStacker.getInstance().getDatabaseConnector() instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create host entities table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "host_entities (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "uuid VARCHAR(36) NOT NULL," +
                    "create_duplicates INTEGER NOT NULL DEFAULT 0" +
                    ")");
        }

        // Create stacked entities table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "stacked_entities (" +
                    "uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                    "host INTEGER NOT NULL," +
                    "serialized_entity VARBINARY(255) NOT NULL" +
                    ")");
        }
    }
}

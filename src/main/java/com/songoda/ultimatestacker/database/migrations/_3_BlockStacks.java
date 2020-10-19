package com.songoda.ultimatestacker.database.migrations;

import com.songoda.core.database.DataMigration;
import com.songoda.core.database.MySQLConnector;
import com.songoda.ultimatestacker.UltimateStacker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _3_BlockStacks extends DataMigration {

    public _3_BlockStacks() {
        super(3);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        String autoIncrement = UltimateStacker.getInstance().getDatabaseConnector() instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create blocks table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "blocks (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "amount INTEGER NOT NULL," +
                    "material TEXT NOT NULL," +
                    "world TEXT NOT NULL, " +
                    "x DOUBLE NOT NULL, " +
                    "y DOUBLE NOT NULL, " +
                    "z DOUBLE NOT NULL " +
                    ")");
        }
    }
}

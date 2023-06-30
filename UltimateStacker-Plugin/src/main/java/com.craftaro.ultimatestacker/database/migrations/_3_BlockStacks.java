package com.craftaro.ultimatestacker.database.migrations;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.core.database.DataMigration;
import com.craftaro.core.database.DatabaseConnector;
import com.craftaro.core.database.MySQLConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _3_BlockStacks extends DataMigration {

    public _3_BlockStacks() {
        super(3);
    }

    @Override
    public void migrate(DatabaseConnector connector, String tablePrefix) throws SQLException {
        String autoIncrement = connector instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create blocks table
        try (Statement statement = connector.getConnection().createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "blocks (" +
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

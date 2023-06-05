package com.craftaro.ultimatestacker.database.migrations;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.songoda.core.database.DataMigration;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.MySQLConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {

    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(DatabaseConnector connector, String tablePrefix) throws SQLException {
        String autoIncrement = connector instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create spawners table
        try (Statement statement = connector.getConnection().createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "spawners (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "amount INTEGER NOT NULL," +
                    "world TEXT NOT NULL, " +
                    "x DOUBLE NOT NULL, " +
                    "y DOUBLE NOT NULL, " +
                    "z DOUBLE NOT NULL " +
                    ")");
        }
    }

}

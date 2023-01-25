package com.songoda.ultimatestacker.database.migrations;

import com.songoda.core.database.DataMigration;
import com.songoda.core.database.MySQLConnector;
import com.songoda.ultimatestacker.UltimateStacker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _4_DataPurge extends DataMigration {

    public _4_DataPurge() {
        super(4);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "host_entities ADD COLUMN updated_at datetime DEFAULT NULL");
        } catch (SQLException e) {
            // Ignore
        }
    }
}

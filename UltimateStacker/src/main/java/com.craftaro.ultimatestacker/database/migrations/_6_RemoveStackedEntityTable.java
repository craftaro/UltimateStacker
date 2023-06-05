package com.craftaro.ultimatestacker.database.migrations;

import com.songoda.core.database.DataMigration;
import com.songoda.core.database.DatabaseConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _6_RemoveStackedEntityTable extends DataMigration {

    public _6_RemoveStackedEntityTable() {
        super(6);
    }

    @Override
    public void migrate(DatabaseConnector connector, String tablePrefix) {
        try (Statement statement = connector.getConnection().createStatement()) {
            statement.execute("DROP TABLE IF EXISTS " + tablePrefix + "stacked_entities");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package com.songoda.ultimatestacker.database.migrations;

import com.songoda.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _5_StackedEntitiesTableUpdate extends DataMigration {

        public _5_StackedEntitiesTableUpdate() {
            super(5);
        }

        @Override
        public void migrate(Connection connection, String tablePrefix) throws SQLException {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE " + tablePrefix + "stacked_entities MODIFY serialized_entity VARBINARY(9999)");
            } catch (SQLException e) {
                // Ignore
                //TODO fix it for sqlite
            }
        }
}

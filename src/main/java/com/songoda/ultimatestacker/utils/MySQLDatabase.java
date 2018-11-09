package com.songoda.ultimatestacker.utils;

import com.songoda.ultimatestacker.UltimateStacker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase {

    private final UltimateStacker instance;

    private Connection connection;

    public MySQLDatabase(UltimateStacker instance) {
        this.instance = instance;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + instance.getConfig().getString("Database.IP") + ":" + instance.getConfig().getString("Database.Port") + "/" + instance.getConfig().getString("Database.Database Name") + "?autoReconnect=true&useSSL=false";
            this.connection = DriverManager.getConnection(url, instance.getConfig().getString("Database.Username"), instance.getConfig().getString("Database.Password"));

            createTables();

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed.");
        }
    }

    private void createTables() {
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + instance.getConfig().getString("Database.Prefix") + "entities` (\n" +
                    "\t`uuid` TEXT NULL,\n" +
                    "\t`amount` INT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + instance.getConfig().getString("Database.Prefix") + "spawners` (\n" +
                    "\t`location` TEXT NULL,\n" +
                    "\t`amount` INT NULL\n" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
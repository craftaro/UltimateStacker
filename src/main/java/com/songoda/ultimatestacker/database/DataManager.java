package com.songoda.ultimatestacker.database;

import com.songoda.ultimatestacker.spawner.SpawnerStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DataManager {

    private final DatabaseConnector databaseConnector;
    private final Plugin plugin;

    public DataManager(DatabaseConnector databaseConnector, Plugin plugin) {
        this.databaseConnector = databaseConnector;
        this.plugin = plugin;
    }

    /**
     * @return the prefix to be used by all table names
     */
    public String getTablePrefix() {
        return this.plugin.getDescription().getName().toLowerCase() + '_';
    }

    public void bulkUpdateSpawners(Collection<SpawnerStack> spawnerStacks) {
        this.databaseConnector.connect(connection -> {
            String updateSpawner = "UPDATE " + this.getTablePrefix() + "spawners SET amount = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateSpawner)) {
                for (SpawnerStack spawnerStack : spawnerStacks) {
                    statement.setInt(1, spawnerStack.getAmount());
                    statement.setInt(2, spawnerStack.getId());
                    statement.executeUpdate();
                    statement.addBatch();
                }

                statement.executeBatch();
            }
        });
    }


    public void updateSpawner(SpawnerStack spawnerStack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updateSpawner = "UPDATE " + this.getTablePrefix() + "spawners SET amount = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateSpawner)) {
                statement.setInt(1, spawnerStack.getAmount());
                statement.setInt(2, spawnerStack.getId());
                statement.executeUpdate();
            }
        }));
    }


    public void createSpawner(SpawnerStack spawnerStack) {
        this.async(() -> this.databaseConnector.connect(connection -> {

            String createSpawner = "INSERT INTO " + this.getTablePrefix() + "spawners (amount, world, x, y, z) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSpawner)) {
                statement.setInt(1, spawnerStack.getAmount());

                statement.setString(2, spawnerStack.getWorld().getName());
                statement.setInt(3, spawnerStack.getX());
                statement.setInt(4, spawnerStack.getY());
                statement.setInt(5, spawnerStack.getZ());
                statement.executeUpdate();
            }
            int spawnerId = this.lastInsertedId(connection);
            this.sync(() -> spawnerStack.setId(spawnerId));
        }));
    }

    public void deleteSpawner(SpawnerStack spawnerStack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteSpawner = "DELETE FROM " + this.getTablePrefix() + "spawners WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteSpawner)) {
                statement.setInt(1, spawnerStack.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void getSpawners(Consumer<Map<Location, SpawnerStack>> callback) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String selectSpawners = "SELECT * FROM " + this.getTablePrefix() + "spawners";

            Map<Location, SpawnerStack> spawners = new HashMap<>();

            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(selectSpawners);
                while (result.next()) {
                    int spawnerId = result.getInt("id");

                    int amount = result.getInt("amount");

                    String world = result.getString("world");
                    int x = result.getInt("x");
                    int y = result.getInt("y");
                    int z = result.getInt("z");
                    Location location = new Location(Bukkit.getWorld(world), x, y, z);

                    SpawnerStack spawnerStack = new SpawnerStack(location, amount);
                    spawnerStack.setId(spawnerId);
                    spawners.put(location, spawnerStack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.sync(() -> callback.accept(spawners));
        }));
    }

    private int lastInsertedId(Connection connection) {
        String query;
        if (this.databaseConnector instanceof SQLiteConnector) {
            query = "SELECT last_insert_rowid()";
        } else {
            query = "SELECT LAST_INSERT_ID()";
        }

        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(query);
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }

    public void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(this.plugin, runnable);
    }

}

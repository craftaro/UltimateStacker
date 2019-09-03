package com.songoda.ultimatestacker.database;

import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class DataManager extends DataManagerAbstract {

    public DataManager(DatabaseConnector databaseConnector, Plugin plugin) {
        super(databaseConnector, plugin);
    }

    public void bulkUpdateSpawners(Collection<SpawnerStack> spawnerStacks) {
        this.databaseConnector.connect(connection -> {
            String updateSpawner = "UPDATE " + this.getTablePrefix() + "spawners SET amount = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateSpawner)) {
                for (SpawnerStack spawnerStack : spawnerStacks) {
                    statement.setInt(1, spawnerStack.getAmount());
                    statement.setInt(2, spawnerStack.getId());
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
                    World world = Bukkit.getWorld(result.getString("world"));

                    if(world == null)
                        continue;

                    int spawnerId = result.getInt("id");

                    int amount = result.getInt("amount");

                    int x = result.getInt("x");
                    int y = result.getInt("y");
                    int z = result.getInt("z");
                    Location location = new Location(world, x, y, z);

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
}

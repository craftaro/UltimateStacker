package com.songoda.ultimatestacker.database;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.SQLiteConnector;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.stackable.block.BlockStack;
import com.songoda.ultimatestacker.stackable.entity.ColdEntityStack;
import com.songoda.ultimatestacker.stackable.entity.EntityStack;
import com.songoda.ultimatestacker.stackable.entity.StackedEntity;
import com.songoda.ultimatestacker.stackable.spawner.SpawnerStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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
        this.queueAsync(() -> this.databaseConnector.connect(connection -> {

            String createSpawner = "INSERT INTO " + this.getTablePrefix() + "spawners (amount, world, x, y, z) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSpawner)) {
                statement.setInt(1, spawnerStack.getAmount());

                statement.setString(2, spawnerStack.getWorld().getName());
                statement.setInt(3, spawnerStack.getX());
                statement.setInt(4, spawnerStack.getY());
                statement.setInt(5, spawnerStack.getZ());
                statement.executeUpdate();
            }
            int spawnerId = this.lastInsertedId(connection, "spawners");
            this.sync(() -> spawnerStack.setId(spawnerId));
        }), "create");
    }

    public void updateBlock(BlockStack blockStack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            if (blockStack.getAmount() == 0) return;
            String updateBlock = "UPDATE " + this.getTablePrefix() + "blocks SET amount = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateBlock)) {
                statement.setInt(1, blockStack.getAmount());
                statement.setInt(2, blockStack.getId());
                statement.executeUpdate();
            }
        }));
    }


    public void createBlock(BlockStack blockStack) {
        this.queueAsync(() -> this.databaseConnector.connect(connection -> {

            String createSpawner = "INSERT INTO " + this.getTablePrefix() + "blocks (amount, material, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSpawner)) {
                statement.setInt(1, blockStack.getAmount());
                statement.setString(2, blockStack.getMaterial().name());

                statement.setString(3, blockStack.getWorld().getName());
                statement.setInt(4, blockStack.getX());
                statement.setInt(5, blockStack.getY());
                statement.setInt(6, blockStack.getZ());
                statement.executeUpdate();
            }
            int blockId = this.lastInsertedId(connection, "blocks");
            this.sync(() -> blockStack.setId(blockId));
        }), "create");
    }


    public void createHostEntity(ColdEntityStack stack) {
        this.queueAsync(() -> this.databaseConnector.connect(connection -> {
            if (stack == null || stack.getHostUniqueId() == null) return;
            String createSerializedEntity = "INSERT INTO " + this.getTablePrefix() + "host_entities (uuid, create_duplicates) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSerializedEntity)) {
                statement.setString(1, stack.getHostUniqueId().toString());
                statement.setInt(2, stack.getCreateDuplicates());
                statement.executeUpdate();
            }
            int stackId = this.lastInsertedId(connection, "host_entities");
            this.sync(() -> stack.setId(stackId));
        }), "create");
    }

    public void createStackedEntity(EntityStack hostStack, StackedEntity stackedEntity) {
        this.queueAsync(() -> this.databaseConnector.connect(connection -> {
            String createSerializedEntity = "INSERT INTO " + this.getTablePrefix() + "stacked_entities (uuid, host, serialized_entity) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSerializedEntity)) {
                if (hostStack.getHostUniqueId() == null) return;
                statement.setString(1, stackedEntity.getUniqueId().toString());
                statement.setInt(2, hostStack.getId());
                statement.setBytes(3, stackedEntity.getSerializedEntity());
                statement.executeUpdate();
            }
        }), "create");
    }

    public void createStackedEntities(ColdEntityStack hostStack, List<StackedEntity> stackedEntities) {
        this.queueAsync(() -> this.databaseConnector.connect(connection -> {
            String createSerializedEntity = "REPLACE INTO " + this.getTablePrefix() + "stacked_entities (uuid, host, serialized_entity) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSerializedEntity)) {
                if (hostStack.getHostUniqueId() == null) return;
                for (StackedEntity entity : stackedEntities) {
                    statement.setString(1, entity.getUniqueId().toString());
                    statement.setInt(2, hostStack.getId());
                    statement.setBytes(3, entity.getSerializedEntity());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }), "create");
    }

    public void updateHost(ColdEntityStack hostStack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updateHost = "UPDATE " + this.getTablePrefix() + "host_entities SET uuid = ?, create_duplicates = ?, updated_at = current_timestamp WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateHost)) {
                if (hostStack.getHostUniqueId() == null) return;
                statement.setString(1, hostStack.getHostUniqueId().toString());
                statement.setInt(2, hostStack.getCreateDuplicates());
                statement.setInt(3, hostStack.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void deleteHost(ColdEntityStack stack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteHost = "DELETE FROM " + this.getTablePrefix() + "host_entities WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteHost)) {
                statement.setInt(1, stack.getId());
                statement.executeUpdate();
            }

            String deleteStackedEntities = "DELETE FROM " + this.getTablePrefix() + "stacked_entities WHERE host = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteStackedEntities)) {
                statement.setInt(1, stack.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void deleteStackedEntity(UUID uuid) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteStackedEntity = "DELETE FROM " + this.getTablePrefix() + "stacked_entities WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteStackedEntity)) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            }
        }));
    }

    public void deleteStackedEntities(List<StackedEntity> entities) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteStackedEntities = "DELETE FROM " + this.getTablePrefix() + "stacked_entities WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteStackedEntities)) {
                for (StackedEntity entity : entities) {
                    if (entity == null) continue;
                    statement.setString(1, entity.getUniqueId().toString());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
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

    public void deleteBlock(BlockStack blockStack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteBlock = "DELETE FROM " + this.getTablePrefix() + "blocks WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteBlock)) {
                statement.setInt(1, blockStack.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void getEntities(Consumer<Map<Integer, ColdEntityStack>> callback) {
        this.async(() -> this.databaseConnector.connect(connection -> {

            Map<Integer, ColdEntityStack> entities = new HashMap<>();

            if (this.databaseConnector instanceof SQLiteConnector) {
                String selectOldEntities = "SELECT * FROM " + this.getTablePrefix() + "host_entities where updated_at <= date('now','-" + Settings.DATABASE_PURGE.getInt() + " day')";
                try (Statement statement = connection.createStatement()) {
                    List<String> toDelete = new ArrayList<>();

                    ResultSet result = statement.executeQuery(selectOldEntities);
                    while (result.next()) {
                        int hostId = result.getInt("id");
                        toDelete.add(String.valueOf(hostId));
                    }
                    statement.execute("DELETE FROM " + this.getTablePrefix() + "host_entities where updated_at <= date('now','-" + Settings.DATABASE_PURGE.getInt() + " day')");
                    statement.execute("DELETE FROM " + this.getTablePrefix() + "stacked_entities where host IN (" + String.join(", ", toDelete) + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            String selectEntities = "SELECT * FROM " + this.getTablePrefix() + "host_entities";
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(selectEntities);
                while (result.next()) {
                    int hostId = result.getInt("id");

                    UUID host = UUID.fromString(result.getString("uuid"));

                    int createDuplicates = result.getInt("create_duplicates");

                    ColdEntityStack stack = new ColdEntityStack(host, hostId);
                    stack.createDuplicates(createDuplicates);
                    entities.put(hostId, stack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String selectStackedEntities = "SELECT * FROM " + this.getTablePrefix() + "stacked_entities";
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(selectStackedEntities);
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("uuid"));
                    int hostId = result.getInt("host");
                    byte[] serializedEntity = result.getBytes("serialized_entity");

                    ColdEntityStack stack = entities.get(hostId);
                    if (stack == null) continue;
                    stack.addEntityToStackSilently(new StackedEntity(uuid, serializedEntity));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.sync(() -> callback.accept(entities));
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

                    if (world == null)
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

    public void getBlocks(Consumer<Map<Location, BlockStack>> callback) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String selectBlocks = "SELECT * FROM " + this.getTablePrefix() + "blocks";

            Map<Location, BlockStack> blocks = new HashMap<>();

            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(selectBlocks);
                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString("world"));

                    if (world == null)
                        continue;

                    int blockId = result.getInt("id");

                    CompatibleMaterial material = CompatibleMaterial.getMaterial(result.getString("material"));

                    int amount = result.getInt("amount");

                    int x = result.getInt("x");
                    int y = result.getInt("y");
                    int z = result.getInt("z");
                    Location location = new Location(world, x, y, z);

                    BlockStack blockStack = new BlockStack(material, location, amount);
                    blockStack.setId(blockId);
                    blocks.put(location, blockStack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.sync(() -> callback.accept(blocks));
        }));
    }
}

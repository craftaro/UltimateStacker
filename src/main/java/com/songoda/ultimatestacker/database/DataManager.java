package com.songoda.ultimatestacker.database;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.DatabaseType;
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
import org.checkerframework.common.returnsreceiver.qual.This;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        try (Connection connection = this.databaseConnector.getConnection()) {
            String updateSpawner = "UPDATE " + this.getTablePrefix() + "spawners SET amount = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(updateSpawner);
            for (SpawnerStack spawnerStack : spawnerStacks) {
                statement.setInt(1, spawnerStack.getAmount());
                statement.setInt(2, spawnerStack.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateSpawner(SpawnerStack spawnerStack) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {

                String updateSpawner = "UPDATE " + this.getTablePrefix() + "spawners SET amount = ? WHERE id = ?";

                PreparedStatement statement = connection.prepareStatement(updateSpawner);
                statement.setInt(1, spawnerStack.getAmount());
                statement.setInt(2, spawnerStack.getId());
                statement.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void createSpawner(SpawnerStack spawnerStack) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createSpawner = "INSERT " + getSyntax("INTO ", DatabaseType.MYSQL) + getSyntax("OR REPLACE INTO ", DatabaseType.SQLITE) + this.getTablePrefix() + "spawners (amount, world, x, y, z) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createSpawner);
                statement.setInt(1, spawnerStack.getAmount());

                statement.setString(2, spawnerStack.getWorld().getName());
                statement.setInt(3, spawnerStack.getX());
                statement.setInt(4, spawnerStack.getY());
                statement.setInt(5, spawnerStack.getZ());
                statement.executeUpdate();
                int spawnerId = this.lastInsertedId(connection, "spawners");
                this.sync(() -> spawnerStack.setId(spawnerId));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void updateBlock(BlockStack blockStack) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String updateBlock = "UPDATE " + this.getTablePrefix() + "blocks SET amount = ? WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(updateBlock);
                if (blockStack.getAmount() == 0) return;
                statement.setInt(1, blockStack.getAmount());
                statement.setInt(2, blockStack.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createBlock(BlockStack blockStack) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createSpawner = "INSERT " + getSyntax("INTO ", DatabaseType.MYSQL) + getSyntax("OR REPLACE INTO ", DatabaseType.SQLITE) + this.getTablePrefix() + "blocks (amount, material, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createSpawner);
                statement.setInt(1, blockStack.getAmount());
                statement.setString(2, blockStack.getMaterial().name());

                statement.setString(3, blockStack.getWorld().getName());
                statement.setInt(4, blockStack.getX());
                statement.setInt(5, blockStack.getY());
                statement.setInt(6, blockStack.getZ());
                statement.executeUpdate();
                int blockId = this.lastInsertedId(connection, "blocks");
                this.sync(() -> blockStack.setId(blockId));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createHostEntity(ColdEntityStack stack) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createSerializedEntity = "INSERT " + getSyntax("INTO ", DatabaseType.MYSQL) + getSyntax("OR REPLACE INTO ", DatabaseType.SQLITE) + this.getTablePrefix() + "host_entities (uuid, create_duplicates) VALUES (?, ?)";
                PreparedStatement statement = connection.prepareStatement(createSerializedEntity);
                if (stack == null || stack.getHostUniqueId() == null) return;
                statement.setString(1, stack.getHostUniqueId().toString());
                statement.setInt(2, stack.getCreateDuplicates());
                statement.executeUpdate();
                int stackId = this.lastInsertedId(connection, "host_entities");
                this.sync(() -> stack.setId(stackId));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createStackedEntity(EntityStack hostStack, StackedEntity stackedEntity) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                String createSerializedEntity = "INSERT " + getSyntax("INTO ", DatabaseType.MYSQL) + getSyntax("OR REPLACE INTO ", DatabaseType.SQLITE) + this.getTablePrefix() + "stacked_entities (uuid, host, serialized_entity) VALUES (?, ?, ?) "
                        + (Settings.MYSQL_ENABLED.getBoolean() ? "ON DUPLICATE KEY UPDATE host = ?, serialized_entity = ?" : "ON CONFLICT(uuid) DO UPDATE SET host = ?, serialized_entity = ?");
                PreparedStatement statement = connection.prepareStatement(createSerializedEntity);
                if (hostStack.getHostUniqueId() == null) return;
                statement.setString(1, stackedEntity.getUniqueId().toString());
                statement.setInt(2, hostStack.getId());
                statement.setBytes(3, stackedEntity.getSerializedEntity());
                statement.setInt(4, hostStack.getId());
                statement.setBytes(5, stackedEntity.getSerializedEntity());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createStackedEntities(ColdEntityStack hostStack, List<StackedEntity> stackedEntities) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createSerializedEntity = "REPLACE INTO " + this.getTablePrefix() + "stacked_entities (uuid, host, serialized_entity) VALUES (?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createSerializedEntity);
                if (hostStack.getHostUniqueId() == null) return;
                for (StackedEntity entity : stackedEntities) {
                    statement.setString(1, entity.getUniqueId().toString());
                    statement.setInt(2, hostStack.getId());
                    statement.setBytes(3, entity.getSerializedEntity());
                    statement.addBatch();
                }
                statement.executeBatch();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void updateHost(ColdEntityStack hostStack) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String updateHost = "UPDATE " + this.getTablePrefix() + "host_entities SET uuid = ?, create_duplicates = ?, updated_at = current_timestamp WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(updateHost);
                if (hostStack.getHostUniqueId() == null) return;
                statement.setString(1, hostStack.getHostUniqueId().toString());
                statement.setInt(2, hostStack.getCreateDuplicates());
                statement.setInt(3, hostStack.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteHost(ColdEntityStack stack) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteStackedEntity(UUID uuid) {
        if (uuid == null)
            return;

       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String deleteStackedEntity = "DELETE FROM " + this.getTablePrefix() + "stacked_entities WHERE uuid = ?";
                PreparedStatement statement = connection.prepareStatement(deleteStackedEntity);
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteStackedEntities(List<StackedEntity> entities) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String deleteStackedEntities = "DELETE FROM " + this.getTablePrefix() + "stacked_entities WHERE uuid = ?";
                PreparedStatement statement = connection.prepareStatement(deleteStackedEntities);
                for (StackedEntity entity : entities) {
                    if (entity == null) continue;
                    statement.setString(1, entity.getUniqueId().toString());
                    statement.addBatch();
                }
                statement.executeBatch();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteSpawner(SpawnerStack spawnerStack) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String deleteSpawner = "DELETE FROM " + this.getTablePrefix() + "spawners WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(deleteSpawner);
                statement.setInt(1, spawnerStack.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteBlock(BlockStack blockStack) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String deleteBlock = "DELETE FROM " + this.getTablePrefix() + "blocks WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(deleteBlock);
                statement.setInt(1, blockStack.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getEntities(Consumer<Map<Integer, ColdEntityStack>> callback) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {

                Map<Integer, ColdEntityStack> entities = new HashMap<>();

                boolean mysql = Settings.MYSQL_ENABLED.getBoolean();
                int databasePurge = Settings.DATABASE_PURGE.getInt();
                String whereStatement = mysql ? "WHERE updated_at < NOW() - INTERVAL " + databasePurge + " DAY" : "WHERE updated_at <= date('now','-" + databasePurge + " day')";
                String selectOldEntities = "SELECT * FROM " + this.getTablePrefix() + "host_entities " + whereStatement;

                try (Statement statement = connection.createStatement()) {
                    List<String> toDelete = new ArrayList<>();

                    ResultSet result = statement.executeQuery(selectOldEntities);
                    while (result.next()) {
                        int hostId = result.getInt("id");
                        toDelete.add(String.valueOf(hostId));
                    }

                    if (!toDelete.isEmpty()) {
                        statement.execute("DELETE FROM " + this.getTablePrefix() + "host_entities " + whereStatement);
                        statement.execute("DELETE FROM " + this.getTablePrefix() + "stacked_entities WHERE host IN (" + String.join(", ", toDelete) + ")");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getSpawners(Consumer<Map<Location, SpawnerStack>> callback) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getBlocks(Consumer<Map<Location, BlockStack>> callback) {
       this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String selectBlocks = "SELECT * FROM " + this.getTablePrefix() + "blocks";

                Map<Location, BlockStack> blocks = new HashMap<>();

                Statement statement = connection.createStatement();

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

                    this.sync(() -> callback.accept(blocks));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}

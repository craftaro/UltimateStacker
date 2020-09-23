package com.songoda.ultimatestacker.convert;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.SQLiteConnector;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.stackable.entity.EntityStackManager;
import com.songoda.ultimatestacker.stackable.spawner.SpawnerStack;
import org.bukkit.Bukkit;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

public class WildStackerConvert implements Convert {

    private Plugin wildStacker;
    private final UltimateStacker plugin;

    public WildStackerConvert() {
        this.plugin = UltimateStacker.getInstance();
        this.wildStacker = Bukkit.getPluginManager().getPlugin("WildStacker");

    }

    @Override
    public String getName() {
        return "WildStacker";
    }

    @Override
    public boolean canEntities() {
        return true;
    }

    @Override
    public boolean canSpawners() {
        return true;
    }

    @Override
    public void convertEntities() {
        EntityStackManager entityStackManager = plugin.getEntityStackManager();

        DatabaseConnector connector = new SQLiteConnector(this.wildStacker);
        connector.connect(connection -> {

            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT uuid, stackAmount FROM entities");
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("uuid"));
                    int amount = result.getInt("stackAmount");
                    if (!entityStackManager.isEntityInColdStorage(uuid))
                        entityStackManager.addLegacyColdStack(uuid, amount);
                }
            }
        });


    }

    @Override
    public void convertSpawners() {
        for (StackedSpawner spawner : WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawners()) {
            SpawnerStack stack = plugin.getSpawnerStackManager().getSpawner(spawner.getLocation());

            stack.setAmount(WildStackerAPI
                    .getSpawnersAmount((CreatureSpawner) spawner.getLocation().getBlock().getState()));
            plugin.updateHologram(stack);
        }
    }

    @Override
    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(wildStacker);
    }
}

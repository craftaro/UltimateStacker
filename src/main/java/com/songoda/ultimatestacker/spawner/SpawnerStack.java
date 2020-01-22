package com.songoda.ultimatestacker.spawner;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.utils.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;

import java.util.Random;

public class SpawnerStack {

    private int id;

    private final Location location;
    private int amount = 1;

    public SpawnerStack(Location location, int amount) {
        this.location = location;
        setAmount(amount);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        UltimateStacker plugin = UltimateStacker.getInstance();
        this.amount = amount;
        plugin.getDataManager().updateSpawner(this);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!(location.getBlock().getState() instanceof CreatureSpawner)) return;
            int count = Settings.STACK_ENTITIES.getBoolean()
                    && !plugin.getStackingTask().isWorldDisabled(location.getWorld()) ? 1 : calculateSpawnCount();
            int maxNearby = amount > 6 ? amount + 3 : 6;
            CreatureSpawner creatureSpawner = (CreatureSpawner) location.getBlock().getState();
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
                creatureSpawner.setMaxNearbyEntities(maxNearby);
                creatureSpawner.setSpawnCount(count);
            } else {
                Reflection.updateSpawner(creatureSpawner, count, maxNearby);
            }
            creatureSpawner.update();
        }, 1L);
    }

    public int calculateSpawnCount() {
        Random random = new Random();
        int count = 0;
        for (int i = 0; i < getAmount(); i++) {
            count += random.nextInt(3) + 1;
        }
        return count;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Location getLocation() {
        return location.clone();
    }

    public int getX() {
        return location.getBlockX();
    }

    public int getY() {
        return location.getBlockY();
    }

    public int getZ() {
        return location.getBlockZ();
    }

    public World getWorld() {
        return location.getWorld();
    }

    @Override
    public String toString() {
        return "SpawnerStack:{"
                + "Amount:\"" + amount + "\","
                + "Location:{"
                + "World:\"" + location.getWorld().getName() + "\","
                + "X:" + location.getBlockX() + ","
                + "Y:" + location.getBlockY() + ","
                + "Z:" + location.getBlockZ()
                + "}"
                + "}";
    }
}

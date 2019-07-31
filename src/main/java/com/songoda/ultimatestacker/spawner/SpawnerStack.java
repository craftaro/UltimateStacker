package com.songoda.ultimatestacker.spawner;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Reflection;
import com.songoda.ultimatestacker.utils.ServerVersion;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

import java.util.Random;

public class SpawnerStack {

    private final Location location;
    private int amount = 1;

    public SpawnerStack(Location location, int amount) {
        this.location = location;
        setAmount(amount);
    }

    public Location getLocation() {
        return location.clone();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        UltimateStacker plugin = UltimateStacker.getInstance();
        this.amount = amount;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!(location.getBlock().getState() instanceof CreatureSpawner)) return;
            int count = Setting.STACK_ENTITIES.getBoolean()
                    && !plugin.getStackingTask().isWorldDisabled(location.getWorld()) ? 1 : calculateSpawnCount();
            int maxNearby = amount > 6 ? amount + 3 : 6;
            CreatureSpawner creatureSpawner = (CreatureSpawner) location.getBlock().getState();
            if (UltimateStacker.getInstance().isServerVersionAtLeast(ServerVersion.V1_12)) {
                creatureSpawner.setMaxNearbyEntities(maxNearby);
                creatureSpawner.setSpawnCount(count);
            } else {
                Reflection.updateSpawner(creatureSpawner, count, maxNearby);
            }
            creatureSpawner.update();
        }, 1L);
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

    public int calculateSpawnCount() {
        Random random = new Random();
        int count = 0;
        for (int i = 0; i < getAmount(); i ++) {
            count += random.nextInt(3 - 1 + 1) + 1;
        }
        return count;
    }
}

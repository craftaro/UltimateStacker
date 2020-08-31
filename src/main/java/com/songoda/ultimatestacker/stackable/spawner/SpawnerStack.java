package com.songoda.ultimatestacker.stackable.spawner;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.stackable.Hologramable;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.Reflection;
import com.songoda.ultimatestacker.utils.Stackable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;

import java.util.Random;

public class SpawnerStack implements Stackable, Hologramable {

    private int id;
    private boolean initialized = false;

    private final Location location;
    private int amount;

    private static final UltimateStacker plugin = UltimateStacker.getInstance();

    public SpawnerStack(Location location, int amount) {
        this.location = location;
        this.amount = amount;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public boolean isValid() {
        return CompatibleMaterial.getMaterial(location.getBlock()) == CompatibleMaterial.SPAWNER;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        plugin.getDataManager().updateSpawner(this);
    }

    public void updateAmount() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!(location.getBlock().getState() instanceof CreatureSpawner)) return;
            int count = Settings.STACK_ENTITIES.getBoolean()
                    && !plugin.getStackingTask().isWorldDisabled(location.getWorld()) ? 1 : calculateSpawnCount();
            int maxNearby = amount > 6 ? amount + 3 : 6;
            CreatureSpawner creatureSpawner = (CreatureSpawner) location.getBlock().getState();
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) {
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

    @Override
    public String getHologramName() {
        if (!(location.getBlock().getState() instanceof CreatureSpawner)) {
            plugin.getSpawnerStackManager().removeSpawner(location);
            return null;
        }
        CreatureSpawner creatureSpawner = (CreatureSpawner) location.getBlock().getState();
        return Methods.compileSpawnerName(creatureSpawner.getSpawnedType(), amount);
    }

    @Override
    public boolean areHologramsEnabled() {
        return Settings.SPAWNER_HOLOGRAMS.getBoolean();
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

    public void initialize() {
        if (!initialized) {
            updateAmount();
            this.initialized = true;
        }
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

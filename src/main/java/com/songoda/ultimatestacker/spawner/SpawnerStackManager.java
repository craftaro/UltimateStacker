package com.songoda.ultimatestacker.spawner;

import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpawnerStackManager {

    private final Map<Location, SpawnerStack> registeredSpawners = new HashMap<>();

    public void addSpawners(Map<Location, SpawnerStack> spawners) {
        this.registeredSpawners.putAll(spawners);
    }

    public SpawnerStack addSpawner(SpawnerStack spawnerStack) {
        this.registeredSpawners.put(roundLocation(spawnerStack.getLocation()), spawnerStack);
        return spawnerStack;
    }

    public SpawnerStack removeSpawner(Location location) {
        return registeredSpawners.remove(roundLocation(location));
    }

    public SpawnerStack getSpawner(Location location) {
        if (!this.registeredSpawners.containsKey(roundLocation(location))) {
            SpawnerStack spawnerStack = addSpawner(new SpawnerStack(roundLocation(location), 1));
            UltimateStacker.getInstance().getDataManager().createSpawner(spawnerStack);
            return spawnerStack;
        }
        return this.registeredSpawners.get(location);
    }

    public SpawnerStack getSpawner(Block block) {
        return this.getSpawner(block.getLocation());
    }

    public boolean isSpawner(Location location) {
        return this.registeredSpawners.get(location) != null;
    }

    public Collection<SpawnerStack> getStacks() {
        return Collections.unmodifiableCollection(this.registeredSpawners.values());
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}

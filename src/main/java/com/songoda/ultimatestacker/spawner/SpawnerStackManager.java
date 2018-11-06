package com.songoda.ultimatestacker.spawner;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpawnerStackManager {

    private static final Map<Location, SpawnerStack> registeredSpawners = new HashMap<>();

    public SpawnerStack addSpawner(SpawnerStack spawnerStack) {
        return registeredSpawners.put(roundLocation(spawnerStack.getLocation()), spawnerStack);
    }

    public SpawnerStack removeSpawner(Location location) {
        return registeredSpawners.remove(roundLocation(location));
    }

    public SpawnerStack getSpawner(Location location) {
        if (!registeredSpawners.containsKey(roundLocation(location))) {
            return addSpawner(new SpawnerStack(roundLocation(location), 1));
        }
        return registeredSpawners.get(location);
    }

    public SpawnerStack getSpawner(Block block) {
        return this.getSpawner(block.getLocation());
    }

    public Collection<SpawnerStack> getStacks() {
        return Collections.unmodifiableCollection(registeredSpawners.values());
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}

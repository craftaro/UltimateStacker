package com.craftaro.ultimatestacker.stackable.spawner;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.api.stack.spawner.SpawnerStack;
import com.craftaro.ultimatestacker.api.stack.spawner.SpawnerStackManager;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpawnerStackManagerImpl implements SpawnerStackManager {

    private final Map<Location, SpawnerStack> registeredSpawners = new HashMap<>();

    @Override
    public void addSpawners(Map<Location, SpawnerStack> spawners) {
        this.registeredSpawners.putAll(spawners);
    }

    @Override
    public SpawnerStack addSpawner(SpawnerStack spawnerStack) {
        this.registeredSpawners.put(roundLocation(spawnerStack.getLocation()), spawnerStack);
        return spawnerStack;
    }

    @Override
    public SpawnerStack removeSpawner(Location location) {
        return registeredSpawners.remove(roundLocation(location));
    }

    @Override
    public SpawnerStack getSpawner(Location location) {
        if (!this.registeredSpawners.containsKey(roundLocation(location))) {
            SpawnerStack spawnerStack = addSpawner(new SpawnerStackImpl(roundLocation(location), 1));
            UltimateStacker.getInstance().getDataManager().createSpawner(spawnerStack);
            return spawnerStack;
        }
        return this.registeredSpawners.get(location);
    }

    @Override
    public SpawnerStack getSpawner(Block block) {
        return this.getSpawner(block.getLocation());
    }

    @Override
    public boolean isSpawner(Location location) {
        return this.registeredSpawners.get(location) != null;
    }

    @Override
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

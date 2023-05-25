package com.craftaro.ultimatestacker.api.stack.spawner;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Map;

public interface SpawnerStackManager {
    void addSpawners(Map<Location, SpawnerStack> spawners);

    SpawnerStack addSpawner(SpawnerStack spawnerStack);

    SpawnerStack getSpawner(Block block);

    SpawnerStack getSpawner(Location location);

    boolean isSpawner(Location location);

    SpawnerStack removeSpawner(Location location);

    Collection<SpawnerStack> getStacks();
}

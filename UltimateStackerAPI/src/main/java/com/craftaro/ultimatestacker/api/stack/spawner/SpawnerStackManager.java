package com.craftaro.ultimatestacker.api.stack.spawner;

import com.songoda.core.database.Data;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SpawnerStackManager {
    void addSpawners(List<SpawnerStack> spawners);

    SpawnerStack addSpawner(SpawnerStack spawnerStack);

    SpawnerStack getSpawner(Block block);

    SpawnerStack getSpawner(Location location);

    boolean isSpawner(Location location);

    SpawnerStack removeSpawner(Location location);

    Collection<SpawnerStack> getStacks();

    Collection<Data> getStacksData();
}

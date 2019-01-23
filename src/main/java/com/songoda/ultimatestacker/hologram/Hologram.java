package com.songoda.ultimatestacker.hologram;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;

import java.util.Collection;

public abstract class Hologram {

    protected final UltimateStacker instance;

    Hologram(UltimateStacker instance) {
        this.instance = instance;
    }

    public void loadHolograms() {
        Collection<SpawnerStack> spawners = instance.getSpawnerStackManager().getStacks();
        if (spawners.size() == 0) return;

        for (SpawnerStack spawner : spawners) {
            if (spawner.getLocation().getWorld() == null) continue;
                add(spawner);
        }
    }

    public void unloadHolograms() {
        Collection<SpawnerStack> spawners = instance.getSpawnerStackManager().getStacks();
        if (spawners.size() == 0) return;

        for (SpawnerStack spawner : spawners) {
            if (spawner.getLocation().getWorld() == null) continue;
                remove(spawner);
        }
    }

    public void add(SpawnerStack spawner) {
        int amount = spawner.getAmount();
        if (spawner.getLocation().getBlock().getType() != Material.SPAWNER) return;

        CreatureSpawner creatureSpawner = (CreatureSpawner) spawner.getLocation().getBlock().getState();
        String name = Methods.compileSpawnerName(creatureSpawner.getSpawnedType(), amount);

        add(spawner.getLocation(), name);
    }

    public void remove(SpawnerStack spawner) {
        remove(spawner.getLocation());
    }

    public void update(SpawnerStack spawner) {
        int amount = spawner.getAmount();
        if (spawner.getLocation().getBlock().getType() != Material.SPAWNER) return;

        CreatureSpawner creatureSpawner = (CreatureSpawner) spawner.getLocation().getBlock().getState();
        String name = Methods.compileSpawnerName(creatureSpawner.getSpawnedType(), amount);

        update(spawner.getLocation(), name);
    }

    protected abstract void add(Location location, String line);

    protected abstract void remove(Location location);

    protected abstract void update(Location location, String line);

    public void processChange(Block block) {
        if (block.getType() != Material.MOB_SPAWNER) return;
        SpawnerStack spawner = instance.getSpawnerStackManager().getSpawner(block);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () ->
                    update(spawner), 1L);
    }
}

package com.songoda.ultimatestacker.handlers;

import com.songoda.arconix.api.hologram.HologramObject;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;

import java.util.Collection;

/**
 * Created by songoda on 3/12/2017.
 */
public class HologramHandler {

    private final UltimateStacker instance;

    public HologramHandler(UltimateStacker instance) {
        this.instance = instance;
        loadHolograms();
    }

    private void loadHolograms() {
        Collection<SpawnerStack> spawners = instance.getSpawnerStackManager().getStacks();
        if (spawners.size() == 0) return;

        for (SpawnerStack spawner : spawners) {
            if (spawner.getLocation().getWorld() == null) continue;
            updateHologram(spawner);
        }
    }

    public void updateHologram(SpawnerStack spawner) {
        if (spawner == null) return;

        Location location = spawner.getLocation().add(0.5, 1, 0.5);

        if (!instance.getConfig().getBoolean("Spawners.Holograms Enabled")) return;

        addHologram(location, spawner);
    }

    public void despawn(Block b) {
        Location location = b.getLocation().add(0.5, 1, 0.5);
        Arconix.pl().getApi().packetLibrary.getHologramManager().removeHologram(location, 1);
    }

    private void addHologram(Location location, SpawnerStack spawner) {
        int amount = spawner.getAmount();

        CreatureSpawner creatureSpawner = (CreatureSpawner) spawner.getLocation().getBlock().getState();
        String name = Methods.compileSpawnerName(creatureSpawner.getSpawnedType(), amount);

        HologramObject hologram = new HologramObject(null, location, name);

        Arconix.pl().getApi().packetLibrary.getHologramManager().addHologram(hologram);
    }

    public void processChange(Block block) {
        if (!block.getType().name().contains("SPAWNER")) return;
        SpawnerStack spawner = instance.getSpawnerStackManager().getSpawner(block);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> updateHologram(spawner), 1L);
    }
}
package com.craftaro.ultimatestacker.listeners;

import com.craftaro.ultimatestacker.UltimateStacker;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class BreedListeners implements Listener {

    private final UltimateStacker plugin;

    public BreedListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBread(EntityBreedEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            event.getFather().removeMetadata("breedCooldown", plugin);
            event.getMother().removeMetadata("breedCooldown", plugin);
        }, 5 * 20 * 60);
        event.getFather().setMetadata("breedCooldown", new FixedMetadataValue(plugin, true));
        event.getFather().removeMetadata("inLove", plugin);
        event.getMother().setMetadata("breedCooldown", new FixedMetadataValue(plugin, true));
        event.getMother().removeMetadata("inLove", plugin);
    }
}

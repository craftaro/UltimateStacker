package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class BreedListeners implements Listener {

    private final UltimateStacker instance;

    public BreedListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onBread(EntityBreedEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> {
            event.getFather().removeMetadata("breedCooldown", instance);
            event.getMother().removeMetadata("breedCooldown", instance);
        }, 5 * 20 * 60);
        event.getFather().setMetadata("breedCooldown", new FixedMetadataValue(instance, true));
        event.getFather().removeMetadata("inLove", instance);
        event.getMother().setMetadata("breedCooldown", new FixedMetadataValue(instance, true));
        event.getMother().removeMetadata("inLove", instance);
    }
}

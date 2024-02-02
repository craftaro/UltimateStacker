package com.craftaro.ultimatestacker.listeners;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
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
        //TODO: Fix breed. It removes a entity from the stack but not spawn a new one. (Splitting mechanic)
        boolean isMotherStacked = plugin.getEntityStackManager().isStackedEntity(event.getMother());
        boolean isFatherStacked = plugin.getEntityStackManager().isStackedEntity(event.getFather());

        if (!isMotherStacked && !isFatherStacked) return;

        if (isMotherStacked) {
            EntityStack stack = plugin.getEntityStackManager().getStackedEntity(event.getMother());
            if (stack.getAmount() <= 1) return;
            stack.releaseHost();
        }

        if (isFatherStacked) {
            EntityStack stack = plugin.getEntityStackManager().getStackedEntity(event.getFather());
            if (stack.getAmount() <= 1) return;
            stack.releaseHost();
        }

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

package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClearLagListeners implements Listener {

    private final UltimateStacker instance;

    public ClearLagListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onClearLaggTask(EntityRemoveEvent event) {
        for (Entity entity : event.getWorld().getEntities()) {
            if (entity instanceof LivingEntity && instance.getEntityStackManager().isStackedAndLoaded((LivingEntity)entity)) {
                instance.getEntityStackManager().removeStack(entity);
                event.addEntity(entity);
            }
        }
    }
}

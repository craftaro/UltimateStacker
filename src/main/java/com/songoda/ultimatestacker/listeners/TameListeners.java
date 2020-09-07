package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.stackable.entity.EntityStack;
import com.songoda.ultimatestacker.stackable.entity.EntityStackManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

public class TameListeners implements Listener {

    private final UltimateStacker plugin;

    public TameListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTame(EntityTameEvent event) {
        LivingEntity entity = event.getEntity();

        EntityStackManager stackManager = plugin.getEntityStackManager();
        if (!stackManager.isStackedAndLoaded(entity)) return;

        EntityStack stack = plugin.getEntityStackManager().getStack(entity);

        if (stack.getAmount() <= 1) return;

        stack.releaseHost();
    }
}

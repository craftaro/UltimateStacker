package com.craftaro.ultimatestacker.listeners;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStackManager;
import com.craftaro.ultimatestacker.stackable.entity.EntityStackManagerImpl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

public class TameListeners implements Listener {

    private final UltimateStacker plugin;

    public TameListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTame(EntityTameEvent event) {
        LivingEntity entity = event.getEntity();

        EntityStackManager stackManager = plugin.getEntityStackManager();
        if (!stackManager.isStackedEntity(entity)) return;

        EntityStack stack = plugin.getEntityStackManager().getStackedEntity(entity);

        if (stack.getAmount() <= 1) return;

        stack.releaseHost();
    }
}

package com.songoda.ultimatestacker.listeners.entity;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.stackable.entity.EntityStack;
import com.songoda.ultimatestacker.stackable.entity.EntityStackManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.util.Arrays;
import java.util.List;

public class EntityCurrentListener implements Listener {

    private final UltimateStacker plugin;

    public EntityCurrentListener(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(EntityTransformEvent event) {
        EntityStackManager stackManager = plugin.getEntityStackManager();
        if (stackManager.isStackedEntity(event.getEntity())
                && event.getEntity() instanceof LivingEntity
                && event.getTransformedEntity() instanceof LivingEntity) {
            if (event.getTransformReason().equals(EntityTransformEvent.TransformReason.SPLIT)) {
                stackManager.getStack((LivingEntity) event.getEntity()).removeEntityFromStack(1);
                event.setCancelled(true);
                return;
            }
            EntityStack stack = stackManager.updateStack((LivingEntity) event.getEntity(), (LivingEntity) event.getTransformedEntity());
            stack.releaseHost();
        }
    }
}

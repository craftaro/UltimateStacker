package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.entity.Split;
import com.songoda.ultimatestacker.settings.Settings;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class ShearListeners implements Listener {

    private UltimateStacker plugin;

    public ShearListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() != EntityType.SHEEP && entity.getType() != EntityType.MUSHROOM_COW) return;
        EntityStackManager stackManager = plugin.getEntityStackManager();
        if (!stackManager.isStacked(entity)) return;

        if (event.getEntity().getType() == EntityType.SHEEP
                && Settings.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.SHEEP_SHEAR)
                || event.getEntity().getType() == EntityType.MUSHROOM_COW
                && Settings.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.MUSHROOM_SHEAR))
            return;

        plugin.getEntityUtils().splitFromStack((LivingEntity)entity);
    }
}

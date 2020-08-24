package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.stackable.entity.EntityStack;
import com.songoda.ultimatestacker.stackable.entity.EntityStackManager;
import com.songoda.ultimatestacker.stackable.entity.Split;
import com.songoda.ultimatestacker.settings.Settings;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepDyeWoolEvent;

public class SheepDyeListeners implements Listener {

    private UltimateStacker plugin;

    public SheepDyeListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDye(SheepDyeWoolEvent event) {
        LivingEntity entity = event.getEntity();

        EntityStackManager stackManager = plugin.getEntityStackManager();
        if (!stackManager.isStackedAndLoaded(entity)) return;

        if (Settings.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.SHEEP_DYE))
            return;

        EntityStack stack = stackManager.getStack(entity);
        if (stack == null) return;
        stack.releaseHost();
    }
}

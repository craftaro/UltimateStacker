package com.craftaro.ultimatestacker.listeners;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStackManager;
import com.craftaro.ultimatestacker.settings.Settings;
import com.craftaro.ultimatestacker.stackable.entity.EntityStackManagerImpl;
import com.craftaro.ultimatestacker.stackable.entity.Split;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepDyeWoolEvent;

public class SheepDyeListeners implements Listener {

    private final UltimateStacker plugin;

    public SheepDyeListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDye(SheepDyeWoolEvent event) {
        LivingEntity entity = event.getEntity();

        EntityStackManager stackManager = plugin.getEntityStackManager();
        if (!stackManager.isStackedEntity(entity)) return;

        if (Settings.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.SHEEP_DYE))
            return;

        EntityStack stack = stackManager.getStackedEntity(entity);
        if (stack == null) return;
        stack.releaseHost();
    }
}

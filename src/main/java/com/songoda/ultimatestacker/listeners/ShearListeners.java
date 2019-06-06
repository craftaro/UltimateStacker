package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.entity.Split;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class ShearListeners implements Listener {

    private UltimateStacker instance;

    public ShearListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() != EntityType.SHEEP && entity.getType() != EntityType.MUSHROOM_COW) return;
        EntityStackManager stackManager = instance.getEntityStackManager();
        if (!stackManager.isStacked(entity)) return;

        if (event.getEntity().getType() == EntityType.SHEEP
                && Setting.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.SHEEP_SHEAR)
                || event.getEntity().getType() == EntityType.MUSHROOM_COW
                && Setting.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.MUSHROOM_SHEAR))
            return;

        Methods.splitFromStack((LivingEntity)entity);
    }
}

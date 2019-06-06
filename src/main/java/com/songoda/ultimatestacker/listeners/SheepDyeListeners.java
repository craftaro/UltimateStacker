package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.entity.Split;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class SheepDyeListeners implements Listener {

    private UltimateStacker instance;

    public SheepDyeListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDye(SheepDyeWoolEvent event) {
        LivingEntity entity = event.getEntity();

        EntityStackManager stackManager = instance.getEntityStackManager();
        if (!stackManager.isStacked(entity)) return;

        if (Setting.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.SHEEP_DYE))
            return;

        Methods.splitFromStack(entity);
    }
}

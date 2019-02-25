package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;

public class DeathListeners implements Listener {

    private final UltimateStacker instance;

    public DeathListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Methods.onDeath(event.getEntity(), new ArrayList<>(event.getDrops()), event.getDroppedExp());
    }
}

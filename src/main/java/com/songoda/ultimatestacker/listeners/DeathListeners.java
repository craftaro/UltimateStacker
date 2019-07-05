package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;

public class DeathListeners implements Listener {

    private final UltimateStacker instance;

    public DeathListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (instance.getEntityStackManager().isStacked(event.getEntity()))
            instance.getEntityStackManager().getStack(event.getEntity())
                    .onDeath(event.getEntity(), new ArrayList<>(event.getDrops()), event.getDroppedExp());
    }
}

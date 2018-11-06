package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropListeners implements Listener {

    private final UltimateStacker instance;

    public DropListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Item dropped = event.getItemDrop();
    }
}

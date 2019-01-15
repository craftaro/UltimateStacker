package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

public class BreedListeners implements Listener {

    private final UltimateStacker instance;

    public BreedListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onBread(EntityBreedEvent event) {

        event.getFather().removeMetadata("inLove", instance);
        event.getMother().removeMetadata("inLove", instance);
    }
}

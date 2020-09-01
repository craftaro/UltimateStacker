package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.stackable.entity.EntityStackManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListeners implements Listener {

    private final EntityStackManager entityStackManager;

    public ChunkListeners(EntityStackManager entityStackManager) {
        this.entityStackManager = entityStackManager;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entityStackManager.isEntityInColdStorage((LivingEntity) entity)) {
                entityStackManager.loadStack((LivingEntity) entity);
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entityStackManager.isStackedAndLoaded((LivingEntity) entity)) {
                entityStackManager.unloadStack((LivingEntity) entity);
            }
        }
    }
}

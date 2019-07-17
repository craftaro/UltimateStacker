package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.spawner.SpawnerStackManager;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class SpawnerListeners implements Listener {

    private final UltimateStacker instance;

    public SpawnerListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent event) {
        if (!Setting.STACK_ENTITIES.getBoolean()) return;
        SpawnerStackManager spawnerStackManager = instance.getSpawnerStackManager();
        if (!spawnerStackManager.isSpawner(event.getSpawner().getLocation())) return;

        SpawnerStack spawnerStack = spawnerStackManager.getSpawner(event.getSpawner().getLocation());

        EntityStack stack = instance.getEntityStackManager().addStack(event.getEntity().getUniqueId(), spawnerStack.calculateSpawnCount());

        instance.getStackingTask().attemptSplit(stack, (LivingEntity)event.getEntity());
    }
}

package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class SpawnerListeners implements Listener {

    private UltimateStacker instance;

    public SpawnerListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSpawner(SpawnerSpawnEvent event) {
/*
        Entity initalEntity = event.getEntity();

        if (!instance.getConfig().getBoolean("Main.Stack Entities")) return;

        List<Entity> entityList = Methods.getSimilarEntitesAroundEntity(initalEntity);

        if (entityList.size() == 0) return;

        int maxEntityStackSize = instance.getConfig().getInt("Entity.Max Stack Size");

        for (Entity entity : entityList) {
            EntityStack stack = instance.getEntityStackManager().getStack(entity);

            //If a stack was found add 1 to this stack.
            if (stack != null && stack.getAmount() < maxEntityStackSize) {
                stack.addAmount(1);
                stack.updateStack();
                initalEntity.remove();
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.broadcastMessage(entityList.size() + 1 + "");

        if ((entityList.size() + 1) >= instance.getConfig().getInt("Entity.Min Stack Amount")) {

            EntityStack stack = instance.getEntityStackManager().addStack(new EntityStack(initalEntity, entityList.size() + 1));
            stack.updateStack();

            for (Entity entity : entityList) {
                entity.remove();
            }
            entityList.size();
        }
        */
    }

}

package com.songoda.ultimatestacker.convert;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import org.bukkit.Bukkit;

public class WildStackerConvert implements Convert {

    private final UltimateStacker plugin;

    public WildStackerConvert(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "WildStacker";
    }

    @Override
    public boolean canEntities() {
        return true;
    }

    @Override
    public boolean canSpawners() {
        return true;
    }

    @Override
    public void convertEntities() {
        EntityStackManager entityStackManager = plugin.getEntityStackManager();
        for (StackedEntity entity : WildStackerAPI.getWildStacker().getSystemManager().getStackedEntities()) {
            if (!entityStackManager.isStacked(entity.getLivingEntity())) {
                entityStackManager.addStack(entity.getLivingEntity(), entity.getStackAmount());
                continue;
            }
            entityStackManager.getStack(entity.getLivingEntity()).setAmount(entity.getStackAmount());
        }

    }

    @Override
    public void convertSpawners() {
        for (StackedSpawner spawner : WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawners()) {
            SpawnerStack stack = plugin.getSpawnerStackManager().getSpawner(spawner.getLocation());
            stack.setAmount(spawner.getStackAmount());
            if (plugin.getHologram() != null)
                plugin.getHologram().add(stack);
        }
    }

    @Override
    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(WildStackerPlugin.getPlugin());
    }
}

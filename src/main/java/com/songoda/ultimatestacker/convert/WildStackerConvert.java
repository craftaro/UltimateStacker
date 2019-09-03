package com.songoda.ultimatestacker.convert;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class WildStackerConvert implements Convert {

    private final UltimateStacker plugin;

    public WildStackerConvert() {
        this.plugin = UltimateStacker.getInstance();
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
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity)) continue;
                if (!entityStackManager.isStacked(entity)) {
                    entityStackManager
                            .addStack(entity, WildStackerAPI.getEntityAmount((LivingEntity)entity));
                    continue;
                }
                entityStackManager
                        .getStack(entity).setAmount(WildStackerAPI.getEntityAmount((LivingEntity)entity));
            }
        }

    }

    @Override
    public void convertSpawners() {
        for (StackedSpawner spawner : WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawners()) {
            SpawnerStack stack = plugin.getSpawnerStackManager().getSpawner(spawner.getLocation());

            stack.setAmount(WildStackerAPI
                    .getSpawnersAmount((CreatureSpawner) spawner.getLocation().getBlock().getState()));
            plugin.updateHologram(stack);
        }
    }

    @Override
    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(WildStackerPlugin.getPlugin());
    }
}

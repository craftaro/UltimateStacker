package com.songoda.ultimatestacker.convert;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import org.bukkit.Bukkit;
import uk.antiperson.stackmob.StackMob;

import java.util.Map;
import java.util.UUID;

public class StackMobConvert implements Convert {

    private final UltimateStacker plugin;

    private final StackMob stackMob;

    public StackMobConvert() {
        this.plugin = UltimateStacker.getInstance();
        stackMob = (StackMob) Bukkit.getPluginManager().getPlugin("StackMob");
    }

    @Override
    public String getName() {
        return "StackMob";
    }

    @Override
    public boolean canEntities() {
        return true;
    }

    @Override
    public boolean canSpawners() {
        return false;
    }

    @Override
    public void convertEntities() {
        EntityStackManager entityStackManager = plugin.getEntityStackManager();
        for (Map.Entry<UUID, Integer> entry : stackMob.getStorageManager().getAmountCache().entrySet()) {
            if (!entityStackManager.isStacked(entry.getKey())) {
                entityStackManager.addStack(entry.getKey(), entry.getValue());
                continue;
            }
            entityStackManager.getStack(entry.getKey()).setAmount(entry.getValue());
        }

    }

    @Override
    public void convertSpawners() {
    }

    @Override
    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(stackMob);
    }
}

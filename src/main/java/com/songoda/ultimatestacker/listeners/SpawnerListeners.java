package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.spawner.SpawnerStackManager;
import com.songoda.ultimatestacker.utils.ServerVersion;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpawnerListeners implements Listener {

    private final UltimateStacker plugin;

    public SpawnerListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent event) {
        if (!Setting.STACK_ENTITIES.getBoolean()
                || !plugin.spawnersEnabled()
                || plugin.getStackingTask().isWorldDisabled(event.getLocation().getWorld())) return;
        SpawnerStackManager spawnerStackManager = plugin.getSpawnerStackManager();
        if (!spawnerStackManager.isSpawner(event.getSpawner().getLocation())) return;

        SpawnerStack spawnerStack = spawnerStackManager.getSpawner(event.getSpawner().getLocation());

        EntityStack stack = plugin.getEntityStackManager().addStack(event.getEntity().getUniqueId(), spawnerStack.calculateSpawnCount());

        plugin.getStackingTask().attemptSplit(stack, (LivingEntity) event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PlayerInteractEventEgg(PlayerInteractEvent event) {
        if (!plugin.spawnersEnabled()
                || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;

        if (event.getItem() == null) return;

        Material type = event.getItem().getType();
        Block block = event.getClickedBlock();

        if (block == null || type == Material.AIR) return;

        if (block.getType() != (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))
                || !type.toString().contains(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? "SPAWN_EGG" : "MONSTER_EGG"))
            return;

        event.setCancelled(true);

        if (!Setting.EGGS_CONVERT_SPAWNERS.getBoolean()
                || !event.getPlayer().hasPermission("ultimatestacker.eggconvert")) {
            event.setCancelled(true);
        }
    }
}

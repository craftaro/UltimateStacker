package com.songoda.ultimatestacker.listeners;

import com.songoda.core.compatibility.LegacyMaterials;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class EntityListeners implements Listener {

    private final UltimateStacker plugin;

    public EntityListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        entity.setMetadata("US_REASON", new FixedMetadataValue(plugin, event.getSpawnReason().name()));

        if (event.getSpawnReason().name().equals("DROWNED")
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.LIGHTNING) {
            String name = event.getEntity().getCustomName();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                    () -> plugin.getEntityStackManager().addSerializedStack(entity, name), 1L);
        }

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEgg(ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().getType() != Material.EGG) return;

        Location location = event.getLocation();

        List<Entity> entities = new ArrayList<>(location.getWorld().getNearbyEntities(location, .1, .5, .1));

        if (entities.isEmpty()) return;

        Entity entity = entities.get(0);

        EntityStackManager stackManager = plugin.getEntityStackManager();

        if (!stackManager.isStacked(entity)) return;

        EntityStack stack = stackManager.getStack(entity);

        ItemStack item = event.getEntity().getItemStack();
        item.setAmount(Math.min((stack.getAmount() - 1) + item.getAmount(), item.getMaxStackSize()));
        event.getEntity().setItemStack(item);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlow(EntityExplodeEvent event) {
        if (!plugin.spawnersEnabled()) return;

        List<Block> destroyed = event.blockList();
        Iterator<Block> it = destroyed.iterator();
        List<Block> toCancel = new ArrayList<>();
        while (it.hasNext()) {
            Block block = it.next();
            if (block.getType() != LegacyMaterials.SPAWNER.getMaterial())
                continue;

            Location spawnLocation = block.getLocation();

            SpawnerStack spawner = plugin.getSpawnerStackManager().getSpawner(block);

            if (Settings.SPAWNERS_DONT_EXPLODE.getBoolean())
                toCancel.add(block);
            else {
                String chance = "";
                if (event.getEntity() instanceof Creeper)
                    chance = Settings.EXPLOSION_DROP_CHANCE_TNT.getString();
                else if (event.getEntity() instanceof TNTPrimed)
                    chance = Settings.EXPLOSION_DROP_CHANCE_CREEPER.getString();
                int ch = Integer.parseInt(chance.replace("%", ""));
                double rand = Math.random() * 100;
                if (rand - ch < 0 || ch == 100) {
                    CreatureSpawner cs = (CreatureSpawner) block.getState();
                    EntityType blockType = cs.getSpawnedType();
                    ItemStack item = Methods.getSpawnerItem(blockType, spawner.getAmount());
                    spawnLocation.getWorld().dropItemNaturally(spawnLocation.clone().add(.5, 0, .5), item);

                    SpawnerStack spawnerStack = plugin.getSpawnerStackManager().removeSpawner(spawnLocation);
                    plugin.getDataManager().deleteSpawner(spawnerStack);
                    plugin.updateHologram(spawner);
                }
            }

            Location nloc = spawnLocation.clone();
            nloc.add(.5, -.4, .5);
            List<Entity> near = (List<Entity>) nloc.getWorld().getNearbyEntities(nloc, 8, 8, 8);
            for (Entity ee : near) {
                if (ee.getLocation().getX() == nloc.getX() && ee.getLocation().getY() == nloc.getY() && ee.getLocation().getZ() == nloc.getZ()) {
                    ee.remove();
                }
            }

        }

        for (Block block : toCancel) {
            event.blockList().remove(block);
        }
    }

}

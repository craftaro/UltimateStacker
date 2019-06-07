package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.ServerVersion;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EntityListeners implements Listener {

    private final UltimateStacker instance;

    public EntityListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onEgg(ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().getType() != Material.EGG) return;

        Location location = event.getLocation();

        List<Entity> entities = new ArrayList<>(location.getWorld().getNearbyEntities(location, .1, .5, .1));

        if (entities.isEmpty()) return;

        Entity entity = entities.get(0);

        EntityStackManager stackManager = instance.getEntityStackManager();

        if (!stackManager.isStacked(entity)) return;

        EntityStack stack = stackManager.getStack(entity);

        ItemStack item = event.getEntity().getItemStack();
        item.setAmount((stack.getAmount() - 1) + item.getAmount() > item.getMaxStackSize() ? item.getMaxStackSize()
                : item.getAmount() + (stack.getAmount() - 1));
        event.getEntity().setItemStack(item);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlow(EntityExplodeEvent event) {
        List<Block> destroyed = event.blockList();
        Iterator<Block> it = destroyed.iterator();
        List<Block> toCancel = new ArrayList<>();
        while (it.hasNext()) {
            Block block = it.next();
            if (block.getType() != (instance.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")))
                continue;

            Location spawnLocation = block.getLocation();

            SpawnerStack spawner = instance.getSpawnerStackManager().getSpawner(block);

            if (Setting.SPAWNERS_DONT_EXPLODE.getBoolean())
                toCancel.add(block);
            else {
                String chance = "";
                if (event.getEntity() instanceof Creeper)
                    chance = Setting.EXPLOSION_DROP_CHANCE_TNT.getString();
                else if (event.getEntity() instanceof TNTPrimed)
                    chance = Setting.EXPLOSION_DROP_CHANCE_CREEPER.getString();
                int ch = Integer.parseInt(chance.replace("%", ""));
                double rand = Math.random() * 100;
                if (rand - ch < 0 || ch == 100) {
                    CreatureSpawner cs = (CreatureSpawner) block.getState();
                    EntityType blockType = cs.getSpawnedType();
                    ItemStack item = Methods.getSpawnerItem(blockType, spawner.getAmount());
                    spawnLocation.getWorld().dropItemNaturally(spawnLocation.clone().add(.5, 0, .5), item);

                    instance.getSpawnerStackManager().removeSpawner(spawnLocation);
                    if (instance.getHologram() != null)
                        instance.getHologram().remove(spawner);
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

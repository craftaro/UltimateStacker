package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EntityListeners implements Listener {

    private final UltimateStacker instance;

    public EntityListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlow(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper) && !(event.getEntity() instanceof TNTPrimed)) return;

        if (event.getEntity() instanceof Creeper) {
            Methods.onDeath((LivingEntity)event.getEntity(), new ArrayList<>(), 0);
        }

            List<Block> destroyed = event.blockList();
        for (Block block : destroyed) {

            if (block.getType() != Material.MOB_SPAWNER) continue;
            Location spawnerLocation = block.getLocation();

            SpawnerStack stack = instance.getSpawnerStackManager().getSpawner(spawnerLocation);

            ItemStack item = Methods.getSpawnerItem(((CreatureSpawner) block.getState()).getSpawnedType(), stack.getAmount());
            spawnerLocation.getWorld().dropItemNaturally(spawnerLocation.clone().add(.5, 0, .5), item);

            instance.getHologram().remove(stack);
        }
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

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        event.getItem().setItemStack(instance.getStackingTask().setMax(event.getItem().getItemStack(), 0, true));

        ItemStack item = event.getItem().getItemStack();

        int amt = item.getAmount();
        int max = item.getMaxStackSize();

        if (amt <= max) return;

        event.setCancelled(true);

        item.setAmount(max);
        amt = amt - max;

        while (amt > max) {
            ItemStack newItem = new ItemStack(item);
            newItem.setAmount(max);

            event.getItem().getWorld().dropItemNaturally(event.getItem().getLocation(), newItem);
            amt = amt - max;
        }

        ItemStack newItem = new ItemStack(item);
        newItem.setAmount(amt);

        event.getItem().getWorld().dropItemNaturally(event.getItem().getLocation(), newItem);
    }

}

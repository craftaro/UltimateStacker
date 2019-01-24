package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
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
    public void onPickup(PlayerPickupItemEvent event) {
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

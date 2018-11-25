package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class EntityListeners implements Listener {

    private final UltimateStacker instance;

    public EntityListeners(UltimateStacker instance) {
        this.instance = instance;
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

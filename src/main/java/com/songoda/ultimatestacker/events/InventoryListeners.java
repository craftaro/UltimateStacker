package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListeners implements Listener {

    private final UltimateStacker instance;

    public InventoryListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onMove(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (new ItemStack(item.getType()).getMaxStackSize() == item.getMaxStackSize()) return;

        instance.getStackingTask().setMax(item, 0, true);
        int amt = item.getAmount();
        int max = item.getMaxStackSize();

        if (amt <= max) return;

        item.setAmount(max);
        amt = amt - max;

        while (amt > max) {
            ItemStack newItem = new ItemStack(item);
            newItem.setAmount(max);

            event.getDestination().addItem(newItem);
            amt = amt - max;
        }

        ItemStack newItem = new ItemStack(item);
        newItem.setAmount(amt);

        event.getDestination().addItem(newItem);
    }

    @EventHandler
    public void onAccept(InventoryPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (new ItemStack(item.getType()).getMaxStackSize() == item.getMaxStackSize()) return;

        instance.getStackingTask().setMax(item, 0, true);
        int amt = item.getAmount();
        int max = item.getMaxStackSize();

        if (amt <= max) return;

        item.setAmount(max);
        amt = amt - max;

        while (amt > max) {
            ItemStack newItem = new ItemStack(item);
            newItem.setAmount(max);

            event.getInventory().addItem(newItem);
            amt = amt - max;
        }

        ItemStack newItem = new ItemStack(item);
        newItem.setAmount(amt);

        event.getInventory().addItem(newItem);
    }

}

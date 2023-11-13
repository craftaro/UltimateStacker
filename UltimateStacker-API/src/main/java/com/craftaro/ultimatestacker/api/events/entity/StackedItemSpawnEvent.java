package com.craftaro.ultimatestacker.api.events.entity;

import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Called before we spawn a stacked items
 */
public class StackedItemSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final ItemStack item;
    private final long amount;
    private boolean cancelled = false;

    public StackedItemSpawnEvent(ItemStack item, long amount) {
        this.item = item;
        this.amount = amount;
    }

    /**
     * Get the item that will be spawned
     *
     * @return ItemStack
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Get the amount of items that will be spawned
     *
     * @return amount
     */
    public long getAmount() {
        return amount;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = true;
    }
}

package com.craftaro.ultimatestacker.api.events.entity;

import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called before we spawn a stacked items
 */
public class StackedItemSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Item item;
    private ItemStack itemStack;
    private long amount;
    private boolean cancelled = false;

    /**
     * Constructor
     *
     * @param item   Item that will be modified. Can be null if we spawn a new one
     * @param itemStack ItemStack that will be spawned
     * @param amount amount
     */
    public StackedItemSpawnEvent(@Nullable Item item, @NotNull ItemStack itemStack, long amount) {
        this.item = item;
        this.itemStack = itemStack;
        this.amount = amount;
    }

    /**
     * Get the item that will be modified
     *
     * @return Item
     */
    public @Nullable Item getItem() {
        return item;
    }

    /**
     * Set the item that will be spawned
     *
     * @param item Item
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * Get the ItemStack that will be spawned
     *
     * @return ItemStack
     */
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Set the item that will be spawned
     *
     * @param itemStack ItemStack
     */
    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null) throw new IllegalArgumentException("ItemStack cannot be null");
        this.itemStack = itemStack;
    }

    /**
     * Get the amount of items that will be spawned
     *
     * @return amount
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Set the amount of items that will be spawned
     *
     * @param amount amount
     */
    public void setAmount(long amount) {
        this.amount = amount;
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

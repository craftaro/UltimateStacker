package com.craftaro.ultimatestacker.api.stack.item;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface StackedItemManager {

    /**
     * Get the StackedItem for the given Item
     * @param item The Item to get the stack for
     * @return The StackedItem for the given Item or null if not stacked
     */
    @Nullable StackedItem getStackedItem(Item item);

    /**
     * Get the StackedItem for the given Item
     * @param item The Item to get the stack for
     * @param create If true, will create a new stack if one does not exist
     * @return The StackedItem for the given Item or null if not stacked
     */
    @NotNull StackedItem getStackedItem(Item item, boolean create);

    /**
     * Create a new StackedItem for the given item
     * @param item The ItemStack to create the stack for
     * @param amount The amount of items in the stack
     * @param location The location to spawn the stack
     * @return The StackedItem for the given Item or null if it could not be created
     */
    @Nullable StackedItem createStack(ItemStack item, Location location, int amount);

    /**
     * Create a new StackedItem for the given item
     * @param item The item to create the stack for
     * @param amount The amount of items in the stack
     * @return The StackedItem for the given Item or null if it could not be created
     */
    @Nullable StackedItem createStack(Item item, int amount);

    /**
     * Get the actual amount of the given item
     * @param item item to check
     * @return The amount of items in the stacked item or vanilla stack size if not stacked
     */
    int getActualItemAmount(Item item);

    /**
     * Returns true if the given item is stacked
     *
     * @param item item to check
     * @return true if the given item is stacked
     */
    boolean isStackedItem(Item item);

    /**
     * Merge two items together if they are the same type
     * @param from first item
     * @param to second item
     * @param ignoreRestrictions ignore ignoreRestrictions such as max stack size, or blacklist
     * @return The merged item or null if they merge was unsuccessful
     */
    StackedItem merge(Item from, Item to, boolean ignoreRestrictions);

    /**
     * Merge two items together if they are the same type
     * @param from first item
     * @param to second item
     * @param ignoreRestrictions ignore ignoreRestrictions such as max stack size, or blacklist
     * @param callback callback to be called when the merge is successful see {@link  ItemMergeCallback#accept(Item, Item, StackedItem)}
     * @return The merged item or null if they merge was unsuccessful
     */
    StackedItem merge(Item from, Item to, boolean ignoreRestrictions, ItemMergeCallback<Item, Item, StackedItem> callback);

    /**
     * Check to see if this material is not permitted to stack
     *
     * @param item Item material to check
     * @return true if this material will not stack
     */
    boolean isMaterialBlacklisted(ItemStack item);

    /**
     * Check to see if this material is not permitted to stack
     *
     * @param type Material to check
     * @return true if this material will not stack
     */
    boolean isMaterialBlacklisted(String type);

    /**
     * Check to see if this material is not permitted to stack
     *
     * @param type Material to check
     * @return true if this material will not stack
     */
    boolean isMaterialBlacklisted(Material type);

    /**
     * Check to see if this material is not permitted to stack
     *
     * @param type Material to check
     * @param data data value for this item (for 1.12 and older servers)
     * @return true if this material will not stack
     */
    boolean isMaterialBlacklisted(Material type, byte data);
}

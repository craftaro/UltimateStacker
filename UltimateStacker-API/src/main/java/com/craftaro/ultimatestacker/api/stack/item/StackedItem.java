package com.craftaro.ultimatestacker.api.stack.item;

import com.craftaro.ultimatestacker.api.utils.Stackable;
import org.bukkit.entity.Item;

public interface StackedItem extends Stackable {

    /**
     * Get the Item entity for this StackedItem
     * @return The Item entity for this StackedItem
     */
    Item getItem();

    /**
     * Removes the custom amount from the item
     * while not destroying the actual item
     */
    Item destroy();

}

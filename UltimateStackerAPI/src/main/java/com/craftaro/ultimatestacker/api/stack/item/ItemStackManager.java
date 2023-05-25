package com.craftaro.ultimatestacker.api.stack.item;

import org.bukkit.entity.Item;

public interface ItemStackManager {

    ItemStack getItem(Item item);

    ItemStack createStack(Item item, int amount);

}

package com.craftaro.ultimatestacker.stackable.item;

import com.craftaro.ultimatestacker.api.stack.item.ItemStack;
import com.songoda.core.compatibility.CompatibleMaterial;
import org.bukkit.entity.Item;

public class ItemStackImpl implements ItemStack {

    private CompatibleMaterial material;
    private Item item;
    private int amount;

    public ItemStackImpl() {
    }

    @Override
    public int getAmount() {
        return 0;
    }

    @Override
    public void setAmount(int amount) {

    }

    @Override
    public void add(int amount) {

    }

    @Override
    public void take(int amount) {

    }
}

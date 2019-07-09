package com.songoda.ultimatestacker.lootables;

import org.bukkit.inventory.ItemStack;

public class Drop {

    private ItemStack itemStack;

    private String command;

    public Drop(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public Drop(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}

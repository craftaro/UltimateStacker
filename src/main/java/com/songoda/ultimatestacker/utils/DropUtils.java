package com.songoda.ultimatestacker.utils;

import com.songoda.lootables.loot.Drop;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DropUtils {

    public static void processStackedDrop(LivingEntity entity, List<Drop> drops, EntityDeathEvent event) {
        List<ItemStack> items = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        for (Drop drop : drops) {
            if (drop == null) continue;

            ItemStack droppedItem = drop.getItemStack();
            if (droppedItem != null) {
                boolean success = false;
                for (ItemStack item : items) {
                    if (item.getType() != droppedItem.getType()
                            || item.getDurability() != droppedItem.getDurability()
                            || item.getAmount() + droppedItem.getAmount() > droppedItem.getMaxStackSize()) continue;
                    item.setAmount(item.getAmount() + droppedItem.getAmount());
                    success = true;
                    break;
                }
                if (!success)
                    items.add(droppedItem);
            }
            if (drop.getCommand() != null)
                commands.add(drop.getCommand());
        }
        if (!items.isEmpty())
            dropItems(items, event);
        else if (!commands.isEmpty())
            runCommands(entity, commands);
    }

    private static void dropItems(List<ItemStack> items, EntityDeathEvent event) {
        event.getDrops().clear();
        for (ItemStack item : items)
            event.getDrops().add(item);
    }

    private static void runCommands(LivingEntity entity, List<String> commands) {
        for (String command : commands) {
            if (entity.getKiller() != null)
                command = command.replace("%player%", entity.getKiller().getName());
            if (!command.contains("%player%"))
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}

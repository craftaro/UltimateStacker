package com.songoda.ultimatestacker.command.commands;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.command.AbstractCommand;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CommandRemoveAll extends AbstractCommand {

    public CommandRemoveAll(AbstractCommand parent) {
        super(parent, false, "removeall");
    }

    @Override
    protected ReturnType runCommand(UltimateStacker instance, CommandSender sender, String... args) {
        if (args.length != 2) return ReturnType.SYNTAX_ERROR;

        String type = args[1];

        if (!type.equalsIgnoreCase("entities")
                && !type.equalsIgnoreCase("items")) {
            return ReturnType.SYNTAX_ERROR;
        }

        int amountRemoved = 0;
        EntityStackManager stackManager = instance.getEntityStackManager();
        for (World world : Bukkit.getWorlds()) {

            for (Entity entityO : world.getEntities()) {
                if (entityO instanceof Player) continue;

                    if (entityO.getType() != EntityType.DROPPED_ITEM && stackManager.isStacked(entityO) && type.equalsIgnoreCase("entities")) {
                        entityO.remove();
                        amountRemoved ++;
                    } else if (entityO.getType() == EntityType.DROPPED_ITEM && type.equalsIgnoreCase("items")) {
                        ItemStack item = ((Item) entityO).getItemStack();
                        if (entityO.isCustomNameVisible() && !entityO.getCustomName().contains(Methods.convertToInvisibleString("IS")) || item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                        continue;
                        entityO.remove();
                        amountRemoved ++;
                    }

            }
        }

        if (type.equalsIgnoreCase("entities") && amountRemoved == 1) type = "Entity";
        if (type.equalsIgnoreCase("items") && amountRemoved == 1) type = "Item";

        if (amountRemoved == 0) {
            instance.getLocale().newMessage("&7No stacked " + type + " exist that could be removed.").sendPrefixedMessage(sender);
        } else {
            instance.getLocale().newMessage("&7Removed &6" + amountRemoved + " stacked " + Methods.formatText(type.toLowerCase(), true) + " &7Successfully.").sendPrefixedMessage(sender);
        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateStacker instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimatestacker.admin";
    }

    @Override
    public String getSyntax() {
        return "/us removeall <entities/items>";
    }

    @Override
    public String getDescription() {
        return "Remove all stacked entites or items from the world.";
    }
}

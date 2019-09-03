package com.songoda.ultimatestacker.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandRemoveAll extends AbstractCommand {

    UltimateStacker instance;

    public CommandRemoveAll() {
        super(false, "removeall");
        instance = UltimateStacker.getInstance();
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 1) return ReturnType.SYNTAX_ERROR;

        String type = args[0];

        boolean all = args.length == 2;

        if (!type.equalsIgnoreCase("entities")
                && !type.equalsIgnoreCase("items")) {
            return ReturnType.SYNTAX_ERROR;
        }

        int amountRemoved = 0;
        EntityStackManager stackManager = instance.getEntityStackManager();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entityO : world.getEntities()) {
                if (entityO instanceof Player) continue;

                if (entityO.getType() != EntityType.DROPPED_ITEM && (stackManager.isStacked(entityO) || all) && type.equalsIgnoreCase("entities")) {
                    entityO.remove();
                    amountRemoved++;
                } else if (entityO.getType() == EntityType.DROPPED_ITEM && type.equalsIgnoreCase("items")) {
                    if (entityO.isCustomNameVisible() && !entityO.getCustomName().contains(TextUtils.convertToInvisibleString("IS")) || all)
                        continue;
                    entityO.remove();
                    amountRemoved++;
                }
            }
        }

        if (type.equalsIgnoreCase("entities") && amountRemoved == 1) type = "Entity";
        if (type.equalsIgnoreCase("items") && amountRemoved == 1) type = "Item";

        if (amountRemoved == 0) {
            instance.getLocale().newMessage("&7No" + (all ? " " : " stacked ")
                    + type + " exist that could be removed.").sendPrefixedMessage(sender);
        } else {
            instance.getLocale().newMessage("&7Removed &6" + amountRemoved + (all ? " " : " stacked ")
                    + Methods.formatText(type.toLowerCase(), true) + " &7Successfully.").sendPrefixedMessage(sender);
        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1)
            return Arrays.asList("entities", "items");
        else if (args.length == 2)
            return Collections.singletonList("all");
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimatestacker.admin";
    }

    @Override
    public String getSyntax() {
        return "/us removeall <entities/items> [all]";
    }

    @Override
    public String getDescription() {
        return "Remove all stacked entites or items from the world.";
    }

}

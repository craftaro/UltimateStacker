package com.songoda.ultimatestacker.command.commands;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.command.AbstractCommand;
import com.songoda.ultimatestacker.gui.GUIConvert;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandConvert extends AbstractCommand {

    public CommandConvert(AbstractCommand parent) {
        super(parent, true, "convert");
    }

    @Override
    protected ReturnType runCommand(UltimateStacker instance, CommandSender sender, String... args) {
        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")
                || Bukkit.getPluginManager().isPluginEnabled("StackMob")) {
            new GUIConvert(instance, (Player) sender);
        } else {
            sender.sendMessage(Methods.formatText("&cYou need to have the plugin &4WildStacker &cor &4StackMob &cenabled " +
                    "in order to convert data."));
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
        return "/us convert";
    }

    @Override
    public String getDescription() {
        return "Allows you to convert data from other stacking plugins.";
    }
}

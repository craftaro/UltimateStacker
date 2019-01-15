package com.songoda.ultimatestacker.command.commands;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.command.AbstractCommand;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.command.CommandSender;

public class CommandUltimateStacker extends AbstractCommand {

    public CommandUltimateStacker() {
        super("UltimateStacker", null, false);
    }

    @Override
    protected ReturnType runCommand(UltimateStacker instance, CommandSender sender, String... args) {
        sender.sendMessage("");
        sender.sendMessage(Methods.formatText(instance.getReferences().getPrefix() + "&7Version " + instance.getDescription().getVersion() + " Created with <3 by &5&l&oSongoda"));

        for (AbstractCommand command : instance.getCommandManager().getCommands()) {
            if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
                sender.sendMessage(Methods.formatText("&8 - &a" + command.getSyntax() + "&7 - " + command.getDescription()));
            }
        }
        sender.sendMessage("");

        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/UltimateStacker";
    }

    @Override
    public String getDescription() {
        return "Displays this page.";
    }
}

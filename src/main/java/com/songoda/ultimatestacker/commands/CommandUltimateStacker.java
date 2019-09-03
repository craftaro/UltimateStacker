package com.songoda.ultimatestacker.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandUltimateStacker extends AbstractCommand {

    UltimateStacker instance;

    public CommandUltimateStacker() {
        super(false, "UltimateStacker");
        instance = UltimateStacker.getInstance();
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        sender.sendMessage("");
        instance.getLocale().newMessage("&7Version " + instance.getDescription().getVersion()
                + " Created with <3 by &5&l&oSongoda").sendPrefixedMessage(sender);

        for (AbstractCommand command : instance.getCommandManager().getAllCommands()) {
            if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
                sender.sendMessage(Methods.formatText("&8 - &a" + command.getSyntax() + "&7 - " + command.getDescription()));
            }
        }
        sender.sendMessage("");

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender cs, String... strings) {
        return null;
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

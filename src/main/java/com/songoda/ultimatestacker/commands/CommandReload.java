package com.songoda.ultimatestacker.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReload extends AbstractCommand {

    UltimateStacker instance;

    public CommandReload() {
        super(false, "reload");
        instance = UltimateStacker.getInstance();
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        instance.reloadConfig();
        instance.getLocale().getMessage("&7Configuration and Language files reloaded.").sendPrefixedMessage(sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimatestacker.admin";
    }

    @Override
    public String getSyntax() {
        return "/us reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }

}

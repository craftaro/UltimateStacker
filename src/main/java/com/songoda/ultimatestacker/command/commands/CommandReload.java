package com.songoda.ultimatestacker.command.commands;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.command.AbstractCommand;
import org.bukkit.command.CommandSender;

public class CommandReload extends AbstractCommand {

    public CommandReload(AbstractCommand parent) {
        super("reload", parent, false);
    }

    @Override
    protected ReturnType runCommand(UltimateStacker instance, CommandSender sender, String... args) {
        instance.reload();
        sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7Configuration and Language files reloaded."));
        return ReturnType.SUCCESS;
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

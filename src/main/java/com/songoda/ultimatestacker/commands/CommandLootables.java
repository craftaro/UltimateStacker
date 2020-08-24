package com.songoda.ultimatestacker.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.lootables.gui.GuiEditor;
import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandLootables extends AbstractCommand {

    UltimateStacker instance;

    public CommandLootables() {
        super(true, "lootables");
        instance = UltimateStacker.getInstance();
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player p = (Player) sender;
        instance.getGuiManager().showGUI(p, new GuiEditor(instance.getLootablesManager().getLootManager()));
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
        return "lootables";
    }

    @Override
    public String getDescription() {
        return "Modify the drop tables.";
    }
}

package com.songoda.ultimatestacker.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.gui.GuiManager;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.gui.GUIConvert;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandConvert extends AbstractCommand {

    private final UltimateStacker plugin;
    private final GuiManager guiManager;

    public CommandConvert(UltimateStacker plugin, GuiManager guiManager) {
        super(CommandType.PLAYER_ONLY, "convert");
        this.guiManager = guiManager;
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")
                || Bukkit.getPluginManager().isPluginEnabled("StackMob")) {
            guiManager.showGUI((Player) sender, new GUIConvert());
        } else {
            sender.sendMessage(Methods.formatText("&cYou need to have the plugin &4WildStacker &cor &4StackMob &cenabled " +
                    "in order to convert data."));
        }
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
        return "convert";
    }

    @Override
    public String getDescription() {
        return "Allows you to convert data from other stacking plugins.";
    }
}

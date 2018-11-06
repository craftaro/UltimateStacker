package com.songoda.ultimatestacker.command.commands;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.command.AbstractCommand;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandGive extends AbstractCommand {

    public CommandGive(AbstractCommand abstractCommand) {
        super("givespawner", abstractCommand, false);
    }

    @Override
    protected ReturnType runCommand(UltimateStacker instance, CommandSender sender, String... args) {
        if (args.length != 4) return ReturnType.SYNTAX_ERROR;

        if (Bukkit.getPlayer(args[1]) == null && !args[1].trim().toLowerCase().equals("all")) {
            sender.sendMessage("Not a player...");
            return ReturnType.SYNTAX_ERROR;
        }

        EntityType type = null;
        for (EntityType types : EntityType.values()) {
            String input = args[2].toUpperCase().replace("_", "").replace(" ", "");
            String compare =  types.name().toUpperCase().replace("_", "").replace(" ", "");
            if (input.equals(compare))
                type = types;
        }

        if (type == null) {
            sender.sendMessage(instance.getReferences().getPrefix() + TextComponent.formatText(instance.getReferences().getPrefix() + "&7The entity Type &6" + args[2] + " &7does not exist. Try one of these:"));
            StringBuilder list = new StringBuilder();

            for (EntityType types : EntityType.values()) {
                list.append(types.name().toUpperCase().replace(" ", "_")).append("&7, &6");
            }
            sender.sendMessage(TextComponent.formatText("&6" + list));
        } else {

            int amt = Integer.parseInt(args[3]);
            ItemStack itemStack = Methods.getSpawnerItem(type, amt);
            if (!args[1].trim().toLowerCase().equals("all")) {
                Player player = Bukkit.getOfflinePlayer(args[1]).getPlayer();
                player.getInventory().addItem(itemStack);
                player.sendMessage(TextComponent.formatText(instance.getLocale().getMessage("command.give.success", Methods.compileSpawnerName(type, amt))));
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.getInventory().addItem(itemStack);
                    player.sendMessage(TextComponent.formatText(instance.getLocale().getMessage("command.give.success", Methods.compileSpawnerName(type, amt))));
                }
            }
        }
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "ultimatestacker.admin";
    }

    @Override
    public String getSyntax() {
        return "/us givespawner <player/all> <type> <level>";
    }

    @Override
    public String getDescription() {
        return "Gives an operator the ability to spawn a spawner of his or her choice.";
    }
}

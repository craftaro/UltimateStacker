package com.songoda.ultimatestacker.command.commands;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.command.AbstractCommand;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandGiveSpawner extends AbstractCommand {

    public CommandGiveSpawner(AbstractCommand abstractCommand) {
        super(abstractCommand, false, "givespawner");
    }

    @Override
    protected ReturnType runCommand(UltimateStacker instance, CommandSender sender, String... args) {
        if (args.length < 3) return ReturnType.SYNTAX_ERROR;

        if (Bukkit.getPlayer(args[1]) == null && !args[1].trim().toLowerCase().equals("all")) {
            sender.sendMessage("Not a player...");
            return ReturnType.SYNTAX_ERROR;
        }

        EntityType type = null;
        for (EntityType types : EntityType.values()) {
            String input = args[2].toUpperCase().replace("_", "").replace(" ", "");
            String compare = types.name().toUpperCase().replace("_", "").replace(" ", "");
            if (input.equals(compare))
                type = types;
        }

        if (type == null) {
            instance.getLocale().newMessage("&7The entity StackType &6" + args[2] + " &7does not exist. Try one of these:").sendPrefixedMessage(sender);
            StringBuilder list = new StringBuilder();

            for (EntityType types : EntityType.values()) {
                if (types.isSpawnable() && types.isAlive() && !types.toString().contains("ARMOR"))
                    list.append(types.name().toUpperCase().replace(" ", "_")).append("&7, &6");
            }
            sender.sendMessage(Methods.formatText("&6" + list));
        } else {

            int amt = args.length == 4 ? Integer.parseInt(args[3]) : 1;
            ItemStack itemStack = Methods.getSpawnerItem(type, amt);
            if (!args[1].trim().toLowerCase().equals("all")) {
                Player player = Bukkit.getOfflinePlayer(args[1]).getPlayer();
                player.getInventory().addItem(itemStack);
                instance.getLocale().getMessage("command.give.success")
                        .processPlaceholder("type", Methods.compileSpawnerName(type, amt))
                        .sendPrefixedMessage(player);
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.getInventory().addItem(itemStack);
                    instance.getLocale().getMessage("command.give.success")
                            .processPlaceholder("type", Methods.compileSpawnerName(type, amt))
                            .sendPrefixedMessage(player);
                }
            }
        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateStacker instance, CommandSender sender, String... args) {
        if (args.length == 2) {
            List<String> players = new ArrayList<>();
            players.add("all");
            players.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            return players;
        } else if (args.length == 3) {
            return Arrays.stream(EntityType.values())
                    .filter(types -> types.isSpawnable() && types.isAlive() && !types.toString().contains("ARMOR"))
                    .map(Enum::name).collect(Collectors.toList());
        } else if (args.length == 4) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimatestacker.admin";
    }

    @Override
    public String getSyntax() {
        return "/us givespawner <player/all> <type> [size]";
    }

    @Override
    public String getDescription() {
        return "Gives an operator the ability to spawn a spawner of his or her choice.";
    }
}

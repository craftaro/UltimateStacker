package com.songoda.ultimatestacker.gui;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.convert.StackMobConvert;
import com.songoda.ultimatestacker.convert.WildStackerConvert;
import com.songoda.ultimatestacker.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GUIConvert extends AbstractGUI {

    private final UltimateStacker plugin;

    public GUIConvert(UltimateStacker plugin, Player player) {
        super(player);
        this.plugin = plugin;

        init("Convert", 9);
    }

    @Override
    public void constructGUI() {
        int current = 0;
        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
            createButton(current, Material.STONE, "&6WildStacker");

            registerClickable(current, ((player1, inventory1, cursor, slot, type) ->
                    new GUIConvertWhat(plugin, player, new WildStackerConvert(plugin))));
            current ++;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("StackMob")) {
            createButton(current, Material.STONE, "&6StackMob");

            registerClickable(current, ((player1, inventory1, cursor, slot, type) ->
                    new GUIConvertWhat(plugin, player, new StackMobConvert(plugin))));
        }

    }

    @Override
    protected void registerClickables() {

    }

    @Override
    protected void registerOnCloses() {

    }
}

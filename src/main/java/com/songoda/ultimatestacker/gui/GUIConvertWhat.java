package com.songoda.ultimatestacker.gui;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.convert.Convert;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GUIConvertWhat extends AbstractGUI {

    private final UltimateStacker plugin;

    private Convert convertFrom = null;

    private boolean entities = true;
    private boolean spawners = true;

    public GUIConvertWhat(UltimateStacker plugin, Player player, Convert convertFrom) {
        super(player);
        this.plugin = plugin;
        this.convertFrom = convertFrom;

        init("What Do You Want To Convert?", 9);
    }

    @Override
    public void constructGUI() {
        inventory.clear();

        if (convertFrom.canEntities())
            createButton(0, Material.STONE, "&7Stacked Entities", entities ? "&aYes" : "&cNo");

        if (convertFrom.canSpawners())
            createButton(1, Material.STONE, "&7Stacked Spawners", spawners ? "&aYes" : "&cNo");

        createButton(8, Material.STONE, "&aRun");

    }

    @Override
    protected void registerClickables() {
        if (convertFrom.canSpawners()) {
            registerClickable(0, ((player1, inventory1, cursor, slot, type) -> {
                entities = !entities;
                constructGUI();
            }));
        }

        if (convertFrom.canSpawners()) {
            registerClickable(1, ((player1, inventory1, cursor, slot, type) -> {
                spawners = !spawners;
                constructGUI();
            }));
        }

        registerClickable(8, ((player1, inventory1, cursor, slot, type) -> {
            if (entities)
                convertFrom.convertEntities();
            if (spawners)
                convertFrom.convertSpawners();

            convertFrom.disablePlugin();

            player.closeInventory();
            player.sendMessage(Methods.formatText("&7Data converted successfully. Remove &6" + convertFrom.getName() + " &7and restart your server to continue."));
        }));

    }

    @Override
    protected void registerOnCloses() {

    }
}

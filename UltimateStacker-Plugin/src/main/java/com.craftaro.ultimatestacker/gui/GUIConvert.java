package com.craftaro.ultimatestacker.gui;

import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatestacker.convert.StackMobConvert;
import com.craftaro.ultimatestacker.convert.WildStackerConvert;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class GUIConvert extends Gui {

    public GUIConvert() {
        setTitle("Convert");
        setRows(1);
        int current = 0;
        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
            this.setButton(current++, GuiUtils.createButtonItem(XMaterial.STONE, ChatColor.GRAY + "WildStacker"),
                    (event) -> event.manager.showGUI(event.player, new GUIConvertWhat(new WildStackerConvert(), this)));
        }

        if (Bukkit.getPluginManager().isPluginEnabled("StackMob")) {
            this.setButton(current++, GuiUtils.createButtonItem(XMaterial.STONE, ChatColor.GRAY + "StackMob"),
                    (event) -> event.manager.showGUI(event.player, new GUIConvertWhat(new StackMobConvert(), this)));
        }

    }

}

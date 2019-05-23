package com.songoda.epicheads.utils.settings;

import com.songoda.epicheads.EpicHeads;
import com.songoda.epicheads.utils.ServerVersion;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Setting {

    AUTOSAVE("Main.Auto Save Interval In Seconds", 15,
            "The amount of time in between saving to file.",
            "This is purely a safety function to prevent against unplanned crashes or",
            "restarts. With that said it is advised to keep this enabled.",
            "If however you enjoy living on the edge, feel free to turn it off."),

    DISCORD("Main.Show Discord Button", true,
            "This is the discord button displayed in the main GUI",
            "Clicking this button will bring you to a discord where you can",
            "add or remove heads to the global library this plugin uses.",
            "AS well as get updates on future releases and features."),

    FREE_IN_CREATIVE("Main.Heads Free In Creative Mode", false,
            "Enabling this will make it so that a player can get all heads",
            "for free as long as they are in the creative game mode."),

    DROP_MOB_HEADS("Main.Drop Mob Heads", true,
            "Should heads drop after a monster is killed?"),

    DROP_PLAYER_HEADS("Main.Drop Player Heads", true,
            "Should a players drop their head on death?"),

    DROP_CHANCE("Main.Head Drop Chance", "25%",
            "When a player or monster is killed what should be",
            "the chance that their head drops?"),

    DISABLED_HEADS("Main.Disabled Global Heads", Arrays.asList(34567, 34568, 34569),
            "These are head ID's from the global database that are disabled.",
            "By default this is filled with non existent ID's."),

    GLASS_TYPE_1("Interfaces.Glass Type 1", 7),
    GLASS_TYPE_2("Interfaces.Glass Type 2", 11),
    GLASS_TYPE_3("Interfaces.Glass Type 3", 3),

    HEAD_COST("Economy.Head Cost", 24.99,
            "The cost the of the head. If you wan't to use PlayerPoints",
            "or item tokens you need to use whole numbers."),

    VAULT_ECONOMY("Economy.Use Vault Economy", true,
            "Should Vault be used?"),

    PLAYER_POINTS_ECONOMY("Economy.Use Player Points Economy", false,
            "Should PlayerPoints be used?"),

    ITEM_ECONOMY("Economy.Use Item Economy", false,
            "Should item tokens be used?"),

    ITEM_TOKEN_TYPE("Economy.Item.Type", EpicHeads.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ? "PLAYER_HEAD" : "SKULL_ITEM",
            "Which item material type should be used?",
            "You can use any of the materials from the following link:",
            "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html"),

    ITEM_TOKEN_ID("Economy.Item.Head ID", 14395,
            "If a player head is used as the token which head ID should be used?",
            "This can be any head from the global database."),

    ITEM_TOKEN_NAME("Economy.Item.Name", "&6Player Head Token",
            "What should the token be named?"),

    ITEM_TOKEN_LORE("Economy.Item.Lore", Arrays.asList("&8Use in /Heads!"),
            "What should the tokens lore be?"),

    LANGUGE_MODE("System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    private String setting;
    private Object option;
    private String[] comments;

    Setting(String setting, Object option, String... comments) {
        this.setting = setting;
        this.option = option;
        this.comments = comments;
    }

    Setting(String setting, Object option) {
        this.setting = setting;
        this.option = option;
        this.comments = null;
    }

    public static Setting getSetting(String setting) {
        List<Setting> settings = Arrays.stream(values()).filter(setting1 -> setting1.setting.equals(setting)).collect(Collectors.toList());
        if (settings.isEmpty()) return null;
        return settings.get(0);
    }

    public String getSetting() {
        return setting;
    }

    public Object getOption() {
        return option;
    }

    public String[] getComments() {
        return comments;
    }

    public List<Integer> getIntegerList() {
        return EpicHeads.getInstance().getConfig().getIntegerList(setting);
    }

    public List<String> getStringList() {
        return EpicHeads.getInstance().getConfig().getStringList(setting);
    }

    public boolean getBoolean() {
        return EpicHeads.getInstance().getConfig().getBoolean(setting);
    }

    public int getInt() {
        return EpicHeads.getInstance().getConfig().getInt(setting);
    }

    public long getLong() {
        return EpicHeads.getInstance().getConfig().getLong(setting);
    }

    public String getString() {
        return EpicHeads.getInstance().getConfig().getString(setting);
    }

    public char getChar() {
        return EpicHeads.getInstance().getConfig().getString(setting).charAt(0);
    }

    public double getDouble() {
        return EpicHeads.getInstance().getConfig().getDouble(setting);
    }
}
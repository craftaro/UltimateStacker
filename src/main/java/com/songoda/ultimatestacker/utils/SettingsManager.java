package com.songoda.ultimatestacker.utils;

import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.event.Listener;

/**
 * Created by songo on 6/4/2017.
 */
public class SettingsManager implements Listener {

    private final UltimateStacker instance;

    public SettingsManager(UltimateStacker instance) {
        this.instance = instance;
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    public void updateSettings() {
        for (settings s : settings.values()) {
            instance.getConfig().addDefault(s.setting, s.option);
        }
    }

    public enum settings {
        o1("Main.Stack Items", true),
        o2("Main.Stack Entities", true),
        o22("Main.Stack Spawners", true),
        o3("Main.Stack Search Tick Speed", 5),
        o4("Entity.Max Stack Size", 15),
        oo("Entity.Min Stack Amount", 5),
        o5("Entity.Kill Whole Stack On Death", false),
        NAME_FORMAT_ENTITY("Entity.Name Format", "&f{TYPE} &6{AMT}x"),
        o6("Item.Max Stack Size", 250),
        NAME_FORMAT_ITEM("Item.Name Format", "&f{TYPE} &6{AMT}x"),
        o7("Spawners.Holograms Enabled", true),
        o8("Spawners.Max Stack Size", 5),
        NAME_FORMAT_SPAWNER("Spawners.Name Format", "&f{TYPE} Spawner &6{AMT}x"),

        DATABASE_SUPPORT("Database.Activate Mysql Support", false),
        DATABASE_IP("Database.IP", "127.0.0.1"),
        DATABASE_PORT("Database.Port", 3306),
        DATABASE_NAME("Database.Database Name", "UltimateStacker"),
        DATABASE_PREFIX("Database.Prefix", "US-"),
        DATABASE_USERNAME("Database.Username", "PUT_USERNAME_HERE"),
        DATABASE_PASSWORD("Database.Password", "PUT_PASSWORD_HERE"),

        DOWNLOAD_FILES("System.Download Needed Data Files", true),
        LANGUGE_MODE("System.Language Mode", "en_US");

        private String setting;
        private Object option;

        settings(String setting, Object option) {
            this.setting = setting;
            this.option = option;
        }

    }
}
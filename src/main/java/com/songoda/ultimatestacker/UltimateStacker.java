package com.songoda.ultimatestacker;

import com.songoda.ultimatestacker.command.CommandManager;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.listeners.*;
import com.songoda.ultimatestacker.hologram.Hologram;
import com.songoda.ultimatestacker.hologram.HologramHolographicDisplays;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.spawner.SpawnerStackManager;
import com.songoda.ultimatestacker.storage.Storage;
import com.songoda.ultimatestacker.storage.StorageRow;
import com.songoda.ultimatestacker.storage.types.StorageMysql;
import com.songoda.ultimatestacker.storage.types.StorageYaml;
import com.songoda.ultimatestacker.tasks.StackingTask;
import com.songoda.ultimatestacker.utils.*;
import com.songoda.ultimatestacker.utils.settings.SettingsManager;
import org.apache.commons.lang.ArrayUtils;
import com.songoda.ultimatestacker.utils.updateModules.LocaleModule;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class UltimateStacker extends JavaPlugin {

    private static UltimateStacker INSTANCE;
    private References references;

    private ConfigWrapper mobFile = new ConfigWrapper(this, "", "mobs.yml");
    private ConfigWrapper itemFile = new ConfigWrapper(this, "", "items.yml");
    private ConfigWrapper spawnerFile = new ConfigWrapper(this, "", "spawners.yml");

    private Locale locale;
    private SettingsManager settingsManager;
    private EntityStackManager entityStackManager;
    private SpawnerStackManager spawnerStackManager;
    private CommandManager commandManager;
    private StackingTask stackingTask;
    private Hologram hologram;

    private ServerVersion serverVersion = ServerVersion.fromPackageName(Bukkit.getServer().getClass().getPackage().getName());
    private Storage storage;

    public static UltimateStacker getInstance() {
        return INSTANCE;
    }

    public void onDisable() {
        this.saveToFile();
        this.storage.closeConnection();
        if (hologram != null)
            this.hologram.unloadHolograms();

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7UltimateStacker " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Methods.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Methods.formatText("&a============================="));
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7UltimateStacker " + this.getDescription().getVersion() + " by &5Songoda <3&7!"));
        console.sendMessage(Methods.formatText("&7Action: &aEnabling&7..."));

        this.settingsManager = new SettingsManager(this);
        this.settingsManager.setupConfig();

        this.commandManager = new CommandManager(this);

        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().contains("ARMOR")) {
                mobFile.getConfig().addDefault("Mobs." + value.name() + ".Enabled", true);
                mobFile.getConfig().addDefault("Mobs." + value.name() + ".Display Name", Methods.formatText(value.name().toLowerCase().replace("_", " "), true));
                mobFile.getConfig().addDefault("Mobs." + value.name() + ".Max Stack Size", -1);
            }
        }
        mobFile.getConfig().options().copyDefaults(true);
        mobFile.saveConfig();

        for (Material value : Material.values()) {
            itemFile.getConfig().addDefault("Items." + value.name() + ".Has Hologram", true);
            itemFile.getConfig().addDefault("Items." + value.name() + ".Max Stack Size", -1);
            itemFile.getConfig().addDefault("Items." + value.name() + ".Display Name", Methods.formatText(value.name().toLowerCase().replace("_", " "), true));
        }
        itemFile.getConfig().options().copyDefaults(true);
        itemFile.saveConfig();

        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().contains("ARMOR")) {
                spawnerFile.getConfig().addDefault("Spawners." + value.name() + ".Max Stack Size", -1);
                spawnerFile.getConfig().addDefault("Spawners." + value.name() + ".Display Name", Methods.formatText(value.name().toLowerCase().replace("_", " "), true));
            }
        }
        spawnerFile.getConfig().options().copyDefaults(true);
        spawnerFile.saveConfig();

        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));

        //Running Songoda Updater
        Plugin plugin = new Plugin(this, 16);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        this.references = new References();
        this.spawnerStackManager = new SpawnerStackManager();
        this.entityStackManager = new EntityStackManager();
        this.stackingTask = new StackingTask(this);

        checkStorage();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (storage.containsGroup("entities")) {
                for (StorageRow row : storage.getRowsByGroup("entities")) {
                    try {
                        EntityStack stack = new EntityStack(
                                UUID.fromString(row.getKey()),
                                row.get("amount").asInt());

                        this.entityStackManager.addStack(stack);
                    } catch (Exception e) {
                        console.sendMessage("Failed to load entity.");
                        e.printStackTrace();
                    }
                }
            }
            if (storage.containsGroup("spawners")) {
                for (StorageRow row : storage.getRowsByGroup("spawners")) {
                    try {
                        Location location = Methods.unserializeLocation(row.getKey());

                        SpawnerStack stack = new SpawnerStack(
                                location,
                                row.get("amount").asInt());

                        this.spawnerStackManager.addSpawner(stack);
                    } catch (Exception e) {
                        console.sendMessage("Failed to load spawner.");
                        e.printStackTrace();
                    }
                }
            }

            // Save data initially so that if the person reloads again fast they don't lose all their data.
            this.saveToFile();
            if (hologram != null)
                hologram.loadHolograms();
        }, 10);
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (isServerVersionAtLeast(ServerVersion.V1_10))
            Bukkit.getPluginManager().registerEvents(new BreedListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new DeathListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new ShearListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new InteractListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityListeners(this), this);

        // Register Hologram Plugin
        if (getConfig().getBoolean("Spawners.Holograms Enabled")) {
            if (pluginManager.isPluginEnabled("HolographicDisplays"))
                hologram = new HologramHolographicDisplays(this);
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, 6000, 6000);

        // Starting Metrics
        new Metrics(this);

        console.sendMessage(Methods.formatText("&a============================="));
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    private void saveToFile() {
        this.storage.closeConnection();
        checkStorage();

        storage.doSave();
    }

    public void reload() {
        String langMode = getConfig().getString("System.Language Mode");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));
        this.locale.reloadMessages();
        this.mobFile = new ConfigWrapper(this, "", "mobs.yml");
        this.itemFile = new ConfigWrapper(this, "", "items.yml");
        this.spawnerFile = new ConfigWrapper(this, "", "spawners.yml");
        this.references = new References();
        this.settingsManager.reloadConfig();
    }

    public boolean spawnersEnabled() {
        if (this.getServer().getPluginManager().isPluginEnabled("EpicSpawners")) return false;
        return this.getConfig().getBoolean("Main.Stack Spawners");
    }

    public Hologram getHologram() { return hologram; }
    
    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    public boolean isServerVersion(ServerVersion version) {
        return serverVersion == version;
    }

    public boolean isServerVersion(ServerVersion... versions) {
        return ArrayUtils.contains(versions, serverVersion);
    }

    public boolean isServerVersionAtLeast(ServerVersion version) {
        return serverVersion.ordinal() >= version.ordinal();
    }

    public References getReferences() {
        return references;
    }

    public Locale getLocale() {
        return locale;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public EntityStackManager getEntityStackManager() {
        return entityStackManager;
    }

    public SpawnerStackManager getSpawnerStackManager() {
        return spawnerStackManager;
    }

    public StackingTask getStackingTask() {
        return stackingTask;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public ConfigWrapper getMobFile() {
        return mobFile;
    }

    public ConfigWrapper getItemFile() {
        return itemFile;
    }

    public ConfigWrapper getSpawnerFile() {
        return spawnerFile;
    }
}

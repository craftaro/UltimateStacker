package com.songoda.ultimatestacker;

import com.songoda.ultimatestacker.command.CommandManager;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.hologram.Hologram;
import com.songoda.ultimatestacker.hologram.HologramHolographicDisplays;
import com.songoda.ultimatestacker.hook.StackerHook;
import com.songoda.ultimatestacker.hook.hooks.JobsHook;
import com.songoda.ultimatestacker.listeners.*;
import com.songoda.ultimatestacker.lootables.LootablesManager;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.spawner.SpawnerStackManager;
import com.songoda.ultimatestacker.storage.Storage;
import com.songoda.ultimatestacker.storage.StorageRow;
import com.songoda.ultimatestacker.storage.types.StorageMysql;
import com.songoda.ultimatestacker.storage.types.StorageYaml;
import com.songoda.ultimatestacker.tasks.StackingTask;
import com.songoda.ultimatestacker.utils.*;
import com.songoda.ultimatestacker.utils.locale.Locale;
import com.songoda.ultimatestacker.utils.settings.Setting;
import com.songoda.ultimatestacker.utils.settings.SettingsManager;
import com.songoda.ultimatestacker.utils.updateModules.LocaleModule;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class UltimateStacker extends JavaPlugin {

    private static UltimateStacker INSTANCE;

    private ConfigWrapper mobFile = new ConfigWrapper(this, "", "mobs.yml");
    private ConfigWrapper itemFile = new ConfigWrapper(this, "", "items.yml");
    private ConfigWrapper spawnerFile = new ConfigWrapper(this, "", "spawners.yml");

    private Locale locale;
    private SettingsManager settingsManager;
    private EntityStackManager entityStackManager;
    private SpawnerStackManager spawnerStackManager;
    private LootablesManager lootablesManager;
    private CommandManager commandManager;
    private StackingTask stackingTask;
    private Hologram hologram;

    private EntityUtils entityUtils;

    private List<StackerHook> stackerHooks = new ArrayList<>();

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

        this.entityUtils = new EntityUtils();

        this.lootablesManager = new LootablesManager();
        this.lootablesManager.createDefaultLootables();
        this.getLootablesManager().getLootManager().loadLootables();


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

        new Locale(this, "en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode"));

        //Running Songoda Updater
        Plugin plugin = new Plugin(this, 16);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        this.spawnerStackManager = new SpawnerStackManager();
        this.entityStackManager = new EntityStackManager();
        this.stackingTask = new StackingTask(this);

        checkStorage();

        Bukkit.getScheduler().runTaskLater(this, () -> {
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
            pluginManager.registerEvents(new BreedListeners(this), this);
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new DeathListeners(this), this);
        pluginManager.registerEvents(new ShearListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new ItemListeners(this), this);
        pluginManager.registerEvents(new TameListeners(this), this);
        pluginManager.registerEvents(new SpawnerListeners(this), this);
        pluginManager.registerEvents(new SheepDyeListeners(this), this);

        if (Setting.CLEAR_LAG.getBoolean() && pluginManager.isPluginEnabled("ClearLag"))
            pluginManager.registerEvents(new ClearLagListeners(this), this);

        // Register Hologram Plugin
        if (Setting.SPAWNER_HOLOGRAMS.getBoolean()) {
            if (pluginManager.isPluginEnabled("HolographicDisplays"))
                hologram = new HologramHolographicDisplays(this);
        }

        // Register Hooks
        if (pluginManager.isPluginEnabled("Jobs")) {
            stackerHooks.add(new JobsHook());
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, 6000, 6000);

        // Starting Metrics
        new Metrics(this);

        console.sendMessage(Methods.formatText("&a============================="));
    }

    public void addExp(Player player, EntityStack stack) {
        for (StackerHook stackerHook : stackerHooks) {
            stackerHook.applyExperience(player, stack);
        }
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
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode"));
        this.locale.reloadMessages();

        this.entityUtils = new EntityUtils();

        this.stackingTask.cancel();
        this.stackingTask = new StackingTask(this);

        this.mobFile = new ConfigWrapper(this, "", "mobs.yml");
        this.itemFile = new ConfigWrapper(this, "", "items.yml");
        this.spawnerFile = new ConfigWrapper(this, "", "spawners.yml");
        this.settingsManager.reloadConfig();
        this.getLootablesManager().getLootManager().loadLootables();
    }

    public boolean spawnersEnabled() {
        return !this.getServer().getPluginManager().isPluginEnabled("EpicSpawners") && Setting.SPAWNERS_ENABLED.getBoolean();
    }

    public Hologram getHologram() {
        return hologram;
    }

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

    public Locale getLocale() {
        return locale;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public LootablesManager getLootablesManager() {
        return lootablesManager;
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

    public EntityUtils getEntityUtils() {
        return entityUtils;
    }
}

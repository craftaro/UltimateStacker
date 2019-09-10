package com.songoda.ultimatestacker;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.configuration.Config;
import com.songoda.core.database.DataMigrationManager;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.MySQLConnector;
import com.songoda.core.database.SQLiteConnector;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatestacker.commands.CommandConvert;
import com.songoda.ultimatestacker.commands.CommandGiveSpawner;
import com.songoda.ultimatestacker.commands.CommandReload;
import com.songoda.ultimatestacker.commands.CommandRemoveAll;
import com.songoda.ultimatestacker.commands.CommandSettings;
import com.songoda.ultimatestacker.commands.CommandUltimateStacker;
import com.songoda.ultimatestacker.database.DataManager;
import com.songoda.ultimatestacker.database.migrations._1_InitialMigration;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.hook.StackerHook;
import com.songoda.ultimatestacker.hook.hooks.JobsHook;
import com.songoda.ultimatestacker.listeners.*;
import com.songoda.ultimatestacker.lootables.LootablesManager;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.spawner.SpawnerStackManager;
import com.songoda.ultimatestacker.storage.Storage;
import com.songoda.ultimatestacker.storage.StorageRow;
import com.songoda.ultimatestacker.storage.types.StorageYaml;
import com.songoda.ultimatestacker.tasks.StackingTask;
import com.songoda.ultimatestacker.utils.EntityUtils;
import com.songoda.ultimatestacker.utils.Methods;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;

public class UltimateStacker extends SongodaPlugin {

    private static UltimateStacker INSTANCE;
    private final static Set<String> whitelist = new HashSet();
    private final static Set<String> blacklist = new HashSet();;

    private final Config mobFile = new Config(this, "mobs.yml");
    private final Config itemFile = new Config(this, "items.yml");
    private final Config spawnerFile = new Config(this, "spawners.yml");

    private final GuiManager guiManager = new GuiManager(this);
    private final List<StackerHook> stackerHooks = new ArrayList<>();
    private EntityStackManager entityStackManager;
    private SpawnerStackManager spawnerStackManager;
    private LootablesManager lootablesManager;
    private CommandManager commandManager;
    private StackingTask stackingTask;

    private DatabaseConnector databaseConnector;
    private DataMigrationManager dataMigrationManager;
    private DataManager dataManager;

    private EntityUtils entityUtils;

    public static UltimateStacker getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }
    
    @Override
    public void onPluginDisable() {
        this.dataManager.bulkUpdateSpawners(this.spawnerStackManager.getStacks());
        HologramManager.removeAllHolograms();
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 16, CompatibleMaterial.IRON_INGOT);

        // Setup Config
        Settings.setupConfig();
		this.setLocale(Settings.LANGUGE_MODE.getString(), false);
        blacklist.clear();
        whitelist.clear();
        whitelist.addAll(Settings.ITEM_WHITELIST.getStringList());
        blacklist.addAll(Settings.ITEM_BLACKLIST.getStringList());
        
        // Setup plugin commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addCommand(new CommandUltimateStacker())
                .addSubCommand(new CommandSettings(guiManager))
                .addSubCommand(new CommandRemoveAll())
                .addSubCommand(new CommandReload())
                .addSubCommand(new CommandGiveSpawner())
                .addSubCommand(new CommandConvert(guiManager));

        this.entityUtils = new EntityUtils();

        this.lootablesManager = new LootablesManager();
        this.lootablesManager.createDefaultLootables();
        this.getLootablesManager().getLootManager().loadLootables();

        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().contains("ARMOR")) {
                mobFile.addDefault("Mobs." + value.name() + ".Enabled", true);
                mobFile.addDefault("Mobs." + value.name() + ".Display Name", Methods.formatText(value.name().toLowerCase().replace("_", " "), true));
                mobFile.addDefault("Mobs." + value.name() + ".Max Stack Size", -1);
                mobFile.addDefault("Mobs." + value.name() + ".Kill Whole Stack", false);
            }
        }
        mobFile.load();
        mobFile.saveChanges();

        for (Material value : Material.values()) {
            itemFile.addDefault("Items." + value.name() + ".Has Hologram", true);
            itemFile.addDefault("Items." + value.name() + ".Max Stack Size", -1);
            itemFile.addDefault("Items." + value.name() + ".Display Name", Methods.formatText(value.name().toLowerCase().replace("_", " "), true));
        }
        itemFile.load();
        itemFile.saveChanges();

        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().contains("ARMOR")) {
                spawnerFile.addDefault("Spawners." + value.name() + ".Max Stack Size", -1);
                spawnerFile.addDefault("Spawners." + value.name() + ".Display Name", Methods.formatText(value.name().toLowerCase().replace("_", " "), true));
            }
        }
        spawnerFile.load();
        spawnerFile.saveChanges();

        this.spawnerStackManager = new SpawnerStackManager();
        this.entityStackManager = new EntityStackManager();
        this.stackingTask = new StackingTask(this);

        guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_10))
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

        if (Settings.CLEAR_LAG.getBoolean() && pluginManager.isPluginEnabled("ClearLag"))
            pluginManager.registerEvents(new ClearLagListeners(this), this);

        // Register Hooks
        if (pluginManager.isPluginEnabled("Jobs")) {
            stackerHooks.add(new JobsHook());
        }
        HologramManager.load(this);

        // Legacy Data
        Bukkit.getScheduler().runTaskLater(this, () -> {
            File folder = getDataFolder();
            File dataFile = new File(folder, "data.yml");

            if (dataFile.exists()) {
                Storage storage = new StorageYaml(this);
                if (storage.containsGroup("spawners")) {
                    for (StorageRow row : storage.getRowsByGroup("spawners")) {
                        try {
                            Location location = Methods.unserializeLocation(row.getKey());

                            SpawnerStack stack = new SpawnerStack(
                                    location,
                                    row.get("amount").asInt());

                            getDataManager().createSpawner(stack);
                        } catch (Exception e) {
                            console.sendMessage("Failed to load spawner.");
                            e.printStackTrace();
                        }
                    }
                }
                dataFile.delete();
            }
        }, 10);

        // Database stuff, go!
        try {
            if (Settings.MYSQL_ENABLED.getBoolean()) {
                String hostname = Settings.MYSQL_HOSTNAME.getString();
                int port = Settings.MYSQL_PORT.getInt();
                String database = Settings.MYSQL_DATABASE.getString();
                String username = Settings.MYSQL_USERNAME.getString();
                String password = Settings.MYSQL_PASSWORD.getString();
                boolean useSSL = Settings.MYSQL_USE_SSL.getBoolean();

                this.databaseConnector = new MySQLConnector(this, hostname, port, database, username, password, useSSL);
                this.getLogger().info("Data handler connected using MySQL.");
            } else {
                this.databaseConnector = new SQLiteConnector(this);
                this.getLogger().info("Data handler connected using SQLite.");
            }
        } catch (Exception ex) {
            this.getLogger().severe("Fatal error trying to connect to database. Please make sure all your connection settings are correct and try again. Plugin has been disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        this.dataManager = new DataManager(this.databaseConnector, this);
        this.dataMigrationManager = new DataMigrationManager(this.databaseConnector, this.dataManager,
                new _1_InitialMigration());
        this.dataMigrationManager.runMigrations();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            final boolean useHolo = Settings.SPAWNER_HOLOGRAMS.getBoolean();
            this.dataManager.getSpawners((spawners) -> {
                this.spawnerStackManager.addSpawners(spawners);
                if (useHolo)
                    loadHolograms();
            });
        }, 20L);
    }

    public void addExp(Player player, EntityStack stack) {
        for (StackerHook stackerHook : stackerHooks) {
            stackerHook.applyExperience(player, stack);
        }
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(mobFile, itemFile, spawnerFile);
    }
    
    @Override
    public void onConfigReload() {
        blacklist.clear();
        whitelist.clear();
        whitelist.addAll(Settings.ITEM_WHITELIST.getStringList());
        blacklist.addAll(Settings.ITEM_BLACKLIST.getStringList());
        
		this.setLocale(getConfig().getString("System.Language Mode"), true);
        this.locale.reloadMessages();

        this.entityUtils = new EntityUtils();

        this.stackingTask.cancel();
        this.stackingTask = new StackingTask(this);

        this.mobFile.load();
        this.itemFile.load();
        this.spawnerFile.load();
        this.getLootablesManager().getLootManager().loadLootables();
    }

    public boolean spawnersEnabled() {
        return !this.getServer().getPluginManager().isPluginEnabled("EpicSpawners") && Settings.SPAWNERS_ENABLED.getBoolean();
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

    public Config getMobFile() {
        return mobFile;
    }

    public Config getItemFile() {
        return itemFile;
    }

    public Config getSpawnerFile() {
        return spawnerFile;
    }

    public EntityUtils getEntityUtils() {
        return entityUtils;
    }

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    void loadHolograms() {
        Collection<SpawnerStack> spawners = getSpawnerStackManager().getStacks();
        if (spawners.isEmpty()) return;

        for (SpawnerStack spawner : spawners) {
            if (spawner.getLocation().getWorld() != null) {
                updateHologram(spawner);
            }
        }
    }

    public void clearHologram(SpawnerStack stack) {
        HologramManager.removeHologram(stack.getLocation());
    }

    public void updateHologram(SpawnerStack stack) {
        // are holograms enabled?
        if(!Settings.SPAWNER_HOLOGRAMS.getBoolean() || !HologramManager.getManager().isEnabled()) return;
        // verify that this is a spawner stack
        if (stack.getLocation().getBlock().getType() != CompatibleMaterial.SPAWNER.getMaterial()) return;
        // grab the spawner block
        CreatureSpawner creatureSpawner = (CreatureSpawner) stack.getLocation().getBlock().getState();
        String name = Methods.compileSpawnerName(creatureSpawner.getSpawnedType(), stack.getAmount());
        // create the hologram
        HologramManager.updateHologram(stack.getLocation(), name);
    }

    public void updateHologram(Block block) {
        // verify that this is a spawner
        if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial()) return;
        // are holograms enabled?
        if(!Settings.SPAWNER_HOLOGRAMS.getBoolean() || !HologramManager.getManager().isEnabled()) return;
        // update this hologram in a tick
        SpawnerStack spawner = getSpawnerStackManager().getSpawner(block);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> updateHologram(spawner), 10L);
    }

    //////// Convenient API //////////
    /**
     * Change the stacked amount for this item
     * 
     * @param item item entity to update
     * @param newAmount number of items this item represents
     */
    public static void updateItemAmount(Item item, int newAmount) {
        updateItemAmount(item, item.getItemStack(), newAmount);
    }

    /**
     * Change the stacked amount for this item
     *
     * @param item item entity to update
     * @param itemStack ItemStack that will represent this item
     * @param newAmount number of items this item represents
     */
    public static void updateItemAmount(Item item, ItemStack itemStack, int newAmount) {
        Material material = itemStack.getType();
        String name = TextUtils.convertToInvisibleString("IS") + Methods.compileItemName(itemStack, newAmount);

        boolean blacklisted = isMaterialBlacklisted(itemStack);

        if (newAmount > 32 && !blacklisted) {
            item.setMetadata("US_AMT", new FixedMetadataValue(INSTANCE, newAmount));
            itemStack.setAmount(32);
        } else {
            item.removeMetadata("US_AMT", INSTANCE);
            itemStack.setAmount(newAmount);
        }
        item.setItemStack(itemStack);

        if ((blacklisted && !Settings.ITEM_HOLOGRAM_BLACKLIST.getBoolean())
                || !INSTANCE.getItemFile().getBoolean("Items." + material + ".Has Hologram")
                || !Settings.ITEM_HOLOGRAMS.getBoolean()
                || newAmount == 1 && !Settings.ITEM_HOLOGRAM_SINGLE.getBoolean())
            return;

        item.setCustomName(name);
        item.setCustomNameVisible(true);
    }

    /**
     * Lookup the stacked size of this item
     *
     * @param item item to check
     * @return stacker-corrected value for the stack size
     */
    public static int getActualItemAmount(Item item) {
        int amount = item.getItemStack().getAmount();
        if (amount >= 32 && item.hasMetadata("US_AMT")) {
            return item.getMetadata("US_AMT").get(0).asInt();
        } else {
            return amount;
        }
    }

    /**
     * Check to see if the amount stored in this itemstack is not the stacked
     * amount
     *
     * @param item item to check
     * @return true if Item.getItemStack().getAmount() is different from the
     * stacked amount
     */
    public static boolean hasCustomAmount(Item item) {
        if (item.hasMetadata("US_AMT")) {
            return item.getItemStack().getAmount() != item.getMetadata("US_AMT").get(0).asInt();
        }
        return false;
    }

    /**
     * Check to see if this material is not permitted to stack
     *
     * @param item Item material to check
     * @return true if this material will not stack
     */
    public static boolean isMaterialBlacklisted(ItemStack item) {
        CompatibleMaterial mat = CompatibleMaterial.getMaterial(item);
        if(mat == null) {
            // this shouldn't happen, but just in case?
            return item == null ? false : (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                ? isMaterialBlacklisted(item.getType()) : isMaterialBlacklisted(item.getType(), item.getData().getData()));
        } else if (mat.usesData()) {
            return isMaterialBlacklisted(mat.name()) || isMaterialBlacklisted(mat.getMaterial(), mat.getData());
        } else {
            return isMaterialBlacklisted(mat.name()) || isMaterialBlacklisted(mat.getMaterial());
        }
    }

    /**
     * Check to see if this material is not permitted to stack
     *
     * @param type Material to check
     * @return true if this material will not stack
     */
    public static boolean isMaterialBlacklisted(String type) {
        return !whitelist.isEmpty() && !whitelist.contains(type)
                || !blacklist.isEmpty() && blacklist.contains(type);
    }

    /**
     * Check to see if this material is not permitted to stack
     *
     * @param type Material to check
     * @return true if this material will not stack
     */
    public static boolean isMaterialBlacklisted(Material type) {
        return !whitelist.isEmpty() && !whitelist.contains(type.name())
                || !blacklist.isEmpty() && blacklist.contains(type.name());
    }

    /**
     * Check to see if this material is not permitted to stack
     *
     * @param type Material to check
     * @param data data value for this item (for 1.12 and older servers)
     * @return true if this material will not stack
     */
    public static boolean isMaterialBlacklisted(Material type, byte data) {
        String combined = type.toString() + ":" + data;

        return !whitelist.isEmpty() && !whitelist.contains(combined)
                || !blacklist.isEmpty() && blacklist.contains(combined);
    }

}

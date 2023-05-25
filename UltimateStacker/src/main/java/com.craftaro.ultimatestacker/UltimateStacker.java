package com.craftaro.ultimatestacker;

import com.craftaro.ultimatestacker.api.UltimateStackerAPI;
import com.craftaro.ultimatestacker.api.stack.block.BlockStack;
import com.craftaro.ultimatestacker.api.stack.block.BlockStackManager;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStackManager;
import com.craftaro.ultimatestacker.api.stack.item.ItemStackManager;
import com.craftaro.ultimatestacker.api.stack.spawner.SpawnerStack;
import com.craftaro.ultimatestacker.api.stack.spawner.SpawnerStackManager;
import com.craftaro.ultimatestacker.commands.CommandConvert;
import com.craftaro.ultimatestacker.commands.CommandGiveSpawner;
import com.craftaro.ultimatestacker.commands.CommandLootables;
import com.craftaro.ultimatestacker.commands.CommandReload;
import com.craftaro.ultimatestacker.commands.CommandRemoveAll;
import com.craftaro.ultimatestacker.commands.CommandSettings;
import com.craftaro.ultimatestacker.commands.CommandSpawn;
import com.craftaro.ultimatestacker.database.DataManager;
import com.craftaro.ultimatestacker.database.migrations._1_InitialMigration;
import com.craftaro.ultimatestacker.database.migrations._2_EntityStacks;
import com.craftaro.ultimatestacker.database.migrations._3_BlockStacks;
import com.craftaro.ultimatestacker.database.migrations._6_RemoveStackedEntityTable;
import com.craftaro.ultimatestacker.hook.hooks.JobsHook;
import com.craftaro.ultimatestacker.listeners.ShearListeners;
import com.craftaro.ultimatestacker.listeners.SheepDyeListeners;
import com.craftaro.ultimatestacker.listeners.SpawnerListeners;
import com.craftaro.ultimatestacker.listeners.TameListeners;
import com.craftaro.ultimatestacker.listeners.item.ItemCurrentListener;
import com.craftaro.ultimatestacker.listeners.item.ItemLegacyListener;
import com.craftaro.ultimatestacker.listeners.item.ItemListeners;
import com.craftaro.ultimatestacker.lootables.LootablesManager;
import com.craftaro.ultimatestacker.stackable.block.BlockStackImpl;
import com.craftaro.ultimatestacker.stackable.entity.EntityStackManagerImpl;
import com.craftaro.ultimatestacker.stackable.entity.custom.CustomEntityManager;
import com.craftaro.ultimatestacker.stackable.item.ItemStackManagerImpl;
import com.craftaro.ultimatestacker.stackable.spawner.SpawnerStackImpl;
import com.craftaro.ultimatestacker.utils.Async;
import com.craftaro.ultimatestacker.utils.Methods;
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
import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.core.hooks.ProtectionManager;
import com.songoda.core.hooks.WorldGuardHook;
import com.songoda.core.utils.TextUtils;
import com.craftaro.ultimatestacker.hook.StackerHook;
import com.craftaro.ultimatestacker.listeners.BlockListeners;
import com.craftaro.ultimatestacker.listeners.BreedListeners;
import com.craftaro.ultimatestacker.listeners.ClearLagListeners;
import com.craftaro.ultimatestacker.listeners.DeathListeners;
import com.craftaro.ultimatestacker.listeners.InteractListeners;
import com.craftaro.ultimatestacker.listeners.entity.EntityCurrentListener;
import com.craftaro.ultimatestacker.listeners.entity.EntityListeners;
import com.craftaro.ultimatestacker.settings.Settings;
import com.craftaro.ultimatestacker.api.utils.Hologramable;
import com.craftaro.ultimatestacker.stackable.block.BlockStackManagerImpl;
import com.craftaro.ultimatestacker.stackable.spawner.SpawnerStackManagerImpl;
import com.craftaro.ultimatestacker.tasks.StackingTask;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UltimateStacker extends SongodaPlugin {

    private static UltimateStacker INSTANCE;
    private final static Set<String> whitelist = new HashSet();
    private final static Set<String> blacklist = new HashSet();

    private final Config mobFile = new Config(this, "mobs.yml");
    private final Config itemFile = new Config(this, "items.yml");
    private final Config spawnerFile = new Config(this, "spawners.yml");

    private final GuiManager guiManager = new GuiManager(this);
    private final List<StackerHook> stackerHooks = new ArrayList<>();
    private EntityStackManager entityStackManager;
    private SpawnerStackManager spawnerStackManager;
    private BlockStackManager blockStackManager;
    private ItemStackManager itemStackManager;
    private LootablesManager lootablesManager;
    private CommandManager commandManager;
    private CustomEntityManager customEntityManager;
    private StackingTask stackingTask;

    private DatabaseConnector databaseConnector;
    private DataMigrationManager dataMigrationManager;
    private DataManager dataManager;
    private UltimateStackerAPI API;

    public static UltimateStacker getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;

        // Register WorldGuard
        WorldGuardHook.addHook("mob-stacking", true);
    }

    @Override
    public void onPluginDisable() {
        this.stackingTask.stop();
        this.stackingTask = null;
        this.dataManager.bulkUpdateSpawners(this.spawnerStackManager.getStacks());
        this.dataManager.bulkUpdateBlocks(this.blockStackManager.getStacks());
        HologramManager.removeAllHolograms();
        Async.shutdown();
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        Async.start();
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
        this.commandManager.addMainCommand("us")
                .addSubCommands(new CommandSettings(this, guiManager),
                        new CommandRemoveAll(this),
                        new CommandReload(this),
                        new CommandGiveSpawner(this),
                        new CommandSpawn(this),
                        new CommandLootables(this),
                        new CommandConvert( guiManager)
                );

        this.lootablesManager = new LootablesManager();
        this.lootablesManager.createDefaultLootables();
        this.getLootablesManager().getLootManager().loadLootables();

        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().contains("ARMOR")) {
                mobFile.addDefault("Mobs." + value.name() + ".Enabled", true);
                mobFile.addDefault("Mobs." + value.name() + ".Display Name", TextUtils.formatText(value.name().toLowerCase().replace("_", " "), true));
                mobFile.addDefault("Mobs." + value.name() + ".Max Stack Size", -1);
                mobFile.addDefault("Mobs." + value.name() + ".Kill Whole Stack", false);
            }
        }
        mobFile.load();
        mobFile.saveChanges();

        for (Material value : Material.values()) {
            itemFile.addDefault("Items." + value.name() + ".Has Hologram", true);
            itemFile.addDefault("Items." + value.name() + ".Max Stack Size", -1);
            itemFile.addDefault("Items." + value.name() + ".Display Name", WordUtils.capitalizeFully(value.name().toLowerCase().replace("_", " ")));
        }
        itemFile.load();
        itemFile.saveChanges();

        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().contains("ARMOR")) {
                spawnerFile.addDefault("Spawners." + value.name() + ".Max Stack Size", -1);
                spawnerFile.addDefault("Spawners." + value.name() + ".Display Name", TextUtils.formatText(value.name().toLowerCase().replace("_", " "), true));
            }
        }
        spawnerFile.load();
        spawnerFile.saveChanges();

        if (Bukkit.getPluginManager().isPluginEnabled("BentoBox")) {
            ProtectionManager.load(Bukkit.getPluginManager().getPlugin("BentoBox"));
        }

        this.spawnerStackManager = new SpawnerStackManagerImpl();
        this.entityStackManager = new EntityStackManagerImpl(this);
        this.blockStackManager = new BlockStackManagerImpl();
        this.itemStackManager = new ItemStackManagerImpl();
        this.customEntityManager = new CustomEntityManager();

        guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_10))
            pluginManager.registerEvents(new BreedListeners(this), this);
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new DeathListeners(this), this);
        pluginManager.registerEvents(new ShearListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13))
            pluginManager.registerEvents(new EntityCurrentListener(this), this);

        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new ItemListeners(this), this);

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12))
            pluginManager.registerEvents(new ItemCurrentListener(), this);
        else
            pluginManager.registerEvents(new ItemLegacyListener(), this);

        pluginManager.registerEvents(new TameListeners(this), this);
        pluginManager.registerEvents(new SpawnerListeners(this), this);
        pluginManager.registerEvents(new SheepDyeListeners(this), this);

        if (Settings.CLEAR_LAG.getBoolean() && pluginManager.isPluginEnabled("ClearLag"))
            pluginManager.registerEvents(new ClearLagListeners(this), this);

        // Register Hooks
        if (pluginManager.isPluginEnabled("Jobs"))
            stackerHooks.add(new JobsHook());

        HologramManager.load(this);
        EntityStackerManager.load();

        // Database stuff, go!
        try {
            if (Settings.MYSQL_ENABLED.getBoolean()) {
                String hostname = Settings.MYSQL_HOSTNAME.getString();
                int port = Settings.MYSQL_PORT.getInt();
                String database = Settings.MYSQL_DATABASE.getString();
                String username = Settings.MYSQL_USERNAME.getString();
                String password = Settings.MYSQL_PASSWORD.getString();
                boolean useSSL = Settings.MYSQL_USE_SSL.getBoolean();
                int poolSize = Settings.MYSQL_POOL_SIZE.getInt();

                this.databaseConnector = new MySQLConnector(this, hostname, port, database, username, password, useSSL, poolSize);
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
                new _1_InitialMigration(),
                new _2_EntityStacks(),
                new _3_BlockStacks(),
                new _6_RemoveStackedEntityTable());
        this.dataMigrationManager.runMigrations();

        API = new UltimateStackerAPI(entityStackManager, itemStackManager, spawnerStackManager, blockStackManager);
    }

    @Override
    public void onDataLoad() {
        if (HologramManager.isEnabled())
            // Set the offset so that the holograms don't end up inside the blocks.
            HologramManager.getHolograms().setPositionOffset(.5,.65,.5);

        // Load current data.
        final boolean useSpawnerHolo = Settings.SPAWNER_HOLOGRAMS.getBoolean();
        this.dataManager.getSpawners((spawners) -> {
            this.spawnerStackManager.addSpawners(spawners);
            if (useSpawnerHolo) {
                if (spawners.isEmpty()) return;

                for (SpawnerStack spawner : spawners.values()) {
                    if (spawner.getLocation().getWorld() != null) {
                        updateHologram(spawner);
                    }
                }
            }
        });

        this.stackingTask = new StackingTask(this);
        final boolean useBlockHolo = Settings.BLOCK_HOLOGRAMS.getBoolean();
        this.dataManager.getBlocks((blocks) -> {
            this.blockStackManager.addBlocks(blocks);
            if (useBlockHolo) {
                if (blocks.isEmpty()) return;

                for (BlockStack stack : blocks.values()) {
                    if (stack.getLocation().getWorld() != null) {
                        updateHologram(stack);
                    }
                }
            }
        });
    }

    public UltimateStackerAPI getAPI() {
        return API;
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

        this.stackingTask.stop();
        this.stackingTask = new StackingTask(this);

        this.mobFile.load();
        this.itemFile.load();
        this.spawnerFile.load();
        this.getLootablesManager().getLootManager().loadLootables();
    }

    public boolean spawnersEnabled() {
        return !this.getServer().getPluginManager().isPluginEnabled("EpicSpawners")
                && Settings.SPAWNERS_ENABLED.getBoolean();
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public LootablesManager getLootablesManager() {
        return lootablesManager;
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

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public BlockStackManager getBlockStackManager() {
        return blockStackManager;
    }

    public EntityStackManager getEntityStackManager() {
        return entityStackManager;
    }

    public ItemStackManager getItemStackManager() {
        return itemStackManager;
    }

    public SpawnerStackManager getSpawnerStackManager() {
        return spawnerStackManager;
    }

    public CustomEntityManager getCustomEntityManager() {
        return customEntityManager;
    }

    public void updateHologram(Hologramable stack) {
        // Is this stack invalid?
        if (!stack.isValid())
            if (stack instanceof BlockStackImpl)
                blockStackManager.removeBlock(stack.getLocation());
            else if (stack instanceof SpawnerStackImpl)
                spawnerStackManager.removeSpawner(stack.getLocation());
        // are holograms enabled?
        if (!stack.areHologramsEnabled() && !HologramManager.getManager().isEnabled()) return;
        // update the hologram
        if (!HologramManager.isHologramLoaded(stack.getHologramId())) {
            HologramManager.createHologram(stack.getHologramId(), stack.getLocation(), stack.getHologramName());
            return;
        }

        HologramManager.updateHologram(stack.getHologramId(), stack.getHologramName());
    }

    public void removeHologram(Hologramable stack) {
        HologramManager.removeHologram(stack.getHologramId());
    }

    //////// Convenient API //////////

    /**
     * Spawn a stacked item at a location
     *
     * @param item     The item to spawn
     * @param amount   The amount of items to spawn
     * @param location The location to spawn the item
     */
    public static void spawnStackedItem(ItemStack item, int amount, Location location) {
        if (item.getType() == Material.AIR) return;
        World world = location.getWorld();
        if (world == null) return;
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_17)) {
            world.dropItem(location, item, dropped -> {
                if (dropped.getItemStack().getType() == Material.AIR) return;
                updateItemMeta(dropped, item, amount);
            });
        } else {
            Item dropped = world.dropItem(location, item);
            if (dropped.getItemStack().getType() == Material.AIR) return;
            updateItemMeta(dropped, item, amount);
        }
    }

    /**
     * Change the stacked amount for this item
     *
     * @param item      item entity to update
     * @param newAmount number of items this item represents
     */
    public static void updateItemAmount(Item item, int newAmount) {
        updateItemAmount(item, item.getItemStack(), newAmount);
    }

    /**
     * Change the stacked amount for this item
     *
     * @param item      item entity to update
     * @param itemStack ItemStack that will represent this item
     * @param newAmount number of items this item represents
     */
    public static void updateItemAmount(Item item, ItemStack itemStack, int newAmount) {
        boolean blacklisted = isMaterialBlacklisted(itemStack);

        if (newAmount > (itemStack.getMaxStackSize() / 2) && !blacklisted)
            itemStack.setAmount(Math.max(1, itemStack.getMaxStackSize() / 2));
        else
            itemStack.setAmount(newAmount);

        // If amount is 0, Minecraft change the type to AIR
        if (itemStack.getType() == Material.AIR)
            return;

        updateItemMeta(item, itemStack, newAmount);
    }

    public static void updateItemMeta(Item item, ItemStack itemStack, int newAmount) {
        Material material = itemStack.getType();
        if (material == Material.AIR)
            return;

        String name = TextUtils.convertToInvisibleString("IS") + Methods.compileItemName(itemStack, newAmount);

        boolean blacklisted = isMaterialBlacklisted(itemStack);

        if (newAmount > (itemStack.getMaxStackSize() / 2) && !blacklisted) {
            item.setMetadata("US_AMT", new FixedMetadataValue(INSTANCE, newAmount));
        } else {
            item.removeMetadata("US_AMT", INSTANCE);
        }
        item.setItemStack(itemStack);

        if ((blacklisted && !Settings.ITEM_HOLOGRAM_BLACKLIST.getBoolean())
                || !INSTANCE.getItemFile().getBoolean("Items." + material + ".Has Hologram")
                || !Settings.ITEM_HOLOGRAMS.getBoolean()
                || newAmount < Settings.ITEM_MIN_HOLOGRAM_SIZE.getInt())
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
        ItemStack itemStack = item.getItemStack();
        int amount = itemStack.getAmount();
        if (/*amount >= (itemStack.getMaxStackSize() / 2) && */item.hasMetadata("US_AMT")) {
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
        if (mat == null) {
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

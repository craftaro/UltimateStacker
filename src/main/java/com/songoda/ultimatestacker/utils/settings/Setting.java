package com.songoda.ultimatestacker.utils.settings;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.Check;
import com.songoda.ultimatestacker.entity.Split;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Setting {

    STACK_SEARCH_TICK_SPEED("Main.Stack Search Tick Speed", 5,
            "The speed in which a new stacks will be created.",
            "It is advised to keep this number low."),

    DISABLED_WORLDS("Main.Disabled Worlds", Arrays.asList("World1", "World2", "World3"),
            "Worlds that stacking doesn't happen in."),

    STACK_ENTITIES("Entities.Enabled", true,
            "Should entities be stacked?"),

    NAME_FORMAT_ENTITY("Entities.Name Format", "&f{TYPE} &6{AMT}x",
            "The text displayed above an entities head where {TYPE} refers to",
            "The entities type and {AMT} is the amount currently stacked."),

    SEARCH_RADIUS("Entities.Search Radius", 5,
            "The distance entities must be to each other in order to stack."),

    MAX_STACK_ENTITIES("Entities.Max Stack Size", 15,
            "The max amount of entities in a single stack."),

    MIN_STACK_ENTITIES("Entities.Min Stack Amount", 5,
            "The minimum amount required before a stack can be formed.",
            "Do not set this to lower than 2."),

    ENTITY_HOLOGRAMS("Entities.Holograms Enabled", true,
            "Should holograms be displayed above stacked entities?"),

    HOLOGRAMS_ON_LOOK_ENTITY("Entities.Only Show Holograms On Look", false,
            "Only show nametags above an entities head when looking directly at them."),

    CUSTOM_DROPS("Entities.Custom Drops", true,
            "Should custom drops be enabled?"),

    KILL_WHOLE_STACK_ON_DEATH("Entities.Kill Whole Stack On Death", false,
            "Should killing a stack of entities kill the whole stack or",
            "just one out of the stack?"),

    CLEAR_LAG("Entities.Clear Lag", false,
            "When enabled, the plugin will hook into ClearLag and extend the",
            "clear task to include stacked entities from this plugin. If this is enabled",
            "the built in task will not run."),

    INSTANT_KILL("Entities.Instant Kill", Arrays.asList("FALL", "DROWNING", "LAVA", "VOID"),
            "Events that will trigger an entire stack to be killed.",
            "It should be noted that this is useless if the above setting is true.",
            "Any of the following can be added to the list:",
            "CONTACT, ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, PROJECTILE",
            "SUFFOCATION, FALL, FIRE, FIRE_TICK",
            "MELTING, LAVA, DROWNING, BLOCK_EXPLOSION",
            "ENTITY_EXPLOSION, VOID, LIGHTNING, SUICIDE",
            "STARVATION, POISON, MAGIC, WITHER",
            "FALLING_BLOCK, THORNS, DRAGON_BREATH, CUSTOM",
            "FLY_INTO_WALL, HOT_FLOOR, CRAMMING, DRYOUT"),

    NO_EXP_INSTANT_KILL("Entities.No Exp For Instant Kills", false,
            "Should no experience be dropped when an instant kill is performed?"),

    STACK_CHECKS("Entities.Stack Checks", Arrays.asList(Check.values()).stream()
            .filter(Check::isEnabledByDefault).map(Check::name).collect(Collectors.toList()),
            "These are checks that are processed before an entity is stacked.",
            "You can add and remove from the list at will.",
            "The acceptable check options are:",
            "SPAWN_REASON, NERFED, AGE, TICK_AGE, PHANTOM_SIZE",
            "IS_TAMED, ANIMAL_OWNER, SKELETON_TYPE",
            "ZOMBIE_BABY, SLIME_SIZE,  PIG_SADDLE, SHEEP_SHEERED",
            "SHEEP_COLOR, WOLF_COLLAR_COLOR, OCELOT_TYPE, HORSE_COLOR",
            "HORSE_STYLE, HORSE_CARRYING_CHEST, HORSE_HAS_ARMOR",
            "HORSE_HAS_SADDLE, HORSE_JUMP, RABBIT_TYPE, VILLAGER_PROFESSION",
            "LLAMA_COLOR, LLAMA_STRENGTH, PARROT_TYPE, PUFFERFISH_STATE",
            "TROPICALFISH_PATTERN, TROPICALFISH_BODY_COLOR, TROPICALFISH_PATTERN_COLOR"),

    SPLIT_CHECKS("Entities.Split Checks", Arrays.asList(Split.values()).stream()
            .map(Split::name).collect(Collectors.toList()),
            "These are checks that when achieved will break separate a single entity",
            "from a stack."),

    KEEP_FIRE("Entities.Keep Fire", true,
            "Should fire ticks persist to the next entity when an entity dies?"),

    KEEP_POTION("Entities.Keep Potion Effects", true,
            "Should potion effects persist to the next entity when an entity dies?"),

    CARRY_OVER_LOWEST_HEALTH("Entities.Carry Over Lowest Health", false,
            "Should the lowest health be carried over when stacked?",
            "This should not be used in collaboration with 'Stack Entity Health'.",
            "If it is used this setting will be overrode."),

    ONLY_STACK_FROM_SPAWNERS("Entities.Only Stack From Spawners", false,
            "Should entities only be stacked if they originate from a spawner?",
            "It should be noted that the identifier that tells the plugin",
            "if the entity originated from a spawner or not is wiped on",
            "server restart."),

    CARRY_OVER_METADATA_ON_DEATH("Entities.Carry Over Metadata On Death", true,
            "With this enabled any metadata assigned from supported plugins such",
            "as EpicSpawners and mcMMO will be preserved when the entity is killed."),

    ONLY_STACK_ON_SURFACE("Entities.Only Stack On Surface", true,
            "Should entities only be stacked if they are touching the ground",
            "or swimming? This does not effect flying entities."),

    STACK_ENTITY_HEALTH("Entities.Stack Entity Health", true,
            "Should entity health be stacked? When enabled Entity stacks will",
            "remember the health of all entities inside of the stack. This",
            "works the best with 'Only Stack On Surface enabled' as entities",
            "falling out of grinders may stack before hitting the ground."),

    ONLY_STACK_FLYING_DOWN("Entities.Only Stack Flying Down", true,
            "Should entities that fly only stack with entities that are lower on the",
            "Y axis. This is important for grinders so that flying entities don't continuously",
            "stack upwards to a higher up entity."),

    STACK_ITEMS("Items.Enabled", true,
            "Should items be stacked?"),

    ITEM_HOLOGRAMS("Items.Holograms Enabled", true,
            "Should holograms be displayed above stacked items?"),

    ITEM_HOLOGRAM_SINGLE("Items.Show Hologram For Single", true,
            "Should holograms be displayed above items when there is only a single",
            "item in the stack?"),

    MAX_STACK_ITEMS("Items.Max Stack Size", 512,
            "The max stack size for items.",
            "Currently this can only be set to a max of 120."),

    NAME_FORMAT_ITEM("Items.Name Format", "&f{TYPE} &r[&6{AMT}x]",
            "The text displayed above a dropped item."),

    NAME_FORMAT_RESET("Items.Name Format Reset", true,
            "Should color codes in dropped item names be removed?",
            "This is added only because it looks smoother in game. This is only visual and",
            "doesn't actually effect the item."),

    SHOW_STACK_SIZE_SINGLE("Items.Show Stack Size For Single", false,
            "When enabled stack sizes for a stack with a single item will",
            "not display the stack size. The stack size will be added",
            "for stacks containing two or more items."),

    SPAWNERS_ENABLED("Spawners.Enabled", true,
            "Should spawners be stacked?"),

    SPAWNER_HOLOGRAMS("Spawners.Holograms Enabled", true,
            "Should holograms be displayed above stacked spawners?"),

    MAX_STACK_SPAWNERS("Spawners.Max Stack Size", 5,
            "What should the max a spawner can stack to be?"),

    SNEAK_FOR_STACK("Spawners.Sneak To Receive A Stacked Spawner", true,
            "Toggle ability to receive a stacked spawner when breaking a spawner while sneaking."),

    SPAWNERS_DONT_EXPLODE("Spawners.Prevent Spawners From Exploding", false,
            "Should spawners not break when blown up?"),

    EXPLOSION_DROP_CHANCE_TNT("Spawners.Chance On TNT Explosion", "100%",
            "Chance of a TNT explosion dropping a spawner."),

    EXPLOSION_DROP_CHANCE_CREEPER("Spawners.Chance On Creeper Explosion", "100%",
            "Chance of a creeper explosion dropping a spawner."),

    NAME_FORMAT_SPAWNER("Spawners.Name Format", "&f{TYPE} Spawner &6{AMT}x",
            "The text displayed above a stacked spawner where {TYPE} refers to",
            "The entities type and {AMT} is the amount currently stacked."),

    DATABASE_SUPPORT("Database.Activate Mysql Support", false,
            "Should MySQL be used for data storage?"),

    DATABASE_IP("Database.IP", "127.0.0.1",
            "MySQL IP"),

    DATABASE_PORT("Database.Port", 3306,
            "MySQL Port"),

    DATABASE_NAME("Database.Database Name", "UltimateStacker",
            "The database you are inserting data into."),

    DATABASE_PREFIX("Database.Prefix", "US-",
            "The prefix for tables inserted into the database."),

    DATABASE_USERNAME("Database.Username", "PUT_USERNAME_HERE",
            "MySQL Username"),

    DATABASE_PASSWORD("Database.Password", "PUT_PASSWORD_HERE",
            "MySQL Password"),

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
        return UltimateStacker.getInstance().getConfig().getIntegerList(setting);
    }

    public List<String> getStringList() {
        return UltimateStacker.getInstance().getConfig().getStringList(setting);
    }

    public boolean getBoolean() {
        return UltimateStacker.getInstance().getConfig().getBoolean(setting);
    }

    public int getInt() {
        return UltimateStacker.getInstance().getConfig().getInt(setting);
    }

    public long getLong() {
        return UltimateStacker.getInstance().getConfig().getLong(setting);
    }

    public String getString() {
        return UltimateStacker.getInstance().getConfig().getString(setting);
    }

    public char getChar() {
        return UltimateStacker.getInstance().getConfig().getString(setting).charAt(0);
    }

    public double getDouble() {
        return UltimateStacker.getInstance().getConfig().getDouble(setting);
    }

}
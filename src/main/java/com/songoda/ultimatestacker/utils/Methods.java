package com.songoda.ultimatestacker.utils;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Setting;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Methods {

    public static void updateInventory(Item item, Inventory inventory) {
        int amount = Methods.getActualItemAmount(item);
        ItemStack itemStack = item.getItemStack();
        final int maxStack = itemStack.getMaxStackSize();

        while (amount > 0) {
            int subtract = Math.min(amount, maxStack);
            amount -= subtract;
            ItemStack newItem = itemStack.clone();
            newItem.setAmount(subtract);
            Map<Integer, ItemStack> result = inventory.addItem(newItem);
            if (result.get(0) != null) {
                amount += result.get(0).getAmount();
                break;
            }
        }

        if (amount <= 0)
            item.remove();
        else
            Methods.updateItemAmount(item, itemStack, amount);
    }

    // Do not touch! API for older plugins
    @Deprecated
    public static boolean isMaterialBlacklisted(Material type) {
        return UltimateStacker.isMaterialBlacklisted(type);
    }

    // Do not touch! API for older plugins
    @Deprecated
    public static boolean isMaterialBlacklisted(Material type, byte data) {
        return UltimateStacker.isMaterialBlacklisted(type, data);
    }

    // Do not touch! API for older plugins
    @Deprecated
    public static void updateItemAmount(Item item, int newAmount) {
        UltimateStacker.updateItemAmount(item, newAmount);
    }

    // Do not touch! API for older plugins
    @Deprecated
    public static void updateItemAmount(Item item, ItemStack itemStack, int newAmount) {
        UltimateStacker.updateItemAmount(item, itemStack, newAmount);
    }

    // Do not touch! API for older plugins
    @Deprecated
    public static int getActualItemAmount(Item item) {
        return UltimateStacker.getActualItemAmount(item);
    }

    // Do not touch! API for older plugins
    @Deprecated
    public static boolean hasCustomAmount(Item item) {
        return UltimateStacker.hasCustomAmount(item);
    }

    public static String compileItemName(ItemStack item, int amount) {
        String nameFormat = Setting.NAME_FORMAT_ITEM.getString();
        String displayName = Methods.formatText(UltimateStacker.getInstance().getItemFile()
                .getString("Items." + item.getType().name() + ".Display Name"));

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            displayName = Setting.NAME_FORMAT_RESET.getBoolean() ?
                    ChatColor.stripColor(item.getItemMeta().getDisplayName()) : item.getItemMeta().getDisplayName();

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        if (amount == 1 && !Setting.SHOW_STACK_SIZE_SINGLE.getBoolean()) {
            nameFormat = nameFormat.replaceAll("\\[.*?]", "");
        } else {
            nameFormat = nameFormat.replace("[", "").replace("]", "");
        }

        String info = TextUtils.convertToInvisibleString(Methods.insertSemicolon(String.valueOf(amount)) + ":");

        return info + Methods.formatText(nameFormat).trim();
    }

    public static boolean canFly(LivingEntity entity) {
        switch (entity.getType()) {
            case GHAST:
            case BLAZE:
            case PHANTOM:
            case BAT:
                return true;
            default:
                return false;
        }
    }

    static Vector getRandomVector() {
        return new Vector(ThreadLocalRandom.current().nextDouble(-1, 1.01), 0, ThreadLocalRandom.current().nextDouble(-1, 1.01)).normalize().multiply(0.5);
    }

    public static String compileSpawnerName(EntityType entityType, int amount) {
        String nameFormat = UltimateStacker.getInstance().getConfig().getString("Spawners.Name Format");
        String displayName = Methods.formatText(UltimateStacker.getInstance().getSpawnerFile().getString("Spawners." + entityType.name() + ".Display Name"));

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        String info = TextUtils.convertToInvisibleString(insertSemicolon(String.valueOf(amount)) + ":");
        return info + Methods.formatText(nameFormat).trim();
    }

    public static String compileEntityName(Entity entity, int amount) {
        String nameFormat = Setting.NAME_FORMAT_ENTITY.getString();
        String displayName = Methods.formatText(UltimateStacker.getInstance().getMobFile().getString("Mobs." + entity.getType().name() + ".Display Name"));

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        String info = TextUtils.convertToInvisibleString(insertSemicolon(String.valueOf(amount)) + ":");

        return info + Methods.formatText(nameFormat).trim();
    }

    public static void takeItem(Player player, int amount) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack item = player.getInventory().getItemInHand();

        int result = item.getAmount() - amount;
        item.setAmount(result);

        player.setItemInHand(result > 0 ? item : null);
    }

    public static ItemStack getSpawnerItem(EntityType entityType, int amount) {
        ItemStack item = CompatibleMaterial.SPAWNER.getItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.compileSpawnerName(entityType, amount));
        CreatureSpawner cs = (CreatureSpawner) ((BlockStateMeta) meta).getBlockState();
        cs.setSpawnedType(entityType);
        ((BlockStateMeta) meta).setBlockState(cs);
        item.setItemMeta(meta);
        return item;
    }

    public static String formatTitle(String text) {
        if (text == null || text.equals(""))
            return "";
        if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
            if (text.length() > 31)
                text = text.substring(0, 29) + "...";
        }
        text = formatText(text);
        return text;
    }

    public static boolean isInt(String number) {
        if (number == null || number.equals(""))
            return false;
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    /**
     * Serializes the location of the block specified.
     *
     * @param b The block whose location is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Block b) {
        if (b == null)
            return "";
        return serializeLocation(b.getLocation());
    }

    /**
     * Serializes the location specified.
     *
     * @param location The location that is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Location location) {
        if (location == null || location.getWorld() == null)
            return "";
        String w = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        String str = w + ":" + x + ":" + y + ":" + z;
        str = str.replace(".0", "").replace(".", "/");
        return str;
    }

    private static Map<String, Location> serializeCache = new HashMap<>();

    /**
     * Deserializes a location from the string.
     *
     * @param str The string to parse.
     * @return The location that was serialized in the string.
     */
    public static Location unserializeLocation(String str) {
        if (str == null || str.equals(""))
            return null;
        if (serializeCache.containsKey(str)) {
            return serializeCache.get(str).clone();
        }
        String cacheKey = str;
        str = str.replace("y:", ":").replace("z:", ":").replace("w:", "").replace("x:", ":").replace("/", ".");
        List<String> args = Arrays.asList(str.split("\\s*:\\s*"));

        World world = Bukkit.getWorld(args.get(0));
        double x = Double.parseDouble(args.get(1)), y = Double.parseDouble(args.get(2)), z = Double.parseDouble(args.get(3));
        Location location = new Location(world, x, y, z, 0, 0);
        serializeCache.put(cacheKey, location.clone());
        return location;
    }

    public static String insertSemicolon(String s) {
        if (s == null || s.equals(""))
            return "";
        StringBuilder hidden = new StringBuilder();
        for (char c : s.toCharArray()) hidden.append(";").append(c);
        return hidden.toString();
    }


    public static String formatText(String text) {
        if (text == null || text.equals(""))
            return "";
        return formatText(text, false);
    }

    public static String formatText(String text, boolean cap) {
        if (text == null || text.equals(""))
            return "";
        if (cap)
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static class Tuple<key, value> {
        public final key x;
        public final value y;

        public Tuple(key x, value y) {
            this.x = x;
            this.y = y;
        }

        public key getKey() {
            return this.x;
        }

        public value getValue() {
            return this.y;
        }
    }
}

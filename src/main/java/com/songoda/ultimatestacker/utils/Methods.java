package com.songoda.ultimatestacker.utils;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Methods {

    private static void handleWholeStackDeath(LivingEntity killed, EntityStack stack, List<ItemStack> items, int droppedExp) {
        Location killedLocation = killed.getLocation();
        for (int i = 1; i < stack.getAmount(); i++) {
            if (i == 1) {
                items.removeIf(it -> it.isSimilar(killed.getEquipment().getItemInHand()));
                for (ItemStack item : killed.getEquipment().getArmorContents()) {
                    items.removeIf(it -> it.isSimilar(item));
                }
            }
            for (ItemStack item : items) {
                killedLocation.getWorld().dropItemNaturally(killedLocation, item);
            }

            killedLocation.getWorld().spawn(killedLocation, ExperienceOrb.class).setExperience(droppedExp);
        }
    }

    private static void handleSingleStackDeath(LivingEntity killed) {
        UltimateStacker instance = UltimateStacker.getInstance();
        EntityStackManager stackManager = instance.getEntityStackManager();
        LivingEntity newEntity = newEntity(killed);

        newEntity.getEquipment().clear();
        
        if (killed.getType() == EntityType.PIG_ZOMBIE)
            newEntity.getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD));

        if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners"))
            if (killed.hasMetadata("ES"))
                newEntity.setMetadata("ES", killed.getMetadata("ES").get(0));

        EntityStack entityStack = stackManager.updateStack(killed, newEntity);

        entityStack.addAmount(-1);

        if (entityStack.getAmount() <= 1) {
            stackManager.removeStack(newEntity);
            newEntity.setCustomNameVisible(false);
            newEntity.setCustomName(null);
        }
    }

    public static void onDeath(LivingEntity killed, List<ItemStack> items, int droppedExp) {
        UltimateStacker instance = UltimateStacker.getInstance();

        EntityStackManager stackManager = instance.getEntityStackManager();

        if (!stackManager.isStacked(killed)) return;

        killed.setCustomName(null);
        killed.setCustomNameVisible(true);
        killed.setCustomName(formatText("&7"));

        EntityStack stack = stackManager.getStack(killed);

        if (instance.getConfig().getBoolean("Entity.Kill Whole Stack On Death") && stack.getAmount() != 1) {
            handleWholeStackDeath(killed, stack, items, droppedExp);
        } else if(instance.getConfig().getBoolean("Entity.Kill Whole Stack On Special Death Cause") && stack.getAmount() != 1) {
            List<String> reasons = instance.getConfig().getStringList("Entity.Special Death Cause");
            EntityDamageEvent lastDamageCause = killed.getLastDamageCause();

            if(lastDamageCause != null) {
                EntityDamageEvent.DamageCause cause = lastDamageCause.getCause();
                for(String s : reasons) {
                    if(!cause.name().equalsIgnoreCase(s)) continue;
                    handleWholeStackDeath(killed, stack, items, droppedExp);
                    return;
                }
            }
            handleSingleStackDeath(killed);
        }
    }

    private static LivingEntity newEntity(LivingEntity killed) {
        LivingEntity newEntity = (LivingEntity) killed.getWorld().spawnEntity(killed.getLocation(), killed.getType());
        newEntity.setVelocity(killed.getVelocity());
        if (killed instanceof Ageable && !((Ageable) killed).isAdult()) {
            ((Ageable) newEntity).setBaby();
        } else if (killed instanceof Sheep) {
            ((Sheep) newEntity).setColor(((Sheep) killed).getColor());
        } else if (killed instanceof Villager) {
            ((Villager) newEntity).setProfession(((Villager) killed).getProfession());
        } else if (killed instanceof Slime) {
            ((Slime)newEntity).setSize(((Slime)killed).getSize());
        }

        newEntity.setFireTicks(killed.getFireTicks());
        newEntity.addPotionEffects(killed.getActivePotionEffects());

        return newEntity;
    }

    public static List<Entity> getSimilarEntitesAroundEntity(Entity initalEntity) {

        //Create a list of all entities around the initial entity of the same type.
        List<Entity> entityList = initalEntity.getNearbyEntities(5, 5, 5).stream()
                .filter(entity -> entity.getType() == initalEntity.getType() && entity != initalEntity)
                .collect(Collectors.toList());

        if (initalEntity instanceof Ageable) {
            if (((Ageable) initalEntity).isAdult()) {
                entityList.removeIf(entity -> !((Ageable) entity).isAdult());
            } else {
                entityList.removeIf(entity -> ((Ageable) entity).isAdult());
            }
        }

        if (initalEntity instanceof Sheep) {
            Sheep sheep = ((Sheep) initalEntity);
            if (sheep.isSheared()) {
                entityList.removeIf(entity -> !((Sheep) entity).isSheared());
            } else {
                entityList.removeIf(entity -> ((Sheep) entity).isSheared());
            }
            entityList.removeIf(entity -> ((Sheep) entity).getColor() != sheep.getColor());
        } else if (initalEntity instanceof Villager) {
            Villager villager = ((Villager) initalEntity);
            entityList.removeIf(entity -> ((Villager) entity).getProfession() != villager.getProfession());
        } else if (initalEntity instanceof Slime) {
            Slime slime = ((Slime) initalEntity);
            entityList.removeIf(entity -> ((Slime)entity).getSize() != slime.getSize());
        }

        if (initalEntity.hasMetadata("breedCooldown")) {
            entityList.removeIf(entity -> !entity.hasMetadata("breedCooldown"));
        }

        return entityList;
    }

    public static String compileSpawnerName(EntityType entityType, int amount) {
        String nameFormat = UltimateStacker.getInstance().getConfig().getString("Spawners.Name Format");
        String displayName = Methods.formatText(UltimateStacker.getInstance().getSpawnerFile().getConfig().getString("Spawners." + entityType.name() + ".Display Name"));

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        String info = Methods.convertToInvisibleString(amount + ":");
        return info + Methods.formatText(nameFormat).trim();
    }

    public static String compileItemName(Material type, int amount) {
        String nameFormat = UltimateStacker.getInstance().getConfig().getString("Item.Name Format");
        String displayName = Methods.formatText(UltimateStacker.getInstance().getItemFile().getConfig().getString("Items." + type.name() + ".Display Name"));

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        String info = Methods.convertToInvisibleString(amount + ":");

        return info + Methods.formatText(nameFormat).trim();
    }

    public static String compileEntityName(Entity entity, int amount) {
        String nameFormat = UltimateStacker.getInstance().getConfig().getString("Entity.Name Format");
        String displayName = Methods.formatText(entity.getType().name().toLowerCase().replace("_", " "), true);

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        String info = Methods.convertToInvisibleString(amount + ":");

        return info + Methods.formatText(nameFormat).trim();
    }

    public static void takeItem(Player player, int amount) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack item = player.getInventory().getItemInHand();
        if (item == null) return;

        int result = item.getAmount() - amount;
        item.setAmount(result);

        player.setItemInHand(result > 0 ? item : null);
    }

    public static ItemStack getSpawnerItem(EntityType entityType, int amount) {
        ItemStack item = new ItemStack(Material.MOB_SPAWNER, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.compileSpawnerName(entityType, amount));
        CreatureSpawner cs = (CreatureSpawner) ((BlockStateMeta) meta).getBlockState();
        cs.setSpawnedType(entityType);
        ((BlockStateMeta) meta).setBlockState(cs);
        item.setItemMeta(meta);
        return item;
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
        if (location == null)
            return "";
        String w = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        String str = w + ":" + x + ":" + y + ":" + z;
        str = str.replace(".0", "").replace("/", "");
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


    public static String convertToInvisibleString(String s) {
        if (s == null || s.equals(""))
            return "";
        StringBuilder hidden = new StringBuilder();
        for (char c : s.toCharArray()) hidden.append(ChatColor.COLOR_CHAR + "").append(c);
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



}

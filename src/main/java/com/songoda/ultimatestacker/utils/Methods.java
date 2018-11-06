package com.songoda.ultimatestacker.utils;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Methods {

    public static List<Entity> getSimilarEntitesAroundEntity(Entity initalEntity) {

        //Create a list of all entities around the initial entity.
        List<Entity> entityList = initalEntity.getNearbyEntities(5, 5, 5);

        //Remove entities of a different type.
        entityList.removeIf(entity1 -> entity1.getType() != initalEntity.getType()
                || entity1 == initalEntity);

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
                entityList.removeIf(entity -> !sheep.isSheared());
            } else {
                entityList.removeIf(entity -> sheep.isSheared());
            }
            entityList.removeIf(entity -> ((Sheep) entity).getColor() != sheep.getColor());
        }

        if (initalEntity instanceof Villager) {
            Villager villager = ((Villager) initalEntity);
            entityList.removeIf(entity -> ((Villager) entity).getProfession() != villager.getProfession());
        }

        return entityList;
    }

    public static String compileSpawnerName(EntityType entityType, int amount) {
        String nameFormat = UltimateStacker.getInstance().getConfig().getString("Spawners.Name Format");
        String displayName = TextComponent.formatText(UltimateStacker.getInstance().getItemFile().getConfig().getString("Spawners." + entityType.name() + ".Display Name"));

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        String info = TextComponent.convertToInvisibleString(amount + ":");

        return info + TextComponent.formatText(nameFormat).trim();
    }

    public static String compileItemName(Material type, int amount) {
        String nameFormat = UltimateStacker.getInstance().getConfig().getString("Item.Name Format");
        String displayName = TextComponent.formatText(UltimateStacker.getInstance().getItemFile().getConfig().getString("Items." + type.name() + ".Display Name"));

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        String info = TextComponent.convertToInvisibleString(amount + ":");

        return info + TextComponent.formatText(nameFormat).trim();
    }

    public static String compileEntityName(Entity entity, int amount) {
        String nameFormat = UltimateStacker.getInstance().getConfig().getString("Entity.Name Format");
        String displayName = TextComponent.formatText(entity.getType().name().toLowerCase().replace("_", " "), true);

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        String info = TextComponent.convertToInvisibleString(amount + ":");

        return info + TextComponent.formatText(nameFormat).trim();
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

}

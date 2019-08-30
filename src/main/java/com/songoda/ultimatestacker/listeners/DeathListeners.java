package com.songoda.ultimatestacker.listeners;

import com.songoda.lootables.loot.Drop;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.DropUtils;
import com.songoda.ultimatestacker.utils.ServerVersion;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Material;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeathListeners implements Listener {

    private final UltimateStacker instance;

    public DeathListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;

        boolean custom = Setting.CUSTOM_DROPS.getBoolean();
        List<Drop> drops = custom ? instance.getLootablesManager().getDrops(event.getEntity()) : new ArrayList<>();

        if (!custom) {
            for (ItemStack item : event.getDrops())
                drops.add(new Drop(item));
        }
        for (ItemStack item : new ArrayList<>(event.getDrops())) {
            if (!shouldDrop(event.getEntity(), item.getType()))
                event.getDrops().remove(item);
        }

        if (instance.getEntityStackManager().isStacked(event.getEntity()))
            instance.getEntityStackManager().getStack(event.getEntity())
                    .onDeath(event.getEntity(), drops, custom, event.getDroppedExp());
        else
            DropUtils.processStackedDrop(event.getEntity(), drops);
    }

    private boolean shouldDrop(LivingEntity entity, Material material) {
        if (entity.getEquipment() != null && entity.getEquipment().getArmorContents().length != 0) {
            List<ItemStack> items = new ArrayList<>(Arrays.asList(entity.getEquipment().getArmorContents()));
            items.add(entity.getEquipment().getItemInHand());
            if (instance.isServerVersionAtLeast(ServerVersion.V1_9))
                items.add(entity.getEquipment().getItemInOffHand());
            for (ItemStack item : items) {
                if (item.getType() == material)
                    return true;
            }
        }
        if (instance.isServerVersionAtLeast(ServerVersion.V1_11) && entity instanceof ChestedHorse) {
            if (((ChestedHorse) entity).getInventory().contains(material))
                return true;
        }

        switch (material.name()) {
            case "SADDLE":
                return !entity.getType().name().equals("RAVAGER");
            case "DIAMOND_HORSE_ARMOR":
            case "GOLDEN_HORSE_ARMOR":
            case "IRON_HORSE_ARMOR":
            case "LEATHER_HORSE_ARMOR":
            case "CYAN_CARPET":
            case "BLACK_CARPET":
            case "BLUE_CARPET":
            case "BROWN_CARPET":
            case "GRAY_CARPET":
            case "GREEN_CARPET":
            case "LIGHT_BLUE_CARPET":
            case "LIGHT_GRAY_CARPET":
            case "LIME_CARPET":
            case "MAGENTA_CARPET":
            case "ORANGE_CARPET":
            case "PINK_CARPET":
            case "PURPLE_CARPET":
            case "RED_CARPET":
            case "WHITE_CARPET":
            case "YELLOW_CARPET":
            case "CARPET":
            case "CHEST":
                return true;
            default:
                return false;
        }
    }
}

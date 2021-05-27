package com.songoda.ultimatestacker.listeners;

import com.songoda.core.compatibility.ServerProject;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.lootables.loot.Drop;
import com.songoda.lootables.loot.DropUtils;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.stackable.entity.EntityStack;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeathListeners implements Listener {

    private final UltimateStacker plugin;
    private final Random random;

    public DeathListeners(UltimateStacker plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }


    private final Map<UUID, List<ItemStack>> finalItems = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        LivingEntity entity = (LivingEntity) event.getEntity();

        if (entity.getHealth() - event.getFinalDamage() < 0)
            finalItems.put(entity.getUniqueId(), getItems(entity));

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getEntityType() == EntityType.PLAYER
                || event.getEntityType() == EntityType.ARMOR_STAND) return;

        boolean custom = Settings.CUSTOM_DROPS.getBoolean();
        List<Drop> drops = custom ? plugin.getLootablesManager().getDrops(event.getEntity())
                : event.getDrops().stream().map(Drop::new).collect(Collectors.toList());

        if (custom) {
            for (ItemStack item : new ArrayList<>(event.getDrops())) {
                if (shouldDrop(event.getEntity(), item.getType()))
                    drops.add(new Drop(item));
            }
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                && !entity.getWorld().getGameRuleValue(GameRule.DO_MOB_LOOT))
            drops.clear();

        if (plugin.getEntityStackManager().isStackedAndLoaded(event.getEntity()))
            plugin.getEntityStackManager().getStack(event.getEntity())
                    .onDeath(entity, drops, custom, event.getDroppedExp(), event);
        else
            DropUtils.processStackedDrop(event.getEntity(), drops, event);
        finalItems.remove(entity.getUniqueId());
    }

    private boolean shouldDrop(LivingEntity entity, Material material) {
        if (entity.getEquipment() != null && entity.getEquipment().getArmorContents().length != 0) {
            if (Settings.DONT_DROP_ARMOR.getBoolean())
                return false;
            if (finalItems.containsKey(entity.getUniqueId())) {
                List<ItemStack> items = finalItems.get(entity.getUniqueId());
                for (ItemStack item : items)
                    if (item.getType() == material)
                        return true;
            }
        }
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)
                && entity instanceof ChestedHorse
                && ((ChestedHorse) entity).getInventory().contains(material))
            return true;

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

    public List<ItemStack> getItems(LivingEntity entity) {
        if (entity.getEquipment() != null && entity.getEquipment().getArmorContents().length != 0) {
            List<ItemStack> items = new ArrayList<>(Arrays.asList(entity.getEquipment().getArmorContents()));
            items.add(entity.getEquipment().getItemInHand());
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9))
                items.add(entity.getEquipment().getItemInOffHand());
            return items;
        }
        return new ArrayList<>();
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || ServerVersion.isServerVersionAtOrBelow(ServerVersion.V1_12))
            return;

        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (!plugin.getEntityStackManager().isStackedAndLoaded(entity)) return;
        EntityStack stack = plugin.getEntityStackManager().getStack(entity);

        if (Settings.KILL_WHOLE_STACK_ON_DEATH.getBoolean() && Settings.REALISTIC_DAMAGE.getBoolean()) {
            Player player = (Player) event.getDamager();
            ItemStack tool = player.getInventory().getItemInHand();
            if (tool.getType().getMaxDurability() < 1 || (tool.getItemMeta() != null && (tool.getItemMeta().isUnbreakable()
                    || (ServerProject.isServer(ServerProject.SPIGOT, ServerProject.PAPER) && tool.getItemMeta().isUnbreakable()))))
                return;

            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY);

            int actualDamage = 0;
            for (int i = 0; i < stack.getAmount(); i++)
                if (checkUnbreakingChance(unbreakingLevel))
                    actualDamage++;

            tool.setDurability((short) (tool.getDurability() + actualDamage));

            if (!this.hasEnoughDurability(tool, 1))
                player.getInventory().setItemInHand(null);

        }
    }

    public boolean hasEnoughDurability(ItemStack tool, int requiredAmount) {
        if (tool.getType().getMaxDurability() <= 1)
            return true;
        return tool.getDurability() + requiredAmount <= tool.getType().getMaxDurability();
    }

    public boolean checkUnbreakingChance(int level) {
        return (1.0 / (level + 1)) > random.nextDouble();
    }

}

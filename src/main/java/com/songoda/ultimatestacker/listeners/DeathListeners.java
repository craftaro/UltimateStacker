package com.songoda.ultimatestacker.listeners;

import com.songoda.core.compatibility.ServerProject;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.lootables.loot.Drop;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.settings.Setting;
import com.songoda.ultimatestacker.utils.DropUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DeathListeners implements Listener {

    private final UltimateStacker instance;
    private Random random;

    public DeathListeners(UltimateStacker instance) {
        this.instance = instance;
        random = new Random();
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
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9))
                items.add(entity.getEquipment().getItemInOffHand());
            for (ItemStack item : items) {
                if (item.getType() == material)
                    return true;
            }
        }
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11) && entity instanceof ChestedHorse) {
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

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!instance.getEntityStackManager().isStacked(event.getEntity())) return;
        EntityStack stack = instance.getEntityStackManager().getStack(event.getEntity());

        if (Setting.KILL_WHOLE_STACK_ON_DEATH.getBoolean() && Setting.REALISTIC_DAMAGE.getBoolean()) {
            Player player = (Player) event.getDamager();
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool.getType().getMaxDurability() < 1 || (tool.getItemMeta() != null && (tool.getItemMeta().isUnbreakable()
                    || (ServerProject.isServer(ServerProject.SPIGOT, ServerProject.PAPER) && tool.getItemMeta().spigot().isUnbreakable()))))
                return;

            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY);
            Damageable damageable = (Damageable) tool.getItemMeta();

            int actualDamage = 0;
            for (int i = 0; i < stack.getAmount(); i++)
                if (checkUnbreakingChance(unbreakingLevel))
                    actualDamage++;

            damageable.setDamage(damageable.getDamage() + actualDamage-1);
            tool.setItemMeta((ItemMeta) damageable);

            if (!this.hasEnoughDurability(tool, 1))
                player.getInventory().setItemInMainHand(null);

        }
    }

    public boolean hasEnoughDurability(ItemStack tool, int requiredAmount) {
        if (!tool.hasItemMeta() || !(tool.getItemMeta() instanceof Damageable) || tool.getType().getMaxDurability() < 1)
            return true;

        Damageable damageable = (Damageable) tool.getItemMeta();
        int durabilityRemaining = tool.getType().getMaxDurability() - damageable.getDamage();
        return durabilityRemaining > requiredAmount;
    }

    public boolean checkUnbreakingChance(int level) {
        return (1.0 / (level + 1)) > random.nextDouble();
    }

}

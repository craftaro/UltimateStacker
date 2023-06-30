package com.craftaro.ultimatestacker.listeners;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
import com.craftaro.ultimatestacker.settings.Settings;
import com.craftaro.ultimatestacker.stackable.entity.Split;
import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.compatibility.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class InteractListeners implements Listener {

    private final UltimateStacker plugin;

    public InteractListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        Player player = event.getPlayer();
        LivingEntity entity = (LivingEntity)event.getRightClicked();

        ItemStack item = player.getInventory().getItemInHand();

        if (!plugin.getEntityStackManager().isStackedEntity(entity)) return;

        if (item.getType() != Material.NAME_TAG && !correctFood(item, entity)) return;

        EntityStack stack = plugin.getEntityStackManager().getStackedEntity(entity);

        if (stack.getAmount() <= 1
                || item.getType() == Material.NAME_TAG
                && Settings.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.NAME_TAG)
                || item.getType() != Material.NAME_TAG
                && Settings.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.ENTITY_BREED))
            return;

        if (item.getType() == Material.NAME_TAG)
            event.setCancelled(true);
        else if (entity instanceof Ageable && !((Ageable) entity).isAdult())
            return;

        stack.releaseHost();

        if (item.getType() == Material.NAME_TAG) {
            entity.setCustomName(item.getItemMeta().getDisplayName());
        } else {
            if (entity instanceof Ageable
                    && !((Ageable) entity).isAdult()) {
                return;
            }
            entity.setMetadata("inLove", new FixedMetadataValue(plugin, true));

            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (entity.isDead()) return;
                entity.removeMetadata("inLove", plugin);
            }, 20 * 20);
        }
    }

    private boolean correctFood(ItemStack is, Entity entity) {
        Material type = is.getType();
        switch (entity.getType().name()) {
            case "COW":
            case "MUSHROOM_COW":
            case "SHEEP":
                return type == Material.WHEAT;
            case "PIG":
                return type == Material.CARROT || (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9) && type == Material.BEETROOT) || type == Material.POTATO;
            case "CHICKEN":
                return type == XMaterial.WHEAT_SEEDS.parseMaterial()
                        || type == Material.MELON_SEEDS
                        || type == Material.PUMPKIN_SEEDS
                        || type == XMaterial.BEETROOT_SEEDS.parseMaterial();
            case "HORSE":
                return (type == Material.GOLDEN_APPLE || type == Material.GOLDEN_CARROT) && ((Horse)entity).isTamed();
            case "WOLF":
                return type == XMaterial.BEEF.parseMaterial()
                        || type == XMaterial.CHICKEN.parseMaterial()
                        || type == XMaterial.COD.parseMaterial()
                        || type == XMaterial.MUTTON.parseMaterial()
                        || type == XMaterial.PORKCHOP.parseMaterial()
                        || type == XMaterial.RABBIT.parseMaterial()
                        || XMaterial.SALMON.equals(XMaterial.matchXMaterial(is))
                        || type == XMaterial.COOKED_BEEF.parseMaterial()
                        || type == XMaterial.COOKED_CHICKEN.parseMaterial()
                        || type == XMaterial.COOKED_COD.parseMaterial()
                        || type == XMaterial.COOKED_MUTTON.parseMaterial()
                        || type == XMaterial.COOKED_PORKCHOP.parseMaterial()
                        || type == XMaterial.COOKED_RABBIT.parseMaterial()
                        || XMaterial.COOKED_SALMON.equals(XMaterial.matchXMaterial(is))
                        && ((Wolf) entity).isTamed();
            case "OCELOT":
                return (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                        ? type == Material.SALMON
                        || type == Material.COD
                        || type == Material.PUFFERFISH
                        || type == Material.TROPICAL_FISH
                        : type == XMaterial.COD.parseMaterial()); // Now broken in 1.13 ((Ocelot) entity).isTamed()
            case "PANDA":
                return (type == Material.BAMBOO);
            case "FOX":
                return type == Material.SWEET_BERRIES;
            case "CAT":
                return (type == Material.COD || type == Material.SALMON) && ((Cat) entity).isTamed();
            case "RABBIT":
                return type == Material.CARROT || type == Material.GOLDEN_CARROT || type == Material.DANDELION;
            case "LLAMA":
                return type == Material.HAY_BLOCK;
            case "TURTLE":
                return type == Material.SEAGRASS;
            default:
                return false;
        }
    }

}

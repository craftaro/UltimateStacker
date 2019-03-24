package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class InteractListeners implements Listener {

    private final UltimateStacker instance;

    public InteractListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSheepDye(SheepDyeWoolEvent event) {
        Sheep entity = event.getEntity();

        if (!instance.getEntityStackManager().isStacked(entity) || event.getColor() == entity.getColor()) return;
        EntityStack stack = instance.getEntityStackManager().getStack(entity);
        if (stack.getAmount() <= 1) return;

        Entity newEntity = entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
        entity.setVelocity(getRandomVector());

        Sheep sheep = ((Sheep) newEntity);
        sheep.setSheared(entity.isSheared());
        sheep.setColor(entity.getColor());

        instance.getEntityStackManager().addStack(new EntityStack(newEntity, stack.getAmount() - 1));
        stack.setAmount(1);
        instance.getEntityStackManager().removeStack(entity);
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        ItemStack item = player.getInventory().getItemInHand();

        if (!instance.getEntityStackManager().isStacked(entity)) return;

        if (item.getType() != Material.NAME_TAG && !correctFood(item, entity)) return;

        EntityStack stack = instance.getEntityStackManager().getStack(entity);

        if (stack.getAmount() <= 1) return;

        if (item.getType() == Material.NAME_TAG)
            event.setCancelled(true);

        Entity newEntity = entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
        entity.setVelocity(getRandomVector());

        if (entity instanceof Ageable) {
            if (((Ageable) entity).isAdult()) {
                ((Ageable) newEntity).setAdult();
            } else {
                ((Ageable) entity).setBaby();
            }
        }

        if (entity instanceof Sheep) {
            Sheep sheep = ((Sheep) newEntity);
            sheep.setSheared(((Sheep)entity).isSheared());
            sheep.setColor(((Sheep)entity).getColor());
        } else if (entity instanceof Villager) {
            Villager villager = ((Villager) newEntity);
            villager.setProfession(((Villager)entity).getProfession());
        }


        instance.getEntityStackManager().addStack(new EntityStack(newEntity, stack.getAmount() - 1));
        stack.setAmount(1);
        instance.getEntityStackManager().removeStack(entity);

        if (item.getType() == Material.NAME_TAG) {
            entity.setCustomName(item.getItemMeta().getDisplayName());
        } else {
            entity.setMetadata("inLove", new FixedMetadataValue(instance, true));

            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                if (entity.isDead()) return;
                entity.removeMetadata("inLove", instance);
            }, 20 * 20);
        }
    }

    private Vector getRandomVector() {
        return new Vector(ThreadLocalRandom.current().nextDouble(-1, 1.01), 0, ThreadLocalRandom.current().nextDouble(-1, 1.01)).normalize().multiply(0.5);
    }

    private boolean correctFood(ItemStack is, Entity entity) {
        Material type = is.getType();
        switch (entity.getType()) {
            case COW:
            case SHEEP:
                return type == Material.WHEAT;
            case PIG:
                return (type == Material.CARROT || type == Material.BEETROOT || type == Material.POTATO);
            case CHICKEN:
                return type == Material.SEEDS
                        || type == Material.MELON_SEEDS
                        || type == Material.BEETROOT_SEEDS
                        || type == Material.PUMPKIN_SEEDS;
            case HORSE:
                return (type == Material.GOLDEN_APPLE || type == Material.GOLDEN_CARROT) && ((Horse)entity).isTamed();
            case WOLF:
                return (type == Material.RAW_BEEF
                        || type == Material.RAW_CHICKEN
                        || type == Material.MUTTON
                        || type == Material.PORK
                        || type == Material.RABBIT
                        || type == Material.RAW_FISH
                        || type == Material.COOKED_BEEF
                        || type == Material.COOKED_CHICKEN
                        || type == Material.COOKED_MUTTON
                        || type == Material.GRILLED_PORK
                        || type == Material.COOKED_RABBIT
                        || type == Material.COOKED_FISH)
                        && ((Wolf) entity).isTamed();
            case OCELOT:
                return (type == Material.RAW_FISH)
                        && ((Ocelot) entity).isTamed();
            case RABBIT:
                return type == Material.CARROT || type == Material.GOLDEN_CARROT || type == Material.YELLOW_FLOWER;
            case LLAMA:
                return type == Material.HAY_BLOCK;
        }
        return false;
    }
}

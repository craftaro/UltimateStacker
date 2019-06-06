package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

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
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        Player player = event.getPlayer();
        LivingEntity entity = (LivingEntity)event.getRightClicked();

        ItemStack item = player.getInventory().getItemInHand();

        if (!instance.getEntityStackManager().isStacked(entity)) return;

        if (item.getType() != Material.NAME_TAG && !correctFood(item, entity)) return;

        EntityStack stack = instance.getEntityStackManager().getStack(entity);

        if (stack.getAmount() <= 1) return;

        if (item.getType() == Material.NAME_TAG)
            event.setCancelled(true);
        else if (entity instanceof Ageable && !((Ageable) entity).isAdult())
            return;

        Entity newEntity = Methods.newEntity(entity);

        instance.getEntityStackManager().addStack(new EntityStack(newEntity, stack.getAmount() - 1));
        stack.setAmount(1);
        instance.getEntityStackManager().removeStack(entity);

        if (item.getType() == Material.NAME_TAG) {
            entity.setCustomName(item.getItemMeta().getDisplayName());
        } else {
            if (entity instanceof Ageable
                    && !((Ageable) entity).isAdult()) {
                return;
            }
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
        boolean is13 = instance.isServerVersionAtLeast(ServerVersion.V1_13);
        Material type = is.getType();
        switch (entity.getType().name()) {
            case "COW":
            case "SHEEP":
                return type == Material.WHEAT;
            case "PIG":
                return type == Material.CARROT || type == Material.BEETROOT || type == Material.POTATO;
            case "CHICKEN":
                return type == (is13 ? Material.WHEAT_SEEDS : Material.valueOf("SEEDS"))
                        || type == Material.MELON_SEEDS
                        || type == Material.BEETROOT_SEEDS
                        || type == Material.PUMPKIN_SEEDS;
            case "HORSE":
                return (type == Material.GOLDEN_APPLE || type == Material.GOLDEN_CARROT) && ((Horse)entity).isTamed();
            case "WOLF":
                return type == (is13 ? Material.BEEF : Material.valueOf("RAW_BEEF"))
                        || type == (is13 ? Material.CHICKEN : Material.valueOf("RAW_CHICKEN"))
                        || (is13 && type == Material.COD)
                        || type == Material.MUTTON
                        || type == (is13 ? Material.PORKCHOP : Material.valueOf("PORK"))
                        || type == Material.RABBIT
                        || (is13 && type == Material.SALMON)
                        || type == Material.COOKED_BEEF
                        || type == Material.COOKED_CHICKEN
                        || (is13 && type == Material.COOKED_COD)
                        || type == Material.COOKED_MUTTON
                        || type == (is13 ? Material.COOKED_PORKCHOP : Material.valueOf("GRILLED_PORK"))
                        || type == Material.COOKED_RABBIT
                        || (is13 && type == Material.COOKED_SALMON)
                        && ((Wolf) entity).isTamed();
            case "OCELOT":
                return (is13 ? type == Material.SALMON
                        || type == Material.COD
                        || type == Material.PUFFERFISH
                        || type == Material.TROPICAL_FISH

                        : type == Material.valueOf("RAW_FISH")); // Now broken in 1.13 ((Ocelot) entity).isTamed()
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
        }
        return false;
    }

}

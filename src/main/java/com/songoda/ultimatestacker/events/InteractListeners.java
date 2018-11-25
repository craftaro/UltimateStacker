package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class InteractListeners implements Listener {

    private final UltimateStacker instance;

    public InteractListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onAccept(InventoryPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (new ItemStack(item.getType()).getMaxStackSize() == item.getMaxStackSize()) return;
        
        instance.getStackingTask().setMax(item, 0, true);
        int amt = item.getAmount();
        int max = item.getMaxStackSize();

        if (amt <= max) return;

        item.setAmount(max);
        amt = amt - max;

        while (amt > max) {
            ItemStack newItem = new ItemStack(item);
            newItem.setAmount(max);

            event.getInventory().addItem(newItem);
            amt = amt - max;
        }

        ItemStack newItem = new ItemStack(item);
        newItem.setAmount(amt);

        event.getInventory().addItem(newItem);
    }


    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        event.getItem().setItemStack(instance.getStackingTask().setMax(event.getItem().getItemStack(), 0, true));

        ItemStack item = event.getItem().getItemStack();

        int amt = item.getAmount();
        int max = item.getMaxStackSize();

        if (amt <= max) return;

        event.setCancelled(true);

        item.setAmount(max);
        amt = amt - max;

        while (amt > max) {
            ItemStack newItem = new ItemStack(item);
            newItem.setAmount(max);

            event.getItem().getWorld().dropItemNaturally(event.getItem().getLocation(), newItem);
            amt = amt - max;
        }

        ItemStack newItem = new ItemStack(item);
        newItem.setAmount(amt);

        event.getItem().getWorld().dropItemNaturally(event.getItem().getLocation(), newItem);

    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.NAME_TAG
                || !instance.getEntityStackManager().isStacked(entity)) return;

        EntityStack stack = instance.getEntityStackManager().getStack(entity);

        if (stack.getAmount() == 1) return;
        event.setCancelled(true);

        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> stack.setAmount(stack.getAmount() - 1), 1L);

        Entity newEntity = entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
        newEntity.setVelocity(getRandomVector());

        if (entity instanceof Ageable) {
            if (((Ageable) entity).isAdult()) {
                ((Ageable) newEntity).setAdult();
            } else {
                ((Ageable) entity).setBaby();
            }
        }

        if (entity instanceof Sheep) {
            Sheep sheep = ((Sheep) newEntity);
            sheep.setSheared(sheep.isSheared());
            sheep.setColor(sheep.getColor());
        }

        if (entity instanceof Villager) {
            Villager villager = ((Villager) newEntity);
            villager.setProfession(villager.getProfession());
        }
        newEntity.setCustomName(item.getItemMeta().getDisplayName());
    }
    private Vector getRandomVector() {
        return new Vector(ThreadLocalRandom.current().nextDouble(-1, 1.01), 0, ThreadLocalRandom.current().nextDouble(-1, 1.01)).normalize().multiply(0.5);
    }
}

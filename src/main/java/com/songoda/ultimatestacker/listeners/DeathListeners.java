package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Material;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DeathListeners implements Listener {

    private final UltimateStacker instance;

    public DeathListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        List<ItemStack> items = Setting.CUSTOM_DROPS.getBoolean()
                ? instance.getLootManager().getDrops(event.getEntity()) : event.getDrops();

        if (items.size() != 0) {
            if (event.getEntity() instanceof Pig && ((Pig) event.getEntity()).hasSaddle())
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(),
                    new ItemStack(Material.SADDLE));

            event.getDrops().clear();
        } else
            items = event.getDrops();

        for (ItemStack item : items) {
            if (item == null) continue;
            event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), item);
        }

        if (instance.getEntityStackManager().isStacked(event.getEntity()))
            instance.getEntityStackManager().getStack(event.getEntity())
                    .onDeath(event.getEntity(), items, event.getDroppedExp());
    }
}

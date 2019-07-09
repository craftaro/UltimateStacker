package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DeathListeners implements Listener {

    private final UltimateStacker instance;

    public DeathListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        List<ItemStack> items = Setting.CUSTOM_DROPS.getBoolean()
                ? instance.getLootManager().getDrops(event.getEntity()) : new ArrayList<>();

        boolean custom = false;
        if (items.size() != 0) {
            event.getDrops().clear();

            for (ItemStack item : items) {
                if (item == null) continue;
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), item);
            }
            custom = true;
        } else
            items = event.getDrops();


        if (instance.getEntityStackManager().isStacked(event.getEntity()))
            instance.getEntityStackManager().getStack(event.getEntity())
                    .onDeath(event.getEntity(), items, custom, event.getDroppedExp());
    }
}

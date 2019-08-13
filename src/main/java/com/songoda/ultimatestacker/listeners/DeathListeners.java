package com.songoda.ultimatestacker.listeners;

import com.songoda.lootables.loot.Drop;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.DropUtils;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.entity.Player;
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
        if (event.getEntity() instanceof Player) return;

        boolean custom = Setting.CUSTOM_DROPS.getBoolean();
        List<Drop> drops = custom ? instance.getLootablesManager().getDrops(event.getEntity()) : new ArrayList<>();

        if (!custom) {
            for (ItemStack item : event.getDrops())
                drops.add(new Drop(item));
        }
        event.getDrops().clear();

        if (instance.getEntityStackManager().isStacked(event.getEntity()))
            instance.getEntityStackManager().getStack(event.getEntity())
                    .onDeath(event.getEntity(), drops, custom, event.getDroppedExp());
    }
}

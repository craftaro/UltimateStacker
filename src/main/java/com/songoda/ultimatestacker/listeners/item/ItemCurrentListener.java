package com.songoda.ultimatestacker.listeners.item;

import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemCurrentListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!Settings.STACK_ITEMS.getBoolean() || event.getItem() instanceof Arrow) return;
        // Amount here is not the total amount of item (32 if more than 32) but the amount of item the player can retrieve
        // ie there is x64 diamonds blocks (so 32), the player pick 8 items so the amount is 8 and not 32

        Item item = event.getItem();
        ItemStack stack = item.getItemStack();
        int amount = UltimateStacker.getActualItemAmount(item);

        if (event.getEntity() instanceof Player) {
            if (amount < (stack.getMaxStackSize() / 2)) return;
            event.setCancelled(true);
            Player player = (Player) event.getEntity();
            player.playSound(player.getLocation(), CompatibleSound.ENTITY_ITEM_PICKUP.getSound(), .2f, (float) (1 + Math.random()));
            Methods.updateInventory(event.getItem(), player.getInventory());
        } else {
            UltimateStacker.updateItemMeta(item, stack, amount - 1);
        }
    }
}

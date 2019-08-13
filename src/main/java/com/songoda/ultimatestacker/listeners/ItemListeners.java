package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.ServerVersion;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemListeners implements Listener {

    private final UltimateStacker instance;

    public ItemListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onMerge(ItemMergeEvent event) {
        int maxItemStackSize = Setting.MAX_STACK_ITEMS.getInt();
        if (!Setting.STACK_ITEMS.getBoolean()) return;

        Item item = event.getTarget();
        ItemStack itemStack = item.getItemStack();

        event.setCancelled(true);

        int specific = instance.getItemFile().getConfig().getInt("Items." + itemStack.getType().name() + ".Max Stack Size");
        int max = specific == -1 && new ItemStack(itemStack.getType()).getMaxStackSize() != 1 ? maxItemStackSize : specific;

        if (Methods.isMaterialBlacklisted(itemStack.getType()))
            max = new ItemStack(itemStack.getType()).getMaxStackSize();

        if (max == -1) max = 1;

        int newAmount = Methods.getActualItemAmount(event.getEntity())
                + Methods.getActualItemAmount(item);

        if (newAmount > max) return;

        Methods.updateItemAmount(item, itemStack, newAmount);
        event.getEntity().remove();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInvPickup(InventoryPickupItemEvent event) {
        if (!Setting.STACK_ITEMS.getBoolean()) return;
        int amount = Methods.getActualItemAmount(event.getItem());
        if (amount <= 32) return;
        event.setCancelled(true);

        Methods.updateInventory(event.getItem(), event.getInventory());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExist(ItemSpawnEvent event) {
        if (!Setting.STACK_ITEMS.getBoolean()) return;

        ItemStack itemStack = event.getEntity().getItemStack();

        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() &&
                StringUtils.substring(itemStack.getItemMeta().getDisplayName(), 0, 3).equals("***")) {
            return; //Compatibility with Shop instance: https://www.spigotmc.org/resources/shop-a-simple-intuitive-shop-instance.9628/
        }

        Methods.updateItemAmount(event.getEntity(), itemStack, event.getEntity().getItemStack().getAmount());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        if (!Setting.STACK_ITEMS.getBoolean()) return;
        if (event.getItem().getItemStack().getAmount() < 32) return;
        event.setCancelled(true);

        event.getPlayer().playSound(event.getPlayer().getLocation(),
                instance.isServerVersionAtLeast(ServerVersion.V1_9) ? Sound.ENTITY_ITEM_PICKUP
                        : Sound.valueOf("ITEM_PICKUP"), .2f, (float) (1 + Math.random()));

        Methods.updateInventory(event.getItem(), event.getPlayer().getInventory());
    }

}

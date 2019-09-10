package com.songoda.ultimatestacker.listeners;

import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.core.utils.BlockUtils;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.utils.Methods;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListeners implements Listener {

    private final UltimateStacker instance;

    public ItemListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onMerge(ItemMergeEvent event) {
        int maxItemStackSize = Settings.MAX_STACK_ITEMS.getInt();
        if (!Settings.STACK_ITEMS.getBoolean()) return;

        Item item = event.getTarget();
        ItemStack itemStack = item.getItemStack();

        event.setCancelled(true);

        int specific = instance.getItemFile().getInt("Items." + itemStack.getType().name() + ".Max Stack Size");
        int max;

        if (UltimateStacker.isMaterialBlacklisted(itemStack))
            max = new ItemStack(itemStack.getType()).getMaxStackSize();
        else
            max = specific == -1 && new ItemStack(itemStack.getType()).getMaxStackSize() != 1 ? maxItemStackSize : specific;

        if (max == -1) max = 1;

        int newAmount = UltimateStacker.getActualItemAmount(event.getEntity())
                + UltimateStacker.getActualItemAmount(item);

        if (newAmount > max) return;

        UltimateStacker.updateItemAmount(item, itemStack, newAmount);
        event.getEntity().remove();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInvPickup(InventoryPickupItemEvent event) {
        if (!Settings.STACK_ITEMS.getBoolean() || !UltimateStacker.hasCustomAmount(event.getItem())) return;
        event.setCancelled(true);

        Methods.updateInventory(event.getItem(), event.getInventory());
        if (event.getInventory().getHolder() instanceof BlockState)
            BlockUtils.updateAdjacentComparators(((BlockState)event.getInventory().getHolder()).getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExist(ItemSpawnEvent event) {
        if (!Settings.STACK_ITEMS.getBoolean()) return;

        ItemStack itemStack = event.getEntity().getItemStack();

        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() &&
                StringUtils.substring(itemStack.getItemMeta().getDisplayName(), 0, 3).equals("***")) {
            return; //Compatibility with Shop instance: https://www.spigotmc.org/resources/shop-a-simple-intuitive-shop-instance.9628/
        }

        UltimateStacker.updateItemAmount(event.getEntity(), itemStack, event.getEntity().getItemStack().getAmount());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        if (!Settings.STACK_ITEMS.getBoolean()) return;
        if (event.getItem().getItemStack().getAmount() < 32) return;
        event.setCancelled(true);

        event.getPlayer().playSound(event.getPlayer().getLocation(), CompatibleSound.ENTITY_ITEM_PICKUP.getSound(), .2f, (float) (1 + Math.random()));

        Methods.updateInventory(event.getItem(), event.getPlayer().getInventory());
    }

}

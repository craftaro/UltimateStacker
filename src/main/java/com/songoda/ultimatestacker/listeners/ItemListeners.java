package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.ServerVersion;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

public class ItemListeners implements Listener {

    private final UltimateStacker instance;

    public ItemListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onMerge(ItemMergeEvent event) {
        event.setCancelled(true);

        int maxItemStackSize = Setting.MAX_STACK_ITEMS.getInt();
        if (!Setting.STACK_ITEMS.getBoolean()) return;

        Item item = event.getTarget();

        int specific = instance.getItemFile().getConfig().getInt("Items." + item.getItemStack().getType().name() + ".Max Stack Size");
        int max = specific == -1 && new ItemStack(item.getItemStack().getType()).getMaxStackSize() != 1 ? maxItemStackSize : specific;

        if (max == -1) max = 1;

        int newAmount = getActualAmount(event.getEntity())
                + getActualAmount(item);

        if (newAmount > max) return;

        updateAmount(item, newAmount);
        event.getEntity().remove();
    }
    @EventHandler
    public void onInvPickup(InventoryPickupItemEvent event) {
        event.setCancelled(true);

        updateInventory(event.getItem(), event.getInventory());
    }

    @EventHandler
    public void onDispense(ItemSpawnEvent event) {
        if (!Setting.STACK_ITEMS.getBoolean()) return;

        updateAmount(event.getEntity(), event.getEntity().getItemStack().getAmount());
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getAmount() < 32) return;
        event.setCancelled(true);

        event.getPlayer().playSound(event.getPlayer().getLocation(),
                instance.isServerVersionAtLeast(ServerVersion.V1_9) ? Sound.ENTITY_ITEM_PICKUP
                        : Sound.valueOf("ITEM_PICKUP"), .2f, (float)(1 + Math.random()));

        updateInventory(event.getItem(), event.getPlayer().getInventory());
    }

    private void updateInventory(Item item, Inventory inventory) {
        int amount = getActualAmount(item);

        while (amount > 0) {
            int subtract = Math.min(amount, 64);
            amount -= subtract;
            ItemStack newItem = item.getItemStack().clone();
            newItem.setAmount(subtract);
            Map<Integer, ItemStack> result = inventory.addItem(newItem);
            if (result.get(0) != null) {
                amount += result.get(0).getAmount();
                break;
            }
        }

        if (amount <= 0)
            item.remove();
        else
            updateAmount(item, amount);
    }

    private void updateAmount(Item item, int newAmount) {
        Material material = item.getItemStack().getType();
        String name = Methods.convertToInvisibleString("IS") +
                Methods.compileItemName(material, newAmount);

        if (newAmount > 32) {
            item.setMetadata("US_AMT", new FixedMetadataValue(instance, newAmount));
            item.getItemStack().setAmount(32);
        } else {
            item.removeMetadata("US_AMT", instance);
            item.getItemStack().setAmount(newAmount);
        }

        if (instance.getItemFile().getConfig().getBoolean("Items." + material + ".Has Hologram")
                && Setting.ITEM_HOLOGRAMS.getBoolean()) {
            item.setCustomName(name);
            item.setCustomNameVisible(true);
        }
    }

    private int getActualAmount(Item item) {
        if (item.hasMetadata("US_AMT")) {
            return item.getMetadata("US_AMT").get(0).asInt();
        } else {
            return item.getItemStack().getAmount();
        }
    }
}

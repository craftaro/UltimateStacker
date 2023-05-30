package com.craftaro.ultimatestacker.listeners.item;

import com.craftaro.ultimatestacker.api.UltimateStackerAPI;
import com.craftaro.ultimatestacker.api.stack.item.StackedItem;
import com.craftaro.ultimatestacker.api.stack.item.StackedItemManager;
import com.songoda.core.nms.NmsManager;
import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.settings.Settings;
import com.craftaro.ultimatestacker.utils.Methods;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemListeners implements Listener {

    private final UltimateStacker plugin;

    public ItemListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMerge(ItemMergeEvent event) {
        int maxItemStackSize = Settings.MAX_STACK_ITEMS.getInt();
        if (!Settings.STACK_ITEMS.getBoolean()) return;

        StackedItem stackedItem = UltimateStacker.getInstance().getStackedItemManager().merge(event.getEntity(), event.getTarget(), false, (fromStack, toStack, merged) -> {
            if (fromStack == null && merged != null) {
                //merge was successful
                event.setCancelled(true);
                event.getEntity().remove(); //remove the item that was merged
            }
        });
    }

    @EventHandler
    public void onInvPickup(InventoryPickupItemEvent event) {
        if (!Settings.STACK_ITEMS.getBoolean() || !UltimateStacker.getInstance().getStackedItemManager().isStackedItem(event.getItem())) {
            return;
        }

        event.setCancelled(true);

        Methods.updateInventory(event.getItem(), event.getInventory());
        if (event.getInventory().getHolder() instanceof BlockState) {
            Block invHolder = ((BlockState) event.getInventory().getHolder()).getBlock();
            NmsManager.getWorld().updateAdjacentComparators(invHolder);
        }
    }

    //Do we need this?

    //@EventHandler
//    public void onExist(ItemSpawnEvent event) {
//        if (!Settings.STACK_ITEMS.getBoolean()) return;
//
//        List<String> disabledWorlds = Settings.DISABLED_WORLDS.getStringList();
//        if (disabledWorlds.stream().anyMatch(worldStr -> event.getEntity().getWorld().getName().equalsIgnoreCase(worldStr)))
//            return;
//
//        ItemStack itemStack = event.getEntity().getItemStack();
//
//        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() &&
//                StringUtils.substring(itemStack.getItemMeta().getDisplayName(), 0, 3).equals("***")) {
//            return; //Compatibility with Shop instance: https://www.spigotmc.org/resources/shop-a-simple-intuitive-shop-instance.9628/
//        }
//
//        StackedItemManager itemStackManager = UltimateStackerAPI.getStackedItemManager();
//
//        if (itemStackManager.isStackedItem(event.getEntity())) {
//            StackedItem stackedItem = UltimateStacker.getInstance().getStackedItemManager().getStackedItem(event.getEntity());
//            stackedItem.setAmount(stackedItem.getAmount() + itemStack.getAmount());
//            UltimateStacker.updateItemAmount(event.getEntity(), itemStack, UltimateStacker.getActualItemAmount(event.getEntity()) + itemStack.getAmount());
//        } else {
//            UltimateStacker.updateItemAmount(event.getEntity(), itemStack, itemStack.getAmount());
//        }
//
//    }
}

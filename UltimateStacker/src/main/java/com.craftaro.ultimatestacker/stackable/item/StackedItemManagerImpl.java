package com.craftaro.ultimatestacker.stackable.item;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.api.stack.item.ItemMergeCallback;
import com.craftaro.ultimatestacker.api.stack.item.StackedItem;
import com.craftaro.ultimatestacker.api.stack.item.StackedItemManager;
import com.craftaro.ultimatestacker.settings.Settings;
import com.craftaro.core.compatibility.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StackedItemManagerImpl implements StackedItemManager {

    @Override
    public @Nullable StackedItem getStackedItem(Item item) {
        if (item.hasMetadata("US_AMT")) {
            return new StackedItemImpl(item);
        }
        return null;
    }

    @Override
    public @NotNull StackedItem getStackedItem(Item item, boolean create) {
        return null;
    }

    @Override
    public @Nullable StackedItem createStack(ItemStack item, Location location, int amount) {
        if (item.getType() == Material.AIR) return null;
        World world = location.getWorld();
        if (world == null) return null;
        AtomicReference<StackedItem> stack = new AtomicReference<>(null);
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_17)) {
            world.dropItem(location, item, dropped -> {
                if (dropped.getItemStack().getType() == Material.AIR) return;
                stack.set(new StackedItemImpl(dropped, amount));
            });
        } else {
            Item dropped = world.dropItem(location, item);
            if (dropped.getItemStack().getType() == Material.AIR) return null;
            stack.set(new StackedItemImpl(dropped, amount));
        }
        return stack.get();
    }

    @Override
    public @Nullable StackedItem createStack(Item item, int amount) {
        return null;
    }

    @Override
    public @Nullable Future<StackedItem> createStackSync(ItemStack item, Location location, int amount) {
        return Bukkit.getScheduler().callSyncMethod(UltimateStacker.getInstance(), () -> createStack(item, location, amount));
    }

    @Override
    public @Nullable Future<StackedItem> createStackSync(Item item, int amount) {
        return Bukkit.getScheduler().callSyncMethod(UltimateStacker.getInstance(), () -> createStack(item, amount));
    }

    @Override
    public int getActualItemAmount(Item item) {
        if (isStackedItem(item)) {
            return item.getMetadata("US_AMT").get(0).asInt();
        } else {
            return item.getItemStack().getAmount();
        }
    }

    @Override
    public boolean isStackedItem(Item item) {
        if (item.hasMetadata("US_AMT")) {
            return item.getItemStack().getAmount() != item.getMetadata("US_AMT").get(0).asInt();
        }
        return false;
    }

    @Override
    public StackedItem merge(Item from, Item to, boolean ignoreRestrictions) {
        return merge(from, to, ignoreRestrictions, null);
    }

    @Override
    public StackedItem merge(Item from, Item to, boolean ignoreRestrictions, ItemMergeCallback<Item, Item, StackedItem> callback) {

        if (!ignoreRestrictions) {
            if (!Settings.STACK_ITEMS.getBoolean()) return null;
            List<String> disabledWorlds = Settings.DISABLED_WORLDS.getStringList();
            if (disabledWorlds.stream().anyMatch(worldStr -> from.getWorld().getName().equalsIgnoreCase(worldStr))) {
                return null;
            }
        }

        int maxItemStackSize = Settings.MAX_STACK_ITEMS.getInt();

        ItemStack fromItemStack = from.getItemStack();
        ItemStack toItemStack = to.getItemStack();

        if (fromItemStack.getType() != toItemStack.getType()) return null;
        if (!ignoreRestrictions && UltimateStacker.isMaterialBlacklisted(fromItemStack)) return null;

        int maxSize = UltimateStacker.getInstance().getItemFile().getInt("Items." + fromItemStack.getType().name() + ".Max Stack Size");

        int fromAmount = getActualItemAmount(from);
        int toAmount = getActualItemAmount(to);

        if (fromAmount + toAmount > maxSize) {
            if (callback != null) callback.accept(from, to, null);
            //merge was unsuccessful
            return null;
        } else {
            StackedItem merged = new StackedItemImpl(to, fromAmount + toAmount);
            if (callback != null) callback.accept(null, to, merged);
            return merged;
        }
    }

    @Override
    public boolean isMaterialBlacklisted(ItemStack item) {
        return UltimateStacker.isMaterialBlacklisted(item);
    }

    @Override
    public boolean isMaterialBlacklisted(String type) {
        return UltimateStacker.isMaterialBlacklisted(type);
    }

    @Override
    public boolean isMaterialBlacklisted(Material type) {
        return UltimateStacker.isMaterialBlacklisted(type);
    }

    @Override
    public boolean isMaterialBlacklisted(Material type, byte data) {
        return UltimateStacker.isMaterialBlacklisted(type, data);
    }
}

package com.songoda.ultimatestacker.listeners;

import com.songoda.core.compatibility.CompatibleHand;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.events.SpawnerBreakEvent;
import com.songoda.ultimatestacker.events.SpawnerPlaceEvent;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.stackable.block.BlockStack;
import com.songoda.ultimatestacker.stackable.block.BlockStackManager;
import com.songoda.ultimatestacker.stackable.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.List;
import java.util.Map;

public class BlockListeners implements Listener {

    private final UltimateStacker plugin;

    public BlockListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        CompatibleHand hand = CompatibleHand.getHand(event);
        ItemStack inHand = hand.getItem(player);

        if (block == null) return;

        if (Settings.STACK_BLOCKS.getBoolean()) {
            BlockStackManager blockStackManager = plugin.getBlockStackManager();

            boolean isStacked = blockStackManager.isBlock(block.getLocation());

            CompatibleMaterial blockType = CompatibleMaterial.getMaterial(block);
            if (blockType == null) return;

            if (isStacked || Settings.STACKABLE_BLOCKS.getStringList().contains(blockType.name())) {
                BlockStack stack = blockStackManager.getBlock(block, blockType);
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (!isStacked) plugin.getDataManager().createBlock(stack);
                    if (stack.getMaterial() == CompatibleMaterial.getMaterial(inHand)) {
                        int amountToAdd = player.isSneaking() || Settings.ALWAYS_ADD_ALL.getBoolean() ? inHand.getAmount() : 1;
                        if (!isStacked) amountToAdd ++;
                        stack.add(amountToAdd);
                        event.setCancelled(true);
                        if (player.getGameMode() != GameMode.CREATIVE)
                            hand.takeItem(player, amountToAdd);
                        plugin.updateHologram(stack);
                    }
                    plugin.getDataManager().updateBlock(stack);
                } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && stack.getAmount() != 0) {
                    event.setCancelled(true);
                    int amountToRemove = player.isSneaking()
                            ? Math.min(Settings.MAX_REMOVEABLE.getInt(), stack.getAmount()) - 1 : 1;

                    ItemStack removed = stack.getMaterial().getItem();
                    removed.setAmount(amountToRemove);
                    stack.take(amountToRemove);
                    int maxStack = removed.getMaxStackSize();

                    while (amountToRemove > 0) {
                        int subtract = Math.min(amountToRemove, maxStack);
                        amountToRemove -= subtract;
                        ItemStack newItem = removed.clone();
                        newItem.setAmount(subtract);
                        if (Settings.ADD_TO_INVENTORY.getBoolean()) {
                            Map<Integer, ItemStack> result = player.getInventory().addItem(newItem);
                            if (result.get(0) != null) {
                                amountToRemove += result.get(0).getAmount();
                                break;
                            }
                        } else {
                            block.getWorld().dropItemNaturally(block.getLocation().clone().add(.5, 1, .5), newItem);
                        }
                    }
                    stack.add(amountToRemove);
                    if (stack.getAmount() < 2)
                        stack.destroy();
                    else {
                        plugin.updateHologram(stack);
                        plugin.getDataManager().updateBlock(stack);
                    }
                }
            }
        }

        if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial()
                || inHand.getType() != CompatibleMaterial.SPAWNER.getMaterial()
                || event.getAction() == Action.LEFT_CLICK_BLOCK) return;

        List<String> disabledWorlds = Settings.DISABLED_WORLDS.getStringList();
        if (disabledWorlds.stream().anyMatch(worldStr -> event.getPlayer().getWorld().getName().equalsIgnoreCase(worldStr)))
            return;

        if (!plugin.spawnersEnabled()) return;

        BlockStateMeta bsm = (BlockStateMeta) inHand.getItemMeta();
        CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();

        EntityType itemType = cs.getSpawnedType();

        int itemAmount = getSpawnerAmount(inHand);
        int specific = plugin.getSpawnerFile().getInt("Spawners." + cs.getSpawnedType().name() + ".Max Stack Size");
        int maxStackSize = specific == -1 ? Settings.MAX_STACK_SPAWNERS.getInt() : specific;

        cs = (CreatureSpawner) block.getState();

        EntityType blockType = cs.getSpawnedType();

        event.setCancelled(true);

        if (itemType == blockType) {
            SpawnerStack stack = plugin.getSpawnerStackManager().getSpawner(block);
            if (player.isSneaking()) return;
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (stack.getAmount() == maxStackSize) return;

                ItemStack overflowItem = null;
                if ((stack.getAmount() + itemAmount) > maxStackSize) {
                    overflowItem = Methods.getSpawnerItem(blockType, (stack.getAmount() + itemAmount) - maxStackSize);
                    itemAmount = maxStackSize - stack.getAmount();
                }

                SpawnerPlaceEvent placeEvent = new SpawnerPlaceEvent(player, block, blockType, itemAmount);
                Bukkit.getPluginManager().callEvent(placeEvent);
                if (placeEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }

                if (overflowItem != null) {
                    if (player.getInventory().firstEmpty() == -1)
                        block.getWorld().dropItemNaturally(block.getLocation().add(.5, 0, .5), overflowItem);
                    else
                        player.getInventory().addItem(overflowItem);
                }

                stack.setAmount(stack.getAmount() + itemAmount);
                plugin.updateHologram(stack);
                hand.takeItem(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial()
                || !(block.getState() instanceof CreatureSpawner) // Needed for a DataPack
                || !plugin.spawnersEnabled())
            return;

        CreatureSpawner cs = (CreatureSpawner) block.getState();
        CreatureSpawner cs2 = (CreatureSpawner) ((BlockStateMeta) event.getItemInHand().getItemMeta()).getBlockState();
        int amount = getSpawnerAmount(event.getItemInHand());

        SpawnerPlaceEvent placeEvent = new SpawnerPlaceEvent(player, block, cs2.getSpawnedType(), amount);
        Bukkit.getPluginManager().callEvent(placeEvent);
        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        SpawnerStack stack = plugin.getSpawnerStackManager().addSpawner(new SpawnerStack(block.getLocation(), amount));
        plugin.getDataManager().createSpawner(stack);

        cs.setSpawnedType(cs2.getSpawnedType());
        cs.update();

        plugin.updateHologram(stack);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial()) return;

        if (!plugin.spawnersEnabled()) return;
        event.setExpToDrop(0);

        CreatureSpawner cs = (CreatureSpawner) block.getState();

        EntityType blockType = cs.getSpawnedType();

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInHand();

        SpawnerStack stack = plugin.getSpawnerStackManager().getSpawner(block);

        event.setCancelled(true);

        int amt = 1;
        boolean remove = false;

        if (player.isSneaking() && Settings.SNEAK_FOR_STACK.getBoolean()) {
            amt = stack.getAmount();
            remove = true;
        } else if (stack.getAmount() <= 1) {
            remove = true;
        }

        SpawnerBreakEvent breakEvent = new SpawnerBreakEvent(player, block, blockType, amt);
        Bukkit.getPluginManager().callEvent(breakEvent);
        if (breakEvent.isCancelled())
            return;

        if (remove) {
            event.setCancelled(false);
            plugin.removeHologram(stack);
            SpawnerStack spawnerStack = plugin.getSpawnerStackManager().removeSpawner(block.getLocation());
            plugin.getDataManager().deleteSpawner(spawnerStack);
        } else {
            stack.setAmount(stack.getAmount() - 1);
            plugin.updateHologram(stack);
        }

        if (player.hasPermission("ultimatestacker.spawner.nosilkdrop") || item.getEnchantments().containsKey(Enchantment.SILK_TOUCH) && player.hasPermission("ultimatestacker.spawner.silktouch")) {
            ItemStack spawner = Methods.getSpawnerItem(blockType, amt);
            if (player.getInventory().firstEmpty() == -1 || !Settings.SPAWNERS_TO_INVENTORY.getBoolean())
                block.getWorld().dropItemNaturally(block.getLocation().add(.5, 0, .5), spawner);
            else
                player.getInventory().addItem(spawner);
        }

    }

    private int getSpawnerAmount(ItemStack item) {
        NBTItem nbtItem = NmsManager.getNbt().of(item);
        if (nbtItem.has("spawner_stack_size"))
            return nbtItem.getNBTObject("spawner_stack_size").asInt();
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return 1;
        if (item.getItemMeta().getDisplayName().contains(":")) {
            int amt = NumberUtils.toInt(item.getItemMeta().getDisplayName().replace("\u00A7", "").replace(";", "").split(":")[0], 1);
            return amt == 0 ? 1 : amt;
        }
        return 1;
    }
}

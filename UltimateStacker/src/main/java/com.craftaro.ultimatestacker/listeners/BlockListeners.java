package com.craftaro.ultimatestacker.listeners;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.api.UltimateStackerAPI;
import com.craftaro.ultimatestacker.api.events.spawner.SpawnerBreakEvent;
import com.craftaro.ultimatestacker.api.events.spawner.SpawnerPlaceEvent;
import com.craftaro.ultimatestacker.api.stack.block.BlockStack;
import com.craftaro.ultimatestacker.api.stack.block.BlockStackManager;
import com.craftaro.ultimatestacker.api.stack.spawner.SpawnerStack;
import com.craftaro.ultimatestacker.settings.Settings;
import com.craftaro.ultimatestacker.stackable.spawner.SpawnerStackImpl;
import com.craftaro.ultimatestacker.utils.Methods;
import com.songoda.core.compatibility.CompatibleHand;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.hooks.ProtectionManager;
import com.songoda.core.third_party.de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

public class BlockListeners implements Listener {

    private final UltimateStacker plugin;

    public BlockListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY) return;
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (block == null) return;

        if (!ProtectionManager.canInteract(player, block.getLocation()) || !ProtectionManager.canBreak(player, block.getLocation())) {
            if (!player.isOp()) {
                return;
            }
        }

        CompatibleHand hand = CompatibleHand.getHand(event);
        ItemStack inHand = hand.getItem(player);
        boolean isSneaking = player.isSneaking();
        Action clickAction = event.getAction();
        int inHandAmount = inHand.getAmount();

        //Stacking blocks
        if (Settings.STACK_BLOCKS.getBoolean()
                && Settings.STACKABLE_BLOCKS.getStringList().contains(block.getType().name()) //Is block stackable
                && !block.getType().equals(CompatibleMaterial.SPAWNER.getMaterial()) //Don't stack spawners here
                ) {

            CompatibleMaterial blockType = CompatibleMaterial.getMaterial(block);
            if (blockType == null) return;

            BlockStackManager blockStackManager = plugin.getBlockStackManager();
            boolean isStacked = blockStackManager.isBlock(block.getLocation());
            BlockStack stack = blockStackManager.getBlock(block.getLocation());

            //Modify stack
            if (isStacked) {
                event.setCancelled(true);
                //Add to stack
                if (clickAction == Action.RIGHT_CLICK_BLOCK) {
                    if (inHand.getType().equals(Material.AIR)) return;
                    if(!blockType.equals(CompatibleMaterial.getMaterial(inHand))) return;
                    //Add all held items to stack
                    if (Settings.ALWAYS_ADD_ALL.getBoolean() || isSneaking) {
                        stack.add(inHandAmount);
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            hand.takeItem(player, inHandAmount);
                        }
                    } else {
                        //Add one held item to stack
                        stack.add(1);
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            hand.takeItem(player, 1);
                        }
                    }
                }

                //Remove from stack
                if (clickAction == Action.LEFT_CLICK_BLOCK) {
                    if (isSneaking) {
                        //Remove all items from stack
                        int amountToRemove = Math.min(Settings.MAX_REMOVEABLE.getInt(), stack.getAmount());
                        ItemStack removed = stack.getMaterial().getItem();
                        removed.setAmount(amountToRemove);
                        stack.take(amountToRemove);
                        if (Settings.ADD_TO_INVENTORY.getBoolean()) {
                            player.getInventory().addItem(removed);
                        } else {
                            player.getWorld().dropItemNaturally(block.getLocation(), removed);
                        }
                    } else {
                        //Remove one item from stack
                        stack.take(1);
                        if (Settings.ADD_TO_INVENTORY.getBoolean()) {
                            player.getInventory().addItem(stack.getMaterial().getItem());
                        } else {
                            player.getWorld().dropItemNaturally(block.getLocation(), stack.getMaterial().getItem());
                        }
                    }
                    if (stack.getAmount() == 0) {
                        //Remove stack
                        stack.destroy();
                        return;
                    }
                }
                //update hologram
                plugin.updateHologram(stack);
                plugin.getDataManager().updateBlock(stack);
                return;
            } else {
                if (isSneaking) return;
                //Check if player clicked the same type as the clicked block
                if (inHand.getType().equals(Material.AIR)) return;
                if(!blockType.equals(CompatibleMaterial.getMaterial(inHand))) return;
                if (clickAction != Action.RIGHT_CLICK_BLOCK) return;
                //Create new stack
                event.setCancelled(true);
                if (player.getGameMode() != GameMode.CREATIVE) {
                    hand.takeItem(player, 1);
                }
                BlockStack newStack = blockStackManager.createBlock(block);
                plugin.getDataManager().createBlock(newStack);
                plugin.updateHologram(newStack);
            }
            return;
        }

        //Stacking spawners
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
            SpawnerStack stack = UltimateStackerAPI.getSpawnerStackManager().getSpawner(block);
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
                if (player.getGameMode() != GameMode.CREATIVE)
                    hand.takeItem(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
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

        SpawnerStack stack = UltimateStackerAPI.getSpawnerStackManager().addSpawner(new SpawnerStackImpl(block.getLocation(), amount));
        plugin.getDataManager().createSpawner(stack);

        cs.setSpawnedType(cs2.getSpawnedType());
        cs.update();

        plugin.updateHologram(stack);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial()) return;

        if (!plugin.spawnersEnabled()) return;
        event.setExpToDrop(0);

        CreatureSpawner cs = (CreatureSpawner) block.getState();

        EntityType blockType = cs.getSpawnedType();

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInHand();

        SpawnerStack stack = UltimateStackerAPI.getSpawnerStackManager().getSpawner(block);

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
            SpawnerStack spawnerStack = UltimateStackerAPI.getSpawnerStackManager().removeSpawner(block.getLocation());
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
        NBTItem nbtItem = new NBTItem(item);
        if (nbtItem.hasKey("spawner_stack_size"))
            return nbtItem.getInteger("spawner_stack_size");

        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return 1;
        if (item.getItemMeta().getDisplayName().contains(":")) {
            int amt = NumberUtils.toInt(item.getItemMeta().getDisplayName().replace("\u00A7", "").replace(";", "").split(":")[0], 1);
            return amt == 0 ? 1 : amt;
        }
        return 1;
    }
}

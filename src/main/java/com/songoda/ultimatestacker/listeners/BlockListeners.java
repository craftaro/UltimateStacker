package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.ServerVersion;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

public class BlockListeners implements Listener {

    private final UltimateStacker instance;

    public BlockListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSpawnerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack item = event.getPlayer().getInventory().getItemInHand();

        if (block == null
                || block.getType() != (instance.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))
                || item.getType() != (instance.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))
                || event.getAction() == Action.LEFT_CLICK_BLOCK) return;

        List<String> disabledWorlds = Setting.DISABLED_WORLDS.getStringList();
        if (disabledWorlds.stream().anyMatch(worldStr -> event.getPlayer().getWorld().getName().equalsIgnoreCase(worldStr))) return;

        if (!instance.spawnersEnabled()) return;

        BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();

        EntityType itemType = cs.getSpawnedType();

        int itemAmount = getSpawnerAmount(item);
        int specific = instance.getSpawnerFile().getConfig().getInt("Spawners." + cs.getSpawnedType().name() + ".Max Stack Size");
        int maxStackSize = specific == -1 ? Setting.MAX_STACK_SPAWNERS.getInt() : specific;

        cs = (CreatureSpawner) block.getState();

        EntityType blockType = cs.getSpawnedType();

        event.setCancelled(true);

        if (itemType == blockType) {
            SpawnerStack stack = instance.getSpawnerStackManager().getSpawner(block);
            if (player.isSneaking()) return;
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (stack.getAmount() == maxStackSize) return;

                if ((stack.getAmount() + itemAmount) > maxStackSize) {
                    ItemStack newItem = Methods.getSpawnerItem(blockType, (stack.getAmount() + itemAmount) - maxStackSize);
                    if (player.getInventory().firstEmpty() == -1)
                        block.getLocation().getWorld().dropItemNaturally(block.getLocation().add(.5, 0, .5), newItem);
                    else
                        player.getInventory().addItem(newItem);

                    itemAmount = maxStackSize - stack.getAmount();
                }

                stack.setAmount(stack.getAmount() + itemAmount);
                if (instance.getHologram() != null)
                    instance.getHologram().update(stack);
                Methods.takeItem(player, itemAmount);
            }
        }

        if (instance.getHologram() != null)
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getHologram().processChange(block), 10L);

    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if (!event.isCancelled()) {
            if (block == null
                    || block.getType() != (instance.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))
                    || !instance.spawnersEnabled())
                return;

            SpawnerStack stack = instance.getSpawnerStackManager().addSpawner(new SpawnerStack(block.getLocation(), getSpawnerAmount(event.getItemInHand())));

            CreatureSpawner cs = (CreatureSpawner) block.getState();
            CreatureSpawner cs2 = (CreatureSpawner) ((BlockStateMeta) event.getItemInHand().getItemMeta()).getBlockState();
            cs.setSpawnedType(cs2.getSpawnedType());
            cs.update();

            if (instance.getHologram() != null)
                instance.getHologram().add(stack);
        }

        if (instance.getHologram() != null)
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getHologram().processChange(block), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != (instance.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) return;

        if (!instance.spawnersEnabled()) return;
        event.setExpToDrop(0);

        CreatureSpawner cs = (CreatureSpawner) block.getState();

        EntityType blockType = cs.getSpawnedType();

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInHand();

        SpawnerStack stack = instance.getSpawnerStackManager().getSpawner(block);

        event.setCancelled(true);

        int amt = 1;

        if (player.isSneaking()) {
            event.setCancelled(false);
            amt = stack.getAmount();
            if (instance.getHologram() != null)
            instance.getHologram().remove(stack);
            instance.getSpawnerStackManager().removeSpawner(block.getLocation());
        } else {
            if (stack.getAmount() <= 1) {
                event.setCancelled(false);
                instance.getSpawnerStackManager().removeSpawner(block.getLocation());
                if (instance.getHologram() != null)
                    instance.getHologram().remove(stack);
            } else {
                stack.setAmount(stack.getAmount() - 1);
                if (instance.getHologram() != null)
                    instance.getHologram().update(stack);
            }
        }
        if (player.hasPermission("ultimatestacker.spawner.nosilkdrop") || item != null && item.getEnchantments().containsKey(Enchantment.SILK_TOUCH) && player.hasPermission("ultimatestacker.spawner.silktouch"))
            block.getWorld().dropItemNaturally(block.getLocation(), Methods.getSpawnerItem(blockType, amt));
    }


    private int getSpawnerAmount(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return 1;
        if (item.getItemMeta().getDisplayName().contains(":")) {
            int amt = NumberUtils.toInt(item.getItemMeta().getDisplayName().replace("\u00A7", "").split(":")[0], 1);
            return amt == 0 ? 1 : amt;
        }
        return 1;
    }
}

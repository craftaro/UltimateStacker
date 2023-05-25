package com.craftaro.ultimatestacker.listeners;

import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStackManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.settings.Settings;
import com.craftaro.ultimatestacker.stackable.entity.EntityStackManagerImpl;
import com.craftaro.ultimatestacker.stackable.entity.Split;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ShearListeners implements Listener {

    private final UltimateStacker plugin;

    public ShearListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        LivingEntity entity = (LivingEntity)event.getEntity();

        if (entity.getType() != EntityType.SHEEP
                && entity.getType() != EntityType.MUSHROOM_COW
                && entity.getType() != EntityType.SNOWMAN) return;
        EntityStackManager stackManager = plugin.getEntityStackManager();
        if (!stackManager.isStackedEntity(entity)) return;

        if (event.getEntity().getType() == EntityType.SHEEP
                && Settings.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.SHEEP_SHEAR)
                || event.getEntity().getType() == EntityType.MUSHROOM_COW
                && Settings.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.MUSHROOM_SHEAR)
                || event.getEntity().getType() == EntityType.SNOWMAN
                && Settings.SPLIT_CHECKS.getStringList().stream().noneMatch(line -> Split.valueOf(line) == Split.SNOWMAN_DERP))
            return;


        EntityStack stack = stackManager.getStackedEntity(entity);

        if (Settings.SHEAR_IN_ONE_CLICK.getBoolean()) {
            World world = entity.getLocation().getWorld();
            int amount = stack.getAmount() - 1;
            ItemStack item = getDrop(entity);
            if (item == null)
                return;

            int amountToDrop = 0;

            if (entity.getType() == EntityType.SHEEP) {
                for (int i = 0; i < amount; i++) {
                    amountToDrop += new Random().nextInt(2) + 1;
                }
            } else
                amountToDrop = item.getAmount() * amount;

            int fullStacks = (int) Math.floor(amountToDrop / 64);
            int nonStack = amountToDrop - (fullStacks * 64);

            for (int i = 0; i < fullStacks; i++) {
                ItemStack clone = item.clone();
                clone.setAmount(64);
                world.dropItemNaturally(entity.getLocation(), clone);
            }
            if (nonStack != 0) {
                ItemStack clone = item.clone();
                clone.setAmount(nonStack);
                world.dropItemNaturally(entity.getLocation(), clone);
            }
        } else
            stack.releaseHost();
    }

    private ItemStack getDrop(Entity entity) {
        ItemStack itemStack = null;

        switch (entity.getType()) {
            case SHEEP:
                itemStack = new ItemStack(CompatibleMaterial.WHITE_WOOL.getMaterial());
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13))
                    itemStack.setType(Material.valueOf(((Sheep) entity).getColor() + "_WOOL"));
                else
                    itemStack.setDurability((short) ((Sheep) entity).getColor().getWoolData());
                break;
            case MUSHROOM_COW:
                itemStack = new ItemStack(CompatibleMaterial.RED_MUSHROOM.getMaterial(), 5);
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)
                        && ((MushroomCow) entity).getVariant() == MushroomCow.Variant.BROWN)
                    itemStack.setType(CompatibleMaterial.BROWN_MUSHROOM.getMaterial());
                break;
        }
        return itemStack;
    }
}

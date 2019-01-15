package com.songoda.ultimatestacker.tasks;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class StackingTask extends BukkitRunnable {

    private final UltimateStacker instance;

    private Class<?> clazzItemStack, clazzItem, clazzCraftItemStack;

    private Method methodGetItem, methodAsNMSCopy;

    private Field fieldMaxStackSize;

    public StackingTask(UltimateStacker instance) {
        this.instance = instance;
        try {
            String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);
            clazzCraftItemStack = Class.forName("org.bukkit.craftbukkit." + ver + ".inventory.CraftItemStack");
            clazzItemStack = Class.forName("net.minecraft.server." + ver + ".ItemStack");
            clazzItem = Class.forName("net.minecraft.server." + ver + ".Item");

            methodAsNMSCopy = clazzCraftItemStack.getMethod("asNMSCopy", ItemStack.class);
            methodGetItem = clazzItemStack.getDeclaredMethod("getItem");

            fieldMaxStackSize = clazzItem.getDeclaredField("maxStackSize");
            fieldMaxStackSize.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int maxItemStackSize = instance.getConfig().getInt("Item.Max Stack Size");
        int maxEntityStackSize = instance.getConfig().getInt("Entity.Max Stack Size");

        EntityStackManager stackManager = instance.getEntityStackManager();
        for (World world : Bukkit.getWorlds()) {

            List<Entity> entities = world.getEntities();
            Collections.reverse(entities);

            List<UUID> removed = new ArrayList<>();

            nextEntity:
            for (Entity entityO : entities) {
                if (entityO == null || entityO instanceof Player) continue;

                if (entityO instanceof Item && instance.getConfig().getBoolean("Main.Stack Items")) {
                    ItemStack item = ((Item) entityO).getItemStack();

                    if (entityO.hasMetadata("grabbed")
                            || item == null
                            || entityO.isCustomNameVisible() && !entityO.getCustomName().contains(Methods.convertToInvisibleString("IS"))
                            || item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                        continue;

                    int specific = instance.getItemFile().getConfig().getInt("Items." + item.getType().name() + ".Max Stack Size");
                    int max = specific == -1 && new ItemStack(item.getType()).getMaxStackSize() != 1 ? maxItemStackSize : specific;

                    if (max == -1) max = 1;

                    if (item.getMaxStackSize() != max && item.getMaxStackSize() != 1 && (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()))
                        setMax(item, max, false);

                    int size = item.getAmount();

                    String name = Methods.convertToInvisibleString("IS") + Methods.compileItemName(item.getType(), size);

                    if (instance.getItemFile().getConfig().getBoolean("Items." + item.getType().name() + ".Has Hologram")) {
                        entityO.setCustomName(name);
                        entityO.setCustomNameVisible(true);
                    }

                    continue;
                }

                if (!(entityO instanceof LivingEntity) || !instance.getConfig().getBoolean("Main.Stack Entities"))
                    continue;

                LivingEntity initalEntity = (LivingEntity) entityO;

                if (initalEntity.isDead()
                        || !initalEntity.isValid()
                        || initalEntity instanceof ArmorStand
                        || initalEntity.hasMetadata("inLove")) continue;

                EntityStack initialStack = stackManager.getStack(initalEntity);
                if (initialStack == null && initalEntity.getCustomName() != null) continue;
                int amtToStack = initialStack != null ? initialStack.getAmount() : 1;

                ConfigurationSection configurationSection = UltimateStacker.getInstance().getMobFile().getConfig();

                if (!configurationSection.getBoolean("Mobs." + initalEntity.getType().name() + ".Enabled"))
                    continue;

                if (configurationSection.getInt("Mobs." + initalEntity.getType().name() + ".Max Stack Size") != -1)
                    maxEntityStackSize = configurationSection.getInt("Mobs." + initalEntity.getType().name() + ".Max Stack Size");

                List<Entity> entityList = Methods.getSimilarEntitesAroundEntity(initalEntity);

                for (Entity entity : new ArrayList<>(entityList)) {
                    if (removed.contains(entity.getUniqueId())) continue;
                    EntityStack stack = stackManager.getStack(entity);
                    if (stack == null && entity.getCustomName() != null) {
                        entityList.remove(entity);
                        continue;
                    }

                    //If a stack was found add 1 to this stack.
                    if (stack != null && (stack.getAmount() + amtToStack) <= maxEntityStackSize) {
                        stack.addAmount(amtToStack);
                        stack.updateStack();
                        removed.add(initalEntity.getUniqueId());
                        initalEntity.remove();

                        continue nextEntity;
                    }
                }

                if (initialStack != null) continue;

                entityList.removeIf(stackManager::isStacked);

                if (entityList.size() < instance.getConfig().getInt("Entity.Min Stack Amount") - 1) continue;

                //If stack was never found make a new one.
                EntityStack stack = stackManager.addStack(new EntityStack(initalEntity, entityList.size() + 1));

                for (Entity entity : entityList) {
                    if (stackManager.isStacked(entity) || removed.contains(entity.getUniqueId())) continue;
                    removed.add(entity.getUniqueId());
                    entity.remove();
                }

                stack.updateStack();
            }
            entities.clear();
            removed.clear();
        }
    }

    public ItemStack setMax(ItemStack item, int max, boolean reset) {
        try {
            Object objItemStack = methodGetItem.invoke(methodAsNMSCopy.invoke(null, item));
            fieldMaxStackSize.set(objItemStack, reset ? new ItemStack(item.getType()).getMaxStackSize() : max);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return item;
    }

    public void startTask() {
        new StackingTask(instance).runTaskTimer(instance, 0, instance.getConfig().getInt("Main.Stack Search Tick Speed"));
    }
}

package com.songoda.ultimatestacker.tasks;

import com.songoda.arconix.api.methods.formatting.TextComponent;
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
import java.util.List;

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

            nextEntity:
            for (Entity entityO : world.getEntities()) {
                if (entityO instanceof Item && instance.getConfig().getBoolean( "Main.Stack Items")) {
                    ItemStack item = ((Item) entityO).getItemStack();

                    if (entityO.isCustomNameVisible() && !entityO.getCustomName().contains(TextComponent.convertToInvisibleString("IS")) || item.hasItemMeta() && item.getItemMeta().hasDisplayName()) continue;

                    if (item.getMaxStackSize() != maxItemStackSize && item.getMaxStackSize() != 1)
                        setMax(item, false);

                    int size = item.getAmount();

                    String name = TextComponent.convertToInvisibleString("IS") + Methods.compileItemName(item.getType(), size);

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
                        || stackManager.isStacked(initalEntity)) continue;

                ConfigurationSection configurationSection = UltimateStacker.getInstance().getMobFile().getConfig();

                if (!configurationSection.getBoolean("Mobs." + initalEntity.getType().name() + ".Enabled"))
                    continue;

                if (configurationSection.getInt("Mobs." + initalEntity.getType().name() + ".Max Stack Size") != -1)
                    maxEntityStackSize = configurationSection.getInt("Mobs." + initalEntity.getType().name() + ".Max Stack Size");

                List<Entity> entityList = Methods.getSimilarEntitesAroundEntity(initalEntity);

                for (Entity entity : entityList) {

                    EntityStack stack = stackManager.getStack(entity);

                    //If a stack was found add 1 to this stack.
                    if (stack != null && stack.getAmount() < maxEntityStackSize) {
                        stack.addAmount(1);
                        stack.updateStack();
                        initalEntity.remove();
                        continue nextEntity;
                    }
                }

                entityList.removeIf(stackManager::isStacked);

                if (entityList.size() < instance.getConfig().getInt("Entity.Min Stack Amount") - 1) continue;

                //If stack was never found make a new one.
                EntityStack stack = stackManager.addStack(new EntityStack(initalEntity, entityList.size() + 1));

                for (Entity entity : entityList) {
                    if (stackManager.isStacked(entity)) continue;
                    entity.remove();
                }

                stack.updateStack();
            }
        }
    }

    public ItemStack setMax(ItemStack item, boolean reset) {
        try {
            int specific = instance.getItemFile().getConfig().getInt("Items." + item.getType().name() + ".Max Stack Size");
            int maxItemStackSize = specific == -1 ? instance.getConfig().getInt("Item.Max Stack Size") : specific;
            Object objItemStack = methodGetItem.invoke(methodAsNMSCopy.invoke(null, item));
            fieldMaxStackSize.set(objItemStack, reset ? new ItemStack(item.getType()).getMaxStackSize() : maxItemStackSize);
        } catch(ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return item;
    }

    public void startTask() {
        new StackingTask(instance).runTaskTimer(instance, 0, instance.getConfig().getInt("Main.Stack Search Tick Speed"));
    }
}

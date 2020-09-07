package com.songoda.ultimatestacker.utils;

import org.bukkit.Bukkit;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {

    private static Class<?> clazzCraftCreatureSpawner, clazzTileEntityMobSpawner = null;
    private static Method methodGetTileEntity, methodGetSpawner;
    private static Field fieldSpawnount, fieldMaxNearbyEntities, fieldSpawner;

    public static CreatureSpawner updateSpawner(CreatureSpawner creatureSpawner, int count, int max) {
        if (!Bukkit.getServer().getClass().getPackage().getName().contains("1.8")) {
            try {
                if (creatureSpawner == null) return null;
                if (clazzCraftCreatureSpawner == null) {
                    String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);
                    clazzCraftCreatureSpawner = Class.forName("org.bukkit.craftbukkit." + ver + ".block.CraftCreatureSpawner");
                    clazzTileEntityMobSpawner = Class.forName("net.minecraft.server." + ver + ".TileEntityMobSpawner");
                    Class<?> clazzMobSpawnerAbstract = Class.forName("net.minecraft.server." + ver + ".MobSpawnerAbstract");
                    methodGetTileEntity = clazzCraftCreatureSpawner.getDeclaredMethod("getTileEntity");
                    methodGetSpawner = clazzTileEntityMobSpawner.getDeclaredMethod("getSpawner");
                    fieldSpawnount = clazzMobSpawnerAbstract.getDeclaredField("spawnCount");
                    fieldSpawnount.setAccessible(true);
                    fieldMaxNearbyEntities = clazzMobSpawnerAbstract.getDeclaredField("maxNearbyEntities");
                    fieldMaxNearbyEntities.setAccessible(true);
                }

                Object objCraftCreatureSpawner = clazzCraftCreatureSpawner.cast(creatureSpawner);
                Object objTileEntityMobSpawner = clazzTileEntityMobSpawner.cast(methodGetTileEntity.invoke(objCraftCreatureSpawner));
                Object objMobSpawnerAbstract = methodGetSpawner.invoke(objTileEntityMobSpawner);
                fieldSpawnount.set(objMobSpawnerAbstract, count);
                fieldMaxNearbyEntities.set(objMobSpawnerAbstract, max);

            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            return creatureSpawner;
        } else {
            try {
                if (clazzCraftCreatureSpawner == null) {
                    String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);
                    clazzCraftCreatureSpawner = Class.forName("org.bukkit.craftbukkit." + ver + ".block.CraftCreatureSpawner");
                    clazzTileEntityMobSpawner = Class.forName("net.minecraft.server." + ver + ".TileEntityMobSpawner");
                    Class<?> clazzMobSpawnerAbstract = Class.forName("net.minecraft.server." + ver + ".MobSpawnerAbstract");
                    methodGetSpawner = clazzTileEntityMobSpawner.getDeclaredMethod("getSpawner");
                    fieldSpawner = clazzCraftCreatureSpawner.getDeclaredField("spawner");
                    fieldSpawner.setAccessible(true);
                    fieldSpawnount = clazzMobSpawnerAbstract.getDeclaredField("spawnCount");
                    fieldSpawnount.setAccessible(true);
                    fieldMaxNearbyEntities = clazzMobSpawnerAbstract.getDeclaredField("maxNearbyEntities");
                    fieldMaxNearbyEntities.setAccessible(true);
                }
                Object objcraftCreatureSpawner = clazzCraftCreatureSpawner.cast(creatureSpawner);
                Object objTileEntityMobSpawner = fieldSpawner.get(objcraftCreatureSpawner);
                Object objMobSpawnerAbstract = methodGetSpawner.invoke(objTileEntityMobSpawner);
                fieldSpawnount.set(objMobSpawnerAbstract, count);
                fieldMaxNearbyEntities.set(objMobSpawnerAbstract, max);

            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            return creatureSpawner;
        }
    }

    public static Object getNMSItemStack(ItemStack item) {
        Class<?> cis = getCraftItemStack();
        java.lang.reflect.Method methodAsNMSCopy;
        try {
            methodAsNMSCopy = cis.getMethod("asNMSCopy", ItemStack.class);
            Object answer = methodAsNMSCopy.invoke(cis, item);
            return answer;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Object getNBTTagCompound(Object nmsitem) {
        Class<?> c = nmsitem.getClass();
        java.lang.reflect.Method methodGetTag;
        try {
            methodGetTag = c.getMethod("getTag");
            return methodGetTag.invoke(nmsitem);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getCraftItemStack() {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (Exception ex) {
            System.out.println("Error in ItemNBTAPI! (Outdated plugin?)");
            ex.printStackTrace();
            return null;
        }

    }


}

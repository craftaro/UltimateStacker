package com.craftaro.ultimatestacker.api;

import com.craftaro.ultimatestacker.api.stack.block.BlockStackManager;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStackManager;
import com.craftaro.ultimatestacker.api.stack.item.ItemStackManager;
import com.craftaro.ultimatestacker.api.stack.spawner.SpawnerStackManager;

public final class UltimateStackerAPI {

    private static EntityStackManager entityStackManager;
    private static ItemStackManager itemStackManager;
    private static SpawnerStackManager spawnerStackManager;
    private static BlockStackManager blockStackManager;
    private static UltimateStackerAPI instance;

    public UltimateStackerAPI(EntityStackManager entityStackManager, ItemStackManager itemStackManager, SpawnerStackManager spawnerStackManager, BlockStackManager blockStackManager) {
        if (UltimateStackerAPI.entityStackManager != null || UltimateStackerAPI.itemStackManager != null || UltimateStackerAPI.spawnerStackManager != null || UltimateStackerAPI.blockStackManager != null) {
            throw new IllegalStateException("UltimateStackerAPI has already been initialized!");
        }
        UltimateStackerAPI.entityStackManager = entityStackManager;
        UltimateStackerAPI.itemStackManager = itemStackManager;
        UltimateStackerAPI.spawnerStackManager = spawnerStackManager;
        UltimateStackerAPI.blockStackManager = blockStackManager;
        instance = this;
    }

    public static UltimateStackerAPI getInstance() {
        return instance;
    }

    /**
     * Used to interact with EntityStacks
     * @return The EntityStackManager
     */
    public static EntityStackManager getEntityStackManager() {
        return entityStackManager;
    }

    /**
     * Used to interact with ItemStacks
     * @return The ItemStackManager
     */
    public static ItemStackManager getItemStackManager() {
        return itemStackManager;
    }

    /**
     * Used to interact with SpawnerStacks
     * @return The SpawnerStackManager
     */
    public static SpawnerStackManager getSpawnerStackManager() {
        return spawnerStackManager;
    }

    public static BlockStackManager getBlockStackManager() {
        return blockStackManager;
    }

}

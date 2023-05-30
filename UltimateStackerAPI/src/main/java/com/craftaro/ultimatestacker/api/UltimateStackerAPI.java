package com.craftaro.ultimatestacker.api;

import com.craftaro.ultimatestacker.api.stack.block.BlockStackManager;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStackManager;
import com.craftaro.ultimatestacker.api.stack.item.StackedItemManager;
import com.craftaro.ultimatestacker.api.stack.spawner.SpawnerStackManager;

public final class UltimateStackerAPI {

    private static EntityStackManager entityStackManager;
    private static StackedItemManager stackedItemManager;
    private static SpawnerStackManager spawnerStackManager;
    private static BlockStackManager blockStackManager;
    private static UltimateStackerAPI instance;

    public UltimateStackerAPI(EntityStackManager entityStackManager, StackedItemManager itemStackManager, SpawnerStackManager spawnerStackManager, BlockStackManager blockStackManager) {
        if (UltimateStackerAPI.entityStackManager != null || UltimateStackerAPI.stackedItemManager != null || UltimateStackerAPI.spawnerStackManager != null || UltimateStackerAPI.blockStackManager != null) {
            throw new IllegalStateException("UltimateStackerAPI has already been initialized!");
        }
        UltimateStackerAPI.entityStackManager = entityStackManager;
        UltimateStackerAPI.stackedItemManager = itemStackManager;
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
     * @return The StackedItemManager
     */
    public static StackedItemManager getStackedItemManager() {
        return stackedItemManager;
    }

    /**
     * Used to interact with SpawnerStacks
     * @return The SpawnerStackManager
     */
    public static SpawnerStackManager getSpawnerStackManager() {
        return spawnerStackManager;
    }

    /**
     * Used to interact with BlockStacks
     * @return The BlockStackManager
     */
    public static BlockStackManager getBlockStackManager() {
        return blockStackManager;
    }

}

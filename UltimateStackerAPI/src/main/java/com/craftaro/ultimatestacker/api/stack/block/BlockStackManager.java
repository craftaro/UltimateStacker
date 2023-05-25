package com.craftaro.ultimatestacker.api.stack.block;

import com.songoda.core.compatibility.CompatibleMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Map;

public interface BlockStackManager {

    void addBlocks(Map<Location, BlockStack> blocks);

    BlockStack addBlock(BlockStack blockStack);

    BlockStack removeBlock(Location location);

    BlockStack getBlock(Location location);

    BlockStack getBlock(Block block, CompatibleMaterial material);

    BlockStack createBlock(Location location, CompatibleMaterial material);

    BlockStack createBlock(Block block);

    boolean isBlock(Location location);

    Collection<BlockStack> getStacks();
}

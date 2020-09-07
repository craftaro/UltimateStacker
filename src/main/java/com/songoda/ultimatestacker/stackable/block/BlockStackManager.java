package com.songoda.ultimatestacker.stackable.block;

import com.songoda.core.compatibility.CompatibleMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BlockStackManager {

    private final Map<Location, BlockStack> registeredBlocks = new HashMap<>();

    public void addBlocks(Map<Location, BlockStack> blocks) {
        this.registeredBlocks.putAll(blocks);
    }

    public BlockStack addBlock(BlockStack blockStack) {
        this.registeredBlocks.put(roundLocation(blockStack.getLocation()), blockStack);
        return blockStack;
    }

    public BlockStack removeBlock(Location location) {
        return registeredBlocks.remove(roundLocation(location));
    }

    public BlockStack getBlock(Location location, CompatibleMaterial material) {
        return this.registeredBlocks.computeIfAbsent(location, b -> new BlockStack(material, location));
    }

    public BlockStack getBlock(Block block, CompatibleMaterial material) {
        return this.getBlock(block.getLocation(), material);
    }

    public boolean isBlock(Location location) {
        return this.registeredBlocks.get(location) != null;
    }

    public Collection<BlockStack> getStacks() {
        return Collections.unmodifiableCollection(this.registeredBlocks.values());
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}

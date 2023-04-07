package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.stackable.block.BlockStackManager;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkListener implements Listener {

    private final BlockStackManager blockStackManager;
    private final UltimateStacker plugin;

    public ChunkListener(UltimateStacker plugin) {
        this.blockStackManager = plugin.getBlockStackManager();
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!Settings.STACK_BLOCKS.getBoolean()) return;
        Chunk chunk = event.getChunk();
        BlockStackManager blockStackManager = plugin.getBlockStackManager();
        blockStackManager.getStacks().stream().filter(stack -> stack.getLocation().getChunk().equals(chunk)).forEach(plugin::updateHologram);
    }
}

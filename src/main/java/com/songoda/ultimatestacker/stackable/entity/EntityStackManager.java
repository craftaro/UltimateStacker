package com.songoda.ultimatestacker.stackable.entity;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityStackManager {

    // These are all stacked mobs loaded into memory.
    private static final Map<UUID, EntityStack> stacks = new HashMap<>();

    // This will only be used for stacks that have not yet been loaded into the game.
    private static final Map<UUID, ColdEntityStack> coldStacks = new HashMap<>();

    private final UltimateStacker plugin;

    public EntityStackManager(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    public EntityStack addStack(EntityStack stack) {
        stacks.put(stack.getHostEntity().getUniqueId(), stack);
        return stack;
    }

    public EntityStack addStack(LivingEntity entity) {
        if (entity == null) return null;
        EntityStack stack = new EntityStack(entity);
        plugin.getDataManager().createHostEntity(stack);
        stacks.put(entity.getUniqueId(), stack);
        return stack;
    }

    public EntityStack addStack(LivingEntity entity, int amount) {
        if (entity == null) return null;
        EntityStack stack = new EntityStack(entity);
        plugin.getDataManager().createHostEntity(stack);
        stacks.put(entity.getUniqueId(), stack);
        stack.createDuplicates(amount - 1);
        plugin.getDataManager().updateHost(stack);
        stack.updateStack();
        return stack;
    }

    @Deprecated
    public EntityStack addSerializedStack(LivingEntity entity, String customName) {
        if (customName != null && customName.contains(String.valueOf(ChatColor.COLOR_CHAR))) {
            String name = customName.replace(String.valueOf(ChatColor.COLOR_CHAR), "")
                    .replace(";", "");
            if (!name.contains(":")) return null;
            String split = name.split(":")[0];
            int amount = Methods.isInt(split) ? Integer.parseInt(split) : 0;
            return addStack(entity, amount);
        }
        return null;
    }

    @Deprecated
    public EntityStack addSerializedStack(LivingEntity entity) {
        return addSerializedStack(entity, entity.getCustomName());
    }

    public EntityStack getStack(LivingEntity entity) {
        EntityStack stack = getStack(entity.getUniqueId());
        if (stack == null) stack = addSerializedStack(entity);
        return stack;
    }

    public EntityStack getStack(UUID uuid) {
        return stacks.get(uuid);
    }

    public EntityStack removeStack(Entity entity) {
        return removeStack(entity.getUniqueId());
    }

    public EntityStack removeStack(UUID uuid) {
        EntityStack stack = stacks.remove(uuid);
        if (stack != null) {
            plugin.getDataManager().deleteHost(stack);
            stack.destroy();
        }

        return stack;
    }

    public Map<UUID, EntityStack> getStacks() {
        return Collections.unmodifiableMap(stacks);
    }

    public EntityStack updateStack(LivingEntity oldEntity, LivingEntity newEntity) {
        EntityStack stack = stacks.remove(oldEntity.getUniqueId());
        if (stack == null) return null;
        stack.setHostEntity(newEntity);
        stacks.put(newEntity.getUniqueId(), stack);
        plugin.getDataManager().updateHost(stack);
        return stack;
    }

    @Deprecated
    public boolean isStacked(UUID entity) {
        return isStackedAndLoaded(entity);
    }

    public boolean isStackedAndLoaded(LivingEntity entity) {
        return stacks.containsKey(entity.getUniqueId());
    }

    public boolean isStackedAndLoaded(UUID entity) {
        return stacks.containsKey(entity);
    }

    public boolean isEntityInColdStorage(UUID entity) {
        return coldStacks.containsKey(entity);
    }

    public boolean isEntityInColdStorage(LivingEntity entity) {
        return isEntityInColdStorage(entity.getUniqueId());
    }

    public void loadStack(LivingEntity entity) {
        ColdEntityStack coldStack = coldStacks.get(entity.getUniqueId());
        if (coldStack == null) return;
        EntityStack stack = new EntityStack(entity, coldStack);
        stack.updateStack();
        stacks.put(entity.getUniqueId(), stack);
    }

    public void unloadStack(LivingEntity entity) {
        EntityStack stack = stacks.get(entity.getUniqueId());
        if (stack == null) return;
        ColdEntityStack coldStack = new EntityStack(entity, stack);
        stack.destroy();
        coldStacks.put(entity.getUniqueId(), coldStack);
    }

    public void addStacks(Collection<ColdEntityStack> entities) {
        for (ColdEntityStack stack : entities)
            coldStacks.put(stack.hostUniqueId, stack);
    }

    public ColdEntityStack addLegacyColdStack(UUID entity, int amount) {
        ColdEntityStack stack = new ColdEntityStack(entity);
        plugin.getDataManager().createHostEntity(stack);
        stack.createDuplicates(amount - 1);
        plugin.getDataManager().updateHost(stack);
        coldStacks.put(entity, stack);
        return stack;
    }

    public void tryAndLoadColdEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof LivingEntity)
                        loadStack((LivingEntity)entity);
                }
            }
        }
    }
}

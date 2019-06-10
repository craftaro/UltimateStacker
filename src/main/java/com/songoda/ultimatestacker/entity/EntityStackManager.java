package com.songoda.ultimatestacker.entity;

import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityStackManager {

    // These are all stacked mobs loaded into memory.
    private static final Map<UUID, EntityStack> stacks = new HashMap<>();

    public EntityStack addStack(EntityStack stack) {
        stacks.put(stack.getEntityUniqueId(), stack);
        return stack;
    }

    public EntityStack addStack(Entity entity, int amount) {
        return addStack(entity.getUniqueId(), amount);
    }

    public EntityStack addStack(UUID uuid, int amount) {
        return stacks.put(uuid, new EntityStack(uuid, amount));
    }

    public EntityStack getStack(Entity entity) {
        return getStack(entity.getUniqueId());
    }

    public EntityStack getStack(UUID uuid) {
        return stacks.get(uuid);
    }

    public boolean isStacked(Entity entity) {
        return isStacked(entity.getUniqueId());
    }

    public boolean isStacked(UUID uuid) {
        return stacks.containsKey(uuid);
    }

    public EntityStack removeStack(UUID entity) {
        return stacks.remove(entity);
    }

    public EntityStack removeStack(Entity entity) {
        return stacks.remove(entity.getUniqueId());
    }

    public Map<UUID, EntityStack> getStacks() {
        return Collections.unmodifiableMap(stacks);
    }

    public EntityStack updateStack(Entity oldEntity, Entity newEntity) {
        EntityStack stack = stacks.remove(oldEntity.getUniqueId());
        if (stack == null) return null;
        stack.setEntity(newEntity);
        stacks.put(newEntity.getUniqueId(), stack);
        return stack;
    }


}

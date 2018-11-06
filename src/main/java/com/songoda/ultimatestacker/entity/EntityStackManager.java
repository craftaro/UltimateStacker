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
        stacks.put(stack.getEntity().getUniqueId(), stack);
        return stack;
    }

    public EntityStack addStack(Entity entity, int amount) {
        return stacks.put(entity.getUniqueId(), new EntityStack(entity, amount));
    }

    public EntityStack getStack(Entity entity) {
        return stacks.get(entity.getUniqueId());
    }

    public boolean isStacked(Entity entity) {
        return stacks.containsKey(entity.getUniqueId());
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

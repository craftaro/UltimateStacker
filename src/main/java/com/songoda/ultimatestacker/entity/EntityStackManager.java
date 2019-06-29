package com.songoda.ultimatestacker.entity;

import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.ChatColor;
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
        EntityStack stack = new EntityStack(uuid, amount);
        stacks.put(uuid, new EntityStack(uuid, amount));
        return stack;
    }

    public EntityStack addSerializedStack(Entity entity, String customName) {
        if (customName != null && customName.contains(String.valueOf(ChatColor.COLOR_CHAR))) {
            String name = customName.replace(String.valueOf(ChatColor.COLOR_CHAR), "")
                    .replace(";", "");
            if (!name.contains(":")) return null;
            String split = name.split(":")[0];
            int amount = Methods.isInt(split) ? Integer.parseInt(split) : 0;
            addStack(entity, amount);
        }
        return null;
    }

    public EntityStack addSerializedStack(Entity entity) {
        return addSerializedStack(entity, entity.getCustomName());
    }

    public EntityStack getStack(Entity entity) {
        EntityStack stack = getStack(entity.getUniqueId());
        if (stack == null) stack = addSerializedStack(entity);
        return stack;
    }

    public EntityStack getStack(UUID uuid) {
        return stacks.get(uuid);
    }

    public boolean isStacked(Entity entity) {
        if (entity == null) return false;
        boolean isStacked = isStacked(entity.getUniqueId());
        if (!isStacked && addSerializedStack(entity) != null) {
            return true;
        }
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

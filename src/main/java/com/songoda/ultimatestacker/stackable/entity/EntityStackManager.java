package com.songoda.ultimatestacker.stackable.entity;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityStackManager {

    private final UltimateStacker plugin;

    public EntityStackManager(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    public EntityStack createStack(LivingEntity entity, int amount) {
        return new EntityStack(entity, amount);
    }

    public boolean isStackedEntity(Entity entity) {
        return entity.hasMetadata("US_AMOUNT");
    }

    public int getAmount(Entity entity) {
        if (!isStackedEntity(entity)) return 1;
        if (entity.getMetadata("US_AMOUNT").isEmpty()) return 1;
        return entity.getMetadata("US_AMOUNT").get(0).asInt();
    }

    public EntityStack addStack(LivingEntity entity) {
        return addStack(entity, getAmount(entity) == 1 ? 1 : getAmount(entity));
    }

    public EntityStack addStack(LivingEntity entity, int amount) {
        if (entity == null) return null;
        if (isStackedEntity(entity)) {
            EntityStack stack = getStack(entity);
            stack.addEntityToStack(amount);
            return stack;
        }
        return null;
    }

    public EntityStack getStack(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return null;
        if (isStackedEntity(entity)) {
            if (entity instanceof LivingEntity) {
                return new EntityStack((LivingEntity) entity);
            }
        }
        return null;
    }

    public EntityStack getStack(LivingEntity entity) {
        if (!isStackedEntity(entity)) return null;
        return new EntityStack(entity);
    }

    public EntityStack decreaseStack(Entity entity) {
        EntityStack stack = getStack((LivingEntity) entity);
        if (stack == null) return null;
        stack.removeEntityFromStack(1);
        return stack;
    }

    public EntityStack decreaseStack(Entity entity, int amount) {
        EntityStack stack = getStack((LivingEntity) entity);
        if (stack == null) return null;
        stack.removeEntityFromStack(amount);
        return stack;
    }

    public EntityStack updateStack(LivingEntity entity) {
        EntityStack stack = getStack(entity);
        if (stack == null) return null;
        stack.updateNameTag();
        return stack;
    }

    public EntityStack updateStack(LivingEntity oldEntity, LivingEntity newEntity) {
        EntityStack stack = getStack(oldEntity);
        if (stack == null) return null;
        int amount = stack.getAmount();
        stack.destroy();
        return createStack(newEntity, amount);
    }

    public void setStack(LivingEntity newEntity, int amount) {
        if (isStackedEntity(newEntity)) {
            EntityStack stack = getStack(newEntity);
            stack.setAmount(amount);
            System.err.println("Stacked entity already exists, updating stack amount to " + amount);
        } else {
            createStack(newEntity, amount);
        }
    }
}

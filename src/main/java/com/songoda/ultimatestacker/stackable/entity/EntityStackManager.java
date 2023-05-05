package com.songoda.ultimatestacker.stackable.entity;

import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

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

    public String getLastPlayerDamage(Entity entity) {
        if (!entity.hasMetadata("US_LAST_PLAYER_DAMAGE")) return null;
        if (entity.getMetadata("US_LAST_PLAYER_DAMAGE").isEmpty()) return null;
        return entity.getMetadata("US_LAST_PLAYER_DAMAGE").get(0).asString();
    }

    public void setLastPlayerDamage(Entity entity, Player player) {
        if (player == null) return;
        if (entity == null) return;
        if (entity instanceof Player) return;
        entity.setMetadata("US_LAST_PLAYER_DAMAGE", new FixedMetadataValue(plugin, player.getName()));
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

    /**
     * Transfers the stack from one entity to another.
     * (e.g. slimes split)
     * @param oldEntity The old entity to transfer the stack from.
     * @param newEntity The new entity to transfer the stack to.
     * @return The new stack.
     */
    public EntityStack transferStack(LivingEntity oldEntity, LivingEntity newEntity, boolean takeOne) {
        EntityStack stack = getStack(oldEntity);
        if (stack == null) return null;
        EntityStack newStack = new EntityStack(newEntity, takeOne ? stack.getAmount()-1 : stack.getAmount());
        stack.destroy();
        return newStack;
    }

    public EntityStack updateStack(LivingEntity oldEntity, LivingEntity newEntity) {
        EntityStack stack = getStack(oldEntity);
        if (stack == null) return null;
        int amount = stack.getAmount()-1;
        stack.destroy();
        if (amount == 0 && newEntity != null) {
            newEntity.remove();
            return null;
        }
        return createStack(newEntity, amount);
    }

    public void setStack(LivingEntity newEntity, int amount) {
        if (amount <= 0) return;
        if (isStackedEntity(newEntity)) {
            EntityStack stack = getStack(newEntity);
            stack.setAmount(amount);
        } else {
            createStack(newEntity, amount);
        }
    }
}

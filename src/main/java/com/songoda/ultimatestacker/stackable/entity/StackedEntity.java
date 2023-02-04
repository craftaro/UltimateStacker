package com.songoda.ultimatestacker.stackable.entity;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class StackedEntity {

    protected int amount;
    protected LivingEntity hostEntity;

    /**
     * Gets an existing stack from an entity or creates a new one if it doesn't exist.
     * @param entity The entity to get the stack from.
     */
    public StackedEntity(LivingEntity entity) {
        if (entity == null) return;
        if (!UltimateStacker.getInstance().getEntityStackManager().isStackedEntity(entity)) {
            entity.setMetadata("US_AMOUNT", new FixedMetadataValue(UltimateStacker.getInstance(), 1));
            this.amount = 1;
            updateNameTag();
        } else {
            //get the amount from the entity
            this.amount = UltimateStacker.getInstance().getEntityStackManager().getAmount(entity);
        }
        this.hostEntity = entity;
    }

    /**
     * Creates a new stack or overrides an existing stack.
     * @param entity The entity to create the stack for.
     * @param amount The amount of entities in the stack.
     */
    public StackedEntity(LivingEntity entity, int amount) {
        if (entity == null) return;
        this.hostEntity = entity;
        this.amount = amount;
        entity.setMetadata("US_AMOUNT", new FixedMetadataValue(UltimateStacker.getInstance(), amount));
        updateNameTag();
    }


    public EntityType getType() {
        return hostEntity.getType();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.hostEntity.setMetadata("US_AMOUNT", new FixedMetadataValue(UltimateStacker.getInstance(), amount));
        updateNameTag();
    }

    public UUID getUuid() {
        return hostEntity.getUniqueId();
    }

    public LivingEntity getHostEntity() {
        return hostEntity;
    }

    protected void updateNameTag() {
        if (hostEntity == null) {
            return;
        }
        hostEntity.setCustomNameVisible(!Settings.HOLOGRAMS_ON_LOOK_ENTITY.getBoolean());
        hostEntity.setCustomName(Methods.compileEntityName(hostEntity, getAmount()));
    }
}

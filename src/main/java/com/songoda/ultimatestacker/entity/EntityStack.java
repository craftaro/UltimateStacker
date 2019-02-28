package com.songoda.ultimatestacker.entity;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class EntityStack {

    private UUID entity;
    private int amount;

    public EntityStack(Entity entity, int amount) {
        this.entity = entity.getUniqueId();
        this.setAmount(amount);
    }

    public EntityStack(UUID uuid, int amount) {
        this.entity = uuid;
        this.setAmount(amount);
    }

    public void updateStack() {
        Entity entity = Bukkit.getEntity(this.entity);
        if (entity == null) return;

        entity.setCustomNameVisible(true);
        entity.setCustomName(Methods.compileEntityName(entity, amount));
    }

    public Entity getEntity() {
        Entity entity = Bukkit.getEntity(this.entity);
        if (entity == null) {
            UltimateStacker.getInstance().getEntityStackManager().removeStack(this.entity);
            return null;
        }
        return entity;
    }

    protected void setEntity(Entity entity) {
        this.entity = entity.getUniqueId();
    }

    public void addAmount(int amount) {
        this.amount = this.amount + amount;
        updateStack();
    }

    public UUID getEntityUniqueId() {
        return entity;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        if (amount == 1) {
            Entity entity = Bukkit.getEntity(this.entity);
            if (entity == null) return;

            UltimateStacker.getInstance().getEntityStackManager().removeStack(this.entity);
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            return;
        }
        this.amount = amount;
        if (amount != 0)
            updateStack();
    }

    @Override
    public String toString() {
        return "EntityStack:{"
                + "Entity:\"" + entity.toString() + "\","
                + "Amount:\"" + amount + "\","
                + "}";
    }
}

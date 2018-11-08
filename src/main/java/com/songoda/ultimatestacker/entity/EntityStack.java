package com.songoda.ultimatestacker.entity;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.entity.Entity;

public class EntityStack {

    private Entity entity;
    private int amount;

    public EntityStack(Entity entity, int amount) {
        this.entity = entity;
        this.amount = amount;
    }

    public void updateStack() {
        entity.setCustomNameVisible(true);
        entity.setCustomName(Methods.compileEntityName(entity, amount));
    }

    public Entity getEntity() {
        return entity;
    }

    protected void setEntity(Entity entity) {
        this.entity = entity;
    }

    public void addAmount(int amount) {
        this.amount = this.amount + amount;
        updateStack();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        if (amount == 1) {
            UltimateStacker.getInstance().getEntityStackManager().removeStack(entity);
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            return;
        }
        this.amount = amount;
        updateStack();
    }

    @Override
    public String toString() {
        return "EntityStack:{"
                + "Entity:\"" + entity.getUniqueId().toString() + "\","
                + "Amount:\"" + amount + "\","
                + "}";
    }
}

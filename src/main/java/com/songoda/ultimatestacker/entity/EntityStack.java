package com.songoda.ultimatestacker.entity;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.ServerVersion;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.UUID;
public class EntityStack {

    private UUID entity;
    private int amount;

    public EntityStack(Entity entity, int amount) {
        this(entity.getUniqueId(), amount);
    }

    public EntityStack(UUID uuid, int amount) {
        this.entity = uuid;
        this.setAmount(amount);
    }

    public void updateStack() {
        Entity entity = getEntityByUniqueId(this.entity);
        if (entity == null) return;

        entity.setCustomNameVisible(!Setting.HOLOGRAMS_ON_LOOK_ENTITY.getBoolean());
        entity.setCustomName(Methods.compileEntityName(entity, amount));
    }

    public Entity getEntity() {
        Entity entity = getEntityByUniqueId(this.entity);
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
            Entity entity = getEntityByUniqueId(this.entity);
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

    public Entity getEntityByUniqueId(UUID uniqueId) {
        if (UltimateStacker.getInstance().isServerVersionAtLeast(ServerVersion.V1_12))
            return Bukkit.getEntity(uniqueId);

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uniqueId))
                    return entity;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "EntityStack:{"
                + "Entity:\"" + entity.toString() + "\","
                + "Amount:\"" + amount + "\","
                + "}";
    }
}

package com.craftaro.ultimatestacker.api.stack.entity;

import com.craftaro.ultimatestacker.api.utils.StackableEntity;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public interface EntityStack extends StackableEntity {


    EntityType getType();

    UUID getUuid();

    LivingEntity getHostEntity();

    LivingEntity takeOneAndSpawnEntity(Location location);

    void releaseHost();

    void destroy();
}

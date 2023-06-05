package com.craftaro.ultimatestacker.api.stack.spawner;

import com.craftaro.ultimatestacker.api.utils.Hologramable;
import com.craftaro.ultimatestacker.api.utils.Stackable;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.database.Data;
import com.songoda.core.nms.world.SpawnedEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.Set;

public interface SpawnerStack extends Stackable, Hologramable, Data {

    int getAmount();

    boolean isValid();

    void setAmount(int amount);

    int calculateSpawnCount(EntityType type);

    int getId();

    void setId(int id);

    Location getLocation();

    String getHologramName();

    boolean areHologramsEnabled();

    int getX();

    int getY();

    int getZ();

    World getWorld();
    
    String getHologramId();

    int spawn(int amountToSpawn, EntityType... types);

    int spawn(int amountToSpawn, String particle, Set<CompatibleMaterial> canSpawnOn, SpawnedEntity spawned, EntityType... types);
}

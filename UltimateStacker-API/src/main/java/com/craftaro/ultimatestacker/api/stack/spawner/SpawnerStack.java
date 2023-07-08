package com.craftaro.ultimatestacker.api.stack.spawner;

import com.craftaro.core.database.Data;
import com.craftaro.core.nms.world.SpawnedEntity;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatestacker.api.utils.Hologramable;
import com.craftaro.ultimatestacker.api.utils.Stackable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.Set;

/**
 * Represents a stack of spawners
 */
public interface SpawnerStack extends Stackable, Hologramable, Data {

    /**
     * Get the id of this stack
     * @return The id of this stack
     */
    int getId();

    /**
     * Calculate the amount of entities that will spawn from this stack
     * @param type The type of entity to calculate for
     * @param ignoreRestrictions Weather or not to ignore max stack size and if the entity is stackable
     * @return The calculated amount
     */
    int calculateSpawnCount(EntityType type, boolean ignoreRestrictions);

    /**
     * Spawn the entities for this SpawnerStack
     * @return The amount of entities that spawned
     */
    int spawn();

    /**
     * Spawn the entities for this SpawnerStack
     * @param noAI Weather or not to spawn the entities with no AI
     * @return The amount of entities that spawned
     */
    int spawn(boolean noAI);

    /**
     * Spawn the entities for this SpawnerStack
     * @param amount The amount of entities to spawn
     * @param noAI Weather or not to spawn the entities with no AI
     * @return The amount of entities that spawned
     */
    int spawn(int amount, boolean noAI);

    /**
     * Spawn the entities for this stack
     * @return The amount of entities that spawned
     */
    int spawn(int amountToSpawn, EntityType... types);

    /**
     * Spawn the entities for this stack
     * @return The location of this stack
     */
    int spawn(int amountToSpawn, String particle, Set<XMaterial> canSpawnOn, SpawnedEntity spawned, EntityType... types);
}

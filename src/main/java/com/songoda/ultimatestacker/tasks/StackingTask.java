package com.songoda.ultimatestacker.tasks;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.hooks.WorldGuardHook;
import com.songoda.core.world.SWorld;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.stackable.entity.Check;
import com.songoda.ultimatestacker.stackable.entity.EntityStack;
import com.songoda.ultimatestacker.stackable.entity.EntityStackManager;
import com.songoda.ultimatestacker.stackable.entity.StackedEntity;
import com.songoda.ultimatestacker.stackable.entity.custom.CustomEntity;
import com.songoda.ultimatestacker.utils.Async;
import com.songoda.ultimatestacker.utils.CachedChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Cat;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class StackingTask extends BukkitRunnable {

    private final UltimateStacker plugin;

    private final EntityStackManager stackManager;

    private final ConfigurationSection configurationSection = UltimateStacker.getInstance().getMobFile();
    private final List<UUID> processed = new ArrayList<>();

    private final Map<CachedChunk, Entity[]> cachedChunks = new HashMap<>();

    private final Map<EntityType, Integer> entityStackSizes = new HashMap<>();
    private final int maxEntityStackSize = Settings.MAX_STACK_ENTITIES.getInt(),
            minEntityStackSize = Settings.MIN_STACK_ENTITIES.getInt(),
            searchRadius = Settings.SEARCH_RADIUS.getInt(),
            maxPerTypeStacksPerChunk = Settings.MAX_PER_TYPE_STACKS_PER_CHUNK.getInt();
    private final List<String> disabledWorlds = Settings.DISABLED_WORLDS.getStringList(),
            stackReasons = Settings.STACK_REASONS.getStringList();
    private final List<Check> checks = Check.getChecks(Settings.STACK_CHECKS.getStringList());
    private final boolean stackFlyingDown = Settings.ONLY_STACK_FLYING_DOWN.getBoolean(),
            stackWholeChunk = Settings.STACK_WHOLE_CHUNK.getBoolean(),
            weaponsArentEquipment = Settings.WEAPONS_ARENT_EQUIPMENT.getBoolean(),
            onlyStackFromSpawners = Settings.ONLY_STACK_FROM_SPAWNERS.getBoolean(),
            onlyStackOnSurface = Settings.ONLY_STACK_ON_SURFACE.getBoolean();

    Set<SWorld> loadedWorlds = new HashSet<>();

    public StackingTask(UltimateStacker plugin) {
        this.plugin = plugin;
        this.stackManager = plugin.getEntityStackManager();

        // Add loaded worlds.
        for (World world : Bukkit.getWorlds())
            loadedWorlds.add(new SWorld(world));

        // Start the stacking task.
        runTaskTimerAsynchronously(plugin, 0, Settings.STACK_SEARCH_TICK_SPEED.getInt());
    }

    @Override
    public void run() {
        // Should entities be stacked?
        if (!Settings.STACK_ENTITIES.getBoolean()) return;

        // Loop through each world.
        for (SWorld sWorld : loadedWorlds) {
            // If world is disabled then continue to the next world.
            if (isWorldDisabled(sWorld.getWorld())) continue;

            // Get the loaded entities from the current world and reverse them.
            List<LivingEntity> entities;
            try {
                entities = getLivingEntitiesSync(sWorld).get();
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
                continue;
            }
            Collections.reverse(entities);

            // Loop through the entities.
            for (LivingEntity entity : entities) {
                // Get entity location to pass around as its faster this way.
                Location location = entity.getLocation();

                // Check to see if entity is not stackable.
                if (!isEntityStackable(entity))
                    continue;

                // Make sure our entity has not already been processed.
                // Skip it if it has been.
                if (this.processed.contains(entity.getUniqueId())) continue;

                // Process the entity.
                this.processEntity(entity, sWorld, location);
            }
        }
        // Clear caches in preparation for the next run.
        this.processed.clear();
        this.cachedChunks.clear();
    }

    private Future<List<LivingEntity>> getLivingEntitiesSync(SWorld sWorld) {
        CompletableFuture<List<LivingEntity>> future = new CompletableFuture<>();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> future.complete(sWorld.getLivingEntities()));

        return future;
    }

    private Future<Entity[]> getEntitiesInChunkSync(CachedChunk cachedChunk) {
        CompletableFuture<Entity[]> future = new CompletableFuture<>();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> future.complete(cachedChunk.getEntities()));

        return future;
    }

    public boolean isWorldDisabled(World world) {
        return disabledWorlds.stream().anyMatch(worldStr -> world.getName().equalsIgnoreCase(worldStr));
    }

    private boolean isEntityStackable(Entity entity) {
        // Make sure we have the correct entity type and that it is valid.
        if (!entity.isValid()
                || entity instanceof HumanEntity
                || entity instanceof ArmorStand

                // Make sure the entity is not in love.
                || entity.hasMetadata("inLove")
                // Or in breeding cooldown.
                || entity.hasMetadata("breedCooldown"))
            return false;

        // Allow spawn if stackreasons are set and match, or if from a spawner
        final String spawnReason = entity.hasMetadata("US_REASON") && !entity.getMetadata("US_REASON").isEmpty()
                ? entity.getMetadata("US_REASON").get(0).asString() : null;
        List<String> stackReasons;
        if (onlyStackFromSpawners) {
            // If only stack from spawners is enabled, make sure the entity spawned from a spawner.
            if (!"SPAWNER".equals(spawnReason))
                return false;
        } else if (!(stackReasons = this.stackReasons).isEmpty() && !stackReasons.contains(spawnReason))
            // Only stack if on the list of events to stack
            return false;

        // Cast our entity to living entity.
        LivingEntity livingEntity = (LivingEntity) entity;

        // If only stack on surface is enabled make sure the entity is on a surface then entity is stackable.
        return !onlyStackOnSurface
                || canFly(livingEntity)
                || entity.getType().name().equals("SHULKER")

                || (livingEntity.isOnGround()
                || (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                && livingEntity.isSwimming()));

    }

    private void processEntity(LivingEntity livingEntity, SWorld sWorld, Location location) {
        // Get the stack from the entity. It should be noted that this value will
        // be null if our entity is not a stack.
        EntityStack stack = plugin.getEntityStackManager().getStack(livingEntity);

        // Is this entity stacked?
        boolean isStack = stack != null;

        // The amount that is stackable.
        int amountToStack = isStack ? stack.getAmount() : 1;

        // Attempt to split our stack. If the split is successful then skip this entity.
        if (isStack && attemptSplit(stack, livingEntity)) return;

        // If this entity is named, a custom entity or disabled then skip it.
        if (!isStack && (livingEntity.getCustomName() != null
                && plugin.getCustomEntityManager().getCustomEntity(livingEntity) == null)
                || !configurationSection.getBoolean("Mobs." + livingEntity.getType().name() + ".Enabled"))
            return;

        // Get the maximum stack size for this entity.
        int maxEntityStackSize = getEntityStackSize(livingEntity);

        // Get similar entities around our entity and make sure those entities are both compatible and stackable.
        List<LivingEntity> stackableFriends = new LinkedList<>();
        for (LivingEntity entity : getSimilarEntitiesAroundEntity(livingEntity, sWorld, location)) {
            // Check to see if entity is not stackable.
            if (!isEntityStackable(entity))
                continue;
            // Add this entity to our stackable friends.
            stackableFriends.add(entity);
        }

        // Loop through our similar stackable entities.
        for (LivingEntity entity : stackableFriends) {
            // Make sure the entity has not already been processed.
            if (this.processed.contains(entity.getUniqueId())) continue;

            // Check our WorldGuard flag.
            Boolean flag = WorldGuardHook.isEnabled() ? WorldGuardHook.getBooleanFlag(livingEntity.getLocation(), "mob-stacking") : null;
            if (flag != null && !flag)
                continue;

            // Get this entities friendStack.
            EntityStack friendStack = stackManager.getStack(entity);

            // Check to see if this entity is stacked and friendStack plus
            // our amount to stack is not above our max friendStack size
            // for this entity.
            if (friendStack != null && (friendStack.getAmount() + amountToStack) <= maxEntityStackSize) {

                // If we are a stack lets merge our stack with the just found friend stack.
                if (isStack) {
                    // Get the host entity.
                    StackedEntity host = stack.getHostAsStackedEntity();
                    // Get all the stacked entities in our stack and add them to a list.
                    List<StackedEntity> entities = stack.takeAllEntities();
                    // Add the host to this list.
                    entities.add(host);
                    // Add the collected entities to the new stack.
                    friendStack.addEntitiesToStackSilently(entities);
                    // Update friend stack to display changes.
                    friendStack.updateStack();
                    // Push changes to the database.
                    plugin.getDataManager().createStackedEntities(friendStack, entities);
                } else {
                    // If we are not stacked add ourselves to the found friendStack.
                    plugin.getDataManager().createStackedEntity(friendStack, friendStack.addEntityToStack(livingEntity));
                }

                // Drop lead if applicable then remove our entity and mark it as processed.
                if (livingEntity.isLeashed())
                    Bukkit.getScheduler().runTask(plugin, () -> livingEntity.getWorld()
                            .dropItemNaturally(livingEntity.getLocation(), CompatibleMaterial.LEAD.getItem()));
                Bukkit.getScheduler().runTask(plugin, livingEntity::remove);
                processed.add(livingEntity.getUniqueId());

                return;
            } else if (friendStack == null
                    && isStack
                    && (stack.getAmount() + 1) <= maxEntityStackSize
                    && canFly(entity)
                    && Settings.ONLY_STACK_FLYING_DOWN.getBoolean()
                    && location.getY() > entity.getLocation().getY()) {

                // Make the friend the new stack host.
                EntityStack newStack = stackManager.updateStack(livingEntity, entity);

                if (newStack == null) {
                    continue;
                }

                // Add our entity to that stack
                plugin.getDataManager().createStackedEntity(newStack, newStack.addEntityToStack(livingEntity));

                // Remove our entity and mark it as processed.
                Bukkit.getScheduler().runTask(plugin, livingEntity::remove);
                processed.add(livingEntity.getUniqueId());
                return;
            }
        }

        // If our entity is stacked then skip this entity.
        if (isStack) return;

        // Check our WorldGuard flag.
        Boolean flag = WorldGuardHook.isEnabled() ? WorldGuardHook.getBooleanFlag(livingEntity.getLocation(), "mob-stacking") : null;
        if (flag != null && !flag)
            return;

        // Remove all stacked entities from our stackable friends.
        stackableFriends.removeIf(stackManager::isStackedAndLoaded);

        // If the stack cap is met then delete this entity.
        if (maxPerTypeStacksPerChunk != -1
                && (getSimilarStacksInChunk(sWorld, livingEntity) + 1) > maxPerTypeStacksPerChunk) {
            Bukkit.getScheduler().runTask(plugin, livingEntity::remove);
            this.processed.add(livingEntity.getUniqueId());
            return;
        }

        // If there are none or not enough stackable friends left to create a new entity,
        // the stack sizes overlap then skip this entity.
        if (stackableFriends.isEmpty()
                || stackableFriends.size() < minEntityStackSize - 1
                || minEntityStackSize > maxEntityStackSize) return;

        // If a stack was never found create a new one.
        EntityStack newStack = stackManager.addStack(livingEntity);

        List<LivingEntity> livingEntities = new LinkedList<>();

        // Loop through the unstacked and unprocessed stackable friends while not creating
        // a stack larger than the maximum.
        stackableFriends.stream().filter(entity -> !stackManager.isStackedAndLoaded(entity)
                && !this.processed.contains(entity.getUniqueId())).limit(maxEntityStackSize).forEach(entity -> {

            // Make sure we're not naming some poor kids pet.
            if (entity.getCustomName() != null
                    && plugin.getCustomEntityManager().getCustomEntity(entity) == null) {
                processed.add(livingEntity.getUniqueId());
                newStack.destroy();
                return;
            }

            // Drop lead if applicable then remove our entity and mark it as processed.
            if (entity.isLeashed()) {
                Bukkit.getScheduler().runTask(plugin, () -> entity.getWorld().dropItemNaturally(entity.getLocation(), CompatibleMaterial.LEAD.getItem()));
            }
            livingEntities.add(entity);
            Bukkit.getScheduler().runTask(plugin, entity::remove);
            processed.add(entity.getUniqueId());

        });

        // Add our new approved entities to the new stack and commit them to the database.
        plugin.getDataManager().createStackedEntities(newStack,
                newStack.addRawEntitiesToStackSilently(livingEntities));

        // Update our stack.
        newStack.updateStack();
    }

    public boolean attemptSplit(EntityStack stack, LivingEntity livingEntity) {
        int stackSize = stack.getAmount();
        int maxEntityStackAmount = getEntityStackSize(livingEntity);

        if (stackSize <= maxEntityStackAmount) return false;

        // Destroy the stack.
        stack.destroy();

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (int i = stackSize; i > 0; i -= maxEntityStackAmount) {
                LivingEntity entity = stack.takeOneAndSpawnEntity(livingEntity.getLocation());
                if (entity == null) continue;
                EntityStack newStack = plugin.getEntityStackManager().addStack(entity);
                newStack.moveEntitiesFromStack(stack, Math.min(i, maxEntityStackAmount) - 1);
                newStack.updateStack();
            }
        });

        // Remove our entity and mark it as processed.
        Bukkit.getScheduler().runTask(plugin, livingEntity::remove);
        processed.add(livingEntity.getUniqueId());
        return true;
    }

    private Set<CachedChunk> getNearbyChunks(SWorld sWorld, Location location, double radius, boolean singleChunk) {
        World world = location.getWorld();
        Set<CachedChunk> chunks = new HashSet<>();
        if (world == null) return chunks;

        CachedChunk firstChunk = new CachedChunk(sWorld, location);
        chunks.add(firstChunk);

        if (singleChunk) return chunks;

        int minX = (int) Math.floor(((location.getX() - radius) - 2.0D) / 16.0D);
        int maxX = (int) Math.floor(((location.getX() + radius) + 2.0D) / 16.0D);
        int minZ = (int) Math.floor(((location.getZ() - radius) - 2.0D) / 16.0D);
        int maxZ = (int) Math.floor(((location.getZ() + radius) + 2.0D) / 16.0D);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                if (firstChunk.getX() == x && firstChunk.getZ() == z) continue;
                chunks.add(new CachedChunk(sWorld, x, z));
            }
        }
        return chunks;
    }

    private List<LivingEntity> getNearbyEntities(SWorld sWorld, Location location, double radius, boolean singleChunk) {
        List<LivingEntity> entities = new ArrayList<>();
        for (CachedChunk chunk : getNearbyChunks(sWorld, location, radius, singleChunk)) {
            if (chunk == null) continue;
            Entity[] entityArray;
            if (cachedChunks.containsKey(chunk)) {
                entityArray = cachedChunks.get(chunk);
            } else {
                try {
                    entityArray = getEntitiesInChunkSync(chunk).get();
                    cachedChunks.put(chunk, entityArray);
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                    continue;
                }
            }

            if (entityArray == null) continue;

            for (Entity e : entityArray) {
                if (e == null) continue;
                if (e.getWorld() != location.getWorld()
                        || !(e instanceof LivingEntity)
                        || (!singleChunk && location.distanceSquared(e.getLocation()) >= radius * radius)) continue;
                entities.add((LivingEntity) e);
            }
        }

        return entities;
    }

    public int getSimilarStacksInChunk(SWorld sWorld, LivingEntity entity) {
        int count = 0;
        for (LivingEntity e : getNearbyEntities(sWorld, entity.getLocation(), -1, true)) {
            if (entity.getType() == e.getType() && plugin.getEntityStackManager().isStackedAndLoaded(e))
                count++;
        }
        return count;
    }

    public List<LivingEntity> getSimilarEntitiesAroundEntity(LivingEntity initialEntity, SWorld sWorld, Location location) {
        // Create a list of all entities around the initial entity of the same type.
        List<LivingEntity> entityList = new LinkedList<>();

        for (LivingEntity entity : getNearbyEntities(sWorld, location, searchRadius, stackWholeChunk)) {
            if (entity.getType() != initialEntity.getType() || entity == initialEntity)
                continue;
            entityList.add(entity);
        }

        CustomEntity customEntity = plugin.getCustomEntityManager().getCustomEntity(initialEntity);
        if (customEntity != null)
            entityList.removeIf(entity -> !customEntity.isSimilar(initialEntity, entity));

        if (stackFlyingDown && canFly(initialEntity))
            entityList.removeIf(entity -> entity.getLocation().getY() > initialEntity.getLocation().getY());

        for (Check check : checks) {
            if (check == null) continue;
            switch (check) {
                case SPAWN_REASON: {
                    if (initialEntity.hasMetadata("US_REASON"))
                        entityList.removeIf(entity -> entity.hasMetadata("US_REASON") && !entity.getMetadata("US_REASON").get(0).asString().equals("US_REASON"));
                }
                case AGE: {
                    if (!(initialEntity instanceof Ageable)) break;

                    if (((Ageable) initialEntity).isAdult()) {
                        entityList.removeIf(entity -> !((Ageable) entity).isAdult());
                    } else {
                        entityList.removeIf(entity -> ((Ageable) entity).isAdult());
                    }
                    break;
                }
                case NERFED: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) break;
                    entityList.removeIf(entity -> entity.hasAI() != initialEntity.hasAI());
                }
                case IS_TAMED: {
                    if (!(initialEntity instanceof Tameable)) break;
                    if (((Tameable) initialEntity).isTamed()) {
                        entityList.removeIf(entity -> !((Tameable) entity).isTamed());
                    } else {
                        entityList.removeIf(entity -> ((Tameable) entity).isTamed());
                    }
                }
                case ANIMAL_OWNER: {
                    if (!(initialEntity instanceof Tameable)) break;

                    Tameable tameable = ((Tameable) initialEntity);
                    entityList.removeIf(entity -> ((Tameable) entity).getOwner() != tameable.getOwner());
                }
                case PIG_SADDLE: {
                    if (!(initialEntity instanceof Pig)) break;
                    entityList.removeIf(entity -> ((Pig) entity).hasSaddle());
                    break;
                }
                case SKELETON_TYPE: {
                    if (!(initialEntity instanceof Skeleton)) break;

                    Skeleton skeleton = (Skeleton) initialEntity;
                    entityList.removeIf(entity -> ((Skeleton) entity).getSkeletonType() != skeleton.getSkeletonType());
                    break;
                }
                case SHEEP_COLOR: {
                    if (!(initialEntity instanceof Sheep)) break;

                    Sheep sheep = ((Sheep) initialEntity);
                    entityList.removeIf(entity -> ((Sheep) entity).getColor() != sheep.getColor());
                    break;
                }
                case SHEEP_SHEARED: {
                    if (!(initialEntity instanceof Sheep)) break;

                    Sheep sheep = ((Sheep) initialEntity);
                    if (sheep.isSheared()) {
                        entityList.removeIf(entity -> !((Sheep) entity).isSheared());
                    } else {
                        entityList.removeIf(entity -> ((Sheep) entity).isSheared());
                    }
                    break;
                }
                case SNOWMAN_DERPED: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)
                            || !(initialEntity instanceof Snowman)) break;

                    Snowman snowman = ((Snowman) initialEntity);
                    if (snowman.isDerp()) {
                        entityList.removeIf(entity -> !((Snowman) entity).isDerp());
                    } else {
                        entityList.removeIf(entity -> ((Snowman) entity).isDerp());
                    }
                    break;
                }
                case LLAMA_COLOR: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)
                            || !(initialEntity instanceof Llama)) break;
                    Llama llama = ((Llama) initialEntity);
                    entityList.removeIf(entity -> ((Llama) entity).getColor() != llama.getColor());
                    break;
                }
                case LLAMA_STRENGTH: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)
                            || !(initialEntity instanceof Llama)) break;
                    Llama llama = ((Llama) initialEntity);
                    entityList.removeIf(entity -> ((Llama) entity).getStrength() != llama.getStrength());
                    break;
                }
                case VILLAGER_PROFESSION: {
                    if (!(initialEntity instanceof Villager)) break;
                    Villager villager = ((Villager) initialEntity);
                    entityList.removeIf(entity -> ((Villager) entity).getProfession() != villager.getProfession());
                    break;
                }
                case SLIME_SIZE: {
                    if (!(initialEntity instanceof Slime)) break;
                    Slime slime = ((Slime) initialEntity);
                    entityList.removeIf(entity -> ((Slime) entity).getSize() != slime.getSize());
                    break;
                }
                case HORSE_CARRYING_CHEST: {
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
                        if (!(initialEntity instanceof ChestedHorse)) break;
                        entityList.removeIf(entity -> ((ChestedHorse) entity).isCarryingChest());
                    } else {
                        if (!(initialEntity instanceof Horse)) break;
                        entityList.removeIf(entity -> ((Horse) entity).isCarryingChest());
                    }
                    break;
                }
                case HORSE_HAS_ARMOR: {
                    if (!(initialEntity instanceof Horse)) break;
                    entityList.removeIf(entity -> ((Horse) entity).getInventory().getArmor() != null);
                    break;
                }
                case HORSE_HAS_SADDLE: {
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            && initialEntity instanceof AbstractHorse) {
                        entityList.removeIf(entity -> ((AbstractHorse) entity).getInventory().getSaddle() != null);
                        break;
                    }
                    if (!(initialEntity instanceof Horse)) break;
                    entityList.removeIf(entity -> ((Horse) entity).getInventory().getSaddle() != null);
                    break;
                }
                case HORSE_JUMP: {
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
                        if (!(initialEntity instanceof AbstractHorse)) break;
                        AbstractHorse horse = ((AbstractHorse) initialEntity);
                        entityList.removeIf(entity -> ((AbstractHorse) entity).getJumpStrength() != horse.getJumpStrength());
                    } else {
                        if (!(initialEntity instanceof Horse)) break;
                        Horse horse = ((Horse) initialEntity);
                        entityList.removeIf(entity -> ((Horse) entity).getJumpStrength() != horse.getJumpStrength());

                    }
                    break;
                }
                case HORSE_COLOR: {
                    if (!(initialEntity instanceof Horse)) break;
                    Horse horse = ((Horse) initialEntity);
                    entityList.removeIf(entity -> ((Horse) entity).getColor() != horse.getColor());
                    break;
                }
                case HORSE_STYLE: {
                    if (!(initialEntity instanceof Horse)) break;
                    Horse horse = ((Horse) initialEntity);
                    entityList.removeIf(entity -> ((Horse) entity).getStyle() != horse.getStyle());
                    break;
                }
                case ZOMBIE_BABY: {
                    if (!(initialEntity instanceof Zombie)) break;
                    Zombie zombie = (Zombie) initialEntity;
                    entityList.removeIf(entity -> ((Zombie) entity).isBaby() != zombie.isBaby());
                    break;
                }
                case WOLF_COLLAR_COLOR: {
                    if (!(initialEntity instanceof Wolf)) break;
                    Wolf wolf = (Wolf) initialEntity;
                    entityList.removeIf(entity -> ((Wolf) entity).getCollarColor() != wolf.getCollarColor());
                    break;
                }
                case OCELOT_TYPE: {
                    if (!(initialEntity instanceof Ocelot)) break;
                    Ocelot ocelot = (Ocelot) initialEntity;
                    entityList.removeIf(entity -> ((Ocelot) entity).getCatType() != ocelot.getCatType());
                }
                case CAT_TYPE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)
                            || !(initialEntity instanceof Cat)) break;
                    Cat cat = (Cat) initialEntity;
                    entityList.removeIf(entity -> ((Cat) entity).getCatType() != cat.getCatType());
                    break;
                }
                case HAS_EQUIPMENT: {
                    if (initialEntity.getEquipment() == null) break;
                    boolean imEquipped = isEquipped(initialEntity);
                    if (imEquipped)
                        entityList = new ArrayList<>();
                    else
                        entityList.removeIf(this::isEquipped);
                    break;
                }
                case RABBIT_TYPE: {
                    if (!(initialEntity instanceof Rabbit)) break;
                    Rabbit rabbit = (Rabbit) initialEntity;
                    entityList.removeIf(entity -> ((Rabbit) entity).getRabbitType() != rabbit.getRabbitType());
                    break;
                }
                case PARROT_TYPE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)
                            || !(initialEntity instanceof Parrot)) break;
                    Parrot parrot = (Parrot) initialEntity;
                    entityList.removeIf(entity -> ((Parrot) entity).getVariant() != parrot.getVariant());
                    break;
                }
                case PUFFERFISH_STATE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof PufferFish)) break;
                    PufferFish pufferFish = (PufferFish) initialEntity;
                    entityList.removeIf(entity -> ((PufferFish) entity).getPuffState() != pufferFish.getPuffState());
                    break;
                }
                case TROPICALFISH_PATTERN: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof TropicalFish)) break;
                    TropicalFish tropicalFish = (TropicalFish) initialEntity;
                    entityList.removeIf(entity -> ((TropicalFish) entity).getPattern() != tropicalFish.getPattern());
                    break;
                }
                case TROPICALFISH_PATTERN_COLOR: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof TropicalFish)) break;
                    TropicalFish tropicalFish = (TropicalFish) initialEntity;
                    entityList.removeIf(entity -> ((TropicalFish) entity).getPatternColor() != tropicalFish.getPatternColor());
                    break;
                }
                case TROPICALFISH_BODY_COLOR: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof TropicalFish)) break;
                    TropicalFish tropicalFish = (TropicalFish) initialEntity;
                    entityList.removeIf(entity -> ((TropicalFish) entity).getBodyColor() != tropicalFish.getBodyColor());
                    break;
                }
                case PHANTOM_SIZE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof Phantom)) break;
                    Phantom phantom = (Phantom) initialEntity;
                    entityList.removeIf(entity -> ((Phantom) entity).getSize() != phantom.getSize());
                    break;
                }
            }
        }

        if (initialEntity.hasMetadata("breedCooldown")) {
            entityList.removeIf(entity -> !entity.hasMetadata("breedCooldown"));
        }

        return entityList;
    }

    public boolean isEquipped(LivingEntity initialEntity) {
        if (initialEntity.getEquipment() == null) return false;
        EntityEquipment equipment = initialEntity.getEquipment();

        return (equipment.getItemInHand().getType() != Material.AIR
                && !weaponsArentEquipment && !equipment.getItemInHand().getEnchantments().isEmpty()
                || (equipment.getHelmet() != null && equipment.getHelmet().getType() != Material.AIR)
                || (equipment.getChestplate() != null && equipment.getChestplate().getType() != Material.AIR)
                || (equipment.getLeggings() != null && equipment.getLeggings().getType() != Material.AIR)
                || (equipment.getBoots() != null && equipment.getBoots().getType() != Material.AIR));
    }

    private int getEntityStackSize(LivingEntity initialEntity) {
        Integer max = entityStackSizes.get(initialEntity.getType());
        if (max == null) {
            max = configurationSection.getInt("Mobs." + initialEntity.getType().name() + ".Max Stack Size");
            if (max == -1) {
                max = maxEntityStackSize;
            }
            entityStackSizes.put(initialEntity.getType(), max);
        }
        return max;
    }

    public boolean canFly(LivingEntity entity) {
        switch (entity.getType()) {
            case GHAST:
            case BLAZE:
            case PHANTOM:
            case BAT:
            case BEE:
                return true;
            default:
                return false;
        }
    }
}

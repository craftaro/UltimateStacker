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
import com.songoda.ultimatestacker.stackable.entity.custom.CustomEntity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StackingTask extends TimerTask {

    private final UltimateStacker plugin;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final EntityStackManager stackManager;

    private final ConfigurationSection configurationSection = UltimateStacker.getInstance().getMobFile();
    private final List<UUID> processed = new ArrayList<>();

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
        //runTaskTimerAsynchronously(plugin, 0, Settings.STACK_SEARCH_TICK_SPEED.getInt());
        executorService.scheduleAtFixedRate(this, 0, (Settings.STACK_SEARCH_TICK_SPEED.getInt()*50L), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executorService.shutdown();
    }

    @Override
    public void run() {
        //make sure if the task running if any error occurs
        try {
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

                Bukkit.getScheduler().runTask(plugin, () -> {
                    // Loop through the entities.
                    for (LivingEntity entity : entities) {
                        // Make sure our entity has not already been processed.
                        // Skip it if it has been.
                        if (this.processed.contains(entity.getUniqueId())) continue;

                        // Check to see if entity is not stackable.
                        if (!isEntityStackable(entity)) {
                            continue;
                        }

                        // Get entity location to pass around as its faster this way.
                        Location location = entity.getLocation();

                        // Process the entity.
                        this.processEntity(entity, sWorld, location);
                    }
                });
            }
            // Clear caches in preparation for the next run.
            this.processed.clear();
        } catch (Exception ignored) {}
    }

    private Future<List<LivingEntity>> getLivingEntitiesSync(SWorld sWorld) {
        CompletableFuture<List<LivingEntity>> future = new CompletableFuture<>();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> future.complete(sWorld.getLivingEntities()));

        return future;
    }

    private Future<Entity[]> getEntitiesInChunkSync(CachedChunk cachedChunk) {
        CompletableFuture<Entity[]> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(this.plugin, () -> future.complete(cachedChunk.getEntities()));
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

        if (!configurationSection.getBoolean("Mobs." + entity.getType().name() + ".Enabled")) {
            return false;
        }

        // Allow spawn if stackreasons are set and match, or if from a spawner
        final String spawnReason = entity.hasMetadata("US_REASON") && !entity.getMetadata("US_REASON").isEmpty()
                ? entity.getMetadata("US_REASON").get(0).asString() : null;
        List<String> stackReasons;
        if (onlyStackFromSpawners) {
            // If only stack from spawners is enabled, make sure the entity spawned from a spawner.
            if (!"SPAWNER".equals(spawnReason))
                return false;
        } else if (!(stackReasons = this.stackReasons).isEmpty() && !stackReasons.contains(spawnReason)) {
            // Only stack if on the list of events to stack
            return false;
        }

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

    private void processEntity(LivingEntity baseEntity, SWorld sWorld, Location location) {

        // Check our WorldGuard flag.
        Boolean flag = WorldGuardHook.isEnabled() ? WorldGuardHook.getBooleanFlag(baseEntity.getLocation(), "mob-stacking") : null;
        if (flag != null && !flag) {
            return;
        }

        // Get the stack from the entity. It should be noted that this value will
        // be null if our entity is not a stack.
        EntityStack baseStack = plugin.getEntityStackManager().getStack(baseEntity);

        // Get the maximum stack size for this entity.
        int maxEntityStackSize = getEntityStackSize(baseEntity);

        // Is this entity stacked?
        boolean isStack = baseStack != null;

        if (isStack && baseStack.getAmount() == maxEntityStackSize) {
            // If the stack is already at the max size then we can skip it.
            processed.add(baseEntity.getUniqueId());
            return;
        }

        // The amount that is stackable.
        int amountToStack = isStack ? baseStack.getAmount() : 1;

        // Attempt to split our stack. If the split is successful then skip this entity.
        if (isStack && attemptSplit(baseStack, baseEntity)) return;

        // If this entity is named, a custom entity or disabled then skip it.
        if (!isStack && (baseEntity.getCustomName() != null
                && plugin.getCustomEntityManager().getCustomEntity(baseEntity) == null)
                || !configurationSection.getBoolean("Mobs." + baseEntity.getType().name() + ".Enabled")) {
            processed.add(baseEntity.getUniqueId());
            return;
        }

        // Get similar entities around our entity and make sure those entities are both compatible and stackable.
        List<LivingEntity> stackableFriends = new LinkedList<>();
        List<LivingEntity> list = getSimilarEntitiesAroundEntity(baseEntity, sWorld, location);
        for (LivingEntity entity : list) {
            // Check to see if entity is not stackable.
            if (!isEntityStackable(entity))
                continue;
            // Add this entity to our stackable friends.
            stackableFriends.add(entity);
        }

        // Loop through our similar stackable entities.
        for (LivingEntity friendlyEntity : stackableFriends) {
            // Make sure the friendlyEntity has not already been processed.
            if (this.processed.contains(friendlyEntity.getUniqueId())) continue;

            // Get this entities friendStack.
            EntityStack friendStack = stackManager.getStack(friendlyEntity);
            int amount = friendStack != null ? friendStack.getAmount() : 1;

            // Check to see if this friendlyEntity is stacked and friendStack plus
            // our amount to stack is not above our max friendStack size
            // for this friendlyEntity.

            boolean overstack = (amount + amountToStack) > maxEntityStackSize;

            if (!overstack) {
                stackManager.createStack(friendlyEntity, amount + amountToStack);
                processed.add(baseEntity.getUniqueId());

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (baseEntity.isLeashed()) {
                        baseEntity.getWorld().dropItemNaturally(baseEntity.getLocation(), CompatibleMaterial.LEAD.getItem());
                    }
                    baseEntity.remove();
                });
                return;
            }
        }
    }

    public boolean attemptSplit(EntityStack baseStack, LivingEntity livingEntity) {
        int stackSize = baseStack.getAmount();
        int maxEntityStackAmount = getEntityStackSize(livingEntity);

        if (stackSize <= maxEntityStackAmount) return false;

        baseStack.setAmount(maxEntityStackAmount);

        Bukkit.getScheduler().runTask(plugin, () -> {
            int finalStackSize = stackSize - maxEntityStackAmount;
            do {
                // Create a new stack, summon entity and add to stack.
                LivingEntity newEntity = (LivingEntity) livingEntity.getWorld().spawnEntity(livingEntity.getLocation(), livingEntity.getType());
                int toAdd = Math.min(finalStackSize, maxEntityStackAmount);
                EntityStack newStack = stackManager.createStack(newEntity, toAdd);
                processed.add(newEntity.getUniqueId());
                finalStackSize -= maxEntityStackAmount;
            } while (finalStackSize >= 0);
        });

        //Mark it as processed.
        processed.add(livingEntity.getUniqueId());
        return true;
    }

    private Set<CachedChunk> getNearbyChunks(SWorld sWorld, Location location, double radius, boolean singleChunk) {
        //get current chunk
        if (radius == -1) {
            return new HashSet<>(Collections.singletonList(new CachedChunk(sWorld, location.getChunk().getX(), location.getChunk().getZ())));
        }
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

    /**
     * Get all entities around an entity within a radius which are similar to the entity.
     * @param entity The entity to get similar entities around.
     * @param radius The radius to get entities around.
     * @param singleChunk Whether to only get entities in the same chunk as the entity.
     * @return A list of similar entities around the entity.
     */
    private List<LivingEntity> getFriendlyStacksNearby(LivingEntity entity, double radius, boolean singleChunk) {
        List<LivingEntity> entities = new ArrayList<>();
        try {
            Set<CachedChunk> chunks = getNearbyChunks(new SWorld(entity.getWorld()), entity.getLocation(), radius, singleChunk);
            for (CachedChunk chunk : chunks) {
                Entity[] entityList = chunk.getEntities();
                for (Entity e : entityList) {
                    if (!processed.contains(e.getUniqueId()) && e.getType() == entity.getType() && e instanceof LivingEntity && e.isValid() && e.getLocation().distance(entity.getLocation()) <= radius) {
                        entities.add((LivingEntity) e);
                    }
                }
            }
            entities.removeIf(entity1 -> entity1.equals(entity) || !UltimateStacker.getInstance().getCustomEntityManager().isStackable(entity1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entities;
    }

    public List<LivingEntity> getSimilarEntitiesAroundEntity(LivingEntity initialEntity, SWorld sWorld, Location location) {
        try {
            // Create a list of all entities around the initial entity of the same type.
            List<LivingEntity> entityList = new LinkedList<>(getFriendlyStacksNearby(initialEntity, searchRadius, stackWholeChunk));

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
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

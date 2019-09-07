package com.songoda.ultimatestacker.tasks;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class StackingTask extends BukkitRunnable {

    private final UltimateStacker plugin;

    private EntityStackManager stackManager;

    ConfigurationSection configurationSection = UltimateStacker.getInstance().getMobFile();

    private List<UUID> processed = new ArrayList<>();

    private int maxEntityStackSize = Settings.MAX_STACK_ENTITIES.getInt();
    private int minEntityStackSize = Settings.MIN_STACK_ENTITIES.getInt();

    private int maxPerTypeStacksPerChunk = Settings.MAX_PER_TYPE_STACKS_PER_CHUNK.getInt();

    public StackingTask(UltimateStacker plugin) {
        this.plugin = plugin;
        this.stackManager = plugin.getEntityStackManager();

        // Start stacking task.
        runTaskTimer(plugin, 0, Settings.STACK_SEARCH_TICK_SPEED.getInt());
    }

    @Override
    public void run() {
        // Should entities be stacked?
        if (!Settings.STACK_ENTITIES.getBoolean()) return;

        // Loop through each world.
        for (World world : Bukkit.getWorlds()) {
            // If world is disabled then continue to the next world.
            if (isWorldDisabled(world)) continue;

            // Get the loaded entities from the current world and reverse them.
            List<Entity> entities = new ArrayList<>(world.getEntities());
            Collections.reverse(entities);

            // Loop through the entities.
            for (Entity entity : entities) {
                // Get entity location to pass around as its faster this way.
                Location location = entity.getLocation();
                // Check to see if entity is stackable.
                if (!isEntityStackable(entity, location)) continue;
                // Make sure our entity has not already been processed.
                // Skip it if it has been.
                if (this.processed.contains(entity.getUniqueId())) continue;

                // Cast our entity to living entity.
                LivingEntity livingEntity = (LivingEntity) entity;

                // Process the entity.
                this.processEntity(livingEntity, location);

            }
        }
        // Clear caches in preparation for the next run.
        this.processed.clear();
        plugin.getEntityUtils().clearChunkCache();
    }

    public boolean isWorldDisabled(World world) {
        List<String> disabledWorlds = Settings.DISABLED_WORLDS.getStringList();
        return disabledWorlds.stream().anyMatch(worldStr -> world.getName().equalsIgnoreCase(worldStr));
    }

    private boolean isEntityStackable(Entity entity, Location location) {
        // Make sure we have the correct entity type and that it is valid.
        if (!entity.isValid()
                || !(entity instanceof LivingEntity)
                || entity instanceof HumanEntity
                || entity instanceof ArmorStand

                // Make sure the entity is not in love.
                || entity.hasMetadata("inLove")
                // Or in breeding cooldown.
                || entity.hasMetadata("breedCooldown"))
            return false;

        // Allow spawn if stackreasons are set and match, or if from a spawner
        final String spawnReason = entity.hasMetadata("US_REASON") ? entity.getMetadata("US_REASON").get(0).asString() : null;
        List<String> stackReasons;
        if (Settings.ONLY_STACK_FROM_SPAWNERS.getBoolean()) {
            // If only stack from spawners is enabled, make sure the entity spawned from a spawner.
            if (!"SPAWNER".equals(spawnReason))
                return false;
        } else if (!(stackReasons = Settings.STACK_REASONS.getStringList()).isEmpty() && !stackReasons.contains(spawnReason))
            // Only stack if on the list of events to stack
            return false;

        // Cast our entity to living entity.
        LivingEntity livingEntity = (LivingEntity) entity;

        // If only stack on surface is enabled make sure the entity is on a surface then entity is stackable.
        return !Settings.ONLY_STACK_ON_SURFACE.getBoolean()
                || Methods.canFly(livingEntity)
                || entity.getType().name().equals("SHULKER")
                || (livingEntity.isOnGround() || location.getBlock().isLiquid());

    }

    private void processEntity(LivingEntity livingEntity, Location location) {
        // Get the stack from the entity. It should be noted that this value will
        // be null if our entity is not a stack.
        EntityStack stack = plugin.getEntityStackManager().getStack(livingEntity);

        // Is this entity stacked?
        boolean isStack = stack != null;

        // The amount that is stackable.
        int amountToStack = isStack ? stack.getAmount() : 1;

        // Attempt to split our stack. If the split is successful then skip this entity.
        if (isStack && attemptSplit(stack, livingEntity)) return;

        // If this entity is named or disabled then skip it.
        if (!isStack && livingEntity.getCustomName() != null
                || !configurationSection.getBoolean("Mobs." + livingEntity.getType().name() + ".Enabled"))
            return;

        // Get the maximum stack size for this entity.
        int maxEntityStackSize = getEntityStackSize(livingEntity);

        // Get similar entities around our entity and make sure those entities are both compatible and stackable.
        List<LivingEntity> stackableFriends = plugin.getEntityUtils().getSimilarEntitiesAroundEntity(livingEntity, location)
                .stream().filter(entity -> isEntityStackable(entity, location))
                .collect(Collectors.toCollection(LinkedList::new));

        // Loop through our similar stackable entities.
        for (LivingEntity entity : stackableFriends) {
            // Make sure the entity has not already been processed.
            if (this.processed.contains(entity.getUniqueId())) continue;

            // Get this entities friendStack.
            EntityStack friendStack = stackManager.getStack(entity);

            // Check to see if this entity is stacked and friendStack plus
            // our amount to stack is not above our max friendStack size
            // for this entity.
            if (friendStack != null && (friendStack.getAmount() + amountToStack) <= maxEntityStackSize) {

                // Add one to the found friendStack.
                friendStack.addAmount(amountToStack);

                // Add our entities health to the friendStacks health.
                friendStack.addHealth(entity.getHealth());

                // Fix the friendStacks health.
                if (!isStack)
                    friendStack.addHealth(entity.getHealth());
                else
                    friendStack.mergeHealth(stack);


                fixHealth(entity, livingEntity);
                if (Settings.STACK_ENTITY_HEALTH.getBoolean())
                    entity.setHealth(entity.getMaxHealth() < livingEntity.getHealth()
                            ? entity.getMaxHealth() : livingEntity.getHealth());


                // Remove our entity and mark it as processed.
                livingEntity.remove();
                processed.add(livingEntity.getUniqueId());

                return;
            } else if (friendStack == null
                    && isStack
                    && (stack.getAmount() + 1) <= maxEntityStackSize
                    && Methods.canFly(entity)
                    && Settings.ONLY_STACK_FLYING_DOWN.getBoolean()
                    && location.getY() > entity.getLocation().getY()) {

                // Create a new stack with the current stacks amount and add one to it.
                EntityStack newStack = stackManager.addStack(entity, stack.getAmount() + 1);

                // Fix the entities health.
                newStack.mergeHealth(stack);
                newStack.addHealth(livingEntity.getHealth());
                fixHealth(livingEntity, entity);
                if (Settings.STACK_ENTITY_HEALTH.getBoolean())
                    entity.setHealth(entity.getHealth());

                // Remove our entities stack from the stack manager.
                stackManager.removeStack(livingEntity);

                // Remove our entity and mark it as processed.
                livingEntity.remove();
                processed.add(livingEntity.getUniqueId());

                return;
            }
        }

        // If our entity is stacked then skip this entity.
        if (isStack) return;

        // Remove all stacked entities from our stackable friends.
        stackableFriends.removeIf(stackManager::isStacked);

        // If the stack cap is met then delete this entity.
        if (maxPerTypeStacksPerChunk != -1
                && (plugin.getEntityUtils().getSimilarStacksInChunk(livingEntity) + 1) > maxPerTypeStacksPerChunk) {
            livingEntity.remove();
            this.processed.add(livingEntity.getUniqueId());
            return;
        }

        // If there are none or not enough stackable friends left to create a new entity,
        // the stack sizes overlap then skip this entity.
        if (stackableFriends.isEmpty()
                || stackableFriends.size() < minEntityStackSize - 1
                || minEntityStackSize > maxEntityStackSize) return;

        // If a stack was never found create a new one.
        EntityStack newStack = stackManager.addStack(new EntityStack(livingEntity,
                Math.min((stackableFriends.size() + 1), maxEntityStackSize)));

        // Loop through the unstacked and unprocessed stackable friends while not creating
        // a stack larger than the maximum.
        stackableFriends.stream().filter(entity -> !stackManager.isStacked(entity)
                && !this.processed.contains(entity.getUniqueId())).limit(maxEntityStackSize).forEach(entity -> {

            // Make sure we're not naming some poor kids pet.
            if (entity.getCustomName() != null) {
                processed.add(livingEntity.getUniqueId());
                newStack.addAmount(-1);
                return;
            }
            // Fix the entities health.
            fixHealth(livingEntity, entity);
            newStack.addHealth(entity.getHealth());

            // Remove our entity and mark it as processed.
            entity.remove();
            processed.add(entity.getUniqueId());
        });

        // Update our stacks health.
        updateHealth(newStack);

        // Update our stack.
        newStack.updateStack();
    }

    private void updateHealth(EntityStack stack) {
        if (Settings.STACK_ENTITY_HEALTH.getBoolean())
            stack.updateHealth(stack.getEntity());
    }


    public boolean attemptSplit(EntityStack stack, LivingEntity livingEntity) {
        int stackSize = stack.getAmount();
        int maxEntityStackAmount = getEntityStackSize(livingEntity);

        if (stackSize <= maxEntityStackAmount) return false;

        for (int i = stackSize; i > 0; i -= maxEntityStackAmount)
            this.processed.add(plugin.getEntityStackManager()
                    .addStack(plugin.getEntityUtils().newEntity(livingEntity), Math.min(i, maxEntityStackAmount)).getEntityUniqueId());

        // Remove our entities stack from the stack manager.
        stackManager.removeStack(livingEntity);

        // Remove our entity and mark it as processed.
        livingEntity.remove();
        processed.add(livingEntity.getUniqueId());
        return true;
    }


    private void fixHealth(LivingEntity entity, LivingEntity initialEntity) {
        if (!Settings.STACK_ENTITY_HEALTH.getBoolean() && Settings.CARRY_OVER_LOWEST_HEALTH.getBoolean() && initialEntity.getHealth() < entity.getHealth())
            entity.setHealth(initialEntity.getHealth());
    }

    private int getEntityStackSize(LivingEntity initialEntity) {
        if (configurationSection.getInt("Mobs." + initialEntity.getType().name() + ".Max Stack Size") != -1)
            maxEntityStackSize = configurationSection.getInt("Mobs." + initialEntity.getType().name() + ".Max Stack Size");
        return maxEntityStackSize;
    }
}

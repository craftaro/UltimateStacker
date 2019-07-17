package com.songoda.ultimatestacker.tasks;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class StackingTask extends BukkitRunnable {

    private final UltimateStacker instance;

    private EntityStackManager stackManager;


    private List<UUID> removed = new ArrayList<>();

    public StackingTask(UltimateStacker instance) {
        this.instance = instance;
        this.stackManager = instance.getEntityStackManager();

        // Start stacking task.
        runTaskTimer(instance, 0, Setting.STACK_SEARCH_TICK_SPEED.getInt());
    }

    @Override
    public void run() {
        List<String> disabledWorlds = Setting.DISABLED_WORLDS.getStringList();

        nextEntity:
        for (World world : Bukkit.getWorlds()) {
            if (disabledWorlds.stream().anyMatch(worldStr -> world.getName().equalsIgnoreCase(worldStr))) continue;

            List<Entity> entities = world.getEntities();
            Collections.reverse(entities);

            for (Entity entityO : entities) {
                if (!(entityO instanceof LivingEntity)
                        || entityO instanceof Player
                        || !entityO.isValid()
                        || !Setting.STACK_ENTITIES.getBoolean())
                    continue nextEntity;

                LivingEntity initialEntity = (LivingEntity) entityO;

                if (initialEntity.isDead()
                        || !initialEntity.isValid()
                        || initialEntity instanceof ArmorStand
                        || initialEntity.hasMetadata("inLove")

                        || Setting.ONLY_STACK_FROM_SPAWNERS.getBoolean()
                        && !(initialEntity.hasMetadata("US_REASON")
                        && initialEntity.getMetadata("US_REASON").get(0).asString().equals("SPAWNER"))

                        || Setting.ONLY_STACK_ON_SURFACE.getBoolean()
                        && !Methods.canFly(initialEntity)
                        && (!initialEntity.isOnGround() && !initialEntity.getLocation().getBlock().isLiquid()))
                    continue nextEntity;

                EntityStack initialStack = stackManager.getStack(initialEntity);
                if (initialStack == null && initialEntity.getCustomName() != null) continue nextEntity;

                attemptAddToStack(initialEntity, initialStack);
            }
            entities.clear();
            removed.clear();
        }
    }

    public boolean attemptAddToStack(LivingEntity initialEntity, EntityStack initialStack) {
        ConfigurationSection configurationSection = UltimateStacker.getInstance().getMobFile().getConfig();

        if (!configurationSection.getBoolean("Mobs." + initialEntity.getType().name() + ".Enabled"))
            return false;

        int minEntityStackAmount = Setting.MIN_STACK_ENTITIES.getInt();
        int amtToStack = initialStack != null ? initialStack.getAmount() : 1;

        List<LivingEntity> entityList = Methods.getSimilarEntitiesAroundEntity(initialEntity);
        entityList.removeIf(entity -> entity.hasMetadata("inLove")
                || entity.hasMetadata("breedCooldown")

                || Setting.ONLY_STACK_FROM_SPAWNERS.getBoolean()
                && !(initialEntity.hasMetadata("US_REASON")
                && initialEntity.getMetadata("US_REASON").get(0).asString().equals("SPAWNER")));


        int maxEntityStackSize = Setting.MAX_STACK_ENTITIES.getInt();
        if (configurationSection.getInt("Mobs." + initialEntity.getType().name() + ".Max Stack Size") != -1)
            maxEntityStackSize = configurationSection.getInt("Mobs." + initialEntity.getType().name() + ".Max Stack Size");


        for (LivingEntity entity : new ArrayList<>(entityList)) {
            if (removed.contains(entity.getUniqueId())) continue;
            EntityStack stack = stackManager.getStack(entity);
            if (stack == null && entity.getCustomName() != null) {
                entityList.remove(entity);
                continue;
            }

            //If a stack was found add 1 to this stack.
            if (stack != null && (stack.getAmount() + amtToStack) <= maxEntityStackSize) {
                stack.addAmount(amtToStack);
                stack.updateStack();

                if (initialStack == null)
                    stack.addHealth(entity.getHealth());
                else
                    stack.mergeHealth(initialStack);

                removed.add(initialEntity.getUniqueId());

                fixHealth(entity, initialEntity);
                if (Setting.STACK_ENTITY_HEALTH.getBoolean())
                    entity.setHealth(entity.getMaxHealth() < initialEntity.getHealth()
                            ? entity.getMaxHealth() : initialEntity.getHealth());

                initialEntity.remove();

                return true;
            } else if (stack == null
                    && initialStack != null
                    && (initialStack.getAmount() + 1) <= maxEntityStackSize
                    && Methods.canFly(entity)
                    && Setting.ONLY_STACK_FLYING_DOWN.getBoolean()
                    && initialEntity.getLocation().getY() > entity.getLocation().getY()) {
                EntityStack newStack = stackManager.addStack(entity, initialStack.getAmount() + 1);

                newStack.mergeHealth(initialStack);

                newStack.addHealth(initialEntity.getHealth());
                removed.add(initialEntity.getUniqueId());

                fixHealth(initialEntity, entity);
                if (Setting.STACK_ENTITY_HEALTH.getBoolean())
                    entity.setHealth(entity.getHealth());

                stackManager.removeStack(initialEntity);
                initialEntity.remove();

                return true;
            }
        }

        if (initialStack != null) return false;

        entityList.removeIf(stackManager::isStacked);

        if (entityList.size() < minEntityStackAmount - 1
                || minEntityStackAmount > maxEntityStackSize
                || minEntityStackAmount == 1 && entityList.size() == 0) return false;

        //If stack was never found make a new one.
        EntityStack stack = stackManager.addStack(new EntityStack(initialEntity, (entityList.size() + 1) >
                maxEntityStackSize ? maxEntityStackSize : entityList.size() + 1));

        entityList.stream().filter(entity -> !stackManager.isStacked(entity)
                && !removed.contains(entity.getUniqueId())).limit(maxEntityStackSize).forEach(entity -> {

            fixHealth(initialEntity, entity);
            stack.addHealth(entity.getHealth());

            removed.add(entity.getUniqueId());
            entity.remove();
        });
        updateHealth(stack);

        stack.updateStack();
        return false;
    }

    public void attemptSplit(EntityStack stack, LivingEntity entity) {
        int stackSize = stack.getAmount();
        int maxEntityStackAmount = Setting.MAX_STACK_ENTITIES.getInt();

        if (stackSize <= maxEntityStackAmount) return;

        for (int i = stackSize; i > 0; i -= maxEntityStackAmount) {
            instance.getEntityStackManager().addStack(Methods.newEntity(entity), i > 25 ? 25 : i);
        }
        entity.remove();
    }

    private void fixHealth(LivingEntity entity, LivingEntity initialEntity) {
        if (!Setting.STACK_ENTITY_HEALTH.getBoolean() && Setting.CARRY_OVER_LOWEST_HEALTH.getBoolean() && initialEntity.getHealth() < entity.getHealth())
            entity.setHealth(initialEntity.getHealth());
    }

    private void updateHealth(EntityStack stack) {
        if (Setting.STACK_ENTITY_HEALTH.getBoolean())
            stack.updateHealth(stack.getEntity());
    }
}

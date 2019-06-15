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

    public StackingTask(UltimateStacker instance) {
        this.instance = instance;

        // Start stacking task.
        runTaskTimer(instance, 0, Setting.STACK_SEARCH_TICK_SPEED.getInt());
    }

    @Override
    public void run() {
        int maxEntityStackSizeGlobal = Setting.MAX_STACK_ENTITIES.getInt();
        int minEntityStackAmount = Setting.MIN_STACK_ENTITIES.getInt();

        List<String> disabledWorlds = Setting.DISABLED_WORLDS.getStringList();

        EntityStackManager stackManager = instance.getEntityStackManager();
        for (World world : Bukkit.getWorlds()) {
            if (disabledWorlds.stream().anyMatch(worldStr -> world.getName().equalsIgnoreCase(worldStr))) continue;

            List<Entity> entities = world.getEntities();
            Collections.reverse(entities);

            List<UUID> removed = new ArrayList<>();

            nextEntity:
            for (Entity entityO : entities) {
                if (entityO == null
                        || entityO instanceof Player
                        || !entityO.isValid()
                        || !(entityO instanceof LivingEntity)
                        || !Setting.STACK_ENTITIES.getBoolean())
                    continue;

                LivingEntity initalEntity = (LivingEntity) entityO;

                if (initalEntity.isDead()
                        || !initalEntity.isValid()
                        || initalEntity instanceof ArmorStand
                        || initalEntity.hasMetadata("inLove")

                        || Setting.ONLY_STACK_FROM_SPAWNERS.getBoolean()
                        && !(initalEntity.hasMetadata("US_REASON")
                        && initalEntity.getMetadata("US_REASON").get(0).asString().equals("SPAWNER")))
                    continue;

                EntityStack initialStack = stackManager.getStack(initalEntity);
                if (initialStack == null && initalEntity.getCustomName() != null) continue;
                int amtToStack = initialStack != null ? initialStack.getAmount() : 1;

                ConfigurationSection configurationSection = UltimateStacker.getInstance().getMobFile().getConfig();

                if (!configurationSection.getBoolean("Mobs." + initalEntity.getType().name() + ".Enabled"))
                    continue;

                int maxEntityStackSize = maxEntityStackSizeGlobal;
                if (configurationSection.getInt("Mobs." + initalEntity.getType().name() + ".Max Stack Size") != -1)
                    maxEntityStackSize = configurationSection.getInt("Mobs." + initalEntity.getType().name() + ".Max Stack Size");

                List<LivingEntity> entityList = Methods.getSimilarEntitiesAroundEntity(initalEntity);
                entityList.removeIf(entity -> entity.hasMetadata("inLove")
                        || entity.hasMetadata("breedCooldown")

                        || Setting.ONLY_STACK_FROM_SPAWNERS.getBoolean()
                        && !(initalEntity.hasMetadata("US_REASON")
                        && initalEntity.getMetadata("US_REASON").get(0).asString().equals("SPAWNER")));

                for (Entity entity : new ArrayList<>(entityList)) {
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
                        removed.add(initalEntity.getUniqueId());
                        initalEntity.remove();

                        continue nextEntity;
                    }
                }

                if (initialStack != null) continue;

                entityList.removeIf(stackManager::isStacked);

                if (entityList.size() < minEntityStackAmount - 1
                        || minEntityStackAmount > maxEntityStackSize
                        || minEntityStackAmount == 1 && entityList.size() == 0) continue;

                //If stack was never found make a new one.
                EntityStack stack = stackManager.addStack(new EntityStack(initalEntity, (entityList.size() + 1) >
                        maxEntityStackSize ? maxEntityStackSize : entityList.size() + 1));

                entityList.stream().filter(entity -> !stackManager.isStacked(entity)
                        && !removed.contains(entity.getUniqueId())).limit(maxEntityStackSize).forEach(entity -> {
                    removed.add(entity.getUniqueId());
                    entity.remove();
                });

                stack.updateStack();
            }
            entities.clear();
            removed.clear();
        }
    }
}
